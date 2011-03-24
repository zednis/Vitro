package edu.cornell.mannlib.ingest.kwinter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.reasoner.IllegalParameterException;

import edu.cornell.mannlib.ingest.kwinter.VivoIngestDocument.Entity;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;


/**
 * This class is used to map class instances to instructors in VIVO.  The instructor NetIDs are in
 * a separate file from the main class enrollment database.  This class is designed to be run after
 * all of the classes for FA07 are in the VIVO database.
 * order.
 * 
 * @author Karl Gluck
 */
public final class IngestFA07InstructorNetIDs
{

	// Default FA07 instructor netID database file to ingest
	public static final String DEFAULT_DATABASE_FILE = "C:\\Documents and Settings\\Karl\\My Documents\\Work - Vivo\\FA07 instructor netid.txt";

	/// Default location for the connection properties
	public static final String DEFAULT_CONNECTION_PROPERTIES_FILE = "C:\\Documents and Settings\\Karl\\My Documents\\Work - Vivo\\code\\webapp\\config\\connection.properties";

	/// VALUEKEYs are used to store data in the Entity structures that are pulled back from the ingest documents
	public static final String VALUEKEY_INSTRUCTORNETID = "Instructor Net ID";

	/// Links established with SemesterClass and the various ClassMeeting individuals in VIVO

	public static final String URI_RELATIONSHIP_SEMESTERCLASS_ACADEMICEMPLOYEE = "http://vivo.library.cornell.edu/ns/0.1#taughtByAcademicEmployee"; 
	public static final String URI_RELATIONSHIP_ACADEMICEMPLOYEE_SEMESTERCLASS = "http://vivo.library.cornell.edu/ns/0.1#teacherOfSemesterClass";
	public static final String URI_RELATIONSHIP_CLASSMEETING_INSTRUCTOR = "http://vivo.library.cornell.edu/ns/0.1#taughtByAcademicEmployee"; 
	public static final String URI_RELATIONSHIP_INSTRUCTOR_CLASSMEETING = "http://vivo.library.cornell.edu/ns/0.1#teacherOfSemesterClass";

	/// This is the instructor's net ID
	public static final String URI_DPROP_NETID = "http://vivo.library.cornell.edu/ns/0.1#CornellemailnetId";
	
	/// This is the VClass of the instructor type
	public static final String URI_VCLASS_CLASS_INSTRUCTOR = "http://www.aktors.org/ontology/portal#AcademicEmployee";

	/**
	 * @param args
	 */
	public static void main( String[] args ) throws Exception
	{
		// Get the source file by either using the default, or whatever the user provided in the arguments
		String sourceFile = args.length >= 1 ? args[0] : DEFAULT_DATABASE_FILE;
		String connectionPropertiesFile = args.length >= 2 ? args[1] : DEFAULT_CONNECTION_PROPERTIES_FILE;
		
		// Create the database connection
		VivoDatabaseConnection connection = new VivoDatabaseConnection( connectionPropertiesFile );
		IndividualDao individualDao = connection.getDaoFactory().getIndividualDao();

		// Load the ingest document
		IngestDocument document = null;
		try {
			document = new IngestDocument( sourceFile );
		} catch( Exception e )
		{
			// Print a warning message
			System.out.println( "Unable to open \"" + sourceFile + "\" to read the instructor/class database (or there was an error doing so)" );

			// Exit
			return;
		}
		
		// Information collected during the ingest process
		int recordsRead = 0;

		// Read entities from the document
		VivoIngestDocument.Entity entity = null;
		while( null != (entity = document.next()) )
		{
			// Find the class in the database and set the instructor for it
			try
			{
				// TODO: some are 'aaatemp' ... temp instructor?
				// TODO: for class meetings w/o instructor, should we add dataproperty with instructor net id?
				String netID = entity.getValue( VALUEKEY_INSTRUCTORNETID ).replaceAll( "\\@cornell\\.edu", "" ).trim();

				// Find the class entry listed in the database that is in the correct semester
				List<VivoDatabaseConnection.PropertyValue> propertyValues = new ArrayList<VivoDatabaseConnection.PropertyValue>();
				propertyValues.add( new VivoDatabaseConnection.DataPropertyValue( IngestFA07ClassDatabase.URI_DPROP_CLASSMEETING_COURSEID, entity.getValue( IngestFA07ClassDatabase.VALUEKEY_COURSEID  ) ) );
				propertyValues.add( new VivoDatabaseConnection.ObjectPropertyValue( IngestFA07ClassDatabase.URI_RELATIONSHIP_CLASSMEETING_ACADEMICSEMESTER, IngestFA07ClassDatabase.URI_ENTITY_ACADEMICSEMESTER_FALL2007 ) );
				Individual vivoClassMeeting = connection.getUniqueIndividualByPropertyValues( propertyValues );

				// Get the instructor by his/her net ID
				Individual vivoInstructor = connection.getUniqueIndividualByDataPropertyValue( URI_DPROP_NETID, netID );

				// Try obtaining the net ID with "@cornell.edu"
				if( vivoInstructor == null )
					vivoInstructor = connection.getUniqueIndividualByDataPropertyValue( URI_DPROP_NETID, netID + "@cornell.edu" );

				// If the instructor doesn't exist, look up data about this person and create an entity
				if( vivoInstructor == null )
				{
					String ldapResult = LdapNetidLookup.lookupByNedit( netID );
					String[] ldapFields = ldapResult.split("\t");
					
					// Get the field data
					String name = ldapFields[1];
					String email = ldapFields[2];
					String position = ldapFields[4];

					System.out.println( "Creating academic employee:  " + netID + " (" + name + ")" );
					
					// Create an entity for this individual
					vivoInstructor = new IndividualImpl();
					vivoInstructor.setName( name );
					vivoInstructor.setURI( "http://vivo.library.cornell.edu/ns/0.1#" + "academicEmployee_" + netID );
					vivoInstructor.setVClassURI( URI_VCLASS_CLASS_INSTRUCTOR );
					vivoInstructor.setDescription( position );
					vivoInstructor.setAnchor( name + "'s  Profile" );
					vivoInstructor.setUrl( "http://www.cornell.edu/search/index.cfm?tab=people&netid=" + netID );

					// 
					vivoInstructor.setFlag1Numeric( 1 );
					vivoInstructor.setFlag1Set( "1" );

					// Create this individual in Vivo
	                try{
	                	vivoInstructor = individualDao.getIndividualByURI( individualDao.insertNewIndividual( vivoInstructor ) );
	                }catch(Exception ex){
	                    System.out.println("could not make individual for instructor: " + vivoInstructor.getName() + " uri: " + vivoInstructor.getURI() +
	                    "\n" + ex.getMessage());
	                }
	                
	                // Add the email as a data property
	                connection.addDataProperty( vivoInstructor.getURI(), URI_DPROP_NETID, email );
	                
	                // Update the individual
	                individualDao.updateIndividual( vivoInstructor );
				}

				// If we have all the data we need, map the instructor to the class
				if( vivoClassMeeting != null && vivoInstructor != null )
				{
					// TODO: Make sure this is in the right semester
					//System.out.println( vivoClassMeeting.getName() );

					// Link this instructor to the class meeting
					connection.addObjectProperty( vivoClassMeeting, URI_RELATIONSHIP_CLASSMEETING_INSTRUCTOR, vivoInstructor );
					connection.addObjectProperty( vivoInstructor, URI_RELATIONSHIP_INSTRUCTOR_CLASSMEETING, vivoClassMeeting );

					// Obtain the semester class
					Individual vivoSemesterClass = connection.getUniqueIndividualByObjectProperty( vivoClassMeeting.getURI(), IngestFA07ClassDatabase.URI_RELATIONSHIP_CLASSMEETING_SEMESTERCLASS );

					// Link to the semester class 
					if( vivoSemesterClass != null )
					{
						connection.addObjectProperty( vivoSemesterClass, URI_RELATIONSHIP_SEMESTERCLASS_ACADEMICEMPLOYEE, vivoInstructor );
						connection.addObjectProperty( vivoInstructor, URI_RELATIONSHIP_ACADEMICEMPLOYEE_SEMESTERCLASS, vivoSemesterClass );
					}
					else
					{
						System.out.println( "Couldn't add object relationship between academic employee " + vivoInstructor.getName() + " and semester class for " + vivoClassMeeting.getName() );
					}
				}
				else
				{
					// TODO: add this entity to a list, and print all of its information at the
					// end of the ingest so it can be resolved by hand.
					//System.out.println( "Unable to link \"" + entity.getName() + "\"" );
				}
			}
			catch( Exception e )
			{
				System.out.println( "Fatal error adding entity " + entity.getName() + " to database" );
				e.printStackTrace();
			}

			// Increment the record count
			recordsRead++;
		}
		
		// Close the connection and the document
		connection.disconnect();
		document.close();

		// Print a status message 
		System.out.println( "Ingest succeeded!  " + recordsRead + " record(s) read" );
	}
	

	/**
	 * Obtain a person's Individual representation based on their Net ID
	 * @param netID
	 * @param dao
	 * @return
	 * @throws InvalidParameterException
	 */
	public static Individual obtainIndividualForPerson( String netID, IndividualDao dao ) throws InvalidParameterException
	{
		// Check the parameters
		if( netID == null || dao == null ) throw new InvalidParameterException();

		// Get the uri of the individual with this net ID
		String uri = dao.getIndividualURIFromNetId( netID );
		
		// Make sure the URI is valid
		if( uri == null ) return null;
		
		// Obtain the entity by using its URI
		return dao.getIndividualByURI( uri );
	}

	/**
	 * This document class can be used to obtain data from the source class 
	 * @author Karl Gluck
	 */
	private static class IngestDocument extends DelimitedIngestDocument
	{
		/**
		 * Sets up this document to ingest the designated source file
		 * @param sourceFile The file from which to read.  This should be
		 * 					 a path to the tab-delimited "FA07 instructor netid.txt"
		 */
		public IngestDocument( String sourceFile ) throws FileNotFoundException
		{
			// Create this tab-delimited file
			super( new FileReader( sourceFile ), "\t" );
		}

		/**
		 * Obtains an entity with data from the document.
		 * @return The next entity in the document, or 'null' if the document has no more
		 *         available data
		 */
		@Override
		public Entity next()
		{
			// Read a record from the document
			boolean obtainedRecord = false;
			try
			{
				obtainedRecord = this.readRecord();
			}
			catch( IOException e )
			{
				System.out.println( "Unable to read record from source file" );
			}
			finally
			{
				// If we couldn't get a record, exit the method
				if( !obtainedRecord ) return null;
			}

			// Create an entity for this record
			EntityImpl entity = new EntityImpl( getLastRecordValue("Dept") + getLastRecordValue("Num") );
			
			// Save the net ID of this professor to the entity
			entity.setValue( VALUEKEY_INSTRUCTORNETID, getLastRecordValue("Ee Netid") );
			
			// Add data that identifies the class in this record
			entity.setValue( IngestFA07ClassDatabase.VALUEKEY_COURSEID, getLastRecordValue( "Cid" ) );
			entity.setValue( IngestFA07ClassDatabase.VALUEKEY_YEAR, getLastRecordValue( "Year" ) );
			entity.setValue( IngestFA07ClassDatabase.VALUEKEY_TERM, getLastRecordValue( "Term" ) );

			// Return the entity we created
			return entity;
		}

	}
}
