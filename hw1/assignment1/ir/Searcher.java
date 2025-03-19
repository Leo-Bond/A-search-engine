/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;

    PageRank pageRank;

    HITSRanker hitsRanker;

    final double tf_idf_weight = 1;
    final double page_rank_weight =100;


    final HashMap<Integer, Double> euclideanLengths = new HashMap<>();

    
    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
        index.readEuclideanLength(euclideanLengths);
        pageRank = new PageRank("linksDavis.txt");
        hitsRanker = new HITSRanker("linksDavis.txt", "davisTitles.txt",null);
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
//    public PostingsList search( Query query, QueryType queryType, RankingType rankingType, NormalizationType normType ) {
//        //
//        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
//        //
//
//        String token = query.queryterm.get(0).term;
//        PostingsList postings = index.getPostings(token);
//        return postings;
//    }

    public PostingsList search(Query query, QueryType queryType, RankingType rankingType, NormalizationType normType) {

        if (queryType == QueryType.PHRASE_QUERY) {
            return phraseQuery(query);
        } else if (queryType == QueryType.INTERSECTION_QUERY) {
            return intersectionQuery(query);
        } else if (queryType == QueryType.RANKED_QUERY) {

            if (rankingType == RankingType.TF_IDF) {
                return rankedQuery(query, normType);
            }
            if (rankingType == RankingType.PAGERANK) {
                return rankedQuery_PageRank(query);

            }
            if (rankingType == RankingType.COMBINATION) {
                return rankedQuery_Combination(query);

            }
            if (rankingType == RankingType.HITS) {
                return  rankedQuery_HITS(query);
            }



        }
        return null;// Return the final intersection result
    }

    /**
     * Intersects two postings lists using the intersection algorithm.
     *
     * @param p1 The first postings list.
     * @param p2 The second postings list.
     * @return A new postings list containing the intersection of p1 and p2.
     */

    private PostingsList intersectionQuery(Query query) {
        // Get all query terms
        ArrayList<PostingsList> postingsLists = new ArrayList<>();

        for (Query.QueryTerm queryTerm : query.queryterm) {
            PostingsList postings = index.getPostings(queryTerm.term);
            if (postings != null) {
                postingsLists.add(postings);
            }
        }
        PostingsList result = postingsLists.get(0);
        for (int i = 1; i < postingsLists.size(); i++) {
            result = intersect(result, postingsLists.get(i));
        }

        return result; // Return the final intersection result
    }




    private PostingsList intersect(PostingsList p1, PostingsList p2) {
        PostingsList result = new PostingsList();
        int i = 0, j = 0;

        while (i < p1.size() && j < p2.size()) {
            PostingsEntry entry1 = p1.get(i);
            PostingsEntry entry2 = p2.get(j);

            if (entry1.docID == entry2.docID) {
                PostingsEntry newEntry = new PostingsEntry(entry1.docID, entry1.offsets, entry1.score);
                result.insert(newEntry);
                i++;
                j++;
            } else if (entry1.docID < entry2.docID) {
                i++;
            } else {
                j++;
            }
        }

        return result;
    }

    private PostingsList phraseQuery(Query query) {
        ArrayList<PostingsList> postingsLists = new ArrayList<>();
        for (Query.QueryTerm queryTerm : query.queryterm) {
            PostingsList postings = index.getPostings(queryTerm.term);
            if (postings != null) {
                postingsLists.add(postings);
            }
        }

        PostingsList result = postingsLists.get(0);
        for (int i = 1; i < postingsLists.size(); i++) {
            result = positionalIntersection(result, postingsLists.get(i), 1);
        }

        return result;

    }

    private PostingsList positionalIntersection(PostingsList p1, PostingsList p2, int k) {
        PostingsList result = new PostingsList();
        int i = 0, j = 0;

        while (i < p1.size() && j < p2.size()) {
            PostingsEntry entry1 = p1.get(i);
            PostingsEntry entry2 = p2.get(j);

            if (entry1.docID == entry2.docID) {
                ArrayList<Integer> offsets1 = entry1.offsets;
                ArrayList<Integer> offsets2 = entry2.offsets;
                ArrayList<Integer> matchingOffsets = new ArrayList<>();
                int m = 0, n = 0;

                while (m < offsets1.size() && n < offsets2.size()) {
                    if (offsets1.get(m) + k == offsets2.get(n)) {
                        matchingOffsets.add(offsets2.get(n));
                        m++;
                        n++;
                    } else if (offsets1.get(m) + k < offsets2.get(n)) {
                        m++;
                    } else {
                        n++;
                    }
                }

                if (!matchingOffsets.isEmpty()) {
                    PostingsEntry newEntry = new PostingsEntry(entry1.docID, matchingOffsets, entry1.score);
                    result.insert(newEntry);
                }

                i++;
                j++;
            } else if (entry1.docID < entry2.docID) {
                i++;
            } else {
                j++;
            }
        }

        return result;
    }

    private PostingsList rankedQuery(Query query, NormalizationType normType) {
        PostingsList result = new PostingsList();

        int N = index.docLengths.size();
        double[] tf_idf = new double[N];
        for (Query.QueryTerm queryTerm : query.queryterm) {
            PostingsList postings = index.getPostings(queryTerm.term);
            double idf = Math.log((double) N / postings.size());
            for (int i = 0; i < postings.size(); i++) {
                int docID = postings.get(i).docID;
                double tf = postings.get(i).score;
                tf_idf[docID] += idf * tf;
            }
        }
        for (int i = 0; i < N; i++) {
            if (tf_idf[i] > 0) {
                if (normType == NormalizationType.NUMBER_OF_WORDS) {
                    tf_idf[i] /= index.docLengths.get(i);
                } else if (normType == NormalizationType.EUCLIDEAN) {
                    tf_idf[i] /= euclideanLengths.get(i);
                }
                PostingsEntry newEntry = new PostingsEntry(i, 0, tf_idf[i]);
                result.insert(newEntry);
            }
        }
        result.sort();
        return result;


    }

    private PostingsList rankedQuery_PageRank(Query query) {
        PostingsList result = new PostingsList();
        int N = index.docLengths.size();
        double[] page_ranks = new double[N];
        for (Query.QueryTerm queryTerm : query.queryterm) {
            PostingsList postings = index.getPostings(queryTerm.term);
            for (int i = 0; i < postings.size(); i++) {
                int docID = postings.get(i).docID;
                String docName = index.docNames.get(docID);
                int index = docName.lastIndexOf("\\");
                String fileName = docName.substring(index + 1);
                System.out.printf("%s\n", fileName);
                page_ranks[docID] += pageRank.getPageRank(fileName);
                System.out.printf("%,6f\n", page_ranks[docID]);

            }
        }
        for (int i = 0; i < N; i++) {
            if(page_ranks[i] > 0) {
                PostingsEntry newEntry = new PostingsEntry(i, 0, page_ranks[i]);
                result.insert(newEntry);
            }
        }
        result.sort();
        return result;


    }

    private PostingsList rankedQuery_Combination(Query query) {
        PostingsList result = new PostingsList();
        int N = index.docLengths.size();
        double[] scores = new double[N];
        double[] tf_idf = new double[N];
        for (Query.QueryTerm queryTerm : query.queryterm) {
            PostingsList postings = index.getPostings(queryTerm.term);
            double idf = Math.log((double) N / postings.size());
            for (int i = 0; i < postings.size(); i++) {
                int docID = postings.get(i).docID;
                double tf = postings.get(i).score;
                tf_idf[docID] += idf * tf;
            }
        }
        for (int i = 0; i < N; i++) {
            if (tf_idf[i] > 0) {
                tf_idf[i] /= index.docLengths.get(i);


                String docName = index.docNames.get(i);
                int index = docName.lastIndexOf("\\");
                String fileName = docName.substring(index + 1);
                System.out.printf("%s\n", fileName);
                scores[i] = tf_idf_weight * tf_idf[i] + page_rank_weight * pageRank.getPageRank(fileName);
                PostingsEntry newEntry = new PostingsEntry(i, 0, scores[i]);
                result.insert(newEntry);
            }

        }
        result.sort();
        return result;


    }

    private PostingsList rankedQuery_HITS(Query query) {
        PostingsList result = new PostingsList();
        HashMap<Integer, Double> hits_scores = new HashMap<>();
        for (Query.QueryTerm queryTerm : query.queryterm) {
            PostingsList postings = index.getPostings(queryTerm.term);
            PostingsList hits = hitsRanker.rank(postings);
            for (int i = 0; i < hits.size(); i++) {
                int docID = hits.get(i).docID;
                double score = hits.get(i).score;
                if (! hits_scores.containsKey(docID)) {
                    hits_scores.put(docID, score);
                }

            }
        }
        for (int docID : hits_scores.keySet()) {
                PostingsEntry newEntry = new PostingsEntry(docID, 0, hits_scores.get(docID));
                result.insert(newEntry);

        }
        result.sort();
        return result;


    }






}

