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

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

//import edu.cornell.mannlib.ingest.karl.Dictionary.Candidate;

public class DictionaryMap<ObjectType>
{
    /**
     * A collection containing entries of this type is returned when a key is
     * looked up in a dictionary map.
     */
    public class Candidate
    {
        protected ObjectType myTerm;
        protected int myDistance;

        /**
         * Initializes the candidate
         */
        public Candidate( ObjectType term, Integer distance )
        {
            myTerm = term;
            myDistance = distance.intValue();
        }

        /**
         * Accesses information stored in the candidate
         */
        public ObjectType getTerm() { return myTerm; }
        public int getDistance() { return myDistance; }

        /**
         * Sorts this Candidate
         */
        public int compareTo( Candidate other )
        {
            // Basic comparison information
            if( other == null ) return +1;
            if( !(other instanceof DictionaryMap.Candidate) ) return +1;

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

    // Dictionary used to look up terms
    protected Dictionary myKeys;

    // Map of correctly-spelled terms to an ambiguous type
    protected HashMap<String,ObjectType> myMap;

    // Allowed distance
    private int myKeyMatchDistance;

    /**
     * Creates a new dictionary map
     */
    public DictionaryMap( int keyMatchDistance )
    {
        myKeyMatchDistance = keyMatchDistance;
        myKeys = new Dictionary();
        myMap = new HashMap<String,ObjectType>();
    }

    /**
     * Adds a new entry to this map
     */
    public void put( String key, ObjectType value )
    {
        // Add this term to the set of keys
        myKeys.addTerm( key );

        // Add a new entry to the map
        myMap.put( key, value );
    }

    /**
     * Gets all of the closest matches for a given key
     */
    public SortedSet<Candidate> findClosestMatches( String key )
    {
        // Get the matches for this key
        SortedSet<Dictionary.Candidate> candidates = myKeys.findClosestMatches( key );

        // For each of the entries, add the value type to the output set
        SortedSet<Candidate> output = new TreeSet<Candidate>();
        for( Dictionary.Candidate candidate : candidates )
            output.add( new Candidate( myMap.get( candidate.getTerm() ), candidate.getDistance() ) );

        // Return the output set
        return output;
    }

    /**
     * Searches the map for a value with the given key (or one very close to it)
     * TODO: this method is badly implemented
     */
    public ObjectType get( String key )
    {
        // Try to get this key from the map explicitly
        ObjectType value = myMap.get( key );

        // If we didn't find anything, look up the key in the dictionary
        if( value == null )
        {
            // Get things that are close to the key
            SortedSet<Dictionary.Candidate> candidates = myKeys.findClosestMatches( key );

            // Make sure the returned match is valid
            if( candidates.size() == 0 ||
                candidates.first().getDistance() > myKeyMatchDistance )
                return null;

            // Be sure that there is one match that's better than another
            if( candidates.size() > 1 )
            {
                Dictionary.Candidate candidateArray[] = (Dictionary.Candidate[])candidates.toArray();
                if( candidateArray[0].getDistance() == candidateArray[1].getDistance() )
                    return null;
            }

            // Look up the new value
            value = myMap.get( candidates.first().getTerm() );
        }

        // Return the value of the object we found with this key
        return value;
    }
}
