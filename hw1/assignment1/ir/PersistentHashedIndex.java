/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, KTH, 2018
 */  

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;


/*
 *   Implements an inverted index as a hashtable on disk.
 *   
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks. 
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "./index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The data file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final long TABLESIZE = 611953L;

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    // ===================================================================

    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */ 
    public class Entry {
        //
        //  YOUR CODE HERE
        //
        long ptr;
        int size;


        public Entry(long ptr, int size) {
            this.ptr = ptr;
            this.size = size;
        }
    }


    // ==================================================================

    public void readEuclideanLength(HashMap<Integer, Double>  euclideanLength) {

    }
    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created. 
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            readDocInfo();
        } catch ( FileNotFoundException e ) {
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */ 
    int writeData( String dataString, long ptr ) {
        try {
            dataFile.seek( ptr ); 
            byte[] data = dataString.getBytes();
            dataFile.write( data );
            return data.length;
        } catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */ 
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file. 
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr ) {
        //
        //  YOUR CODE HERE
        try {
            dictionaryFile.seek(ptr);  // Move to the correct position in the file
            dictionaryFile.writeLong(entry.ptr);  // Write the pointer to the postings list
            dictionaryFile.writeInt(entry.size); // Write the size to the postings list
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry( long ptr ) {   
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE 
        //
        try {
            dictionaryFile.seek(ptr);
            long position = dictionaryFile.readLong();
            int size = dictionaryFile.readInt();
            if (position == 0 && size == 0) {
                return null;
            }
            return new Entry(position, size);
        } catch (IOException e) {
            return null;
        }
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for ( Map.Entry<Integer,String> entry : docNames.entrySet() ) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write( docInfoEntry.getBytes() );
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try ( BufferedReader br = new BufferedReader(freader) ) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNames.put( new Integer(data[0]), data[1] );
                docLengths.put( new Integer(data[0]), new Integer(data[2]) );
            }
        }
        freader.close();
    }


    public long hashFunction(String key) {
        return (Math.abs(key.hashCode()) % TABLESIZE) * (Long.BYTES + Integer.BYTES);
    }
    /**
     *  Write the index to files.
     */
    public void writeIndex()  {
        int collisions = 0;
        int i = 0;
        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // Write the dictionary and the postings list

            // 
            //  YOUR CODE HERE
            dictionaryFile.setLength(0);
            dictionaryFile.setLength(TABLESIZE * (Long.BYTES + Integer.BYTES));


            for (Map.Entry<String, PostingsList> entry : index.entrySet()) {
                String key = entry.getKey();
                PostingsList list = entry.getValue();
                long hash = hashFunction(key);
                long position = free;

                while (readEntry(hash) != null) {
                    hash = (hash + Long.BYTES + Integer.BYTES) % (TABLESIZE * (Long.BYTES + Integer.BYTES));
                    collisions++;
                }
//                i++;
//                if ( i%10 == 0 ) System.err.println( i );

                String postingList_item_string = key + ":" + list.toString();
                //System.err.println( " postingList_item_string: "  + postingList_item_string );
                int size = writeData(postingList_item_string, position);
                writeEntry(new Entry(position, size), hash);
                free += size;

            }

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );
    }




    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
//        if (index.containsKey(token)) {
//            return index.get(token);
//        }
//        return null;
        long hash = hashFunction(token);
        Entry entry = readEntry(hash);
        while (entry != null) {
            String data = readData(entry.ptr, entry.size);
            if (data != null && data.startsWith(token + ":")) {
                return PostingsList.fromString(data.substring(token.length() + 1));
            }
            hash = (hash + Long.BYTES + Integer.BYTES) % (TABLESIZE * (Long.BYTES + Integer.BYTES));
            entry = readEntry(hash);
        }
        return null;
    }


    /**
     *  Inserts this token in the main-memory hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        //
        //  YOUR CODE HERE
        //
        if (index.containsKey(token)) {
            PostingsList list = index.get(token);

            if (list.get(list.size()-1).docID == docID) {
                list.get(list.size()-1).offsets.add(offset);
            } else {
                PostingsEntry entry = new PostingsEntry(docID, offset, 0);
                list.insert(entry);
            }
        }
        else {
            PostingsList list = new PostingsList();
            PostingsEntry entry = new PostingsEntry(docID, offset, 0);
            list.insert(entry);
            index.put(token, list);
        }
    }


    /**
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk..." );
        writeIndex();

        System.err.println( "done!" );
    }
}
