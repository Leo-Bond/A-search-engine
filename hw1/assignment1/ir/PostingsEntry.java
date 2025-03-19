/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;

    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
       return Double.compare( other.score, score );
    }


    //
    // YOUR CODE HERE
    //
    public ArrayList<Integer> offsets = new ArrayList<>();

    public PostingsEntry(int docID, int offset, double score) {
        this.docID = docID;
        this.score = score;
        offsets.add(offset);
    }
    public PostingsEntry(int docID, ArrayList<Integer> offsets, double score) {
        this.docID = docID;
        this.score = score;
        this.offsets = offsets;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DocID: ").append(docID).append(", Offsets: ");
        for (int offset : offsets) {
            sb.append(offset).append(" ");
        }
        return sb.toString();
    }

    public static PostingsEntry fromString(String str) {
        String[] parts = str.split(", ");
        int docID = Integer.parseInt(parts[0].split(": ")[1]); // 解析 DocID
        String[] offsetStrs = parts[1].split(": ")[1].split(" "); // 解析 Offsets
        ArrayList<Integer> offsets = new ArrayList<>();
        for (String offsetStr : offsetStrs) {
            if (!offsetStr.isEmpty()) {
                offsets.add(Integer.parseInt(offsetStr));
            }
        }
        return new PostingsEntry(docID, offsets,1);
    }





}

