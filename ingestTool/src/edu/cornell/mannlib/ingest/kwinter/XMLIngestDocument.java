package edu.cornell.mannlib.ingest.kwinter;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;

/**
 * This class implements a VivoIngestDocument that takes an XML file as input and
 * allows the subclass to access records within it. 
 * @author Karl Gluck
 */
public abstract class XMLIngestDocument extends VivoIngestDocument
{
	/// The document object used to read entries from the XML file
	private Document myDocument;

	/**
	 * Initializes the ingest document to read from a given source
	 * @param reader The data source for the XML document
	 */
	public XMLIngestDocument( Reader reader ) throws DocumentException
	{
        // Construct a reader for the XML document
        SAXReader sax = new SAXReader();

        // Obtain the document object by parsing the source data
        myDocument = sax.read( reader );
	}

	/**
	 * Initializes the ingest document to read from the provided stream
	 * @param inputStream The data stream for the XML document
	 */
	public XMLIngestDocument( InputStream inputStream ) throws DocumentException
	{
        // Construct a reader for the XML document
        SAXReader sax = new SAXReader();

        // Obtain the document object by parsing the source stream
        myDocument = sax.read( inputStream );
	}

	/**
	 * Removes allocated data used by the document and breaks connections with data sources
	 */
	@Override
	public void close()
	{
		myDocument = null;
	}

	/**
	 * Gets the XML document reader from which to obtain data
	 * @return
	 */
	protected Document getXMLDocument()
	{
		return myDocument;
	}

	/**
	 * Creates an entity whose children and values mirror the hierarchy of values
	 * contained within this element in the XML document structure
	 * @param element The element for which to create the entity
	 * @return New entity filled with data pulled from the document
	 */
	protected EntityImpl createEntity( Element element )
	{
		// Create the entity implementation
		EntityImpl entity = new EntityImpl( element.getName() );
		
		// Generate the values and children of this entity
		createEntityHierarchy( element, entity );
		
		// Return the entity we created
		return entity;
	}

	/**
	 * Recursive helper method used by createEntity to parse the document structure
	 * @param element The element for which to fill the entity provided with data
	 * @param entity Empty container entity
	 */
	private void createEntityHierarchy( Element element, EntityImpl entity )
	{
		// Get an iterator for the child elements within this element
		Iterator<Element> elementIterator = element.elementIterator();
		
		// Add all of the elements with string values to the iterator
		while( elementIterator.hasNext() )
		{
			// Get the element here
			Element subelement = elementIterator.next();
			
			// If this element has no children, just add it as a value;
			// otherwise, add this element as a new node in the
			// hierarchy, and add data to that new node
			if( subelement.elements().isEmpty() )
				entity.setValue( subelement.getName(), subelement.getText() );
			else
				createEntityHierarchy( subelement, entity.addChild( subelement.getName() ) );
		}
	}
}
