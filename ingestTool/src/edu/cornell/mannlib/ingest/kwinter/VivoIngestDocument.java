package edu.cornell.mannlib.ingest.kwinter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A VivoIngestDocument is implemented in various ways for the parsing of an XML file,
 * tab-delimited file or even a website.  They all implement this common interface,
 * however, to make it easy to change data sources without having to adjust the
 * code that manipulates the data.  This makes it simple to create new code that reads
 * different kinds of data, and prevents the programmer from having to learn different
 * interfaces every time.
 * @author Karl Gluck
 */
public abstract class VivoIngestDocument
{
	/**
	 * Obtains an entity with data from the document.  The format of the entity and what it
	 * contains are entirely the responsibility of the subclass that is used to implement
	 * this document interface.
	 * @return The next entity in the document, or 'null' if the document has no more
	 *         available data
	 */
	public abstract Entity next();
	
	/**
	 * Removes allocated data used by the document and breaks connections with data sources
	 */
	public abstract void close();

	/**
	 * An entity is a chunk of stand-alone data that was obtained from the ingest document.  This
	 * is a hierarchical structure with an unlimited number of sub-elements; so in theory, the
	 * document could return its entire contents as a single top-level Entity.  However, this is
	 * not usually the most efficient way to manipulate data, and thus most ingest documents
	 * will choose to return small pieces of data that relate as directly as possible to entries
	 * that need to be created in the database.
	 * @author Karl
	 */
	public abstract class Entity
	{
		/**
		 * Gets the name of this entity
		 * @return Entity name
		 */
		public abstract String getName();
		
		/**
		 * Gets the string value associated with the specified key in this entity
		 * @param name The name of the value to obtain
		 * @return The value associated with 'name' or null
		 */
		public abstract String getValue( String name );
		
		/**
		 * Gets a child of this entity by its name
		 * @param name The name of the child entity to obtain
		 * @return The child entity, or null
		 */
		public abstract Entity getChild( String name );
		
		/**
		 * Obtains an iterator that can be used to move through the child entities.  This
		 * is useful if the entity has a variable number of members.
		 * @return Child iterator
		 */
		public abstract Iterator<Entity> getChildren();

		/**
		 * Gets a string value associated with a key in this entity, and parses it
		 * into a list by breaking it up whenever the delimiter is found.
		 * @param name The name of the value to obtain
		 * @param delimiter The delimiter at which to break up the returned value
		 * @return A list of broken parts of the value, or an empty list
		 * @note It is always valid to use the returned list (null will not be returned) 
		 */
		public List<String> getValueList( String name, String delimiter )
		{
			// Get the value associated with this name
			String value = getValue( name );
			
			// If the valid is invalid, return an empty list
			if( null == value )
				return new ArrayList<String>();

	        // Create a buffer with which to tokenize the input
	        StringBuffer tokenizer = new StringBuffer( value );

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
	            list.add( value.substring( lastIndexFound, index ) );

	            // Advance the search position
	            lastIndexFound = index + delimiterLength;

	            // Find the next string
	            index = tokenizer.indexOf( delimiter, lastIndexFound );
	        }

	        // Add the rest of the input to this buffer
	        String remainder = value.substring( lastIndexFound );
	        if( !remainder.isEmpty() )
	        	list.add( remainder );

	        // Return the list
	        return list;
		}
	}


	/**
	 * This class can be used by derived Document types to implement
	 * the abstract methods of Entity.
	 * @author Karl
	 */
	protected class EntityImpl extends Entity
	{
		/// The name of this entity
		protected String myName;
		
		/// The values this entity holds
		protected Map<String,String> myValues;
		
		/// The children of this entity
		protected Map<String,Entity> myChildren;

		/**
		 * Constructs the entity implementation
		 * @param name The name of this entity
		 */
		public EntityImpl( String name )
		{
			myName = name;
			myValues = new TreeMap<String,String>();
			myChildren = new TreeMap<String,Entity>();
		}

		/**
		 * Gets the name of this entity
		 * @return Entity name
		 */
		@Override
		public String getName()
		{
			return myName;
		}

		/**
		 * Gets the string value associated with the specified key in this entity
		 * @param name The name of the value to obtain
		 * @return The value associated with 'name' or null
		 */
		@Override
		public String getValue( String name )
		{
			return myValues.get( name );
		}

		/**
		 * Sets a string value associated with a name.  If the name already exists,
		 * this method overwrites it.
		 * @param name The key to write to
		 * @param value The value to assign
		 */
		public void setValue( String name, String value )
		{
			myValues.put( name, value );
		}

		/**
		 * Takes the result of the toString method on each of each entry in a list and
		 * build a delimited string value to assign to 'name'.  The list's string values
		 * can be recovered by using getValueList( name, delimiter ). 
		 * An exception is thrown if any list entry contains the delimiter
		 * @param name The key to write to
		 * @param list The list of values to assign to the key
		 * @param delimiter The delimiter to use between the values (this shouldn't
		 * 					occur anywhere in the list entries).
		 * @throws Exception
		 */
		public void setValueList( String name, List list, String delimiter ) throws Exception
		{
			setValue( name, "" );
			appendValueList( name, list, delimiter );
		}

		/**
		 * Takes the result of the toString method on each entry in the list and builds a
		 * delimited string value.  This value is then appended to the current value of
		 * the entry in 'name' followed by a delimiter.
		 * An exception is thrown if any new list entry contains the delimiter
		 * @param name The key to write to
		 * @param list The list of values to assign to the key
		 * @param delimiter The delimiter to use between the values (this shouldn't
		 * 					occur anywhere in the list entries).
		 * @throws Exception
		 */
		public void appendValueList( String name, List list, String delimiter ) throws Exception
		{
			// Temporarily holds the value we're constructing
			String value = getValue( name );

			// If the list is 'null', delete this value
			if( list == null )
			{
				// Erase the key, if it exists
				if( value != null ) myValues.remove( name );
				
				// Nothing more to do
				return;
			}

			// If the value is invalid, use an empty string; otherwise, append the delimiter
			if( value == null || value.isEmpty() )
				value = "";
			else
				value += delimiter;
			
			// Build the value to assign
			for( Object entry : list )
			{
				// Add the delimiter unless we're at the start of the list
				if( entry != list.get(0) )
					value += delimiter;
				
				// Get the string value of this entry
				String entryString = entry.toString();
				
				// Do a check to make sure the delimiter doesn't exist in the string
				if( entryString.contains( delimiter ) )
					throw new Exception( "Delimiter occurred within list entry" );

				// Add this entry to the string
				value += entry.toString();
			}
			
			// Set this value
			setValue( name, value );
		}

		/**
		 * Gets a child of this entity by its name
		 * @param name The name of the child entity to obtain
		 * @return The child entity, or null
		 */
		@Override
		public Entity getChild( String name )
		{
			return myChildren.get( name );
		}

		/**
		 * Adds a new child to this entity with the given name, and returns the empty
		 * created interface so that it can be filled with data.  This will overwrite
		 * any child with the given name.
		 * @param name The name of the entity to add
		 * @return The created entity
		 */
		public EntityImpl addChild( String name )
		{
			EntityImpl entity = new EntityImpl( name );
			myChildren.put( name, entity );
			return entity;
		}

		/**
		 * Obtains an iterator that can be used to move through the child entities.  This
		 * is useful if the entity has a variable number of members.
		 * @return Child iterator
		 */
		@Override
		public Iterator<Entity> getChildren()
		{
			return myChildren.values().iterator();
		}
		
		/**
		 * Shows the contents of this entity
		 */
		@Override
		public String toString()
		{
			return myValues.toString();
		}
	}

}

/*
VivoIngestDocument
	VivoTabIngestDocument
		- Fall07Enrollment
	VivoXMLIngestDocument
		- 
	VivoWebIngestDocument
		- CourseCatalogFromWeb
*/