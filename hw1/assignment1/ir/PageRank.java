package ir;
import java.util.*;
import java.io.*;


public class PageRank {

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

	static double[] rank;

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a HashMap, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a HashMap whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;


	String titlesFilePath = "davisTitles.txt";
	String[] docTitle = new String[MAX_NUMBER_OF_DOCS];
	HashMap<String, Integer> fileNameToDocId = new HashMap<>();
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	docTitle = readTitles(titlesFilePath);

	iterate( noOfDocs, 1000 );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. 
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new HashMap<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
//    void iterate( int numberOfDocs, int maxIterations ) {
//
//	// YOUR CODE HERE
//
//
//    }
//	public void iterate( int noOfDocs, int maxIterations ) {
//
//		double[][] P = new double[noOfDocs][noOfDocs];
//
//		for (int j = 0; j < noOfDocs; j++) {
//			if (out[j] == 0) {
//
//				for (int i = 0; i < noOfDocs; i++) {
//					P[i][j] = 1.0 / noOfDocs;
//				}
//			} else {
//				if (link.get(j) != null) {
//					for (Integer target : link.get(j).keySet()) {
//						P[target][j] = 1.0 / out[j];
//					}
//				}
//			}
//		}
//
//		double c = 1 - BORED;
//		double[][] G = new double[noOfDocs][noOfDocs];
//		for (int i = 0; i < noOfDocs; i++) {
//			for (int j = 0; j < noOfDocs; j++) {
//				G[i][j] = c * P[i][j] + (1 - c) / noOfDocs;
//			}
//		}
//
//		double[] rank = new double[noOfDocs];
//		double[] newRank = new double[noOfDocs];
//
//		for (int i = 0; i < noOfDocs; i++) {
//			rank[i] = 1.0 / noOfDocs;
//		}
//
//		for (int iter = 0; iter < maxIterations; iter++) {
//			Arrays.fill(newRank, 0.0);
//
//			for (int i = 0; i < noOfDocs; i++) {
//				double sum = 0.0;
//				for (int j = 0; j < noOfDocs; j++) {
//					sum += rank[j] * G[i][j];
//				}
//				newRank[i] = sum;
//			}
//
//			double diff = 0.0;
//			for (int i = 0; i < noOfDocs; i++) {
//				diff += Math.abs(newRank[i] - rank[i]);
//			}
//
//			System.arraycopy(newRank, 0, rank, 0, noOfDocs);
//
//			if (diff < EPSILON) {
//				System.err.println("Converged after " + (iter + 1) + " iterations.");
//				break;
//			}
//		}
//
//		Integer[] docIndices = new Integer[noOfDocs];
//		for (int i = 0; i < noOfDocs; i++) {
//			docIndices[i] = i;
//		}
//		Arrays.sort(docIndices, new Comparator<Integer>() {
//			public int compare(Integer a, Integer b) {
//				return Double.compare(rank[b], rank[a]);
//			}
//		});
//
//		System.out.println("Top 30 pages by PageRank (Matrix version):");
//		for (int i = 0; i < Math.min(30, noOfDocs); i++) {
//			int docId = docIndices[i];
//			System.out.printf("%s : %.6f\n", docName[docId], rank[docId]);
//		}
//	}



	void iterate( int numberOfDocs, int maxIterations ) {
		rank = new double[numberOfDocs];
		double[] newRank = new double[numberOfDocs];


		for (int i = 0; i < numberOfDocs; i++) {
			rank[i] = 1.0 / numberOfDocs;
		}

		for (int iter = 0; iter < maxIterations; iter++) {
			Arrays.fill(newRank, 0.0);

			double danglingSum = 0.0;
			for (int j = 0; j < numberOfDocs; j++) {
				if (out[j] == 0) {
					danglingSum += rank[j];
				}
			}
			for (int i = 0; i < numberOfDocs; i++) {
				newRank[i] += danglingSum / numberOfDocs;
			}

			for (int j = 0; j < numberOfDocs; j++) {
				if (out[j] > 0) {
					HashMap<Integer, Boolean> outlinks = link.get(j);
					if (outlinks != null) {
						double share = rank[j] / out[j];
						for (Integer target : outlinks.keySet()) {
							newRank[target] += share;
						}
					}
				}
			}

			for (int i = 0; i < numberOfDocs; i++) {
				newRank[i] = BORED / numberOfDocs + (1 - BORED) * newRank[i];
			}

			double diff = 0.0;
			for (int i = 0; i < numberOfDocs; i++) {
				diff += Math.abs(newRank[i] - rank[i]);
			}

			System.arraycopy(newRank, 0, rank, 0, numberOfDocs);

			if (diff < EPSILON) {
				System.err.println("Converged after " + (iter + 1) + " iterations.");
				break;
			}
		}

		Integer[] docIndices = new Integer[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			docIndices[i] = i;
		}
		Arrays.sort(docIndices, new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				return Double.compare(rank[b], rank[a]);
			}
		});

		System.out.println("Top 30 pages by PageRank:");
		for (int i = 0; i < Math.min(30, numberOfDocs); i++) {
			int docId = docIndices[i];
			int number = Integer.parseInt(docName[docId]);

			String title = docTitle[number];
			System.out.printf("%s, %s : %.6f\n", title, docName[docId], rank[docId]);
		}

		try (PrintWriter writer = new PrintWriter(new FileWriter("pagerankScores.txt"))) {
			for (int i = 0; i < numberOfDocs; i++) {
				int docId = docIndices[i];
				writer.printf("%.6f\t%s%n", rank[docId], docName[docId]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String[] readTitles(String filename) {
		String[] docTitle = new String[MAX_NUMBER_OF_DOCS];
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(";");
				if (parts.length == 2) {
					int docID = Integer.parseInt(parts[0].trim());
					String title = parts[1].trim();
					if (docID >= 0 && docID < MAX_NUMBER_OF_DOCS) {
						docTitle[docID] = title;
						fileNameToDocId.put(title, docID);
					} else {
						System.err.println("out of range");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return docTitle;
	}

	public double getPageRank(String fileName) {
		Integer docName = fileNameToDocId.get(fileName);
		String docNameStr = docName.toString();
		Integer docId = docNumber.get(docNameStr);


		System.out.printf("%d\n", docId);
		return rank[docId];
	}



	/* --------------------------------------------- */



    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
		PageRank pageRank = new PageRank( args[0] );
		System.out.printf("%.6f\n", pageRank.getPageRank("UC_Davis.f"));
	}
    }
}