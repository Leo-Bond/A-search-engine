/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;

public class PostingsList {
    
    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();


    /** Number of postings in this list. */
    public int size() {
    return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
    return list.get( i );
    }

    // 
    //  YOUR CODE HERE
    //

    public void insert(PostingsEntry entry) {
        if (!containsDocID(entry.docID)) {
            list.add(entry);
        }
    }

    // Helper method to check if a docID already exists
    private boolean containsDocID(int docID) {
        for (PostingsEntry e : list) {
            if (e.docID == docID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PostingsList: [\n");
        for (PostingsEntry entry : list) {
            sb.append("  ").append(entry.toString()).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static PostingsList fromString(String str) {
        PostingsList postingsList = new PostingsList();
        String[] lines = str.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("DocID:")) {
                PostingsEntry entry = PostingsEntry.fromString(line.trim());
                postingsList.insert(entry);
            }
        }
        return postingsList;
    }

    public PostingsList sort() {
        list.sort(PostingsEntry::compareTo);
        return this;
    }




}

