package ir;

import java.io.*;
import java.util.*;


public class PersistentScalableHashedIndex extends PersistentHashedIndex {

    private static final int MAX_TOKENS_PER_FLUSH = 100000;

    private int tokenCount = 0;

    private List<IntermediateIndex> intermediateIndexes = new ArrayList<>();

    private List<Thread> mergeThreads = new ArrayList<>();

    private int flushCount = 0;
    private int mergeCount = 0;

    public static class IntermediateIndex {
        public File dictionaryFile;
        public File dataFile;
        public IntermediateIndex(File dict, File data) {
            this.dictionaryFile = dict;
            this.dataFile = data;
        }
    }

    public PersistentScalableHashedIndex() {
        super();
    }


    @Override
    public void insert(String token, int docID, int offset) {
        super.insert(token, docID, offset);
        tokenCount++;
        if( tokenCount >= MAX_TOKENS_PER_FLUSH ) {
            try {
                flushCurrentIndex();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void flushCurrentIndex() throws IOException {
        System.out.println("Flushing intermediate index " + flushCount);
        String dictFileName = INDEXDIR + "/dictionary_intermediate_" + flushCount;
        String dataFileName = INDEXDIR + "/data_intermediate_" + flushCount;

        RandomAccessFile dictRAF = new RandomAccessFile(dictFileName, "rw");
        RandomAccessFile dataRAF = new RandomAccessFile(dataFileName, "rw");

        dictRAF.setLength(TABLESIZE * (Long.BYTES + Integer.BYTES));

        long localFree = 0;

        for (Map.Entry<String, PostingsList> entry : index.entrySet()) {
            String key = entry.getKey();
            PostingsList list = entry.getValue();
            long hash = hashFunction(key);
            while ( readEntryFromFile(dictRAF, hash) != null ) {
                hash = (hash + Long.BYTES + Integer.BYTES) % (TABLESIZE * (Long.BYTES + Integer.BYTES));
            }
            String postingsString = key + ":" + list.toString();
            byte[] data = postingsString.getBytes();
            dataRAF.seek(localFree);
            dataRAF.write(data);
            Entry dictEntry = new Entry(localFree, data.length);
            writeEntryToFile(dictRAF, dictEntry, hash);
            localFree += data.length;
        }

        dictRAF.close();
        dataRAF.close();

        synchronized(intermediateIndexes) {
            intermediateIndexes.add(new IntermediateIndex(new File(dictFileName), new File(dataFileName)));
        }

        tokenCount = 0;
        flushCount++;

        dictionaryFile = new RandomAccessFile(INDEXDIR + "/" + DICTIONARY_FNAME, "rw");
        dataFile = new RandomAccessFile(INDEXDIR + "/" + DATA_FNAME, "rw");

        synchronized(intermediateIndexes) {
            if( intermediateIndexes.size() >= 2 ) {
                IntermediateIndex idx1 = intermediateIndexes.remove(0);
                IntermediateIndex idx2 = intermediateIndexes.remove(0);
                Thread mergeThread = new Thread(new MergeTask(idx1, idx2));
                mergeThread.start();
                mergeThreads.add(mergeThread);
            }
        }
    }


    private void writeEntryToFile(RandomAccessFile raf, Entry entry, long ptr) {
        try {
            raf.seek(ptr);
            raf.writeLong(entry.ptr);
            raf.writeInt(entry.size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Entry readEntryFromFile(RandomAccessFile raf, long ptr) {
        try {
            raf.seek(ptr);
            long pos = raf.readLong();
            int size = raf.readInt();
            if (pos == 0 && size == 0)
                return null;
            return new Entry(pos, size);
        } catch (IOException e) {
            return null;
        }
    }


    private class MergeTask implements Runnable {
        private IntermediateIndex idx1;
        private IntermediateIndex idx2;

        public MergeTask(IntermediateIndex i1, IntermediateIndex i2) {
            this.idx1 = i1;
            this.idx2 = i2;
        }

        @Override
        public void run() {
            System.out.println("Merging intermediate indexes: " +
                    idx1.dictionaryFile.getName() + " and " +
                    idx2.dictionaryFile.getName());
            try {
                HashMap<String, PostingsList> mergedMap = new HashMap<>();
                mergeIntermediateIndexIntoMap(idx1, mergedMap);
                mergeIntermediateIndexIntoMap(idx2, mergedMap);

                String mergedDictName = INDEXDIR + "/dictionary_merged_" + mergeCount;
                String mergedDataName = INDEXDIR + "/data_merged_" + mergeCount;
                RandomAccessFile mergedDictRAF = new RandomAccessFile(mergedDictName, "rw");
                RandomAccessFile mergedDataRAF = new RandomAccessFile(mergedDataName, "rw");
                mergedDictRAF.setLength(TABLESIZE * (Long.BYTES + Integer.BYTES));
                long mergedFree = 0;

                for (Map.Entry<String, PostingsList> entry : mergedMap.entrySet()) {
                    String key = entry.getKey();
                    PostingsList list = entry.getValue();
                    long hash = hashFunction(key);
                    while ( readEntryFromFile(mergedDictRAF, hash) != null ) {
                        hash = (hash + Long.BYTES + Integer.BYTES) % (TABLESIZE * (Long.BYTES + Integer.BYTES));
                    }
                    String postingsString = key + ":" + list.toString();
                    byte[] data = postingsString.getBytes();
                    mergedDataRAF.seek(mergedFree);
                    mergedDataRAF.write(data);
                    Entry dictEntry = new Entry(mergedFree, data.length);
                    writeEntryToFile(mergedDictRAF, dictEntry, hash);
                    mergedFree += data.length;
                }
                mergedDictRAF.close();
                mergedDataRAF.close();

                idx1.dictionaryFile.delete();
                idx1.dataFile.delete();
                idx2.dictionaryFile.delete();
                idx2.dataFile.delete();

                synchronized(intermediateIndexes) {
                    intermediateIndexes.add(new IntermediateIndex(new File(mergedDictName), new File(mergedDataName)));
                }
                mergeCount++;
                System.out.println("Merge completed: created " + mergedDictName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void mergeIntermediateIndexIntoMap(IntermediateIndex idx, HashMap<String, PostingsList> map) throws IOException {
            RandomAccessFile dictRAF = new RandomAccessFile(idx.dictionaryFile, "r");
            RandomAccessFile dataRAF = new RandomAccessFile(idx.dataFile, "r");
            long entrySize = Long.BYTES + Integer.BYTES;
            long tableByteSize = TABLESIZE * entrySize;
            for (long pos = 0; pos < tableByteSize; pos += entrySize) {
                Entry entry = readEntryFromFile(dictRAF, pos);
                if (entry != null) {
                    dataRAF.seek(entry.ptr);
                    byte[] data = new byte[entry.size];
                    dataRAF.readFully(data);
                    String postingsString = new String(data);
                    int colonIndex = postingsString.indexOf(":");
                    if (colonIndex == -1) continue;
                    String token = postingsString.substring(0, colonIndex);
                    String postingsData = postingsString.substring(colonIndex + 1);
                    PostingsList postingsList = PostingsList.fromString(postingsData);
                    if (map.containsKey(token)) {
                        PostingsList existing = map.get(token);
                        for (int i = 0; i < postingsList.size(); i++) {
                            PostingsEntry pe = postingsList.get(i);
                        }
                    } else {
                        map.put(token, postingsList);
                    }
                }
            }
            dictRAF.close();
            dataRAF.close();
        }
    }


    @Override
    public void cleanup() {
        if (!index.isEmpty()) {
            try {
                flushCurrentIndex();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Thread t : mergeThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (intermediateIndexes.size() > 1) {
            IntermediateIndex idx1, idx2;
            synchronized (intermediateIndexes) {
                idx1 = intermediateIndexes.remove(0);
                idx2 = intermediateIndexes.remove(0);
            }
            new MergeTask(idx1, idx2).run();
        }
        if (intermediateIndexes.size() == 1) {
            IntermediateIndex finalIndex = intermediateIndexes.get(0);
            try {
                dictionaryFile = new RandomAccessFile(finalIndex.dictionaryFile, "r");
                dataFile = new RandomAccessFile(finalIndex.dataFile, "r");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writeDocInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("Final index is ready.");
    }
}
