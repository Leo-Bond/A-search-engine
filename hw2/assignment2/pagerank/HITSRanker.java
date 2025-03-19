/**
 *   Computes the Hubs and Authorities for an every document in a query-specific
 *   link graph, induced by the base set of pages.
 *
 *   @author Dmytro Kalpakchi
 */

package ir;

import java.util.*;
import java.io.*;


public class HITSRanker {

    /**
     *   Max number of iterations for HITS
     */
    final static int MAX_NUMBER_OF_STEPS = 1000;

    /**
     *   Convergence criterion: hub and authority scores do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.001;

    /**
     *   The inverted index
     */
    Index index;

    /**
     *   Mapping from the titles to internal document ids used in the links file
     */
    HashMap<String,Integer> titleToId = new HashMap<String,Integer>();

    /**
     *   Sparse vector containing hub scores
     */
    HashMap<Integer,Double> hubs;

    /**
     *   Sparse vector containing authority scores
     */
    HashMap<Integer,Double> authorities;



    HashMap<Integer, HashMap<Integer, Boolean>> link = new HashMap<>();

    int numNodes = 0;




    /* --------------------------------------------- */

    /**
     * Constructs the HITSRanker object
     * 
     * A set of linked documents can be presented as a graph.
     * Each page is a node in graph with a distinct nodeID associated with it.
     * There is an edge between two nodes if there is a link between two pages.
     * 
     * Each line in the links file has the following format:
     *  nodeID;outNodeID1,outNodeID2,...,outNodeIDK
     * This means that there are edges between nodeID and outNodeIDi, where i is between 1 and K.
     * 
     * Each line in the titles file has the following format:
     *  nodeID;pageTitle
     *  
     * NOTE: nodeIDs are consistent between these two files, but they are NOT the same
     *       as docIDs used by search engine's Indexer
     *
     * @param      linksFilename   File containing the links of the graph
     * @param      titlesFilename  File containing the mapping between nodeIDs and pages titles
     * @param      index           The inverted index
     */
    public HITSRanker( String linksFilename, String titlesFilename, Index index ) {
        this.index = index;
        readDocs( linksFilename, titlesFilename );
    }


    /* --------------------------------------------- */

    /**
     * A utility function that gets a file name given its path.
     * For example, given the path "davisWiki/hello.f",
     * the function will return "hello.f".
     *
     * @param      path  The file path
     *
     * @return     The file name.
     */
    private String getFileName( String path ) {
        String result = "";
        StringTokenizer tok = new StringTokenizer( path, "\\/" );
        while ( tok.hasMoreTokens() ) {
            result = tok.nextToken();
        }
        return result;
    }


    /**
     * Reads the files describing the graph of the given set of pages.
     *
     * @param      linksFilename   File containing the links of the graph
     * @param      titlesFilename  File containing the mapping between nodeIDs and pages titles
     */
//    void readDocs( String linksFilename, String titlesFilename ) {
//        //
//        // YOUR CODE HERE
//        //
//    }
    void readDocs(String linksFilename, String titlesFilename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(titlesFilename));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    int nodeID = Integer.parseInt(parts[0].trim());
                    String pageTitle = parts[1].trim();
                    titleToId.put(pageTitle, nodeID);
                    if (nodeID + 1 > numNodes) {
                        numNodes = nodeID + 1;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(linksFilename));
            String line;
            while ((line = reader.readLine()) != null) {
                int semicolonIndex = line.indexOf(";");
                if (semicolonIndex == -1) continue;
                int fromNode = Integer.parseInt(line.substring(0, semicolonIndex).trim());
                String outPart = line.substring(semicolonIndex + 1).trim();
                if (!link.containsKey(fromNode)) {
                    link.put(fromNode, new HashMap<Integer, Boolean>());
                }
                if (!outPart.isEmpty()) {
                    String[] outNodes = outPart.split(",");
                    for (String outNodeStr : outNodes) {
                        int toNode = Integer.parseInt(outNodeStr.trim());
                        link.get(fromNode).put(toNode, true);
                        if (toNode + 1 > numNodes) {
                            numNodes = toNode + 1;
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Perform HITS iterations until convergence
     *
     * @param      titles  The titles of the documents in the root set
     */
//    private void iterate(String[] titles) {
//        //
//        // YOUR CODE HERE
//        //
//    }

    private void iterate(String[] titles) {
        hubs = new HashMap<Integer, Double>();
        authorities = new HashMap<Integer, Double>();
        for (int i = 0; i < numNodes; i++) {
            hubs.put(i, 1.0);
            authorities.put(i, 1.0);
        }

        for (int step = 0; step < MAX_NUMBER_OF_STEPS; step++) {
            // 新的 authority 和 hub 分数
            HashMap<Integer, Double> newAuthorities = new HashMap<>();
            HashMap<Integer, Double> newHubs = new HashMap<>();
            for (int i = 0; i < numNodes; i++) {
                newAuthorities.put(i, 0.0);
                newHubs.put(i, 0.0);
            }

            for (Map.Entry<Integer, HashMap<Integer, Boolean>> entry : link.entrySet()) {
                int j = entry.getKey();
                HashMap<Integer, Boolean> outlinks = entry.getValue();
                for (Integer i : outlinks.keySet()) {
                    newAuthorities.put(i, newAuthorities.get(i) + hubs.get(j));
                }
            }

            for (Map.Entry<Integer, HashMap<Integer, Boolean>> entry : link.entrySet()) {
                int i = entry.getKey();
                HashMap<Integer, Boolean> outlinks = entry.getValue();
                double sum = 0.0;
                for (Integer k : outlinks.keySet()) {
                    sum += authorities.getOrDefault(k, 0.0);
                }
                newHubs.put(i, sum);
            }

            double normA = 0.0;
            for (double value : newAuthorities.values()) {
                normA += value * value;
            }
            normA = Math.sqrt(normA);
            if (normA != 0) {
                for (int i = 0; i < numNodes; i++) {
                    newAuthorities.put(i, newAuthorities.get(i) / normA);
                }
            }

            double normH = 0.0;
            for (double value : newHubs.values()) {
                normH += value * value;
            }
            normH = Math.sqrt(normH);
            if (normH != 0) {
                for (int i = 0; i < numNodes; i++) {
                    newHubs.put(i, newHubs.get(i) / normH);
                }
            }

            double diff = 0.0;
            for (int i = 0; i < numNodes; i++) {
                diff += Math.abs(newAuthorities.get(i) - authorities.get(i));
                diff += Math.abs(newHubs.get(i) - hubs.get(i));
            }

            hubs = newHubs;
            authorities = newAuthorities;

            if (diff < EPSILON) {
                System.err.println("HITS converged after " + (step + 1) + " iterations.");
                break;
            }
        }
    }



    /**
     * Rank the documents in the subgraph induced by the documents present
     * in the postings list `post`.
     *
     * @param      post  The list of postings fulfilling a certain information need
     *
     * @return     A list of postings ranked according to the hub and authority scores.
     */
    PostingsList rank(PostingsList post) {
        //
        // YOUR CODE HERE
        //
        return null;
    }


    /**
     * Sort a hash map by values in the descending order
     *
     * @param      map    A hash map to sorted
     *
     * @return     A hash map sorted by values
     */
    private HashMap<Integer,Double> sortHashMapByValue(HashMap<Integer,Double> map) {
        if (map == null) {
            return null;
        } else {
            List<Map.Entry<Integer,Double> > list = new ArrayList<Map.Entry<Integer,Double> >(map.entrySet());
      
            Collections.sort(list, new Comparator<Map.Entry<Integer,Double>>() {
                public int compare(Map.Entry<Integer,Double> o1, Map.Entry<Integer,Double> o2) { 
                    return (o2.getValue()).compareTo(o1.getValue()); 
                } 
            }); 
              
            HashMap<Integer,Double> res = new LinkedHashMap<Integer,Double>(); 
            for (Map.Entry<Integer,Double> el : list) { 
                res.put(el.getKey(), el.getValue()); 
            }
            return res;
        }
    } 


    /**
     * Write the first `k` entries of a hash map `map` to the file `fname`.
     *
     * @param      map        A hash map
     * @param      fname      The filename
     * @param      k          A number of entries to write
     */
    void writeToFile(HashMap<Integer,Double> map, String fname, int k) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fname));
            
            if (map != null) {
                int i = 0;
                for (Map.Entry<Integer,Double> e : map.entrySet()) {
                    i++;
                    writer.write(e.getKey() + ": " + String.format("%.5g%n", e.getValue()));
                    if (i >= k) break;
                }
            }
            writer.close();
        } catch (IOException e) {}
    }


    /**
     * Rank all the documents in the links file. Produces two files:
     *  hubs_top_30.txt with documents containing top 30 hub scores
     *  authorities_top_30.txt with documents containing top 30 authority scores
     */
    void rank() {
        iterate(titleToId.keySet().toArray(new String[0]));
        HashMap<Integer,Double> sortedHubs = sortHashMapByValue(hubs);
        HashMap<Integer,Double> sortedAuthorities = sortHashMapByValue(authorities);
        writeToFile(sortedHubs, "hubs_top_30.txt", 30);
        writeToFile(sortedAuthorities, "authorities_top_30.txt", 30);
    }


    /* --------------------------------------------- */


    public static void main( String[] args ) {
        if ( args.length != 2 ) {
            System.err.println( "Please give the names of the link and title files" );
        }
        else {
            HITSRanker hr = new HITSRanker( args[0], args[1], null );
            hr.rank();
        }
    }
} 