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

import java.util.TreeMap;
import java.util.ArrayList;

/**
 * Represents an entity with data read from the ingest file
 */
public class IngestEntity
{
    /// This entity's generic class name
    protected String myType;

    /// This entity's properties
    protected TreeMap<String,ArrayList<String>> myProperties;

    /**
     * Generates this entity with the given type specifier
     */
    public IngestEntity( String type )
    {
        myType = type;
        myProperties = new TreeMap<String,ArrayList<String>>();
    }

    /**
     * Obtains this entity's type
     */
    public String getType()
    {
        return myType;
    }

    /**
     * Gives this entity a new property value
     * @param key The key value for the property
     * @param value The new value to add
     */
    public void addProperty( String key, String value )
    {
        // Obtain the current value for this entry
        ArrayList<String> currentValues = myProperties.get( key );

        // Make sure the list exists
        if( currentValues == null )
        {
            // Create a list
            currentValues = new ArrayList<String>();

            // Put it into the map
            myProperties.put( key, currentValues );
        }

        // Add this value to the current list
        currentValues.add( value );
    }

    /**
     * Gives this entity a list of new property values
     * @param key The key value for the property
     * @param value The new value to add
     */
    public void addProperty( String key, ArrayList<String> values )
    {
        // Obtain the current value for this entry
        ArrayList<String> currentValues = myProperties.get( key );

        // Make sure the list exists
        if( currentValues == null )
        {
            // Create a list
            currentValues = new ArrayList<String>();

            // Put it into the map
            myProperties.put( key, currentValues );
        }

        // Add this value to the current list
        currentValues.addAll( values );
    }


    /**
     * Obtains the property values associated with the given key
     */
    public ArrayList<String> getProperty( String key )
    {
        return myProperties.get( key );
    }

    /**
     * Determines whether or not this entity has properties
     */
    public boolean hasProperties()
    {
        return !myProperties.isEmpty();
    }

    /**
     * Converts this entity into a string
     */
    public String toString()
    {
        return "IngestEntity(" + myType + ") = {\n" + myProperties.toString() + "\n}";
    }
}
