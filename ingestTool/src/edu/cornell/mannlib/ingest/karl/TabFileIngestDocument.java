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

import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.ArrayList;

import edu.cornell.mannlib.ingest.karl.IngestDocument;
import edu.cornell.mannlib.ingest.karl.IngestEntityProcessor;


/**
 * This type of document generates the "row" tag on each row, and provides members
 * as defined by the heading on the file
 */
public abstract class TabFileIngestDocument extends IngestDocument
{
    private StreamTokenizer myStream;
    private ArrayList<String> myHeadings;
    private String mySection;
    private Iterator<String> mySectionIterator;
    private String myField;

    /**
     * Passive constructor
     */
    public TabFileIngestDocument( IngestEntityProcessor processor )
    {
        super( processor );
    }

    /**
     * Sets up this document
     * @data The source information to read
     */
    public void open( InputStream data ) throws Exception
    {
        // Sorry.  It's deperecated.  Can't do much about it.
        myStream = new StreamTokenizer( data );

        // Initialize the tokenizer
        myStream.resetSyntax();
        myStream.eolIsSignificant( true );
        myStream.lowerCaseMode( false );
        myStream.slashSlashComments( true );
        myStream.slashStarComments( true );
        myStream.wordChars( 0, 255 ); // everything thats not a tab is a word
        myStream.ordinaryChar( '\t' );
        myStream.ordinaryChar( StreamTokenizer.TT_EOL );

        // Read the headings from the stream
        myStream.nextToken();
        myHeadings = new ArrayList<String>();
        while( myStream.ttype != StreamTokenizer.TT_EOL )
        {
            if( myStream.ttype == StreamTokenizer.TT_WORD )
                if( !myStream.sval.equals( "\t" ) )
                    myHeadings.add( myStream.sval.trim() );

            myStream.nextToken();
        }
    }

    /**
     * Builds a string with the calls to ParseField for of ParseField actions for the specified set
     * @param actionsVariable The name of the actions variable to create
     * @param entityVariable The name of the entity variable to create
     * @param entityName The parameter to provide to IngestEntity's constructor
     * @param formatter The formatter variable/declaration to stick into the last part of ParseField
     */
    public String buildParseFieldActions( String actionsVariable, String entityVariable, String entityName, String formatter )
    {
        // Build the actions
        String actions = "HashMap<String,IngestAction> " + actionsVariable + " = new HashMap<String,IngestAction>();\n" +
                         "IngestEntity " + entityVariable + " = new IngestEntity( \"" + entityName + "\");\n\n";

        // Prints all of the headings formatted as ParseField actions to the debug window
        for( Iterator<String> j = myHeadings.iterator(); j.hasNext(); )
        {
            String heading = j.next();
            actions += actionsVariable + ".put( \"" + heading + "\", new ParseField( \"" + heading + "\", " + entityVariable +", " + formatter + " ) );\n";
        }

        // Return the string we just built
        return actions;
    }

    /**
     *
     */
    @Override
    public void endSection( String tag ) throws Exception
    {
        // Check to see whether or not the tag is valid
        if( tag == null )
        {
            // Make sure we are at the end of a row or the end of the file
            if( !(myHeadings == null) && (mySectionIterator == null || mySectionIterator.hasNext()) )
                throw new IllegalStateException( "Can't leave section without finishing data (yet...fix this)" );
        }
        else
        {
            if( !tag.equals( mySection ) )
                throw new IllegalStateException( "Tag doesn't match" );
        }
    }

    /**
     *
     */
    @Override
    public String getNextTag(String tagToClose) throws Exception
    {
        // Check to make sure we actually have a document
        if( myStream == null )
            throw new IllegalStateException( "Document not open" );

        // Check to see whether or not we are at the end of the document
        if( myHeadings == null )
            return null;

        // Check to see what to do with the tag
        if( tagToClose == null )
        {
            // Check to see what is happening
            if( mySectionIterator == null )
            {
                // Start this section at the beginning of the header list
                mySectionIterator = myHeadings.iterator();

                // We are in a "row" tag
                mySection = "row";
            }
            else
            {
                // We're going to start iterating through this row
                scanSection();
            }
        }
        else
        {
            if( tagToClose.equals( "row" ) )
            {
                // Make sure this row didn't end prematurely
                if( mySectionIterator == null || mySectionIterator.hasNext() )
                    throw new IllegalStateException( "Invalid endSection( \"row\" )" );

                // Get rid of the row so that a new one can be started
                mySectionIterator = null;

                // We're in another row
                return getNextTag( null );
            }
            else
            {
                // End this section
                endSection( tagToClose );

                // Get the next section
                scanSection();
            }
        }

        // Return the current section
        return mySection;
    }

    @Override
    public String readField() throws Exception
    {
        return myField;
    }

    private void scanSection() throws Exception
    {
        if( null == mySectionIterator )
            return;

        // Advance to the next section
        if( mySectionIterator.hasNext() )
            mySection = mySectionIterator.next();
        else
            mySection = null;

        // Read the field
        myField = scanField();
    }

    // this is a separate method because it can be recursive to help sort out misplaced EOLs
    private String scanField() throws Exception
    {
        boolean availableSection = mySectionIterator.hasNext();
        boolean availableData = myStream.ttype == StreamTokenizer.TT_EOL;
        boolean sectionEnded = mySection == null;
        //if( !availableData &&  availableSection ) // problem
        //if(  availableData && !availableSection ) // problem
        if( !sectionEnded && (!availableSection && !availableData) )
        {
            mySection = null;
            return "";
        }

        // Get the next token from the stream
        myStream.nextToken();

        // Create the return value string (just in case)
        String retval = new String();

        // Do something with the token
        if( myStream.ttype == StreamTokenizer.TT_WORD )
        {
            // This field contained information
            retval = myStream.sval;

            // Read the next member--it should be a \t for more data, or EOL
            // if we're done reading
            myStream.nextToken();

            // Check the type
            if( myStream.ttype == StreamTokenizer.TT_EOL )
            {
                // Make sure we are at the end of the iteration
                if( mySectionIterator.hasNext() )
                {
                    // Sometimes they put EOL markers in junk text for NO apparent reason!
                    // Read more data from the file...quick n' dirty fix (cant handle multiple EOLs in a row)
                    retval += scanField();
                    //throw new IllegalStateException( "Field data ended before header finished" );
                }
            }
            else if( myStream.ttype == StreamTokenizer.TT_EOF )
            {
                // End the file
                eof();
            }
            else if( myStream.ttype != '\t' )
                throw new IllegalStateException( "Field was followed by data other than a delimiter" );
        }
        else if( myStream.ttype == '\t' )
        {
            // This is a delimiter where a field is supposed to be, so put it back
            //myStream.pushBack();

            // This field is empty
            retval = "";
        }
        else if( myStream.ttype == StreamTokenizer.TT_EOL )
        {
            mySection = null;
        }
        else if( myStream.ttype == StreamTokenizer.TT_EOF )
        {
            // End the file
            eof();
        }
        else
            // We don't know what to do with this input
            throw new IllegalStateException( "Illegal field" );

        // Return the return value
        return retval;
    }

    void eof()
    {
        // Exit iteration
        mySectionIterator = null;
        myHeadings = null;
        mySection = null;
    }
}
