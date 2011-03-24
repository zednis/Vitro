package edu.cornell.mannlib.ingest.kwinter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.cornell.mannlib.ingest.kwinter.VivoIngestDocument.Entity;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;



//TODO: add a way to confirm which semester a class meeting is in.  For now, this code works fine--but in the future,
//with multiple semesters, course Ids could be reused and this won't link crossreferences correctly!


/**
 * This class imports/updates data from the FA07 <b>class</b> database into VIVO.  This class was designed
 * to reference course listings in VIVO ingested from the online course-catalog (using the appropriate
 * ingest class), but in theory those course listings will be created if they don't already exist, and the
 * catalog ingester should have no problem with updating them.
 *  
 * @author Karl
 */
public final class IngestFA07ClassDatabase
{
	// Default FA07 database file to ingest
	public static final String DEFAULT_DATABASE_FILE = "C:\\Documents and Settings\\Karl\\My Documents\\Work - Vivo\\VIVO FA07 Course.txt";

	/// Default location for the connection properties
	public static final String DEFAULT_CONNECTION_PROPERTIES_FILE = "C:\\Documents and Settings\\Karl\\My Documents\\Work - Vivo\\code\\webapp\\config\\connection.properties";
	
	/// This is the base URI for all Cornell classes
	public static final String BASE_CLASS_URI = VivoDatabaseConnection.BASE_URI + "class";
	
	/// These constants are used to pass entity values around
	public static final String VALUEKEY_COURSEID 		= "Course ID Number";
	public static final String VALUEKEY_YEAR			= "Year";
	public static final String VALUEKEY_TERM			= "Term";
	public static final String VALUEKEY_COURSENUMBER 	= "Course Catalog Number";
	public static final String VALUEKEY_CREDITHOURS = "Credits";
	public static final String VALUEKEY_GRADEOPTION = "Grade Option";
	public static final String VALUEKEY_DEPARTMENT = "Department";
	public static final String VALUEKEY_COLLEGE = "College";
	public static final String VALUEKEY_SECTIONTYPE_VCLASSURI = "Section Type VClass URI";
	public static final String VALUEKEY_CLASSINDEX = "Class Num";
	
	public static final String VALUEKEY_TIME = "Time";
	public static final String VALUEKEY_TOTALENROLLMENT = "Total Enrollment";
	
	public static final String VALUEKEY_LOCATION_BUILDINGCODE = "Location";
	public static final String VALUEKEY_LOCATION_ROOM = "Room";

	public static final String VALUEKEY_PARENTCOURSEID  = "Parent Course ID Number";
	public static final String VALUEKEY_CROSSLISTINGIDS = "Crosslisted ID Numbers";
	
	/// These URIs are for the VIVO database ingest of courses
	/// updated 1/28/2007 from email on Jan 22 from Jon
	/// QUESTION:
	/// all relationships have inverses.  What happens if a crosslisted class links another class,
	/// then that class also links the first, both using the forward-direction ("To") crosslist?
	/// does the crosslisting show up both forward and backward on both entities?
	/// ANSWER:
	/// inverse listings aren't currently implemented in VIVO, so only the forward-listing will
	/// ever be displayed
	public static final String URI_VCLASS_SEMESTERCLASS = "http://vivo.library.cornell.edu/ns/0.1#SemesterClass";
	public static final String URI_RELATIONSHIP_CANONICALCOURSE_TO_SEMESTERCLASS = "http://vivo.library.cornell.edu/ns/0.1#taughtAsSemesterClass";
	public static final String URI_RELATIONSHIP_SEMESTERCLASS_CANONICALCOURSE = "http://vivo.library.cornell.edu/ns/0.1#semesterClassFor";
	public static final String URI_RELATIONSHIP_SEMESTERCLASS_SEMESTER = "http://vivo.library.cornell.edu/ns/0.1#classTaughtInSemester";
	public static final String URI_RELATIONSHIP_SEMESTER_SEMESTERCLASS = "http://vivo.library.cornell.edu/ns/0.1#timeIntervalForsemesterClass";
	public static final String URI_RELATIONSHIP_SEMESTERCLASS_COLLEGE = "http://vivo.library.cornell.edu/ns/0.1#semesterClassRegisteredByCollege";
	public static final String URI_RELATIONSHIP_COLLEGE_SEMESTERCLASS = "http://vivo.library.cornell.edu/ns/0.1#registersSemesterClass";
	public static final String URI_RELATIONSHIP_SEMESTERCLASS_DEPARTMENT = "http://vivo.library.cornell.edu/ns/0.1#classListedIn";
	public static final String URI_RELATIONSHIP_DEPARTMENT_SEMESTERCLASS = "http://vivo.library.cornell.edu/ns/0.1#listsClass";
	public static final String URI_DPROP_SEMESTERCLASS_YEARANDTERM = "http://vivo.library.cornell.edu/ns/0.1#classYearTerm";
	public static final String URI_DPROP_SEMESTERCLASS_CODE3DIGIT = "http://vivo.library.cornell.edu/ns/0.1#classCourseCode3Digit";
	public static final String URI_DPROP_SEMESTERCLASS_CODE4DIGIT = "http://vivo.library.cornell.edu/ns/0.1#classCourseCode4Digit";
	public static final String URI_DPROP_SEMESTERCLASS_CREDITHOURS = "http://vivo.library.cornell.edu/ns/0.1#classCredits";
	public static final String URI_DPROP_SEMESTERCLASS_GRADEOPTION = "http://vivo.library.cornell.edu/ns/0.1#classGradeOption";

	// Unused URIs
	public static final String URI_DPROP_SEMESTERCLASS_COURSEID = "http://vivo.library.cornell.edu/ns/0.1#classCourseId";

	public static final String URI_VCLASS_CLASSMEETING = "http://vivo.library.cornell.edu/ns/0.1#ClassMeeting";
	public static final String URI_VCLASS_CLASSLAB = "http://vivo.library.cornell.edu/ns/0.1#ClassLab";
	public static final String URI_VCLASS_CLASSLECTURE = "http://vivo.library.cornell.edu/ns/0.1#ClassLecture";
	public static final String URI_VCLASS_CLASSSECTION = "http://vivo.library.cornell.edu/ns/0.1#ClassSection";
	public static final String URI_RELATIONSHIP_SEMESTERCLASS_CLASSMEETING = "http://vivo.library.cornell.edu/ns/0.1#hasClassMeeting";
	public static final String URI_RELATIONSHIP_CLASSMEETING_SEMESTERCLASS = "http://vivo.library.cornell.edu/ns/0.1#meetingOfSemesterClass";
	public static final String URI_RELATIONSHIP_CLASSMEETING_LOCATION = "http://vivo.library.cornell.edu/ns/0.1#classMeetingLocation";
	public static final String URI_RELATIONSHIP_CLASSMEETING_CROSSLISTING = "http://vivo.library.cornell.edu/ns/0.1#crossListedToClassMeeting";
	public static final String URI_RELATIONSHIP_CLASSMEETING_CROSSLISTING_INV = "http://vivo.library.cornell.edu/ns/0.1#crossListedFromClassMeeting";
	public static final String URI_DPROP_CLASSMEETING_COURSEID = "http://vivo.library.cornell.edu/ns/0.1#classMeetingCourseId";
	public static final String URI_DPROP_CLASSMEETING_TIME = "http://vivo.library.cornell.edu/ns/0.1#classMeetingTime";
	public static final String URI_DPROP_CLASSMEETING_TOTALENROLLMENT = "http://vivo.library.cornell.edu/ns/0.1#classMeetingTotalEnrollment";

	// TODO: add these relationships
	public static final String URI_RELATIONSHIP_ACADEMICSEMESTER_CLASSMEETING = ""; // THIS INVERSE DOESNT EXIST?!
	public static final String URI_RELATIONSHIP_CLASSMEETING_ACADEMICSEMESTER = "http://vivo.library.cornell.edu/ns/0.1#classMeetingTaughtInSemester";

	public static final String URI_DPROP_COLLEGE_CODE = "http://vivo.library.cornell.edu/ns/0.1#departmentcourseInfocode";
	
	/// This is the entity to which all of the semester classes and class meetings are linked
	public static final String URI_ENTITY_ACADEMICSEMESTER_FALL2007 = "http://vivo.library.cornell.edu/ns/0.1#Fall2007";

	/* data that we need:
	 * SEMESTERCLASS
	 * 	relationships:
	 * 		canonical course
	 * 		semester
	 * 		department
	 * 		college
	 * 	data properties:
	 * 		course ID - ??? where did this come from?  There is no "semester class" course ID!
	 * 		year and term
	 * 		code 3 digit
	 * 		code 4 digit
	 * 		credit hours
	 * 		grade option
	 * 
	 * CLASS MEETING
	 * 	relationships:
	 * 		semester class - specific meeting of a class
	 * 		class meeting - crosslisted class meetings!!
	 * 		location - how do you search for buildings/rooms?
	 * 	data properties:
	 * 		course ID
	 * 		time
	 * 		total enrollment
	 */
	
	protected static class VivoClassMeeting
	{
		// Non-VIVO data used to identify this meeting
		protected String myCourseCode3Digit; // The 3-digit course code is used to produce a SemesterClass
		protected boolean myInstanceIsRoot;	 // Whether or not this is a root class meeting (i.e. not a subclass lecture/lab)
		protected String myCreditHours;
		protected String myGradeOption;
		protected String myDepartment;
		protected String myCollege;
		protected String myClassNumber;
		
		// VIVO data that specifies what kind of class this is (lecture, lab or section)
		protected String myVivoVClassURI;
		protected VivoSemesterClass myVivoSemesterClassRef;
		
		// Relationships
		protected List<String> myCrosslistedCourseIDs;
		protected String myLocation;

		// Data Properties
		protected String myCourseID;
		protected String myTime;
		protected String myTotalEnrollment;
		
		public String getCourseCode3Digit() { return myCourseCode3Digit; }
		public boolean isInstanceRoot() { return myInstanceIsRoot; }
		public String getVivoVClassURI() { return myVivoVClassURI; }
		public List<String> getCrosslistedCourseIDs() { return myCrosslistedCourseIDs; }
		public String getLocation() { return myLocation; }
		public String getCourseID() { return myCourseID; }
		public String getTime() { return myTime; }
		public String getTotalEnrollment() { return myTotalEnrollment; }
		public String getCreditHours() { return myCreditHours; }
		public String getGradeOption() { return myGradeOption; }
		public String getDepartment() { return myDepartment; }
		public String getCollege() { return myCollege; }
		
		// Temporary data used between passes in the push to VIVO
		private Individual myClassMeetingToCrosslist;
		
		/**
		 * Used when a VivoSemesterClass obtains data from this class meeting to link this meeting into the class
		 * @param ref
		 */
		public void setVivoSemesterClassRef( VivoSemesterClass ref ) { myVivoSemesterClassRef = ref; }

		/**
		 * Initializes this class meeting using the provided source entity
		 * @param sourceEntity
		 */
		public VivoClassMeeting( VivoIngestDocument.Entity sourceEntity )
		{
			myCourseCode3Digit = sourceEntity.getValue( VALUEKEY_DEPARTMENT ) + " " + sourceEntity.getValue( VALUEKEY_COURSENUMBER );
			myInstanceIsRoot = sourceEntity.getValue( VALUEKEY_PARENTCOURSEID ).isEmpty();
			myCourseID = sourceEntity.getValue( VALUEKEY_COURSEID );
			myCreditHours = sourceEntity.getValue( VALUEKEY_CREDITHOURS );
			myGradeOption = sourceEntity.getValue( VALUEKEY_GRADEOPTION );
			myDepartment = sourceEntity.getValue( VALUEKEY_DEPARTMENT );
			myCollege = sourceEntity.getValue( VALUEKEY_COLLEGE );
			myClassNumber = sourceEntity.getValue( VALUEKEY_CLASSINDEX );
			myVivoVClassURI = sourceEntity.getValue( VALUEKEY_SECTIONTYPE_VCLASSURI );
			myTime = sourceEntity.getValue( VALUEKEY_TIME );
			myTotalEnrollment = sourceEntity.getValue( VALUEKEY_TOTALENROLLMENT );
			myCrosslistedCourseIDs = sourceEntity.getValueList(VALUEKEY_CROSSLISTINGIDS, ",");
			myLocation = sourceEntity.getValue( VALUEKEY_LOCATION_BUILDINGCODE ) + " : " + sourceEntity.getValue( VALUEKEY_LOCATION_ROOM );
			myClassMeetingToCrosslist = null;
		}
		
		public void pushToVivo_Pass1( VivoDatabaseConnection connection, String yearAndTerm )
		{
			// Get the VIVO DAOs and initialize multi-use local data
			IndividualDao individualDao = connection.getDaoFactory().getIndividualDao();
			List<Individual> individuals = null;
			
			// Get the canonical course for this semester class
			Individual classMeeting = connection.getUniqueIndividualByDataPropertyValue( URI_DPROP_CLASSMEETING_COURSEID, myCourseID );

			// If the class meeting doesn't exist yet, create it
			if( classMeeting == null )
			{
				// Create a name for this class meeting
				String classMeetingName = "";
				if( myVivoVClassURI.equals(URI_VCLASS_CLASSLAB) ) classMeetingName += "Lab";
				else if( myVivoVClassURI.equals( URI_VCLASS_CLASSSECTION ) ) classMeetingName += "Section";
				else classMeetingName += "Lecture";
				
				// Add the index of the class to the name
				classMeetingName += " " + myClassNumber;

				// Create the individual
				classMeeting = new IndividualImpl();
				classMeeting.setURI( VivoDatabaseConnection.BASE_URI + "classMeeting" + myCourseID.replaceAll(" *", "") + yearAndTerm.replaceAll(" *", "") );
				classMeeting.setVClassURI( myVivoVClassURI );
				classMeeting.setName( classMeetingName + " for " + myCourseCode3Digit );

				// Create this individual in Vivo
                try{
                	classMeeting = individualDao.getIndividualByURI( individualDao.insertNewIndividual( classMeeting ) );
                }catch(Exception ex){
                    System.out.println("could not make individual for course: " + classMeeting.getName() + " uri: " + classMeeting.getURI() +
                    "\n" + ex.getMessage());
                }
                
                // Add the data properties to the class meeting
    			connection.addDataProperty( classMeeting, URI_DPROP_CLASSMEETING_COURSEID, myCourseID );
    			connection.addDataProperty( classMeeting, URI_DPROP_CLASSMEETING_TIME, myTime );
    			connection.addDataProperty( classMeeting, URI_DPROP_CLASSMEETING_TOTALENROLLMENT, myTotalEnrollment );
    			
    			// Add a relationship to the semester class
    			String classMeetingURI = classMeeting.getURI();
    			String vivoSemesterClassURI = myVivoSemesterClassRef.getVivoIndividualURI();
    			if( classMeetingURI != null && vivoSemesterClassURI != null )
    			{
	    			connection.addObjectProperty( classMeetingURI, URI_RELATIONSHIP_CLASSMEETING_SEMESTERCLASS, vivoSemesterClassURI );
	    			connection.addObjectProperty( vivoSemesterClassURI, URI_RELATIONSHIP_SEMESTERCLASS_CLASSMEETING, classMeetingURI );
	    			
	    			//propagate flag1 from class to classMeeting
	    			Individual semClass = individualDao.getIndividualByURI(vivoSemesterClassURI);	    			
	    			if( semClass != null ){   
	    			    if( classMeeting.getFlag1Set() != null && classMeeting.getFlag1Set().length() > 0 )
	    			        classMeeting.setFlag1Set(  semClass.getFlag1Set() + "," + classMeeting.getFlag1Set() );
	    			    else
	    			        classMeeting.setFlag1Set( semClass.getFlag1Set() );
	    			}
    			}
    			else
    			{
    				// TODO: warn about this problem!
    			}
    			
    			// Add a relationship to the semester
    			connection.addObjectProperty( classMeetingURI, URI_RELATIONSHIP_CLASSMEETING_ACADEMICSEMESTER, URI_RELATIONSHIP_CLASSMEETING_ACADEMICSEMESTER );

    			// Update this individual
    			individualDao.updateIndividual( classMeeting );
    			
    			// Since this is a new class meeting, save it
    			myClassMeetingToCrosslist = classMeeting;
			}
		}
		
		/**
		 * The second stage of the push to VIVO sets up the crosslist relationships between class meetings
		 * @param connection
		 */
		public void pushToVivo_Pass2( VivoDatabaseConnection connection )
		{
			// If we didn't create a new class meeting, we don't have to run this pass
			if( myClassMeetingToCrosslist == null ) return;

			// Add cross-listings
			for( String courseID : myCrosslistedCourseIDs )
			{
				Individual otherClassMeeting =  connection.getUniqueIndividualByDataPropertyValue( URI_DPROP_CLASSMEETING_COURSEID, courseID );
				
				// If this course doesn't exist, warn--but move on
				if( otherClassMeeting == null )
				{
					System.out.println( "Unable to find crosslisted course #" + courseID + " for " + myClassMeetingToCrosslist.getName() );
					continue;
				}
				
				// Add the relationship between these two entries (the reverse relationship should be created by the other crosslisting
				connection.addObjectProperty( myClassMeetingToCrosslist, URI_RELATIONSHIP_CLASSMEETING_CROSSLISTING, otherClassMeeting );
			}
		}
	}

	/**
	 * Holds an instance of a semester class
	 */
	protected static class VivoSemesterClass
	{
		//protected String myCourseID;
		protected String myCourseCode3Digit;
		protected String myCreditHours;
		protected String myGradeOption;
		protected String myDepartment;
		protected String myCollege;
		
		// Once inserted, this semester class's URI
		protected String myVivoIndividualURI;
		
		/**
		 * After this class has been inserted, this method returns the individual's URI in VIVO
		 * @return
		 */
		public String getVivoIndividualURI() { return myVivoIndividualURI; }

		/**
		 * Initializes this class with default values.  Use mergeDataFrom() to add information.
		 */
		public VivoSemesterClass()
		{
			zero();
			
			// The year and term are the same for all classes
			//myYearAndTerm = yearAndTerm;
			//myVivoSemesterURI = vivoSemesterURI;
		}
		
		/**
		 * Resets all of the data in this class
		 */
		public void zero()
		{
			//myCourseID = null;
			myCourseCode3Digit = null;
			myCreditHours = null;
			myGradeOption = null;
			myDepartment = null;
			myCollege = null;
		}
		
		/**
		 * Obtains data from the source meeting instance and uses it to add to the data in
		 * this class.  In this way, VivoSemesterClass instances should gather all of their
		 * fields.
		 */
		public void mergeDataFrom( VivoClassMeeting meeting )
		{
			if( myCourseCode3Digit == null ) myCourseCode3Digit = meeting.getCourseCode3Digit();
			if( myCreditHours == null )		 myCreditHours = meeting.getCreditHours();
			if( myGradeOption == null )		 myGradeOption = meeting.getGradeOption();
			if( myDepartment == null )		 myDepartment = meeting.getDepartment();
			if( myCollege == null )			 myCollege = meeting.getCollege();
			
			// Link this class
			meeting.setVivoSemesterClassRef(this);
		}

		/**
		 * 
		 * @param connection The connection with which to communicate to the VIVO database
		 */
		public void pushToVivo( VivoDatabaseConnection connection, String yearAndTerm, String vivoSemesterURI )
		{
			// Get the VIVO DAOs and initialize multi-use local data
			IndividualDao individualDao = connection.getDaoFactory().getIndividualDao();
			List<Individual> individuals = null;

			// Gets the list of ALL courses that match this class's code; this is only used to multilink the classes
			List<Individual> allMatchingCanonicalCourses = individualDao.getIndividualsByDataProperty( IngestDownloadedCourseCatalog.URI_DPROP_CANONICALCOURSE_CODE3DIGIT, myCourseCode3Digit );

			// Get the canonical course for this semester class
			Individual canonicalCourse = connection.getUniqueIndividualByDataPropertyValue(
											IngestDownloadedCourseCatalog.URI_DPROP_CANONICALCOURSE_CODE3DIGIT, myCourseCode3Digit );

			// Get this semester class, if it's already in the database
			individuals = individualDao.getIndividualsByDataProperty( URI_DPROP_SEMESTERCLASS_CODE3DIGIT, myCourseCode3Digit );
			Individual semesterClass = null;

			// Obtain the individual whose properties we need to edit
			if( individuals.size() > 1 ) System.out.println( myCourseCode3Digit + " has more than one instance as a SemesterClass in VIVO!" );
			if( individuals.isEmpty() )
			{
				// Get the name of the course for this class
				String canonicalCourseName = canonicalCourse == null ? "" : canonicalCourse.getName();

				// Create the semester class
				semesterClass = new IndividualImpl();
				semesterClass.setURI( VivoDatabaseConnection.BASE_URI + "semesterClass" + myCourseCode3Digit.replaceAll(" *", "") + yearAndTerm.replaceAll(" *", "") );
				semesterClass.setVClassURI( URI_VCLASS_SEMESTERCLASS );
				semesterClass.setName( myCourseCode3Digit + ": " + canonicalCourseName + "(" + yearAndTerm + ")" );

				// Create this individual in Vivo
                try{
                	semesterClass = individualDao.getIndividualByURI( individualDao.insertNewIndividual( semesterClass ) );
                }catch(Exception ex){
                    System.out.println("could not make individual for course: " + semesterClass.getName() + " uri: " + semesterClass.getURI() +
                    "\n" + ex.getMessage());
                }

                // Add data that only needs to be copied upon creation
                if( canonicalCourse != null )
                {
    				// Get the 3- and 4-digit course codes
    				DataPropertyStatementDao dps = connection.getDaoFactory().getDataPropertyStatementDao();
    				Collection<DataPropertyStatement> threeDigitCodes = dps.getDataPropertyStatementsForIndividualByDataPropertyURI( canonicalCourse, IngestDownloadedCourseCatalog.URI_DPROP_CANONICALCOURSE_CODE3DIGIT );
    				Collection<DataPropertyStatement> fourDigitCodes  = dps.getDataPropertyStatementsForIndividualByDataPropertyURI( canonicalCourse, IngestDownloadedCourseCatalog.URI_DPROP_CANONICALCOURSE_CODE4DIGIT );

    				// Re-add the three-digit codes
    				for( DataPropertyStatement stmt : threeDigitCodes )
    				{
    					// Change this statement to focus on the semester class
    					stmt.setIndividualURI( myVivoIndividualURI );
    					stmt.setDatapropURI( URI_DPROP_SEMESTERCLASS_CODE3DIGIT );
    					
    					// Add this as a new statement
    					dps.insertNewDataPropertyStatement( stmt );
    				}

    				// Re-add the four-digit codes
    				for( DataPropertyStatement stmt : fourDigitCodes )
    				{
    					// Change this statement to focus on the semester class
    					stmt.setIndividualURI( myVivoIndividualURI );
    					stmt.setDatapropURI( URI_DPROP_SEMESTERCLASS_CODE4DIGIT );
    					
    					// Add this as a new statement
    					dps.insertNewDataPropertyStatement( stmt );
    				}

    				// Add a relationship to the canonical course
    				for( Individual eachCanonicalCourse : allMatchingCanonicalCourses )
    				{
	    				connection.addObjectProperty(  semesterClass,  URI_RELATIONSHIP_SEMESTERCLASS_CANONICALCOURSE,  eachCanonicalCourse );
	    				connection.addObjectProperty( eachCanonicalCourse, URI_RELATIONSHIP_CANONICALCOURSE_TO_SEMESTERCLASS, semesterClass );
	    				
	    				//set propagate flags from course to class	    		
	    				if( semesterClass != null ){   
	                        if( semesterClass .getFlag1Set() != null && semesterClass .getFlag1Set().length() > 0 )
	                            semesterClass .setFlag1Set(  semesterClass .getFlag1Set() + "," + eachCanonicalCourse.getFlag1Set() );
	                        else
	                            semesterClass .setFlag1Set( eachCanonicalCourse.getFlag1Set() );
	    				}
    				}    				    				
                }
                else
                {
    				// Add the three-digit code
    				connection.addDataProperty( semesterClass, URI_DPROP_SEMESTERCLASS_CODE3DIGIT, myCourseCode3Digit );
                }
			}
			else
				semesterClass = individuals.get( 0 );
			
			// Save this individual's URI
			myVivoIndividualURI = semesterClass.getURI();

			// Mirror the Course Catalog data, if it exists
			if( canonicalCourse != null )
			{
				semesterClass.setDescription( canonicalCourse.getDescription() );
				semesterClass.setAnchor( canonicalCourse.getAnchor() );
				semesterClass.setUrl( canonicalCourse.getUrl() );
				semesterClass.setFlag1Numeric( canonicalCourse.getFlag1Numeric() );
				semesterClass.setFlag1Set( canonicalCourse.getFlag1Set() );

			}
			else
			{
				// Set up default information
				semesterClass.setFlag1Numeric( 1 );
				semesterClass.setFlag1Set( "1" );
			}
			
			// TODO: what is this ?!?!? 
			//connection.addDataProperty( semesterClass, URI_DPROP_SEMESTERCLASS_COURSEID, value);
			
			// These data properties are filled out without influence from the course catalog
			connection.addDataProperty( semesterClass, URI_DPROP_SEMESTERCLASS_YEARANDTERM, yearAndTerm );
			connection.addDataProperty( semesterClass, URI_DPROP_SEMESTERCLASS_CREDITHOURS, myCreditHours );
			connection.addDataProperty( semesterClass, URI_DPROP_SEMESTERCLASS_GRADEOPTION, myGradeOption );

			// Link to the semester in which this class is taught
			connection.addObjectProperty( semesterClass.getURI(), URI_RELATIONSHIP_SEMESTERCLASS_SEMESTER, vivoSemesterURI );
			connection.addObjectProperty( vivoSemesterURI, URI_RELATIONSHIP_SEMESTER_SEMESTERCLASS, semesterClass.getURI() );

			// Link to the college
			// TODO: output a warning about not being able to find a college
			Individual vivoCollege = connection.getUniqueIndividualByDataPropertyValue( URI_DPROP_COLLEGE_CODE, myCollege );
			if( null != vivoCollege )
			{
				connection.addObjectProperty( semesterClass, URI_RELATIONSHIP_SEMESTERCLASS_COLLEGE, vivoCollege );
				connection.addObjectProperty( vivoCollege, URI_RELATIONSHIP_COLLEGE_SEMESTERCLASS, semesterClass );
			}
			
			// Link to the department
			Individual vivoDepartment = IngestDownloadedCourseCatalog.findDepartmentByCode( individualDao, myDepartment );
			if( vivoDepartment != null )
			{
				// Link this department
				connection.addObjectProperty( semesterClass, URI_RELATIONSHIP_SEMESTERCLASS_DEPARTMENT, vivoDepartment );
				connection.addObjectProperty( vivoDepartment, URI_RELATIONSHIP_DEPARTMENT_SEMESTERCLASS, semesterClass );
                
				// Make sure the canonical course links the same department; if not, it's likely the department code
				// couldn't be found for the canonical course and this database will be able to fix the link!
				if( canonicalCourse != null )
				{
					// TODO: update the canonical course department link.  I can't do this right now because no parallel
					// for DatatypePropertyStatementDao.getDataPropertyStatementsForIndividualByDataPropertyURI exists
					// for ObjectPropertyStatementDao
					//ObjectPropertyStatementDao ops = connection.getDaoFactory().getObjectPropertyStatementDao();
				}
			}
			else
			{
				// TODO: check the canonical course to see if it was linked to a department
			}

			// Update this individual
			individualDao.updateIndividual( semesterClass );
		}
	}
	
	/**
	 * This class holds all of the SemesterClass and ClassMeeting instances so that they can be written
	 * into VIVO.
	 */
	protected static class Aggregator
	{
		protected String myYearAndTerm;
		protected String myVivoSemesterURI;
		
		protected List<VivoClassMeeting> myClassMeetings;
		
		public Aggregator( String yearAndTermDataprop, String vivoSemesterURI )
		{
			myYearAndTerm = yearAndTermDataprop;
			myVivoSemesterURI = vivoSemesterURI;
			myClassMeetings = new ArrayList<VivoClassMeeting>();
		}
		
		/**
		 * Puts a new class meeting into this aggregator, based on the entity which pulled data from a
		 * row of the database.
		 * @param entity
		 */
		public void add( VivoIngestDocument.Entity entity )
		{
			myClassMeetings.add( new VivoClassMeeting( entity ) );
		}
		
		/**
		 * Takes the data contained in this aggregator and sends it to VIVO
		 * @param connection
		 */
		public void pushToVivo( VivoDatabaseConnection connection )
		{
			// This map's key is the course code for the semester class, so that we can create a unique
			// semester class for each course code
			Map<String,VivoSemesterClass> semesterClasses = new HashMap<String,VivoSemesterClass>();
			
			// Keeps track of timing
			long startTime = System.currentTimeMillis();
			System.out.println( "Starting stage 1" );
			System.out.println( "\tStage 1 parses the " + String.valueOf(myClassMeetings.size()) + " class meetings to find unique semester classes for " + myYearAndTerm );
			
			// Add all of the class meetings to the hash map, and use the data to fill out semester classes
			for( VivoClassMeeting meeting : myClassMeetings )
			{
				// Obtain the course code for this meeting and the associated semester class
				String courseCode = meeting.getCourseCode3Digit();
				VivoSemesterClass semesterClass = semesterClasses.get( courseCode );
				
				// If the semester class doesn't exist yet, add it to the database
				if( semesterClass == null )
				{
					semesterClass = new VivoSemesterClass();
					semesterClasses.put( courseCode, semesterClass );
				}

				// Add this meeting's data to the class
				semesterClass.mergeDataFrom( meeting );
			}
			
			// Print the finish time
			System.out.println( "Finished stage 1 after " + String.valueOf((System.currentTimeMillis() - startTime)/1000) + " seconds " );
			System.out.println( "Starting stage 2" ); startTime = System.currentTimeMillis();
			System.out.println( "\tThis stage adds " + String.valueOf( semesterClasses.size() ) + " unique semester classes to the VIVO database" );
			
			// Add all of the unique semester classes to the VIVO database
			for( VivoSemesterClass semesterClass : semesterClasses.values() )
				semesterClass.pushToVivo( connection, myYearAndTerm, myVivoSemesterURI );

			System.out.println( "Finished stage 2 after " + String.valueOf((System.currentTimeMillis() - startTime)/1000) + " seconds " );
			System.out.println( "Starting stage 3" ); startTime = System.currentTimeMillis();
			System.out.println( "\tThis stage adds all of the class meetings, data properties and relationships to semester classes to VIVO" );
			
			// Now, add all of the classes to the database
			for( VivoClassMeeting meeting : myClassMeetings )
				meeting.pushToVivo_Pass1( connection, myYearAndTerm );

			System.out.println( "Finished stage 3 after " + String.valueOf((System.currentTimeMillis() - startTime)/1000) + " seconds " );
			System.out.println( "Starting stage 4" ); startTime = System.currentTimeMillis();
			System.out.println( "\tThe final stage sets up the relationships between crosslisted class meetings" );
			
			// Finally, cross-list them
			for( VivoClassMeeting meeting : myClassMeetings )
				meeting.pushToVivo_Pass2( connection );

			System.out.println( "Finished stage 4 after " + String.valueOf((System.currentTimeMillis() - startTime)/1000) + " seconds " );
		}
	}

	/**
	 */
	public static void main( String[] args ) throws Exception
	{
		// Just for debugging
	    Runtime runtime = Runtime.getRuntime();
	    System.out.println("\tMemory available to Java:\t" + runtime.maxMemory() / 1024);

		// Get the source file by either using the default, or whatever the user provided in the arguments
		String sourceFile = args.length >= 1 ? args[0] : DEFAULT_DATABASE_FILE;
		String connectionPropertiesFile = args.length >= 2 ? args[1] : DEFAULT_CONNECTION_PROPERTIES_FILE;
		
		// Create the database connection
		VivoDatabaseConnection connection = new VivoDatabaseConnection( connectionPropertiesFile );

		// Load the ingest document
		IngestDocument document = null;
		try {
			document = new IngestDocument( sourceFile );
		} catch( Exception e )
		{
			// Print a warning message
			System.out.println( "Unable to open \"" + sourceFile + "\" to read the class database (or there was an error)" );

			// Exit
			return;
		}
		
		// Information collected during the ingest process
		int recordsRead = 0;
		long startTime = System.currentTimeMillis();
		
		// Initialize the content aggregator
		Aggregator agg = new Aggregator( "Fall 2007", URI_ENTITY_ACADEMICSEMESTER_FALL2007 );

		// Read entities from the document
		VivoIngestDocument.Entity entity = null;
		while( null != (entity = document.next()) )
		{
			// Put this entity into the aggregator
			agg.add( entity );

			// Increment the record count
			recordsRead++;
		}
		
		// 
		System.out.println( recordsRead + " records read from source file" );
		
		// Close the document
		document.close();
		document = null;
		
		// Dump everything into VIVO
		agg.pushToVivo( connection );
		
		// Close the connection
		connection.disconnect();
		connection = null;

		// Print a status message 
		System.out.println( "Ingest finished after " + String.valueOf( (int)Math.floor((System.currentTimeMillis() - startTime) / (1000.0 * 60)) ) + " minutes.  " + recordsRead + " record(s) read" );
	}


	/**
	 * This is the specific document class that is used to ingest the FA07 database
	 * @author Karl Gluck
	 */
	private static class IngestDocument extends DelimitedIngestDocument
	{
		/**
		 * Sets up this document to ingest the designated source file
		 * @param sourceFile The file from which to read.  This should be
		 * 					 a path to the tab-delimited VIVO FA07 Course.txt
		 */
		public IngestDocument( String sourceFile ) throws FileNotFoundException
		{
			// Create this tab-delimited file
			super( new FileReader( sourceFile ), "\t" );
		}

		/**
		 * This is where I'm figuring out what the different headers in VIVO FA07 Course.txt mean:
		 * Course College		-	The 2-letter college that this course is under (ex. AG = ag school)
		 * Course Dept			-	Department code (AEM, M&AE, ECE)
		 * Course Num			-	The course's 3-digit ID number (AEM >>101<<, M&AE >>324<<)
		 * Course Sect Type		-	What kind of course the PARENT course is
		 * 								1 = normal lecture course
		 * 								2 = lab class (with no parent lecture)
		 * 								3 = independent study (ex. AEM 380 HONORS RESEARCH, AEM 800 MASTER'S-LEVEL THESIS)
		 * Course Sect Num		-	The number of this class.  For some classes with multiple instances
		 * 							this is valid.  For example, there are two different AEM 101 lectures.
		 * 							This is always 2 digits and will always be valid.
		 * Subcourse Sect Type	-	If this is a sub-course listing (Course Sect Type = 1), such as the lab part of a class,
		 * 							this lists its type:
		 * 								blank = lecture
		 * 								2 = lab
		 * 								3 = section
		 * Subcourse Sect Num	-	Unique section number, if this is a sub-course listing (otherwise
		 * 							it is blank).  Two digits.
		 * Year					-	Two-digit date code for the year.  Always 07 here.  HOORAY Y2K BUGS!
		 * Term					-	Two-digit (consecutive?) term number.  Always 70 here. 
		 * Cid					-	6-digit ENTIRELY unique course ID number
		 * College				-	Same as Course College as far as I can tell
		 * Dept					-	Same as Course Dept I think
		 * Num					-	Same as Course Num
		 * Sect Type			-	Single-digit identifier of...something confusing
		 * Sect Num				-	some sort of composite of Course Num and Course Sect Num
		 * Course Title			-	ALL-CAPS shortened name of the class
		 * Parent Cid			-	ID of the parent class, or blank if this is a root class
		 * Credit Hours			-	Blank, or garbage, for subclasses; otherwise, 0#00 where # = credits
		 * Variable Hours Flag	-	blank for subclasses; Y or N for roots; if Y, Credit Hours is the maximum value
		 * Grade Opt			-	What kind of grading is allowed
		 * 								E = 
		 * 								L = letter grades only
		 * 								O = 
		 * Beg Time1			-	24-hour 4-digit time when the class starts for the first weekday
		 * Beg Time2			-	24-hour 4-digit time for the class ends on the first weekday
		 * Day1					-	one of {MTWRF} day code	
		 * Build1				-	Campus building number
		 * Room1				-	Room code within the building (AUD = auditorium, # = number, etc
		 * Beg Time2			-	(same as Beg Time1, but for the second day)
		 * End Time2			-
		 * Day2					-
		 * Build2				-	Apparently the class can meet in a different building on different days!
		 * Room2				-	wouldn't that be confusing if the classroom changed?
		 * Beg Time3
		 * End Time3
		 * Day3
		 * Build3
		 * Room3
		 * Beg Time4
		 * End Time5
		 * Day4
		 * Build4
		 * Room4
		 * Beg Time5
		 * End Time5
		 * Day5
		 * Build
		 * Room5
		 * Beg Time6
		 * End Time6
		 * Day6
		 * Build6
		 * Room6
		 * Public Time			-	The (more nicely formatted) time that you actually show to people
		 * Public Building		-	The standard two-letter building code
		 * Public Room			-	The room in the building
		 * Instructor Ssn		-	Well...they're probably not going to give us that
		 * Instructor Name		-	Name of the instructor in LASTNAME, X.Y.Z. format
		 * Total Enrolled		-	4 digit number of how many students are in this class
		 * Coll1				-	The first college for listing enrollment.  This can be any of the
		 * 							normal 2-letter codes (AG, IL, AR, HO) but can also be
		 * 							categories such as UN for undergraduate or GR for graduate
		 * Enroll1				-	How many students enrolled in the class are from this college
		 * ...
		 * ...
		 * ...
		 * Coll25				-	I guess they really need 25 columns
		 * Enroll25				-	of enrollment data...woo
		 * Final Exam			-	The group in which this class takes its exam.  Doesn't exist
		 * 							for all classes, for some reason.  Maybe only parent classes?
		 * 							Anywho, this is a letter.  So far I only see A's and C's.
		 * Alias				-	Who knows???
		 * Grade Sheet Code		-	What it sounds like.  Mostly the same as Final Exam.
		 * Remarks1				-	This is a very badly worded and awfully formatted description
		 * ...					-	of the course.  It should really be avoided at all costs.
		 * Remarks6				-	Yuck.
		 * Section Status		-	???
		 * Xlist1...Xlist6		-	Crosslisted course IDs
		 * Special Program		-	?
		 * State Date			-	?
		 * End Date				-	?
		 * Filler				-	?
		 * Roster Print Override-	?
		 * Max Allowed			-	?
		 * Xlist Profile		-	Some sort of hashing for the crosslisting...its a HUGE number
		 * 
		 * ... there's more here but it's getting a little useless
		 */
		
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

            // 4/28/08: Make sure this is not a subclass
            if( !getLastRecordValue("Parent Cid").trim().isEmpty() )
                return next();

			// Create an entity
			EntityImpl entity = new EntityImpl( getLastRecordValue("Course Dept") + getLastRecordValue("Course Num") );


			entity.setValue( VALUEKEY_COURSEID, getLastRecordValue( "Cid" ) );
			entity.setValue( VALUEKEY_YEAR, getLastRecordValue( "Year" ) );
			entity.setValue( VALUEKEY_TERM, getLastRecordValue( "Term" ) );
			entity.setValue( VALUEKEY_COURSENUMBER, getLastRecordValue("Course Num") );
			
			// Set the number of credits
			String creditHours = getLastRecordValue("Credit Hours");
			if( creditHours != null && !(creditHours = creditHours.trim()).isEmpty() )
			{
				String variable = getLastRecordValue("Variable Hours Flag").equals("Y") ? " (variable)" : "";
				entity.setValue( VALUEKEY_CREDITHOURS, String.valueOf(Integer.parseInt(creditHours) / 100) + variable );
			}

			entity.setValue( VALUEKEY_DEPARTMENT, getLastRecordValue("Course Dept") );
			entity.setValue( VALUEKEY_COLLEGE, getLastRecordValue("Course College") );

			// Determine what kind of section this is; it should always succeed because the Course Sect Type field is always set
			String subcourseSectType = getLastRecordValue( "Subcourse Sect Type" ).trim();
			int courseSectTypeValue = Integer.parseInt( getLastRecordValue( "Course Sect Type" ).trim() );
			switch( courseSectTypeValue  )
			{
				// If the value is 1, this is either (a) a lecture, if the subcourse type isn't set, or (b) a lab or section,
				// as defined by the subcourse type
				case 1:
				{
					if( !subcourseSectType.isEmpty() )
					{
						// Get the value of the subcourse
						int subcourseSectTypeValue = Integer.parseInt( subcourseSectType );
						
						// Save the subclass type
						entity.setValue( VALUEKEY_CLASSINDEX, getLastRecordValue("Subcourse Sect Num") );
						
						// Depending on this value, set the type
						if( subcourseSectTypeValue == 2 )
						{
							entity.setValue( VALUEKEY_SECTIONTYPE_VCLASSURI, URI_VCLASS_CLASSSECTION );
						}
						else
						{
							entity.setValue( VALUEKEY_SECTIONTYPE_VCLASSURI, URI_VCLASS_CLASSLAB );
						}
					}
					else
					{
						entity.setValue( VALUEKEY_SECTIONTYPE_VCLASSURI, URI_VCLASS_CLASSLECTURE );
						entity.setValue( VALUEKEY_CLASSINDEX, getLastRecordValue("Course Sect Num") );
					}
				} break;
				
				// If this type is 2, this is a lab
				case 2:
				{
					entity.setValue( VALUEKEY_SECTIONTYPE_VCLASSURI, URI_VCLASS_CLASSLAB );
					entity.setValue( VALUEKEY_CLASSINDEX, getLastRecordValue("Course Sect Num") );
				} break;
				
				// If this type is 3, this is a section
				default: System.out.println( "Unknown section type:  " + String.valueOf( courseSectTypeValue ) + " for " + entity.getName() + "; creating as section" );
				case 3:
				{
					entity.setValue( VALUEKEY_SECTIONTYPE_VCLASSURI, URI_VCLASS_CLASSSECTION );
					entity.setValue( VALUEKEY_CLASSINDEX, getLastRecordValue("Course Sect Num") );
				} break;
			}

			// Build the spatial and temporal location of this class
			entity.setValue( VALUEKEY_LOCATION_BUILDINGCODE, getLastRecordValue( "Build1" ) );
			entity.setValue( VALUEKEY_LOCATION_ROOM, getLastRecordValue( "Room1" ) );
			entity.setValue( VALUEKEY_TIME, getLastRecordValue( "Public Time")  );
			
			// Get enrollment information
			entity.setValue( VALUEKEY_TOTALENROLLMENT, getLastRecordValue( "Total Enrolled" ) );
			
			// Load the parent CourseID number
			entity.setValue( VALUEKEY_PARENTCOURSEID, getLastRecordValue( "Parent Cid" ) );
			
			// Build a list of the crosslistings
			List<String> crosslistings = new ArrayList<String>();
			for( int i = 1; i <= 5; ++i )
			{
				// Build the name of the field
				String field = "Xlist" + String.valueOf( i );
				
				// Add to the list, if valid
				String value = getLastRecordValue( field );
				if( value != null ) value = value.trim();
				if( value != null && !value.isEmpty() )
					crosslistings.add( value );
			}
			
			// Add the crosslistings
			// Add the list of crosslistings to the entity
			try {
				if( !crosslistings.isEmpty() )
					entity.setValueList( VALUEKEY_CROSSLISTINGIDS, crosslistings, "," );
			} catch( Exception e )
			{
				System.out.println( "THIS SHOULD NEVER HAPPEN" );
			}

			// Get the grade option
			String gradeOption = getLastRecordValue( "Grade Opt" );
			if( gradeOption.equals( "E" ) )
			{
				/// TODO: I TOTALLY MADE THIS UP...IT NEEDS TO BE CONFIRMED!
				entity.setValue( VALUEKEY_GRADEOPTION, "S/U or Letter Grade" );
			}
			if( gradeOption.equals( "L" ) )
			{
				entity.setValue( VALUEKEY_GRADEOPTION, "Letter Only" );
			}
			else
			{
				/// TODO: I TOTALLY MADE THIS UP...IT NEEDS TO BE CHECKED!!!!
				entity.setValue( VALUEKEY_GRADEOPTION, "S/U" );
			}
			
			// Return the entity we created for this record in the database
			return entity;
		}

	}
}
