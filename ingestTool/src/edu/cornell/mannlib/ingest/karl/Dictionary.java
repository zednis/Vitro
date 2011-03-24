package edu.cornell.mannlib.ingest.karl;

/*
Copyright © 2003-2008 by the Cornell University and the Cornell
Research Foundation, Inc.  All Rights Reserved.

Permission to use, copy, modify and distribute any part of VITRO
("WORK") and its associated copyrights for educational, research and
non-profit purposes, without fee, and without a written agreement is
hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Those desiring to incorporate WORK into commercial products or use
WORK and its associated copyrights for commercial purposes should
contact the Cornell Center for Technology Enterprise and
Commercialization at 395 Pine Tree Road, Suite 310, Ithaca, NY 14850;
email:cctecconnect@cornell.edu; Tel: 607-254-4698; FAX: 607-254-5454
for a commercial license.

IN NO EVENT SHALL THE CORNELL RESEARCH FOUNDATION, INC. AND CORNELL
UNIVERSITY BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
OUT OF THE USE OF WORK AND ITS ASSOCIATED COPYRIGHTS, EVEN IF THE
CORNELL RESEARCH FOUNDATION, INC. AND CORNELL UNIVERSITY MAY HAVE BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  */

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * A Dictionary can find words similar to the one input by the user
 * @author Karl Gluck
 */
public class Dictionary
{
    /// Logger used to display debugging information
    private static final Logger log = Logger.getLogger(Dictionary.class.getName());

    /// These constants specify the fewest number of candidates that should be
    /// found both above and below the source word's hash when searching a
    /// Dictionary.  These are important constants because the terms in the
    /// set are sorted by their hashCode() methods, which does not necessairily
    /// mean that the strings with the closest Levenshtein distances are placed
    /// near each other.  This constant essentially determines the scope of
    /// the search.
    /// The terms in the Dictionary are not sorted by Levenshtein distance to
    /// each other because this distance is an absolute value, and hence would
    /// not provide reliable ordering.
    private static final int MINIMUM_CANDIDATES_PER_SEARCH = 5;

    /// The maximum distance at which strings will match
    private static final int REASONABLE_LEVENSHTEIN_DISTANCE = 2;

    /// List of the terms in this dictionary
    private NavigableSet<String> myTerms;

    /**
     * A collection containing entries of this type is returned when a word is
     * looked up in the dictionary.
     */
    public final class Candidate implements Comparable<Candidate>
    {
        protected String myTerm;
        protected int myDistance;

        /**
         * Initializes the candidate
         */
        public Candidate( String term, Integer distance )
        {
            myTerm = term;
            myDistance = distance.intValue();
        }

        /**
         * Accesses information stored in the candidate
         */
        public String getTerm() { return myTerm; }
        public int getDistance() { return myDistance; }

        /**
         * Sorts this Candidate
         */
        public int compareTo( Candidate other )
        {
            // Basic comparison information
            if( other == null ) return +1;
            if( !(other instanceof Candidate) ) return +1;

            // Compare the distances
            return this.getDistance() - ((Candidate)other).getDistance();
        }

        /**
         * Converts this candidate to a string
         */
        public String toString()
        {
            return myDistance + ":  " + myTerm;
        }

        /**
         * Determines whether or not this candidate is a reasonable match for the
         * word that produced this Candidate in a search
         */
        public boolean isReasonableMatch()
        {
            return Dictionary.reasonableMatch( getDistance() );
        }
    }

    /**
     * Initializes a dictionary by giving it a file with terms.  This file
     * should be a simple text file with a single term per line.
     * @param TermsFile The path of the source file to open
     */
    public Dictionary( String termsFile ) throws FileNotFoundException, IOException
    {
        // Disable the logger
        log.setLevel( org.apache.log4j.Level.OFF );

        // Open up the requested stream
        FileReader termsReader = new FileReader( termsFile );

        // Tokenize this stream
        StreamTokenizer terms = new StreamTokenizer( termsReader );

        // Initialize the tokenizer
        terms.resetSyntax();
        terms.eolIsSignificant( true );
        terms.lowerCaseMode( false );
        terms.slashSlashComments( true );
        terms.slashStarComments( true );
        terms.wordChars( ' ', '~' );

        // Instantiate the terms
        myTerms = new TreeSet<String>();

        // Fill the internal set with terms
        while( StreamTokenizer.TT_EOF != terms.nextToken() )
        {
            // Add this word to the dictionary
            if( terms.ttype == StreamTokenizer.TT_WORD )
                addTerm( terms.sval );
        }
    }

    /**
     * Initializes an empty dictionary
     */
    public Dictionary()
    {
        // Disable the logger
        log.setLevel( org.apache.log4j.Level.OFF );

        // Create an empty set of terms
        myTerms = new TreeSet<String>();
    }

    /**
     * Adds a new term to this dictionary
     * @param term The term to add
     */
    public void addTerm( String term )
    {
        myTerms.add( term );
    }


    /**
     * Searches the dictionary and returns a set of the matches that are the
     * best candidates for being equivalent to the given word.  Distance is
     * determined via the Levenshtein algorithm.  The returned set is
     * sorted by the strength of the match, from strongest to least strong.
     */
    public SortedSet<Candidate> findClosestMatches( String word )
    {
        // Find the subsets near the word
        NavigableSet<String> headSet = myTerms.headSet( word, true  );
        NavigableSet<String> tailSet = myTerms.tailSet( word, false );

        // Used for looking around the sets
        String term;

        // The set of returned terms that are near the word
        SortedSet<Candidate> bestCandidates = new TreeSet<Candidate>();

        // Whether we're polling the head or tail set
        boolean pollingHead = true;

        // Look for matches in the lists.  This little piece of code here looks backward in the
        // head set and forward in the tail set as long as the values aren't null.  The only time
        // a null term is passed inside is when we're polling the head set but it ran out of data,
        // in which case the very first statement inside the loop will swap which set is being polled.
        while( (null != (term = (pollingHead ? headSet.pollLast() : tailSet.pollFirst() ))) || pollingHead )
        {
            // If we've got a null term but we're on the head list, just switch lists
            if( term == null && pollingHead )
            {
                pollingHead = false;
                continue;
            }

            // Get the distance between the terms
            int distance = getLevenshteinDistance( term, word );

            // These flags determine whether or not this match should be
            // added to the "best candidates" list.
            boolean requiredCandidate = bestCandidates.size() < MINIMUM_CANDIDATES_PER_SEARCH;
            boolean goodCandidate = reasonableMatch( distance );

            // Check the flags
            if( requiredCandidate || goodCandidate )
            {
                // Insert this candidate
                bestCandidates.add( new Candidate( term, distance ) );

                // Output information if this is not a good candidate; it's not a warning
                // because this just means that the scope of the search may be a little
                // too wide.
                if( !goodCandidate )
                {
                    log.info( "Candidate \"" + term +
                              "\" selected as possible match for \"" + word +
                              "\" even though it's not a good candidate" );
                }
            }
            else
            {
                // Change states.  If we were polling the head set, we flip the flag
                // so as to start polling the tail set
                if( pollingHead )
                    pollingHead = false;
                else
                    break; // We're done!
            }
        }

        // Build the set to return
        SortedSet<Candidate> returnedCandidates = new TreeSet<Candidate>();

        // Add entries to the returned candidates list that are reasonable.  If
        // none of the best candidates are reasonable, spit out a warning and
        // return at least one match.
        for( Iterator<Candidate> bestCandidate = bestCandidates.iterator();
                                 bestCandidate.hasNext(); )
        {
            // Get this candidate from the iterator
            Candidate candidate = bestCandidate.next();

            // Check to see whether or not this is a good candidate
            if( candidate.isReasonableMatch() )
                returnedCandidates.add( candidate );
        }

        // If we have no candidates, just add the first one in the list and discard
        // all the rest
        if( returnedCandidates.isEmpty() )
        {
            // Make sure we found something (if we didn't, no amount of
            // processing can give a result!)
            if( bestCandidates.isEmpty() )
            {
                log.warn( "No objects were returned for " + word );
                return returnedCandidates;
            }

            // Get the result we're going to add
            Candidate candidate = bestCandidates.first();

            // Insert in the return list
            returnedCandidates.add( bestCandidates.first() );

            // Warn that this result may be weird
            log.warn( "Candidate \"" + candidate.getTerm() +
                      "\" is being returned as the only candidate for \"" + word +
                      "\" but they probably aren't similar (distance = " + candidate.getDistance() + ")" );
        }
        else
            log.info( "From a list of " + bestCandidates.size() +
                      " candidates, the Dictionary has selected " + returnedCandidates.size() +
                      " to return as probable matches" );

        // Return the most probable matches
        return returnedCandidates;
    }

    /**
     * Determines whether the distance between two strings is slight enough
     * to be considered a candidate
     */
    protected static boolean reasonableMatch( int levenshteinDistance )
    {
        return levenshteinDistance <= REASONABLE_LEVENSHTEIN_DISTANCE;
    }

    /*
    Mr. Gilleland,

    As you may know, the Apache Jakarta Commons project had appropriated
    your sample implementation of the Levenshtein Distance algorithm for
    its commons-lang Java library.  While attempting to use it with two
    very large strings, I encountered an OutOfMemoryError, due to the fact
    that a matrix is created with the dimensions of the two strings'
    lengths.  I know you created the implementation to go with your
    (excellent) illustration of the algorithm, so this matrix approach
    translates that illustration and tutorial perfectly.

    However, as I said, the matrix approach doesn't lend itself to getting
    the edit distance of two large strings.  For this purpose, I modified
    your implementation to use two single-dimensional arrays; this is
    clearly more memory-friendly (although it probably results in some very
    slight performance degradation when comparing smaller strings).

    I've submitted the modification to the maintainers of the commons-lang
    project, and I've appended the relevant method below.

    Thanks!

    Chas Emerick  */
    public static int getLevenshteinDistance( String s, String t )
    {
        if ( s == null || t == null )
            throw new IllegalArgumentException( "Strings must not be null" );

        /*
          The difference between this impl. and the previous is that, rather
           than creating and retaining a matrix of size s.length()+1 by t.length()+1,
           we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
           is the 'current working' distance array that maintains the newest distance cost
           counts as we iterate through the characters of String s.  Each time we increment
           the index of String t we are comparing, d is copied to p, the second int[].  Doing so
           allows us to retain the previous cost counts as required by the algorithm (taking
           the minimum of the cost count to the left, up one, and diagonally up and to the left
           of the current cost count being calculated).  (Note that the arrays aren't really
           copied anymore, just switched...this is clearly much better than cloning an array
           or doing a System.arraycopy() each time  through the outer loop.)

           Effectively, the difference between the two implementations is this one does not
           cause an out of memory condition when calculating the LD over two very large strings.
          */

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n+1]; //'previous' cost array, horizontally
        int d[] = new int[n+1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i<=n; i++) {
        p[i] = i;
        }

        for (j = 1; j<=m; j++) {
            t_j = t.charAt(j-1);
            d[0] = j;

            for (i=1; i<=n; i++) {
                cost = s.charAt(i-1)==t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }
}
