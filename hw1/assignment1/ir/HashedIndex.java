/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();



    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        //
        // YOUR CODE HERE

        //if the token is in the index
        if (index.containsKey(token)) {
            PostingsList list = index.get(token);

            if (list.get(list.size()-1).docID == docID) {
                list.get(list.size()-1).offsets.add(offset);
                list.get(list.size()-1).score++;
            } else {
                PostingsEntry entry = new PostingsEntry(docID, offset, 1);
                list.insert(entry);
            }
        }
        else {
            PostingsList list = new PostingsList();
            PostingsEntry entry = new PostingsEntry(docID, offset, 1);
            list.insert(entry);
            index.put(token, list);
        }
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        if (index.containsKey(token)) {
            return index.get(token);
        }
        return null;
    }


    public void writeEuclideanLength(String outputFile) {
        int N = docNames.size();
        double[] euclideanLengths = new double[N];
        for (Map.Entry<String, PostingsList> entry : index.entrySet()) {
            PostingsList postingsList = entry.getValue();
            double idf = Math.log(N / (double) postingsList.size());
            for (int i = 0; i < postingsList.size(); i++) {
                PostingsEntry pe = postingsList.get(i);
                int docID = pe.docID;
                double tf = pe.score;
                euclideanLengths[docID] += (tf * idf) * (tf * idf);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (int docID = 0; docID < N; docID++) {
                double length = Math.sqrt(euclideanLengths[docID]);
                String docName = docNames.get(docID);
                writer.write(docID + ";" + length);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readEuclideanLength(HashMap<Integer, Double>  euclideanLength) {
        String filePath = "EuclideanLengths.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    int docID = Integer.parseInt(parts[0].trim());
                    double length = Double.parseDouble(parts[1].trim());
                    euclideanLength.put(docID, length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
        writeEuclideanLength("EuclideanLengths.txt");
        System.err.print( "finishing calculating EuclideanLengths" );
    }
}
