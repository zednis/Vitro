package edu.cornell.mannlib.ingest.kwinter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelAuditor;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetup;


/* 2008-08-04 BJL made some methods and fields static to eliminate compilation errors
 * 
 */

/**
 * This class makes it easy to connect to the Vivo database and begin editing.  It includes
 * helpful methods to establish 
 * @author Karl Gluck
 */
public class VivoDatabaseConnection
{
	/// This is the base URI used for all VIVO database entries
	public static final String BASE_URI = "http://vivo.library.cornell.edu/ns/0.1#";
	
	/// This is the user-URI for the ingest tool
	public static final String INGEST_TOOL_URI = BASE_URI + "karlsIngestToolkit";

    /// Used during makeDBModelFromPropertiesFile in connect()
    private static final String JENA_DB_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
    private static final String JENA_AUDIT_MODEL = "http://vitro.mannlib.cornell.edu/ns/db/experimental/audit";
    
    /// Used during makeDBModelFromPropertiesFile in connect()
    /// TODO: [1-5-2007] what is this?
    private static final OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_DL_MEM;

    /// Our connection to the Vivo database
    protected static WebappDaoFactory myDaoFactory;
    
    /// The connection properties file
    protected static String myConnectionPropertiesFile;

    /**
     * The derivatives of this class are used to search for individuals in the VIVO database that
     * match multiple parameters.  Essentially, this is an inefficient method of executing queries
     * such as "FIND List<Individual> WHERE EACH HAS RELATIONSHIP {inSemester} TO (Fall 2007) AND HAS DATA PROPERTY {courseID} EQUALS (ECE 210)"
     * @author Karl
     */
    static abstract class PropertyValue
    {
    	/**
    	 * Gets a list of individuals directly from VIVO which match this property
    	 * @param connection The connection to use to pull matching individuals
    	 * @return A list of individuals matching this property value
    	 */
    	public abstract List<Individual> getMatchingIndividuals( VivoDatabaseConnection connection );
    	
    	/**
    	 * Returns a list of Individuals that have a property value matching this one.
    	 * @param individuals The list from which to match Individuals
    	 * @return List of individuals that match this filter
    	 */
    	public List<Individual> getMatchingIndividuals( List<Individual> individuals )
    	{
    		List<Individual> matchList = new ArrayList<Individual>();
    		for( Individual individual : individuals )
    			if( matches( individual ) ) matchList.add( individual );
    		return matchList;
    	}
    	
    	/**
    	 * Returns whether or not the property value indicated 
    	 * @param individual
    	 * @return
    	 */
    	public abstract boolean matches( Individual individual );
    }
    
    static class DataPropertyValue extends PropertyValue
    {
    	/// The URI of the data property to check
    	String myPropertyURI;
    	
    	/// The value of the data property to match
    	String myPropertyValue;
    	
    	/// The value of the data property to match
    	String myDatatypeURI;
    	
    	public DataPropertyValue( String uri, String value, String typeURI )
    	{
    		myPropertyURI = uri;
    		myPropertyValue = value;
    		myDatatypeURI = typeURI;
    	}
    	
    	public DataPropertyValue( String uri, String value )
    	{
    		this( uri, value, null );
    	}

    	@Override
    	public List<Individual> getMatchingIndividuals( VivoDatabaseConnection connection )
    	{
    		if( myPropertyURI == null || myPropertyValue == null ) return new ArrayList<Individual>();
    		return connection.getDaoFactory().getIndividualDao().getIndividualsByDataProperty( myPropertyURI, myPropertyValue, myDatatypeURI, null );
    	}

    	@Override
    	public boolean matches( Individual individual )
    	{
    		if( myPropertyURI == null || myPropertyValue == null ) return false;
    		for( DataPropertyStatement dps : individual.getDataPropertyStatements() )
    			if( dps.getDatapropURI().equals(myPropertyURI) &&
    			    (myDatatypeURI == null || dps.getDatatypeURI().equals(myDatatypeURI)) &&
    			    dps.getData().equals( myPropertyValue ) ) return true;
    		return false;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return "match individuals with the property { " + myPropertyURI + " } equal to { " + myPropertyValue + " }" + (myDatatypeURI == null ? "" : " of type (" + myDatatypeURI + ")");
    	}
    }
    
    static class ObjectPropertyValue extends PropertyValue
    {
    	String myPropertyURI, myObjectURI;
    	
    	public ObjectPropertyValue( String uri, String objectURI )
    	{
    		myPropertyURI = uri;
    		myObjectURI = objectURI;
    	}
    	
    	public ObjectPropertyValue( String uri, Individual object )
    	{
    		this( uri, object.getURI() );
    	}

    	@Override
    	public List<Individual> getMatchingIndividuals( VivoDatabaseConnection connection )
    	{
    		// If either is invalid, don't return anything
    		if( null == myObjectURI || null == myPropertyURI ) return new ArrayList<Individual>();
    		
    		// Get a list of object property statements
    		ObjectProperty property = getDaoFactory().getObjectPropertyDao().getObjectPropertyByURI( myPropertyURI );
    		List<ObjectPropertyStatement> statements = getDaoFactory().getObjectPropertyStatementDao().getObjectPropertyStatements( property );
    		property = null;
    		
    		// Go through the list, and find those whose subject is the given URI
    		List<Individual> individuals = new ArrayList<Individual>();
    		for( ObjectPropertyStatement stmt : statements )
    		{
    			// Check to see if the object is the same
    			if( stmt.getObjectURI().equals(myObjectURI) )
    			{
    				// Only add valid individuals to the list
    				Individual i = stmt.getObject();
    				if( i != null ) individuals.add( i );
    			}
    		}
    		
    		// Return the list
    		return individuals;	
    	}
    	
    	@Override
    	public boolean matches( Individual individual )
    	{
    		if( null == myObjectURI || null == myPropertyURI ) return false;
    		for( ObjectPropertyStatement ops : individual.getObjectPropertyStatements() )
    			if( ops.getPropertyURI().equals(myPropertyURI) && ops.getObjectURI().equals(myObjectURI) ) return true;
    		return false;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return "match individuals with the relationship { " + myPropertyURI + " } referencing { " + myObjectURI + " }";
    	}
    }

    /**
     * Initializes the connection to the database
     * @param connectionProperties Full path to the connection properties file to use
     */
	public VivoDatabaseConnection( String connectionPropertiesFile )
	{
		// Initialize internal variables
		myDaoFactory = null;
		myConnectionPropertiesFile = connectionPropertiesFile;
	}

	/**
	 * Establishes a connection to the server
	 */
	public static void connect()
	{
		// This class helps initialize our connection to the database
		JenaDataSourceSetup jdss = new JenaDataSourceSetup();
		
		// The different models we're going to build
		OntModel dbOntModel = null;		// The database ontology model, read directly from the MySQL database
		Model memModel = null;			// In-memory Jena/RDF model intermediary
		OntModel memOntModel = null;	// In-memory ontology model, ready for use
		OntModel auditModel = null;

		// Connect to the database
		try {
			
			System.out.println( "Making Jena DB model..." );
			dbOntModel = jdss.makeDBModelFromPropertiesFile( myConnectionPropertiesFile, JENA_DB_MODEL, ONT_MODEL_SPEC );
			
			System.out.println( "Registering model auditor..." );
			dbOntModel.getBaseModel().register( new ModelAuditor(auditModel,dbOntModel) );
			auditModel = jdss.makeDBModelFromPropertiesFile( myConnectionPropertiesFile, JENA_AUDIT_MODEL, ONT_MODEL_SPEC );

		} catch( Exception e ) {
			System.out.println( VivoDatabaseConnection.class.getName() + " could not open Jena DB model named " + JENA_DB_MODEL );
			return;
		}

		// Make the in-memory model from the database ontology model
		memModel = ModelFactory.createDefaultModel();
		memModel.add( dbOntModel );
		
		// Create the in-memory ontology model
		memOntModel = ModelFactory.createOntologyModel( ONT_MODEL_SPEC, memModel );
		memOntModel.prepare();
		
		// Make the memory model synchronize with the db model automatically
		memOntModel.getBaseModel().register(new ModelSynchronizer(dbOntModel));

		// Create DAO administrator to complete the connection setup
		myDaoFactory = new WebappDaoFactoryJena( memOntModel, dbOntModel );
		myDaoFactory = myDaoFactory.getUserAwareDaoFactory( INGEST_TOOL_URI );
	}

	/**
	 * Removes this class's connection to the Vivo server
	 */
	public void disconnect()
	{
		myDaoFactory = null;
	}

	/**
	 * Obtains the DAO factory for Vivo
	 * @return 'null', if the connection has not been established, or a DAO factory that
	 * 		   can be used to obtain, edit and save entries in the Vivo database
	 */
	public static WebappDaoFactory getDaoFactory()
	{
		// If the connection hasn't been made, try to obtain it
		if( null == myDaoFactory )
			connect();

		// Return the DAO factory object in its current state (if the connection fails,
		// this may be null)
		return myDaoFactory;
	}
	
	/**
	 * Adds a new data property to the database for the given entity.
	 * @param entity The entity for which to add the data property
	 * @param propertyURI The URI of the data property to be added
	 * @param value The value to assign to the property
	 */
	public void addDataProperty( Individual entity, String propertyURI, String value )
	{
		// Check the parameters (we only have to validate the "entity" parameter, because
		// if the others are invalid, they will be caught later).
		if( entity == null ) throw new InvalidParameterException();
		
		// If the data property value is invalid, don't do anything
		if( value == null ) return;
		
		// Add this data property using the helper method
		addDataProperty( entity.getURI(), propertyURI, value );
	}
	
	/**
	 * Adds a new data property to the database for the given entity.
	 * @param entityURI The URI of the entity for which to add the data property
	 * @param propertyURI The URI of the data property to be added
	 * @param value The value to assign to the property
	 */
	public void addDataProperty( String entityURI, String propertyURI, String value )
	{
		// Don't add null data properties
		if( value == null ) return;
		
		// Build the statement
		DataPropertyStatement stmt = new DataPropertyStatementImpl();
		stmt.setIndividualURI(entityURI);
		stmt.setDatapropURI(propertyURI);
		stmt.setData(value);
		
		// Add this statement to the database
		getDaoFactory().getDataPropertyStatementDao().insertNewDataPropertyStatement(stmt);
	}

	/**
	 * Adds an object property to the database for the given entity.
	 * @param subject The subject (reference) of the statement
	 * @param propertyURI The object property type to create
	 * @param object The object (destination) of the statement
	 */
	public void addObjectProperty( Individual subject, String propertyURI, Individual object ) throws InvalidParameterException
	{
		// Make sure the parameters that we explicitly use are valid
		if( subject == null || object == null )
			throw new InvalidParameterException();

		// Use the helper method
		addObjectProperty( subject.getURI(), propertyURI, object.getURI() );
	}

	/**
	 * Adds an object property to the database for the given entity.
	 * @param subjectURI The URI of the subject (reference) of the statement
	 * @param propertyURI The object property type to create
	 * @param objectURI The URI of the object (destination) of the statement
	 */
	public void addObjectProperty( String subjectURI, String propertyURI, String objectURI ) throws InvalidParameterException
	{
		// Check the parameters
		if( subjectURI == null || propertyURI == null || objectURI == null )
			throw new InvalidParameterException();
		
		// Build the statement
	    ObjectPropertyStatement stmt = new ObjectPropertyStatementImpl();
	    stmt.setSubjectURI(subjectURI);
	    stmt.setPropertyURI(propertyURI);
	    stmt.setObjectURI(objectURI);

	    // Insert this statement
	    getDaoFactory().getObjectPropertyStatementDao().insertNewObjectPropertyStatement(stmt);
	}
	
	public List<Individual> getIndividualsByPropertyValues( List<PropertyValue> propertyValues )
	{
		if( propertyValues == null || propertyValues.size() < 1 ) return new ArrayList<Individual>();

		List<Individual> list = null;
		for( PropertyValue value : propertyValues )
		{
			// Filter Individuals by this property value
			if( list == null ) list = value.getMatchingIndividuals( this );
			else
				list = value.getMatchingIndividuals( list );
			
			// If we've removed all valid elements, exit this method
			if( list == null || list.isEmpty() ) return new ArrayList<Individual>();
		}
		
		return list;
	}

	public List<Individual> getIndividualsByPropertyValue( PropertyValue propertyValue )
	{
		List<PropertyValue> propertyValues = new ArrayList<PropertyValue>();
		propertyValues.add( propertyValue );
		return getIndividualsByPropertyValues( propertyValues );
	}
	
	public Individual getUniqueIndividualByPropertyValues( List<PropertyValue> propertyValues )
	{
		List<Individual> individuals = getIndividualsByPropertyValues( propertyValues );
		
		// Make sure only 1 element was returned
		Individual individual = null;
		if( individuals != null && individuals.size() > 1 ) System.out.println( "getUniqueIndividualByPropertyValue( " + propertyValues + " ) returned " + individuals.size() + " results, when it should be unique" );
		else if( individuals != null && !individuals.isEmpty() ) individual = individuals.get(0);
		
		return individual;
	}

	public Individual getUniqueIndividualByPropertyValue( PropertyValue propertyValue )
	{
		List<Individual> individuals = getIndividualsByPropertyValue( propertyValue );
		
		// Make sure only 1 element was returned
		Individual individual = null;
		if( individuals != null && individuals.size() > 1 ) System.out.println( "getUniqueIndividualByPropertyValue( " + propertyValue + " ) returned " + individuals.size() + " results, when it should be unique" );
		else if( individuals != null && !individuals.isEmpty() ) individual = individuals.get(0);
		
		return individual;
	}

	/**
	 * Obtains an individual for whom the specified data property has the provided value.  If more than one
	 * entity has this value, the first in the list is returned and an error message is printed.
	 * @param propertyURI
	 * @param value
	 * @return
	 */
	public Individual getUniqueIndividualByDataPropertyValue( String propertyURI, String value )
	{
		return getUniqueIndividualByPropertyValue( new DataPropertyValue( propertyURI, value ) );
	}
	

	/**
	 * Gets the first individual that is involved in the given relationship to another individual.  This method
	 * is used when, for example, one wants to find the unique object which links to a known object
	 * @return
	 */
	public Individual getUniqueIndividualByObjectProperty( String propertyURI, String objectURI )
	{
		return getUniqueIndividualByPropertyValue( new ObjectPropertyValue( propertyURI, objectURI ) );
	}
}
