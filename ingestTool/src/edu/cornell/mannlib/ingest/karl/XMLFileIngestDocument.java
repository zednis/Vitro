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

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;

import java.util.ArrayDeque;
import java.util.Iterator;

import java.lang.IllegalStateException;


public abstract class XMLFileIngestDocument extends IngestDocument
{
    private Document myDocument;

    /**
     * Encapsulates parsing "slowly" through the XML document (it allows multiple
     * accesses to the same element, which a regular Iterator does not)
     */
    private class XMLElementIterator
    {
        /// Iterator that references the current location in the list
        private Iterator<Element> myIterator;

        /// The element currently referenced by myIterator
        private Element myElement;

        /**
         * Sets up this XML element iterator for the list of items referenced
         * @param iterator Iterator reference into list to examine
         */
        public XMLElementIterator( Iterator<Element> iterator )
        {
            /// Save the iterator
            myIterator = iterator;

            /// Obtain the first element
            myElement = iterator.next();
        }

        /**
         * Obtains the iterator for the children of this object
         */
        public XMLElementIterator getChild()
        {
            // Don't allow an invalid element
            if( myElement == null )
                throw new IllegalStateException( "Iterator without an element cannot provide a child" );

            // Return the child iterator
            return new XMLElementIterator( myElement.elementIterator() );
        }

        /**
         * Obtains the name of the current XML element
         */
        public String getName()
        {
            // Make sure the element exists
            if( myElement == null ) return null;

            // Return the element's name
            return myElement.getName();
        }

        /**
         * Determines whether this element's name is the same as the one provided
         * @param name The name with which to compare
         * @return Whether or not the name and this element's name are equal
         */
        public boolean nameEquals( String name )
        {
            // Nulls match
            if( myElement == null ) return name == null;

            // Compare strings
            return getName().equals( name );
        }

        /**
         * Moves to the next element in the list
         * @return The next tag
         */
        public String advance()
        {
            if( myIterator.hasNext() )
            {
                // Obtain the next member in the iteration
                myElement = myIterator.next();

                // Return the current tag name
                return myElement.getName();
            }
            else
            {
                // Invalidate this element iterator
                myElement = null;

                // No name
                return null;
            }
        }

        /**
         * Reads the field of the XML element currently being referenced
         */
        public String getField()
        {
            // Make sure the element exists
            if( myElement == null )
                throw new IllegalStateException( "Iterator without an element cannot provide a field" );

            // Return the element's text
            return myElement.getText();
        }
    }

    /// The current set of iterators
    private ArrayDeque<XMLElementIterator> myParseStack;

    /**
     * Passive constructor
     */
    public XMLFileIngestDocument( IngestEntityProcessor processor )
    {
        super( processor );
    }

    /**
     * Opens the target input stream for parsing
     * @param data The source of the data
     */
    public void open( InputStream data ) throws Exception
    {
        // Construct a reader for the XML document
        SAXReader sax = new SAXReader();

        // Obtain the document object by parsing the file
        myDocument = sax.read( data );

        // Generate the stack
        myParseStack = new ArrayDeque<XMLElementIterator>();
    }

    /**
     * Obtains the next tag in the XML document.
     * @param tagToClose If this is null, the document will parse the current
     *                   element's children.  If this is a valid string, the
     *                   document will move up the the stack until the tag is
     *                   found, and move to the element after that tag.
     *                   This is useful to skip errors during parsing that
     *                   throw exceptions up to higher actions, which shouldn't
     *                   need to know about the lower-level components.
     */
    public String getNextTag( String tagToClose ) throws Exception
    {
        // Check to make sure we actually have a document
        if( myDocument == null )
            throw new IllegalStateException( "Document not open" );

        // Check to see which action is being requested
        if( tagToClose == null )
        {
            // If the stack is empty, add something
            if( myParseStack.isEmpty() )
            {
                // Grab a copy of the element iterator from the root of the document
                Iterator<Element> elementIterator =
                    (Iterator<Element>)(myDocument.getRootElement().elementIterator());

                // Add this element to the stack
                myParseStack.addLast( new XMLElementIterator( elementIterator ) );
            }
            else
            {
                // Go down in the element stack
                myParseStack.addLast( getCurrentIterator().getChild() );
            }
        }
        else
        {
            // Close this section
            endSection( tagToClose );
        }

        // Return the tag for the new section
        return getCurrentIterator().getName();
    }

    /**
     * Obtains the current XML element iterator
     * @return The deepest element iterator in the stack
     */
    private XMLElementIterator getCurrentIterator() throws IllegalStateException
    {
        // If the stack is empty, we can't do anything
        if( myParseStack == null || myParseStack.isEmpty() )
            throw new IllegalStateException( "Empty document has no iterator" );

        // Return the last element in the stack
        return myParseStack.getLast();
    }

    /**
     * Leaves the designated XML section
     */
    public void endSection( String tag )
    {
        if( tag == null )
        {
            // Exit this section
            myParseStack.removeLast();
        }
        else
        {
            try
            {
                // Obtain the iterator with the tag we should close
                while( false == getCurrentIterator().nameEquals( tag ) )
                {
                    // Erase the last element in the stack
                    myParseStack.removeLast();
                }
            }
            catch( IllegalStateException illegalState )
            {
                // The parse stack was emptied by this list; a bad tag was provided
                throw new IllegalStateException( "Section name \"" + tag + "\" not found.  XML document may be malformed." );
            }

            // Advance to the next element
            getCurrentIterator().advance();
        }
    }

    /**
     * Reads the current field from the document
     */
    public String readField() throws Exception
    {
        return getCurrentIterator().getField();
    }
}
