package edu.cornell.mannlib.ingest.kwinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implements a VivoIngestDocument for any kind of delimited-value flattened database
 * file.  This class assumes that each value in the database is on its own row, separated
 * by a certain character sequence.  The first row in the database is the title of each
 * column at the corresponding location in the rows that follow.
 * @author Karl Gluck
 */
public abstract class DelimitedIngestDocument extends VivoIngestDocument
{
	/// The reader used by this class
	private BufferedReader myDataSource;

	/// The delimiter by which to split up the input records
	private String myDelimiter; 

	/// The column-labels in this document
	private String myColumnLabels[];
	
	/// The last record that was obtained from the document
	private String myLastRecord[];

	/// Maps a column name to its index.  This is used to speed up name lookups.
	protected Map<String,Integer> myColumnLabelIndices;
	
	/**
	 * Opens the ingest document using the given source reader and delimiting string
	 * @param reader The source of the document
	 * @param delimiter The string that breaks up columns in this document
	 */
	public DelimitedIngestDocument( Reader reader, String delimiter )
	{
		// Initialize the data source
		myDataSource = new BufferedReader( reader );
		
		// Save the delimiter
		myDelimiter = delimiter;
		
		// Initialize other information
		myColumnLabels = null;
		myLastRecord = null;
		myColumnLabelIndices = new TreeMap<String,Integer>();
	}

	/**
	 * Removes allocated data used by the document and breaks connections with data sources
	 */
	@Override
	public void close()
	{
		try {
			myDataSource.close();
		} catch( Exception e ) {
			// Don't really need to do anything here; we're closing, after all 
		}
		finally
		{
			myDataSource = null;
			myDelimiter = null;
			myColumnLabels = null;
			myLastRecord = null;
		}
	}
	
	/**
	 * Returns the list of column labels used in this document
	 * @return Column labels
	 */
	protected String[] getColumnLabels()
	{
		return myColumnLabels;
	}

	/**
	 * Obtains the record that was most recently read using readRecord()
	 * @return The most recent record
	 */
	protected String[] getLastRecord()
	{
		return myLastRecord;
	}

	/**
	 * Obtains a new record from the data source
	 * @return Whether or not a record was found
	 * @throws IOException
	 */
	protected boolean readRecord() throws IOException
	{
		// Read the current line from the data source
		String line = myDataSource.readLine();
		
		// If the line is null, we're out of data
		if( line == null ) return false;
		
		// If the line is empty, read another record
		if( line.isEmpty() ) return readRecord();

		// Split this line by the delimiter
		String entries[] = line.split( "\\Q" + myDelimiter + "\\E" );

		// If the column labels don't exist yet, assign them
		if( myColumnLabels == null )
		{
			// Save the column labels
			myColumnLabels = entries;
			
			// Read again so that the current record is filled with data
			return readRecord();
		}

		// This statement handles the case that there is a carriage return
		// or line feed inside of an entry in the document, which is
		// sometimes the case with malformed database exports.
		while( entries.length < myColumnLabels.length )
		{
			// Read another line
			line = myDataSource.readLine();
			
			// If this line is invalid, don't bother
			if( line == null || line.isEmpty() ) break;

			// Split this line
			String appendEntries[] = line.split( "\\Q" + myDelimiter + "\\E" );
			
			// The first entry of the new line and and last entry of the
			// last line should be concatenated, because they are actually
			// from the same delimited entry--they were just split by a CR/LF
			entries[entries.length-1] += appendEntries[0];
			
			// Create a new array to hold both lines
			String newEntries[] = new String[entries.length - 1 + appendEntries.length];

			// Copy the first array of entries
			for( int i = 0; i < entries.length; ++i )
				newEntries[i] = entries[i];
			
			// Copy the second array of entries starting at the second
			// string available in the appending line
			for( int i = 1; i < appendEntries.length; ++i )
				newEntries[(i-1) + entries.length] = appendEntries[i];
			
			// TODO: remove this debug output
			System.out.println( "[INFO] Fixed broken record:  " + entries.length + "->" + newEntries.length );

			// Assign this new line as the entries line
			entries = newEntries;
		}

		// Make sure there are an equal number of entries in this
		// record as in the column labels
		if( entries.length != myColumnLabels.length )
		{
			// Erase the current record
			myLastRecord = null;
			
			// Let the calling method know about this problem
			throw new IOException( "Invalid record found:  {" + entries + "}" );
		}
		
		// Save this record
		myLastRecord = entries;
		
		// Success
		return true;
	}

	/**
	 * Uses a fast algorithm to find the index of the column of data with the
	 * label passed as a parameter.  The first lookup will be in O(n) time,
	 * but subsequent lookups will be in better than O(log x) time, where x
	 * is the number of other columns accessed so far.
	 * @param columnLabel The name of the column to find
	 * @return The column's index, or -1.
	 */
	public int getColumnIndex( String columnLabel )
	{
		// First, try to find the record index in the map
		Integer index = myColumnLabelIndices.get(columnLabel);
		if( index != null ) return index.intValue();
		
		// Get this list of all of the column labels
		String columnLabels[] = getColumnLabels();
		if( columnLabels == null ) return -1;
		
		// Search the column labels for this name
		for( int i = 0; i < columnLabels.length; ++i )
			if( columnLabels[i].equals( columnLabel ) )
			{
				// Add this index to the map
				myColumnLabelIndices.put( columnLabel, new Integer(i) );
				
				// Return the value we found
				return i;
			}
		
		// Doesn't exist
		return -1;
	}

	/**
	 * Gets the value entered at a particular column in the last
	 * record that was read from the database. 
	 * @param columnLabel The label of the last column
	 * @return
	 */
	public String getLastRecordValue( String columnLabel )
	{
		int index = getColumnIndex( columnLabel );
		if( index < 0 ) return null;
		else
			return getLastRecord()[index];
	}
}
