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

import java.util.ArrayList;
import java.util.SortedSet;
import java.lang.StringBuffer;


public class IngestFormatter
{
    /**
     * Takes the incoming string of data and turns it into some other format
     * @param input Source string
     * @return List of returned strings
     */
    public ArrayList<String> format( String input )
    {
        // By default, do no formatting
        return listOf( input );
    }

    /**
     * Creates a list from a single entry
     */
    public static ArrayList<String> listOf( String entry )
    {
        // Create a new list
        ArrayList<String> list = new ArrayList<String>();

        // Add this entry
        list.add( entry );

        // Return the list
        return list;
    }

    /**
     * Creates an empty array list of strings
     */
    public static ArrayList<String> emptyList()
    {
        return new ArrayList<String>();
    }

    /**
     * Turns the input into a list of strings, broken up by the delimiting string
     */
    public static ArrayList<String> tokenize( String input, String delimiter )
    {
        // Create a buffer with which to tokenize the input
        StringBuffer tokenizer = new StringBuffer( input );

        // The list of strings to build
        ArrayList<String> list = new ArrayList<String>();

        // Iteration information
        int index = tokenizer.indexOf( delimiter );
        int lastIndexFound = 0;
        int delimiterLength = delimiter.length();

        // Add each token to the list
        while( index != -1 )
        {
            // Add the string that was found
            list.add( input.substring( lastIndexFound, index ) );

            // Advance the search position
            lastIndexFound = index + delimiterLength;

            // Find the next string
            index = tokenizer.indexOf( delimiter, lastIndexFound );
        }

        // Add the rest of the input to this buffer
        list.add( input.substring( lastIndexFound ) );

        // Return the list
        return list;
    }

    public static class StringFormatter extends IngestFormatter
    {
        public ArrayList<String> format( String input )
        {
            // Get rid of whitespace around the input
            String inputTrimmed = input.trim();

            // Only return a result if there was something in the input string
            if( inputTrimmed.length() > 0 )
                return listOf( input.trim() );
            else
                return emptyList();
        }
    }

    public static class Title extends IngestFormatter
    {
        /**
         * Formats the input using standard title conventions:
         * "the", "for", "with", "of", "and", "in":  lower-case
         * all other words have the first letter capitalized
         * one space between words
         */
        public ArrayList<String> format( String input )
        {
            // These words should be lower-case in a title
            String[] lowerCaseWords = { "the", "for", "with", "of", "and", "in" };

            // Create a buffer with which to split up the input
            StringBuffer tokenizer = new StringBuffer( input.trim().toLowerCase() );

            // Erase sets of spaces > 1
            int spaces = -1;
            while( (spaces = tokenizer.indexOf( "  " )) != -1 )
            {
                tokenizer.delete( spaces, spaces + 2 );
            }

            // Start at the beginning and capitalize words
            int search = -1;
            do
            {
                // Increment to the character one past the space
                search++;

                // Get the word here
                String word = tokenizer.substring( search );

                // Make sure this isn't a word that should be lower-case
                boolean capitalize = true;
                for( int i =0; i < lowerCaseWords.length; ++i )
                {
                    if( lowerCaseWords[i].equals( word + " " ) )
                    {
                        // Don't capitalize this item
                        capitalize = false;

                        // Exit the loop
                        break;
                    }
                }

                // If necessary, capitalize this character
                if( capitalize )
                    tokenizer.setCharAt( search, Character.toUpperCase( word.charAt(0) ) );


            } while( (search = tokenizer.indexOf( " ", search )) != -1 );

            // Return the result
            return listOf( tokenizer.toString() );
        }
    }

    /**
     * Breaks up a list of elements with a delimiter and formats each resulting element
     */
    public static class List extends IngestFormatter
    {
        /// Delimiter for the list
        String myDelimiter;

        /// Formatter invoked for each list element
        IngestFormatter myElementFormatter;

        /**
         * Sets up this list formatter
         * @param delimiter The token that should be used to break up the list
         * @param elementFormatter The formatter that is invoked for each list element
         */
        public List( String delimiter, IngestFormatter elementFormatter )
        {
            myDelimiter = delimiter;
            myElementFormatter = elementFormatter;
        }

        /**
         * Formats the incoming string input into a set of output.  This method
         * is specialized to break up the input by a delimiter and format each
         * of the resulting pieces with the provided formatter.
         * @input The source string
         * @return List of resulting strings
         */
        public ArrayList<String> format( String input )
        {
            // Get a list of elements
            ArrayList<String> list = tokenize( input, myDelimiter );

            // Format the list
            ArrayList<String> output = new ArrayList<String>();
            for( String element : list )
                output.addAll( myElementFormatter.format( element ) );

            // Return the formatted list
            return output;
        }
    }

    /**
     * Looks up the input field in a phrase dictionary
     */
    public static class Phrase extends IngestFormatter
    {
        /// The dictionary used to correct the phrase's spelling
        Dictionary myDictionary;

        /**
         * Initializes this class
         * @param dictionary The dictionary to use to look up the incoming string
         */
        public Phrase( Dictionary dictionary )
        {
            myDictionary = dictionary;
        }

        /**
         * Formats the input string by removing whitespace and looking up the resulting
         * entry in the dictionary of terms.
         */
        public ArrayList<String> format( String input )
        {
            // Find the closest element in the dictionary and return it
            SortedSet<Dictionary.Candidate> candidates =
                myDictionary.findClosestMatches( input.trim() );

            // If there are no candidates, return an empty list
            if( candidates.isEmpty() )
                return new ArrayList<String>();
            else
                return listOf( candidates.first().getTerm() );
        }
    }

    /**
     * Looks up the input field in a dictionary map
     */
    public static class MappedPhrase extends IngestFormatter
    {
        /// The dictionary used to turn one phrase into another
        DictionaryMap<String> myDictionaryMap;

        /**
         * Initializes this class
         * @param dictionaryMap The dictionary map to use to look up the incoming string
         */
        public MappedPhrase( DictionaryMap<String> dictionaryMap )
        {
            myDictionaryMap = dictionaryMap;
        }

        /**
         * Formats the input string by removing whitespace and looking up the resulting
         * entry in the dictionary of terms.
         */
        public ArrayList<String> format( String input )
        {
            // Find the closest element in the dictionary and return it
            SortedSet<DictionaryMap<String>.Candidate> candidates =
                myDictionaryMap.findClosestMatches( input.trim() );

            // If there are no candidates, return an empty list
            if( candidates.isEmpty() )
                return new ArrayList<String>();
            else
                return listOf( candidates.first().getTerm() );
        }
    }
}

