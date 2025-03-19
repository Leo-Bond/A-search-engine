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

	HashMap<String, Double> exect_pagerank = new HashMap<String, Double>();

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


//	String titlesFilePath = "davisTitles.txt";
	String titlesFilePath = "svwikiTitles.txt";
	String[] docTitle = new String[MAX_NUMBER_OF_DOCS];
	HashMap<String, Integer> fileNameToDocId = new HashMap<>();
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	docTitle = readTitles(titlesFilePath);
    iterate( noOfDocs, 1000 );
//
//	for(int i=10; i<=100; i+=5 ) {
//		mc_random_start(noOfDocs, 17478 * i );
//	}
//
//	for(int i=1; i<100; i+=5 ) {
//		mc_cyclic_start(noOfDocs, 17478 * i);
//	}
//
//	for(int i=1; i<100; i+=5 ) {
//		mc_complete_path_stopping_at_dangling_nodes(noOfDocs, 17478 * i);
//	}
//
//	for (int i=1; i<100; i+=5 ) {
//		mc_complete_path_with_random_start(noOfDocs, 17478 * i);
//	}

	mc_random_start_svwiki(noOfDocs, 100000);


//	mc_cyclic_start(noOfDocs, 17478 * 100);
//	mc_complete_path_stopping_at_dangling_nodes(noOfDocs, 17478 * 100);
//	mc_complete_path_with_random_start(noOfDocs, 17478 * 100);

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
				exect_pagerank.put(docName[docId], rank[docId]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	void mc_random_start(int numberOfDocs, int totalRuns) {
		HashMap<String, Double> mc1_pagerank = new HashMap<String, Double>();
		int[] endCount = new int[numberOfDocs];
		Random rand = new Random();

		for (int run = 0; run < totalRuns; run++) {

			int currentPage = rand.nextInt(numberOfDocs);

			while (true) {
				if (rand.nextDouble() < BORED) {
					break;
				}
				if (out[currentPage] == 0) {
					currentPage = rand.nextInt(numberOfDocs);
					continue;
				}
				HashMap<Integer, Boolean> outlinks = link.get(currentPage);
				List<Integer> targets = new ArrayList<>(outlinks.keySet());
				int randomIndex = rand.nextInt(targets.size());
				currentPage = targets.get(randomIndex);
			}
			endCount[currentPage]++;
		}

		double[] pr = new double[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			pr[i] = (double) endCount[i] / totalRuns;
		}

		outputRank(numberOfDocs, pr, "mc_random_start_top30.txt", mc1_pagerank);

		compute_squared_differences(mc1_pagerank, "mc1_squared_differences.txt");

	}

	void mc_cyclic_start(int numberOfDocs, int totalRuns) {
		HashMap<String, Double> mc2_pagerank = new HashMap<String, Double>();
		int[] endCount = new int[numberOfDocs];
		int m = totalRuns / numberOfDocs;
		Random rand = new Random();

		for (int startPage = 0; startPage < numberOfDocs; startPage++) {
			for (int run = 0; run < m; run++) {
				int currentPage = startPage;
				while (true) {
					if (rand.nextDouble() < BORED) {
						break;
					}
					if (out[currentPage] == 0) {
						currentPage = rand.nextInt(numberOfDocs);
						continue;
					}
					HashMap<Integer, Boolean> outlinks = link.get(currentPage);
					List<Integer> targets = new ArrayList<>(outlinks.keySet());
					int randomIndex = rand.nextInt(targets.size());
					currentPage = targets.get(randomIndex);
				}
				endCount[currentPage]++;
			}
		}

		double[] pr = new double[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			pr[i] = (double) endCount[i] / totalRuns;
		}

		outputRank(numberOfDocs, pr, "mc_cyclic_start_top30.txt", mc2_pagerank);


		compute_squared_differences(mc2_pagerank, "mc2_squared_differences.txt");
	}


	void mc_complete_path_stopping_at_dangling_nodes(int numberOfDocs, int totalRuns) {
		HashMap<String, Double> mc4_pagerank = new HashMap<String, Double>();
		int[] visitCount = new int[numberOfDocs];
		int m = totalRuns / numberOfDocs;
		Random rand = new Random();

		for (int startPage = 0; startPage < numberOfDocs; startPage++) {
			for (int run = 0; run < m; run++) {

				int currentPage = startPage;
				while (rand.nextDouble() > BORED) {
					visitCount[currentPage]++;

					if (out[currentPage] == 0) {
						break;
					}

					HashMap<Integer, Boolean> outlinks = link.get(currentPage);
					List<Integer> targets = new ArrayList<>(outlinks.keySet());
					int randomIndex = rand.nextInt(targets.size());
					currentPage = targets.get(randomIndex);
				}
			}
		}

		int totalVisits = 0;
		for (int i = 0; i < numberOfDocs; i++) {
			totalVisits += visitCount[i];
		}

		double[] pr = new double[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			pr[i] = (double) visitCount[i] / totalVisits;
		}

		outputRank(numberOfDocs, pr, "mc_complete_path_stopping_at_dangling_nodes_top30.txt", mc4_pagerank);


		compute_squared_differences(mc4_pagerank, "mc4_squared_differences.txt");
	}

	void mc_complete_path_with_random_start(int numberOfDocs, int totalRuns) {
		HashMap<String, Double> mc5_pagerank = new HashMap<String, Double>();
		int[] visitCount = new int[numberOfDocs];
		Random rand = new Random();


		for (int run = 0; run < totalRuns; run++) {
			int currentPage = rand.nextInt(numberOfDocs);
			while (rand.nextDouble() > BORED) {
				visitCount[currentPage]++;

				if (out[currentPage] == 0) {
					break;
				}

				HashMap<Integer, Boolean> outlinks = link.get(currentPage);
				List<Integer> targets = new ArrayList<>(outlinks.keySet());
				int randomIndex = rand.nextInt(targets.size());
				currentPage = targets.get(randomIndex);
			}

		}

		int totalVisits = 0;
		for (int i = 0; i < numberOfDocs; i++) {
			totalVisits += visitCount[i];
		}

		double[] pr = new double[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			pr[i] = (double) visitCount[i] / totalVisits;
		}

		outputRank(numberOfDocs, pr, "mc_complete_path_with_random_start_top30.txt", mc5_pagerank);

		compute_squared_differences(mc5_pagerank, "mc5_squared_differences.txt");
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



	void outputRank(int numberOfDocs, double[] array, String fileName, HashMap<String, Double> estimate_rank) {

		Integer[] indices = new Integer[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			indices[i] = i;
		}
		Arrays.sort(indices, new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				return Double.compare(array[b], array[a]);
			}
		});

		System.out.println("Top 30 pages by PageRank (Monte Carlo simulation):");
		for (int i = 0; i < 30; i++) {
			int doc = indices[i];
			System.out.printf("%s: %.6f\n", docName[doc], array[doc]);
		}

		try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
			for (int i = 0; i < 30; i++) {
				int doc = indices[i];
				writer.printf("%s, %s: %.6f\n", docName[doc], docTitle[Integer.parseInt(docName[doc])], array[doc]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 30; i++) {
			int doc = indices[i];
			estimate_rank.put(docName[doc], array[doc]);
		}
	}


	void compute_squared_differences(HashMap<String, Double> estimate_rank, String fileName) {
		double sum_squared_diff = 0;
		for (Map.Entry<String, Double> entry : estimate_rank.entrySet()) {
			String doc = entry.getKey();
			double estimatedValue = entry.getValue();

			if (exect_pagerank.containsKey(doc)) {
				double exactValue = exect_pagerank.get(doc);
				double diff = estimatedValue - exactValue;
				sum_squared_diff += diff * diff;
			} else {
				System.err.println("Exact pagerank does not contain document: " + doc);
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
			writer.newLine();
			writer.write(String.format("%.9f", sum_squared_diff));
		} catch (IOException e) {
			e.printStackTrace();
		}


	}


	void mc_random_start_svwiki(int numberOfDocs, int totalRuns) {
		HashMap<String, Double> mc1_pagerank = new HashMap<String, Double>();
		int[] endCount = new int[numberOfDocs];
		Random rand = new Random();

		for (int run = 0; run < totalRuns; run++) {

			int currentPage = rand.nextInt(numberOfDocs);

			while (true) {
				if (rand.nextDouble() < BORED) {
					break;
				}
				if (out[currentPage] == 0) {
					currentPage = rand.nextInt(numberOfDocs);
					continue;
				}
				HashMap<Integer, Boolean> outlinks = link.get(currentPage);
				List<Integer> targets = new ArrayList<>(outlinks.keySet());
				int randomIndex = rand.nextInt(targets.size());
				currentPage = targets.get(randomIndex);
			}
			endCount[currentPage]++;
		}

		double[] pr = new double[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			pr[i] = (double) endCount[i] / totalRuns;
		}

		outputRank(numberOfDocs, pr, "svwiki_top30.txt", mc1_pagerank);


	}

	/* --------------------------------------------- */



    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
		PageRank pageRank = new PageRank( args[0] );
	}
    }
}