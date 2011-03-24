package edu.cornell.mannlib.ingest.kwinter;

// TODO: some courses are ingested multiple times because they exist on multiple course pages...weird huh

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;

/**
 * This class uses the Vivo ingest framework to read data from the flattened XML
 * course catalog catalog and put it into the database.
 * @author Karl Gluck
 */
public final class IngestDownloadedCourseCatalog
{
    // Default course catalog file to ingest
    public static final String DEFAULT_COURSE_CATALOG_FILE = "./vivocourse.txt";
    public static final String COURSES_WITH_PROBLEMS_OUTPUT_XML_FILE = "./courses_with_problems.xml";

    /// Default location for the connection properties
    public static final String DEFAULT_CONNECTION_PROPERTIES_FILE = "./config/connection.properties";

    public static final String URI_VCLASS_CANONICALCOURSE = "http://vivo.library.cornell.edu/ns/0.1#CanonicalCourse";
    public static final String URI_RELATIONSHIP_CANONICALCOURSE_CROSSLIST = "http://vivo.library.cornell.edu/ns/0.1#crossListedFromCanonicalCourse";
    public static final String URI_RELATIONSHIP_CANONICALCOURSE_TO_DEPARTMENT = "http://vivo.library.cornell.edu/ns/0.1#listedInDepartment";
    public static final String URI_RELATIONSHIP_DEPARTMENT_TO_CANONICALCOURSE = "http://vivo.library.cornell.edu/ns/0.1#listsCourse";
    public static final String URI_DPROP_CANONICALCOURSE_SEMESTERTIMING = "http://vivo.library.cornell.edu/ns/0.1#courseSemesterTiming";
    public static final String URI_DPROP_CANONICALCOURSE_CREDITS = "http://vivo.library.cornell.edu/ns/0.1#courseCredits";
    public static final String URI_DPROP_CANONICALCOURSE_INFO = "http://vivo.library.cornell.edu/ns/0.1#courseInfo";
    public static final String URI_DPROP_CANONICALCOURSE_GRADETYPE = "http://vivo.library.cornell.edu/ns/0.1#courseGradeOption";
    public static final String URI_DPROP_CANONICALCOURSE_PREREQUISITES = "http://vivo.library.cornell.edu/ns/0.1#coursePrerequisite";
    public static final String URI_DPROP_CANONICALCOURSE_TITLE = "http://vivo.library.cornell.edu/ns/0.1#courseTitle";
    public static final String URI_DPROP_CANONICALCOURSE_CODE3DIGIT = "http://vivo.library.cornell.edu/ns/0.1#courseCode3Digit";
    public static final String URI_DPROP_CANONICALCOURSE_CODE4DIGIT = "http://vivo.library.cornell.edu/ns/0.1#courseCode4Digit";

    // Unused URIs
    public static final String URI_RELATIONSHIP_CANONICALCOURSE_CROSSLIST_INV = "http://vivo.library.cornell.edu/ns/0.1#crossListedFromCanonicalCourse";

    /// These URIs are used to look up data inside of VIVO.  This first set is for linking departments.
    public static final String URI_DPROP_DEPARTMENT_INFO_CODE = "http://vivo.library.cornell.edu/ns/0.1#departmentcourseInfocode";
    public static final String URI_DPROP_DEPARTMENT_HR_CODE = "http://vivo.library.cornell.edu/ns/0.1#departmentHRcode";
    public static final String URI_DPROP_ACTIVITY_HR_CODE = "http://vivo.library.cornell.edu/ns/0.1#activityHRcode";
    public static final String URI_DPROP_CALS_IMPACT_DEPT_NAME = "http://vivo.library.cornell.edu/ns/0.1#CALSImpactDeptName";

    // Value Keys assined to ingested Course entities.  These are used to look up properties of the entity.
    public static final String VALUEKEY_SEMESTERSOFFERED = "SemestersOffered";
    public static final String VALUEKEY_TITLE = "Title";
    public static final String VALUEKEY_INFO = "Info";
    public static final String VALUEKEY_CREDITS = "Credits";
    public static final String VALUEKEY_GRADETYPE = "GradeType";
    public static final String VALUEKEY_PREREQUISITES = "Prerequisites";
    public static final String VALUEKEY_COREQUISITES = "Corequisites";
    public static final String VALUEKEY_NOTES = "Notes";
    public static final String VALUEKEY_DESCRIPTION = "Description";
    public static final String VALUEKEY_DEPARTMENT = "Department";
    public static final String VALUEKEY_CATALOGURL = "CatalogURL";
    public static final String VALUEKEY_3DIGITCOURSENUMBERS = "3DigitCN";
    public static final String VALUEKEY_4DIGITCOURSENUMBERS = "4DigitCN";
    public static final String VALUEKEY_CROSSLISTED_COURSE_CODES = "clstcc";

    public static final String URI_STRING_DATATYPE = "http://www.w3.org/2001/XMLSchema#string";

    // These aren't us
    public static final String VALUEKEY_COLLEGE = "College";

    // These are used for returning invalid entities; it is ignored unless an entity fails to ingest properly
    public static final String VALUEKEY_UNINGESTABLE_ENTITY_XML = "UnusableXML";



    /**
     * Ingests the course catalog from a flattened XML file
     * @param args The first index, if it exists, will be interpreted as a course catalog file;
     *             the second index, if it exists, will be the connection properties file
     */
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Course Catalog Ingest Tool\n" +
                            "==========================\n" +
                            "Initializing..." );

        // Just for debugging
        Runtime runtime = Runtime.getRuntime();
        System.out.println("\tMemory available to Java:\t" + runtime.maxMemory() / 1024);

        // Get the source file by either using the default, or whatever the user provided in the arguments
        String sourceFile = args.length >= 1 ? args[0] : DEFAULT_COURSE_CATALOG_FILE;
        String connectionPropertiesFile = args.length >= 2 ? args[1] : DEFAULT_CONNECTION_PROPERTIES_FILE;

        System.out.println("\tUsing source file:\t\t" + sourceFile );

        // Create the database connection
        VivoDatabaseConnection connection = new VivoDatabaseConnection( connectionPropertiesFile );

        // Load the ingest document
        IngestDocument document = IngestDocument.open( sourceFile );
        System.out.print( "\tLoading source file..." );
        if( document == null ) return;
        System.out.println( "succeeded" );

        // Information collected during the ingesting process
        int recordsRead = 0, recordsAggregated = 0;

        // Status message
        System.out.println( "\n\n=== Stage 1:  Pull Courses into Memory ===\n" );

        // The aggregator is used to hold courses as they are being brought in from the source file
        IngestAggregator aggregator = new IngestAggregator();
        VivoIngestDocument.Entity entity = null;
        long lastUpdateTime = System.currentTimeMillis();
        while( null != (entity = document.next()) )
        {
            // Add this entity to the aggregator
            try
            {
                aggregator.add( entity );
                recordsAggregated++;

            } catch( Exception e )
            {
                // Log this entity's XML data, and move on
                document.addUnusableEntityXML( entity.getValue( VALUEKEY_UNINGESTABLE_ENTITY_XML ) );
            }

            // Add to the number of entries
            recordsRead++;
        }

        // Close the ingest document
        document.close();

        // Output the list of entities that failed to ingest properly
        document.writeUnusableXMLEntitiesToFile( COURSES_WITH_PROBLEMS_OUTPUT_XML_FILE );

        // Status message
        System.out.println( "Stage 1 finished!  " + recordsAggregated + " courses read in "  +
                            String.valueOf( (int)Math.floor((System.currentTimeMillis() - lastUpdateTime) / (1000.0)) ) + " seconds, with " +
                            String.valueOf( document.getUnusableXMLEntities().size() ) + " unparsable entries, " +
                            String.valueOf( recordsRead - recordsAggregated ) + " uningestable entries (total of " +
                            String.valueOf( recordsRead + document.getUnusableXMLEntities().size() ) + " processed records)" );

        // Run stage 2 to set up relationships and entity URIs
        aggregator.executeStage2();

        // Run the final stage to commit everything to VIVO
        aggregator.executeStage3( connection );

        // Close the connection
        connection.disconnect();

        // Print a status message
        System.out.println( "Ingest successfully completed!" );
    }


    /**
     * Thoroughly searches VIVO for a department using the given department code
     * @param individualDao
     * @param departmentCode
     * @return
     */
    public static Individual findDepartmentByCode( IndividualDao individualDao, String department )
    {
        final String URI_DPROP_DEPARTMENTCODE_ARRAY[] =
        {
            URI_DPROP_DEPARTMENT_INFO_CODE,
            URI_DPROP_DEPARTMENT_HR_CODE,
            URI_DPROP_ACTIVITY_HR_CODE,
            URI_DPROP_CALS_IMPACT_DEPT_NAME,
        };

        String departmentVersions[] = { department, department.replace( " ", "" ) };
        List<Individual> vivoDepartmentList = null;
        for( int departmentIndex = 0; departmentIndex < 2 && (vivoDepartmentList == null || vivoDepartmentList.isEmpty()); ++departmentIndex )
            for( int uriSource = 0; uriSource < URI_DPROP_DEPARTMENTCODE_ARRAY.length && (vivoDepartmentList == null || vivoDepartmentList.isEmpty()); ++uriSource )
                for( int method = 0; method < 2 && (vivoDepartmentList == null || vivoDepartmentList.isEmpty()); ++method )
                {
                    switch( method )
                    {
                    case 0: vivoDepartmentList = individualDao.getIndividualsByDataProperty( URI_DPROP_DEPARTMENTCODE_ARRAY[uriSource], departmentVersions[departmentIndex] ); break;
                    case 1: vivoDepartmentList = individualDao.getIndividualsByDataProperty( URI_DPROP_DEPARTMENTCODE_ARRAY[uriSource], departmentVersions[departmentIndex], URI_STRING_DATATYPE, null ); break;
                    }
                }

        // If we found something, return it!
        if( vivoDepartmentList != null && !vivoDepartmentList.isEmpty() )
            return vivoDepartmentList.get(0);

        // If all else fails, return null
        return null;
    }

    private static abstract class CourseCode implements Comparable<CourseCode>
    {
        protected String myDepartment;
        protected String myNumber;

        public CourseCode( String department, String number )
        {
            myDepartment = department;
            myNumber = number;
        }

        public String getPrettyCode()
        {
            return myDepartment + " " + myNumber;
        }

        public String getURIFormattedCode()
        {
            try
            {
                // Encode the course number
                return URLEncoder.encode( myDepartment.replaceAll( " ", "" ) + myNumber, "UTF-8" );

            } catch( Exception e )
            {
                System.out.println( "This shouldn't EVER HAPPEN; if it does, something is wrong with URLEncoder & CourseCode" );
                return null;
            }
        }

        @Override
        public int compareTo( CourseCode rhs )
        {
            int department = myDepartment.compareTo( rhs.myDepartment );
            if( department != 0 ) return department;
            return myNumber.compareTo( rhs.myNumber );
        }

        /**
         * This function is CRUCIAL!!
         */
        @Override
        public boolean equals( Object rhs )
        {
            if( !(rhs instanceof CourseCode) ) return false;
            return 0 == compareTo( (CourseCode)rhs );
        }

        @Override
        public String toString()
        {
            return getPrettyCode();
        }

        /**
         * Adds the data property for this course code to the given Individual, which should be of the
         * @param connection
         * @param vivoCanonicalCourse
         */
        abstract void commitDataProperty( VivoDatabaseConnection connection, String canonicalCourseURI );
    }

    private static class CourseCode3Digit extends CourseCode
    {
        public CourseCode3Digit( String department, String number ) throws Exception
        {
            // Save the code
            super( department, number );

            // Make sure the number is valid
            if( Float.valueOf( number ) >= 1000.0 ) throw new Exception( "Three-digit course code assigned to " + department + " " + number );
        }

        @Override
        void commitDataProperty( VivoDatabaseConnection connection, String canonicalCourseURI )
        {
            // Add the department code to the course Individual
            connection.addDataProperty( canonicalCourseURI, URI_DPROP_CANONICALCOURSE_CODE3DIGIT, getPrettyCode() );
        }
    }

    private static class CourseCode4Digit extends CourseCode
    {
        public CourseCode4Digit( String department, String number ) throws Exception
        {
            // Save the code
            super( department, number );

            // Make sure the number is valid
            if( Float.valueOf( number ) < 1000.0 ) throw new Exception( "Four-digit course code assigned to " + department + " " + number );
        }

        @Override
        void commitDataProperty( VivoDatabaseConnection connection, String canonicalCourseURI )
        {
            // Add the department code to the course Individual
            connection.addDataProperty( canonicalCourseURI, URI_DPROP_CANONICALCOURSE_CODE4DIGIT, getPrettyCode() );
        }
    }

    /**
     * Stores a course that has been ingested from the source XML file while it is being sent to VIVO
     * @author Karl
     *
     */
    private static class CanonicalCourse
    {
        /// This is all of the course codes that directly reference this course
        protected List<CourseCode> myCourseCodes;

        /// These course codes are the codes of crosslisted classes
        protected List<CourseCode> myCrosslistedCourseCodes;

        /// This list is populated with references to the in-ingest-memory structures that store crosslisted courses
        protected Set<CanonicalCourse> myCrosslistedCourseReferences = new HashSet<CanonicalCourse>();

        /// Once the canonical course has been added to VIVO, this value stores its URI
        protected String myVivoIndividualURI;

        /// This is data from the entity used to set the value of data properties of the course Individual
        protected String mySemestersOffered, myTitle, myInfo, myCredits, myGradeType, myPrerequisites,
                         myCorequisites, myNotes, myDescription, myType, myDepartment, myCatalogURL;

        /**
         * Spawns this course from the given entity
         * @param entity
         */
        public CanonicalCourse( VivoIngestDocument.Entity sourceEntity ) throws Exception
        {
            mySemestersOffered  = sourceEntity.getValue( VALUEKEY_SEMESTERSOFFERED );
            myTitle             = sourceEntity.getValue( VALUEKEY_TITLE );
            myInfo              = sourceEntity.getValue( VALUEKEY_INFO );
            myCredits           = sourceEntity.getValue( VALUEKEY_CREDITS );
            myGradeType         = sourceEntity.getValue( VALUEKEY_GRADETYPE );
            myPrerequisites     = sourceEntity.getValue( VALUEKEY_PREREQUISITES );
            myCorequisites      = sourceEntity.getValue( VALUEKEY_COREQUISITES );
            myNotes             = sourceEntity.getValue( VALUEKEY_NOTES );
            myDescription       = sourceEntity.getValue( VALUEKEY_DESCRIPTION );
            myDepartment        = sourceEntity.getValue( VALUEKEY_DEPARTMENT );
            myCatalogURL        = sourceEntity.getValue( VALUEKEY_CATALOGURL );

            // Grab the course numbers from the entity
            List<String> threeDigitCourseNumbers = sourceEntity.getValueList( VALUEKEY_3DIGITCOURSENUMBERS, "," );
            List<String> fourDigitCourseNumbers = sourceEntity.getValueList( VALUEKEY_4DIGITCOURSENUMBERS, "," );


            // Add new course codes to the internal list
            myCourseCodes = new ArrayList<CourseCode>();
            for( String threeDigit : threeDigitCourseNumbers ) myCourseCodes.add( new CourseCode3Digit( myDepartment, threeDigit ) );
            for( String fourDigit  : fourDigitCourseNumbers  ) myCourseCodes.add( new CourseCode4Digit( myDepartment, fourDigit  ) );

            // Create a URI for this course
            myVivoIndividualURI = VivoDatabaseConnection.BASE_URI + "cornellCourse" + myDepartment;
            if( threeDigitCourseNumbers.size() > 0 ) myVivoIndividualURI += "_" + threeDigitCourseNumbers.get(0).replaceAll( " *", "" );
            if( fourDigitCourseNumbers.size() > 0 )  myVivoIndividualURI += "_" + fourDigitCourseNumbers.get(0).replaceAll( " *", "" );

            // Add all of the crosslisted codes
            myCrosslistedCourseCodes = new ArrayList<CourseCode>();
            List<String> crosslistedCourseCodes = sourceEntity.getValueList( VALUEKEY_CROSSLISTED_COURSE_CODES, "," );
            for( String code : crosslistedCourseCodes )
            {
                // Extract components; should be either DEPT|### or DEPT|###|####
                String components[] = code.split( "\\|" );

                // Get rid of invalid codes
                if( components.length < 2 || components.length > 3 ) throw new Exception( "Invalid crosslisted code:  " + code );

                // Get the departments
                String department = components[0];

                // Add the three-digit code always (it's guaranteed to exist)
                myCrosslistedCourseCodes.add( new CourseCode3Digit( department, components[1] ) );

                // If the four-digit code exists, add it too!
                if( components.length > 2 )
                    myCrosslistedCourseCodes.add( new CourseCode4Digit( department, components[2] ) );
            }
        }

        /**
         * This method is called during stage 1 to set up the map of course IDs to course objects.
         * @param ingestedCourses
         */
        public void addSelfToIngestedCourses( Map<String,CanonicalCourse> courseCodeToCourseMap )
        {
            for( CourseCode courseCode : myCourseCodes )
                courseCodeToCourseMap.put( courseCode.getURIFormattedCode(), this );
        }

        /**
         * This method is called if this course is a duplicate of one already in the ingest so that
         * only one course exists for a given code; this method copies "more-relavent" data into
         * the existing course.
         * @param courseCodeToCourseMap
         */
        public void mergeWithExisting( Map<String,CanonicalCourse> courseCodeToCourseMap ) throws Exception
        {
        	// Get the course
        	CanonicalCourse otherCourse = null;
        	Iterator<CourseCode> iterator = myCourseCodes.iterator();
    		while( otherCourse == null && iterator.hasNext() )
    			otherCourse = courseCodeToCourseMap.get( iterator.next() );

    		// If the course wasn't found, this is a problem since it's supposed to exist
    		// in the database!
    		if( otherCourse == null ) throw new Exception( "mergeIntoExisting called for course with no existing partner!" );
    		
    		// Do the merge
    		mergeWith( otherCourse );
        }
        
        /**
         * Exchanges data between two class instances that are supposed to represent the same
         * logical course. 
         * @param otherCourse
         */
        public void mergeWith( CanonicalCourse otherCourse )
        {
        	// Handle the description text
        	boolean myDescriptionNull = myDescription == null,
        		 otherDescriptionNull = otherCourse.myDescription == null;
        	if( myDescriptionNull && !otherDescriptionNull )
        		myDescription = otherCourse.myDescription;
        	if( !myDescriptionNull && otherDescriptionNull )
        		otherCourse.myDescription = myDescription;
        	if( !myDescriptionNull && !otherDescriptionNull)
        	{
        		boolean myDescriptionComplete = !myDescription.matches("[Ff]or description.*"),
        		     otherDescriptionComplete = !otherCourse.myDescription.matches("[Ff]or description.*");
        		if( myDescriptionComplete && !otherDescriptionComplete )
        			otherCourse.myDescription = myDescription;
        		if( !myDescriptionComplete && otherDescriptionComplete )
        			myDescription = otherCourse.myDescription;

        		if( myDescriptionComplete && otherDescriptionComplete )
        		{
        			if( myDescription.length() > otherCourse.myDescription.length() )
        				otherCourse.myDescription = myDescription;
        			else
        				myDescription = otherCourse.myDescription;
        		}
        	}
        	
        	// Make sure that all of the course codes are shared
        	for( CourseCode code : myCourseCodes )
        		if( !otherCourse.myCourseCodes.contains(code) )
        			otherCourse.myCourseCodes.add( code );
        	myCourseCodes.clear();
        	myCourseCodes.addAll( otherCourse.myCourseCodes );

        	// Make sure that all of the course crosslistings are shared
        	for( CourseCode crosslistingCode : myCrosslistedCourseCodes )
        		if( !otherCourse.myCrosslistedCourseCodes.contains( crosslistingCode ) )
        			otherCourse.myCrosslistedCourseCodes.add( crosslistingCode );
        	myCrosslistedCourseCodes.clear();
        	myCrosslistedCourseCodes.addAll( otherCourse.myCrosslistedCourseCodes );
        }

        /**
         * Finds out whether or not at least one of the course codes in the provided list is
         * contained in this course's crosslisting
         * @param courseCodeList
         * @return
         */
        public boolean areAnyOfTheseCourseCodesCrosslisted( List<CourseCode> courseCodeList )
        {
            for( CourseCode courseCode : courseCodeList )
                if( isCourseCodeCrosslisted( courseCode ) ) return true;
            return false;
        }

        /**
         * Determines whether or not this course contains the given course code as a crosslisting
         * @param courseCode The course code to check
         * @return Whether or not the code is in the crosslisting
         */
        public boolean isCourseCodeCrosslisted( CourseCode courseCode )
        {
            return myCrosslistedCourseCodes.contains( courseCode );
        }
        
        @Override
        public boolean equals( Object other )
        {
        	if( !(other instanceof CanonicalCourse) ) return false;
        	CanonicalCourse otherCourse = (CanonicalCourse)other;
        	
        	// Compare codes
        	for( CourseCode code : myCourseCodes )
        		if( otherCourse.myCourseCodes.contains(code) ) return true;

        	return false;
        }

        /**
         *
         * @param ingestedCourses
         */
        public void establishCrosslistedCourseReferences( Map<String,CanonicalCourse> courseCodeToCourseMap ) throws Exception
        {
            // Repeat for all of the crosslisted codes
            for( CourseCode courseCode : myCrosslistedCourseCodes )
            {
                // Find the crosslisted course in the map
                CanonicalCourse crosslistedCourse = courseCodeToCourseMap.get( courseCode.getURIFormattedCode() );

                // Make sure the course is valid
                if( crosslistedCourse == null )
                {
                    // Warn about this
                    //System.out.println( "Crosslisted course " + courseCode + " for " + toString() + " wasn't ingested, so its being" +
                    //                  "added as an ID for this course" );

                    // Add the ID to this class
                    myCourseCodes.add( courseCode );
                    // TODO: we don't remove this code from the crosslisting codes, because we're iterating through them
                    // right now...is this a problem?

                    // Move on to the next entry
                    continue;
                }

                // Add this entry to the references; this is a Set because ALL of the course
                // codes for a course are added, and if multiple crosslistings point to the
                // same course, we don't want to establish multiple links with that course.
                myCrosslistedCourseReferences.add( crosslistedCourse );

                // Make sure that this relationship is reciprocal
                if( !crosslistedCourse.areAnyOfTheseCourseCodesCrosslisted( myCourseCodes ) )
                {
                    // Log the fact that we're fixing this entry; it's important to know if there are logical
                    // errors in reciprocity because that might indicate a problem with the ingest process
                    System.out.println( "Added crosslisting to course " + crosslistedCourse + ", which is crosslisted by " + toString() + " without a reciprocal relationship" );

                    // Add to the other course's crosslisting array.  We don't just add to the
                    // crosslisted course codes, because that course may already have established
                    // its crosslisted course references.  If this fails, we don't have to worry--it was probably
                    // linked by a prior call.
                    crosslistedCourse.myCrosslistedCourseReferences.add( this );
//                  if( !crosslistedCourse.myCrosslistedCourseReferences.add( this ) )
//                      throw new Exception( "Crosslisted course" + crosslistedCourse + " doesn't contain " + toString() + " in its " +
//                                           "set of course codes, but somehow has already listed it in the crosslisted course relationships!" );
                }
            }

            // From all of the crosslisted courses, find the one with the FULL description text, and copy that description
            // TODO: this can be significantly shortened by only executing once; also, the code is very similar
            // to that used during mergeWith, so maybe make this a method of CanonicalCourse?
            for( CanonicalCourse crosslistedCourse : myCrosslistedCourseReferences )
            {
                if( myDescription != null && crosslistedCourse.myDescription != null &&
                    (!crosslistedCourse.myDescription.matches("[Ff]or description.*")) &&
                    crosslistedCourse.myDescription.length() > myDescription.length() )
                    myDescription = crosslistedCourse.myDescription;
            }
        }

        /**
         * Adds the crosslistings for this class to the Vivo database
         * @param connection
         */
        public void commitCrosslistingRelationships( VivoDatabaseConnection connection )
        {
            for( CanonicalCourse crosslistedCourse : myCrosslistedCourseReferences )
                connection.addObjectProperty( myVivoIndividualURI, URI_RELATIONSHIP_CANONICALCOURSE_CROSSLIST, crosslistedCourse.myVivoIndividualURI );
        }

        /**
         *
         * @param individualDao
         */
        public void destroyConflictingIndividual( IndividualDao individualDao )
        {
            // Try to obtain this individual
            Individual vivoIndividual = individualDao.getIndividualByURI( myVivoIndividualURI );
            if( vivoIndividual != null ) individualDao.deleteIndividual( myVivoIndividualURI );
        }

        /**
         *
         * @param connection
         * @param individualDao
         * @throws Exception
         */
        public void createVivoIndividual( VivoDatabaseConnection connection, IndividualDao individualDao ) throws Exception
        {
            // Create the course object and give it some initial data
            Individual vivoIndividual = new IndividualImpl();
            vivoIndividual.setVClassURI( URI_VCLASS_CANONICALCOURSE );
            vivoIndividual.setName( myTitle );
            vivoIndividual.setDescription( myDescription );
            vivoIndividual.setURI( myVivoIndividualURI );

            // Set the anchor text for this individual
            vivoIndividual.setAnchor( myCourseCodes.get(0).getPrettyCode() + " Catalog Page" );
            vivoIndividual.setUrl( myCatalogURL );

            // Create this individual in Vivo
            vivoIndividual = individualDao.getIndividualByURI( individualDao.insertNewIndividual( vivoIndividual ) );

            // Get the individual's URI back from the database
            myVivoIndividualURI = vivoIndividual.getURI();
        }

        /**
         * Adds the data properties for this course to VIVO
         * @param connection
         */
        public void commitDataProperties( VivoDatabaseConnection connection )
        {
            // Add data properties to the course individual
            connection.addDataProperty( myVivoIndividualURI, URI_DPROP_CANONICALCOURSE_SEMESTERTIMING, mySemestersOffered );
            connection.addDataProperty( myVivoIndividualURI, URI_DPROP_CANONICALCOURSE_TITLE, myTitle );
            connection.addDataProperty( myVivoIndividualURI, URI_DPROP_CANONICALCOURSE_INFO, myInfo );

            // These have been disabled because it's difficult to parse them correctly
            //connection.addDataProperty( myVivoIndividualURI, URI_DPROP_CANONICALCOURSE_CREDITS, myCredits );
            //connection.addDataProperty( myVivoIndividualURI, URI_DPROP_CANONICALCOURSE_GRADETYPE, myGradeType );
            //connection.addDataProperty( myVivoIndividualURI, URI_DPROP_CANONICALCOURSE_PREREQUISITES, myPrerequisites );

            // Add course codes for this class
            for( CourseCode courseCode : myCourseCodes )
                courseCode.commitDataProperty( connection, myVivoIndividualURI );
        }

        /**
         *
         * @param connection
         */
        public void commitDepartmentRelationship( VivoDatabaseConnection connection )
        {
            Individual vivoDepartment = IngestDownloadedCourseCatalog.findDepartmentByCode( connection.getDaoFactory().getIndividualDao(), myDepartment );
            if( vivoDepartment != null )
            {
                // Only link to the first department with this code (there should only be one)
                connection.addObjectProperty( vivoDepartment.getURI(), URI_RELATIONSHIP_DEPARTMENT_TO_CANONICALCOURSE, myVivoIndividualURI );
                connection.addObjectProperty( myVivoIndividualURI, URI_RELATIONSHIP_CANONICALCOURSE_TO_DEPARTMENT, vivoDepartment.getURI() );

                Individual course = connection.getDaoFactory().getIndividualDao().getIndividualByURI(myVivoIndividualURI);
                if( course != null ) {
                    //this is what should happen:
                    //course.setFlag1Numeric( vivoDepartment.getFlag1Numeric() | course.getFlag1Numeric() );

                    course.setFlag1Set( vivoDepartment.getFlag1Set() );
                    connection.getDaoFactory().getIndividualDao().updateIndividual( course );

                }else{
                    System.out.println("could not get course so flag1 not set");
                }
            }
            else
            {
                // TODO: add entity to list to link dept. manually later
            }
        }

        @Override
        public String toString()
        {
            return myTitle + " " + myCourseCodes.toString();
        }
    }

    /**
     * The aggregator assists in the ingest process, which occurs in several stages:
     *  1- Get data from the downloaded course catalog XML file
     *  2- Create entities in VIVO for all of the courses
     *  3- Add crosslisting relationships for all of the entities
     * @author Karl
     */
    private static class IngestAggregator
    {
        protected Map<String,CanonicalCourse> myCourseCodeToIngestedCoursesMap = new HashMap<String,CanonicalCourse>();
        protected Set<CanonicalCourse> myIngestedCourses = new HashSet<CanonicalCourse>();

        /**
         * Adds a new course to this ingested courses based on entity data
         * @param entity
         */
        public void add( VivoIngestDocument.Entity entity ) throws Exception
        {
            // Create the course
            CanonicalCourse course = new CanonicalCourse( entity );

            // Add it to the set
            if( myIngestedCourses.add( course ) )
            {
                // Add this to the map
                course.addSelfToIngestedCourses( myCourseCodeToIngestedCoursesMap );
            }
            else
            {
            	// Merge into the course we currently have
            	course.mergeWithExisting( myCourseCodeToIngestedCoursesMap );

                System.out.println( "[Warning]: Course " + course.toString() + " is a duplicate" );
            }
        }

        /**
         * Stage 2 of the ingest process is responsible for linking all of the courses together that have crosslisting relationships.
         * This is a separate stage because it requires that all of the courses that are going to be in the database are in memory, so
         * we can't execute this while the courses are still being aggregated.
         * @throws Exception
         */
        public void executeStage2() throws Exception
        {
            long lastUpdateTime = System.currentTimeMillis();

            System.out.println( "Course Codes: " + String.valueOf( myCourseCodeToIngestedCoursesMap.size() ) );

            for( CanonicalCourse course : myIngestedCourses )
                course.establishCrosslistedCourseReferences( myCourseCodeToIngestedCoursesMap );

            System.out.println( "Stage 2 finished establishing crosslisting relationships after " +
                                String.valueOf( (int)Math.floor((System.currentTimeMillis() - lastUpdateTime) / (1000.0)) ) + " seconds" );
        }

        /**
         * Stage 3 pushes all of the data we have in the aggregator to the VIVO database.  It is the final
         * stage of the ingest process.
         * @param connection
         * @throws Exception
         */
        public void executeStage3( VivoDatabaseConnection connection ) throws Exception
        {
            // Duration monitor
            long lastUpdateTime = System.currentTimeMillis();

            // Get the DAO used to control Individuals
            IndividualDao individualDao = connection.getDaoFactory().getIndividualDao();

// TODO: this only needs to be done if we're ingesting into a database with courses in it already
//          System.out.println( "Now removing conflicting courses from database" );
//
//          // Get rid of all of the individuals that could conflict with ones we are ingesting
          for( CanonicalCourse course : myIngestedCourses )
              course.destroyConflictingIndividual( individualDao );

          System.out.println( "Finished searching for conflicting courses; creating new courses in database" );

            // Create all of the individuals
            long courseCounter = 0;
            for( CanonicalCourse course : myIngestedCourses )
            {
                // Generate the individual
                course.createVivoIndividual( connection, individualDao );

                // Create the data properties for this individual, including course codes
                course.commitDataProperties( connection );

                // Link to the department
                course.commitDepartmentRelationship( connection );

                // If this is taking a long time, print a message
                if( courseCounter % 500 == 499 )
                {
                    System.out.println( String.valueOf( courseCounter ) + " course entities created (" +
                                        String.valueOf(courseCounter / (double)myIngestedCourses.size()*100) + "% done)" );
                }
                courseCounter++;
            }

            System.out.println( "Stage 3: finished creating new courses after " +
                    String.valueOf( (int)Math.ceil((System.currentTimeMillis() - lastUpdateTime) / (1000.0) / 60.0) ) + " minutes" );

            // Link crosslisted individuals
            for( CanonicalCourse course : myIngestedCourses )
                course.commitCrosslistingRelationships( connection );

            // Let the user know that we're done!
            System.out.println( "Completed stage 3 after " +
                    String.valueOf( (int)Math.ceil((System.currentTimeMillis() - lastUpdateTime) / (1000.0) / 60.0) ) + " minutes" );
        }
    }



    /**
     * This is the specific document class that is used to ingest the course catalog
     * @author Karl Gluck
     */
    private static class IngestDocument extends XMLIngestDocument
    {
        /// The iterator used to look through root elements
        protected Iterator<Element> myRootIterator;

        /// The department codes for classes at Cornell
        /// @note These are obtained from FA07.xls.  The following were added:
        ///     AN SC, P ED, BIOGD, BIOS, STSCI, A&EP, T&AM, VISST, VETMI, VETMM, VTPMD
        ///     HE, LAW, NCC, NRE, NMI, MIL, NAV, PE, OUTED, VETCS, VTBMS,
        ///     SWED, B&SOC, IM, JWST, WRIT, LSP, S HUM, RELST, CATAL, S&TS,
        ///     VISST, SYSEN, VTMED, EDUC, FD SC, AIS, IARD, LA, SNES, AAP, CHEM, COLLS, ILR
        ///     NAV S, STBTRY, VETPH, NS&E

        protected static final String DEPARTMENT_CODES[] = {

            "A&EP", "AAP", "AAS", "AEM", "AIR S", "AIS", "AGSCI", "ALS", "AM ST", "AMST", "AN SC", "ANTHR", "ARCH", "ARKEO", "ART", "ART H",
            "AS&RC", "ASIAN", "ASTRO", "B&SOC", "BEE", "BENGL", "BIO G", "BIO&S", "BIOAP", "BIOBM", "BIOEE", "BIOGD",
            "BIOMI", "BIONB", "BIOPL", "BIOS", "BME", "BTRY", "BURM", "CAPS", "CATAL", "CEE", "CHEM", "CHEME", "CHIN",
            "CHLIT", "CIS", "CLASS", "COGST", "COLLS", "COM L", "COMM", "CRP", "CS", "CSS", "CZECH", "D SOC", "DSOC",
            "DANCE", "DEA", "DUTCH", "EAS", "ECE", "ECON", "EDUC", "ENGL", "ENGLB", "ENGLF", "ENGRC",
            "ENGRD", "ENGRG", "ENGRI", "ENTOM", "FD SC", "FGSS", "FGSs", "FILM", "FREN", "FRLIT", "FSAD", "GERST",
            "GOVT", "GREEK", "H ADM", "HD", "HE", "HINDI", "HIST", "HORT", "HUNGR", "IARD", "ILR", "ILRCB",
            "ILRHR", "ILRIC", "ILRID", "ILRLE", "ILROB", "ILRST", "IM", "INDO", "INFO", "ITAL",
            "JAPAN", "JPLIT", "JWST", "KHMER", "KOREA", "KRLIT", "LA", "LANAR", "LAT A", "LATIN", "LAW", "LING", "LASP", "LSP", "M&AE", "MATH",
            "MEDVL", "MIL", "MIL S", "MS&E", "MUSIC", "NAV", "NAV S", "NBA", "NCC", "NEPAL", "NES", "NMI", "NRE", "NS", "NS&E",
            "NTRES", "OR&IE", "OUTED", "P ED", "PALI", "PAM", "PE", "PHIL", "PHYS", "PL BR", "PL PA",
            "POLSH", "PORT", "PSYCH", "QUECH", "RELST", "ROM S", "RUSSA", "RUSSL", "S HUM", "S&TS", "SANSK",
            "SEBCR", "SINHA", "SNES", "SNLIT", "SOC", "SPAN", "STBTRY", "STSCI", "SWED", "SYSEN", "T&AM", "TAG", "TAMIL",
            "THAI", "THETR", "TOX", "UKRAN", "URDU", "VISST", "VETCS", "VETMI", "VETMM", "VIET", "VISST", "VETPH", "VTBMS", "VTLIT", "VTMED", "VTPMD", "WRIT"
        };

        /**
         * Creates an ingest document for the provided source file
         * @param sourceFile
         * @return
         */
        public static IngestDocument open( String sourceFile )
        {
            // Load the ingest document
            IngestDocument document = null;
            try {
                document = new IngestDocument( sourceFile );
            } catch( Exception e )
            {
                // Print a warning message
                System.out.println( "Unable to open \"" + sourceFile + "\" to read the course catalog (or there was an error)" );
                e.printStackTrace();

                // Exit
                return null;
            }

            // Return the document that was generated
            return document;
        }

        /**
         * Initializes the course ingest document to read from a given file
         * @param sourceFile The XML file that contains course data
         * @throws DocumentException
         * @throws FileNotFoundException
         */
        protected IngestDocument( String sourceFile ) throws DocumentException, FileNotFoundException
        {
            // Initialize this document
            super( new FileReader( sourceFile ) );

            // Reset members
            myRootIterator = null;
            myUnusableXMLEntities = new ArrayList<UnusableEntityXML>();
        }

        /**
         * Returns the correctly-spelled and formatted department code from the code provided.  This
         * is necessary because the course catalog is really inconsistent with naming.
         * @param code
         * @return
         */
        public static String correctDepartmentCodeSpelling( String code )
        {
            if( code.equals( "FGSs" ) ) return "FGSS";
            return code;
        }

        /**
         * Removes allocated data used by the document and breaks connections with data sources
         */
        @Override
        public void close()
        {
            // Get rid of the iterator
            myRootIterator = null;

            // Close the XML document
            super.close();
        }

        /**
         * Looks through the beginning of the parameter string for a department code and
         * returns which code, if any, was found.  If no department was found, the return
         * code is -1.
         * @param searchString The string in which to look for the department
         * @return The index of the department found, or -1
         */
        protected static int findDepartmentCode( String searchString )
        {
            List<Integer> departments = new ArrayList<Integer>();
            int searchStringLength = searchString.length();

            // Add all of the departments that match
            for( int i = 0; i < DEPARTMENT_CODES.length; ++i )
            {
                // The current department being analyzed
                final String dept = DEPARTMENT_CODES[i];

                // If this department is in the string, add the index to the list
                if( searchStringLength >= dept.length() &&
                    searchString.substring( 0, dept.length() ).equals( DEPARTMENT_CODES[i] ) )
                    departments.add( new Integer(i) );
            }

            // Find the longest match and return it
            int longestIndex = -1, longestLength = 0;
            for( Integer deptIndex : departments  )
            {
                // The department being analyzed
                final String dept = DEPARTMENT_CODES[deptIndex.intValue()];

                // If this department name is longer, save it
                if( dept.length() > longestLength )
                {
                    longestLength = dept.length();
                    longestIndex = deptIndex.intValue();
                }
            }

            // Return the index
            return longestIndex;
        }

        /**
         * Resets the document read point to the start of the document
         */
        public void reset()
        {
            myRootIterator = null;
        }


        public static String[] resplitEntriesByRemovingMatchingGroup1Only( String[] array, String regex )
        {
            Pattern p = Pattern.compile( regex );
            List<String> output = new ArrayList<String>();
            for( String entry : array )
                output.addAll( splitForMatchesRemoveGroup1Only( entry, p ) );
            return output.toArray(new String[0]);
        }

        public static List<String> splitForMatchesRemoveGroup1Only( String string, String regex )
        {
            return splitForMatchesRemoveGroup1Only( string, Pattern.compile( regex ) );
        }

        public static List<String> splitForMatchesRemoveGroup1Only( String string, Pattern p )
        {
            List<String> output = new ArrayList<String>();

            // Make sure the string isn't empty
            if( string.isEmpty() ) return output;

            // Do the search
            Matcher m = p.matcher( string );
            int start = 0;
            while( m.find( start ) )
            {
                // Add the start to the output array
                output.add( string.substring( start, m.start(1) ) );

                // Move the start pointer
                start = m.end() + 1;
            }

            // Add whatever's left
            String remainder = string.substring( start );
            if( !remainder.isEmpty() ) output.add( remainder );

            // Return the result
            return output;
        }

        public void addUnusableEntityXML( String entityXML )
        {
            myUnusableXMLEntities.add( new UnusableEntityXML( entityXML ) );
        }

        /// This list of entities failed parsing
        protected List<UnusableEntityXML> myUnusableXMLEntities;

        /**
         * Obtains the list of XML entities that failed parsing
         * @return List of UnusableXMLEntity objects
         */
        public List<UnusableEntityXML> getUnusableXMLEntities() { return myUnusableXMLEntities; }

        public void writeUnusableXMLEntitiesToFile( String file )
        {
            try {
                BufferedWriter bw = new BufferedWriter( new FileWriter( file ) );
                for( UnusableEntityXML ety2 : myUnusableXMLEntities )
                    bw.write( ety2.getXML() + "\n" );
                bw.close();
            }
            catch( IOException e )
            {
                System.out.println( "Couldn't write " + myUnusableXMLEntities.size() + " to " + file + "; entities with problems WONT BE RECOVERABLE!" );
                e.printStackTrace();
            }

        }

        /**
         * An entry of this class type is stored in a list for each XML entity that couldn't be parsed
         * correctly during the ingest process.  Use saveUnusableXMLEntityAndReturnNext during the
         * next() method to take care of error reporting and continuing to parse objects from the document.
         * @author Karl
         */
        public class UnusableEntityXML
        {
            protected String myXML;

            public String getXML() { return myXML; }

            /**
             * Initializes this class
             * @param entity The entity that failed parsing
             * @param errorDescription Why the entity couldn't be parsed
             */
            public UnusableEntityXML( Entity xmlEntity, String errorDescription )
            {
                String unparsedTitle    = xmlEntity.getValue( "UnparsedTitle" );
                String title            = xmlEntity.getValue( "Title" );
                String info             = xmlEntity.getValue( "Info" );
                String college          = xmlEntity.getValue( "College" );
                String number           = xmlEntity.getValue( "Number" );
                String department       = xmlEntity.getValue( "Department" );
                String crosslistings    = xmlEntity.getValue( "Crosslistings" );
                String description      = xmlEntity.getValue( "Description" );
                String catalogURL       = xmlEntity.getValue( "SourceURL" );

                // Save these entries into the XML member
                myXML = "<Course>\n" +
                        "<Error><![CDATA["          + errorDescription +"]]></Error>\n" +
                        "<UnparsedTitle><![CDATA["  + unparsedTitle +   "]]></UnparsedTitle>\n" +
                        "<Title><![CDATA["          + title +           "]]></Title>\n" +
                        "<Info><![CDATA["           + info +            "]]></Info>\n"+
                        "<Description><![CDATA["    + description +     "]]></Description>\n" +
                        "<College><![CDATA["        + college +         "]]></College>\n" +
                        "<Number><![CDATA["         + number +          "]]></Number>\n" +
                        "<Department><![CDATA["     + department +      "]]></Department>\n" +
                        "<Crosslistings><![CDATA["  + crosslistings +   "]]></Crosslistings>\n" +
                        "<SourceURL><![CDATA["      + catalogURL +      "]]></SourceURL>\n" +
                        "</Course>\n";
            }

            /**
             * Initializes this class using preformatted XML code
             * @param xml The XML that the entity was created from
             */
            public UnusableEntityXML( String xml )
            {
                myXML = xml;
            }
        }

        /**
         * This function does the work of actually processing entities from the XML file.  It's written as a separate method
         * because this process involves lots of operations which could fail.  By wrapping this method in a try { } block,
         * the next() method can, in a single step, make a list of all of the entities which fail processing.
         * @param xmlEntity The entity generated data parsed from the XML file structure
         * @return An entity that has all of the fields necessary to return a fully-parsed course from next()
         * @throws Exception If something goes wrong
         */
        public Entity parseXMLEntity( Entity xmlEntity ) throws Exception
        {
            // Get the attributes so we can refactor them
            // xmlEntity.getValue( "UnparsedTitle" ) is the raw title from the document without information pulled from it.
            String title = xmlEntity.getValue( "Title" );
            String info = xmlEntity.getValue( "Info" );
            String college = xmlEntity.getValue( "College" );
            String number = xmlEntity.getValue( "Number" );
            String department = xmlEntity.getValue( "Department" );
            String crosslistings = xmlEntity.getValue( "Crosslistings" );
            String description = xmlEntity.getValue( "Description" );
            String catalogURL = xmlEntity.getValue( "SourceURL" );

            // Skip null entries
            if( title == null || title.isEmpty() )
                throw new Exception( "The entity spawned from " + catalogURL + " is invalid and could not be ingested" );

            // Create the entity
            EntityImpl entity = new EntityImpl( title );
            entity.setValue( VALUEKEY_INFO, info );
/*
            // Find the number of credits this class can be
            if( info != null )
            {
                Matcher m = Pattern.compile( "([0-9](?:.[0-9])?(?: *[Vv]ariable *)? *[Cc]redit[^\\.]*)\\. *" ).matcher( info );
                if( m.find() )
                {
                    // We found a number of credits!  Hooray
                    entity.setValue( VALUEKEY_CREDITS, m.group(1).trim() );

                    // Remove the number of credits from the info
                    info = info.substring( 0, m.start() ) + info.substring( m.end() );
                    info = info.trim();
                }
            }

            // Find the semesters in which the course is offered
            if( info != null )
            {
                List<String> semestersOffered = new ArrayList<String>();
                final String semesterPattern = "(?:(?:[Ss]ummer)|(?:[Ww]inter)|(?:[Ff]all)|(?:[Ss]pring))";

                // Look for the sentence containing the offered semesters
                Matcher m = Pattern.compile( "(" + semesterPattern + "[^\\.]*)\\. *" ).matcher( info );

                // If we found the sentence, look up the semesters; otherwise, just use default ones
                if( m.find() )
                {
                    // The sentence in which the semesters during which the class is offered exists
                    int sentenceStart = m.start(), sentenceEnd = m.end();
                    String semestersOfferedSentence = m.group(1);

                    // Build a new matcher to look for semesters
                    m = Pattern.compile( "(" + semesterPattern + ")" ).matcher( semestersOfferedSentence );
                    int startIndex = 0;
                    while( m.find( startIndex ) )
                    {
                        semestersOffered.add( m.group(1).toLowerCase() );
                        startIndex = m.end();
                    }

                    // Remove this area from the info block
                    info = info.substring( 0, sentenceStart ) + info.substring( sentenceEnd );
                    info = info.trim();
                }
                else
                {
                    // By default, all classes are offered fall & spring
                    semestersOffered.add( "Fall" );
                    semestersOffered.add( "Spring" );
                }

                // Add the semesters that this class is offered
                entity.setValueList( VALUEKEY_SEMESTERSOFFERED, semestersOffered, "," );
            }

            // Get the grading type
            if( info != null )
            {
                Matcher m = Pattern.compile( "((?:Letter grades only)|(?:S\\-U or letter grades)|(?:S\\-U grades only))\\. *" ).matcher( info );
                if( m.find() )
                {
                    entity.setValue( VALUEKEY_GRADETYPE, m.group(1).trim() );

                    // Remove this area from the info block
                    info = info.substring( 0, m.start() ) + info.substring( m.end() );
                    info = info.trim();
                }
            }

            // Get the prerequisite information from the listing
            if( info != null )
            {
                // The second part of this fun little pattern means "look for text while you either (a) haven't
                // encountered a period or (b) you counter a period but it's followed by a non-whitespace character.
                Pattern p = Pattern.compile( "[Pp]re\\-?requisites?\\:((?:[^\\.]|(?:\\.\\S))*)\\." );
                Matcher m = null;
                if( (m = p.matcher(info)).find() )
                {
                    // Save the prerequisites
                    entity.setValue( VALUEKEY_PREREQUISITES, m.group(1).trim() );

                    // Remove from the info
                    info = info.substring( 0, m.start() ) + info.substring( m.end() );
                    info = info.trim();
                }
            }

            // Get the corequisite information
            if( info != null )
            {
                Pattern p = Pattern.compile( "(?:[Pp]re\\-? or )?[Cc]o\\-?requisites?\\:((?:[^\\.]|(?:\\.\\S))*)\\." );
                Matcher m = null;
                if( (m = p.matcher(info)).find() )
                {
                    // Save the prerequisites
                    entity.setValue( VALUEKEY_COREQUISITES, m.group(1).trim() );

                    // Remove from the info
                    info = info.substring( 0, m.start() ) + info.substring( m.end() );
                    info = info.trim();
                }
            }*/

            // Whatever's left in the "info" string at this point is considered "notes"
            // TODO: At some point, we'll get rid of "info" entirely, since it just says the same thing as the
            // other fields.
            entity.setValue( VALUEKEY_NOTES, info );

            // From the course number, pull out the codes for this course
            List<String> courseNumbers3Digit = new ArrayList<String>();
            List<String> courseNumbers4Digit = new ArrayList<String>();

            //
            final String decimalRegex = "(?:\\.\\d(?:\\d)?)?";
            final String threeDigitRegex = "(\\d\\d\\d" + decimalRegex + ")";
            final String fourDigitRegex = "(\\d\\d\\d\\d" + decimalRegex + ")";
            Pattern threeDigitPattern = Pattern.compile( threeDigitRegex );
            Pattern fourDigitPattern = Pattern.compile( threeDigitRegex );
            Pattern singleCodeParenthesisPattern = Pattern.compile( threeDigitRegex + " *\\(" + fourDigitRegex + "\\)" );
            Pattern singleCodeBracketsPattern = Pattern.compile( threeDigitRegex + " *\\[" + fourDigitRegex + "\\]" );
            Matcher scppMatcher = singleCodeParenthesisPattern.matcher( number );
            Pattern twoCodeParenthesisPattern = Pattern.compile( threeDigitRegex + "[\\/\\-]" + threeDigitRegex + "\\(" + fourDigitRegex + "[\\/\\-]" + fourDigitRegex + "\\)" );
            Pattern twoCodeBracketsPattern = Pattern.compile( threeDigitRegex + "[\\/\\-]" + threeDigitRegex + "\\[" + fourDigitRegex + "[\\/\\-]" + fourDigitRegex + "\\]" );
            if( scppMatcher.matches() )
            {
                courseNumbers3Digit.add( scppMatcher.group(1) );
                courseNumbers4Digit.add( scppMatcher.group(2) );
            }
            else
            {
                // Try a different matcher, for multiple codes
                Matcher tcppMatcher = twoCodeParenthesisPattern.matcher( number );
                if( tcppMatcher.matches() )
                {
                    courseNumbers3Digit.add( tcppMatcher.group(1) );
                    courseNumbers3Digit.add( tcppMatcher.group(2) );
                    courseNumbers4Digit.add( tcppMatcher.group(3) );
                    courseNumbers4Digit.add( tcppMatcher.group(4) );
                }
                else
                {
                    // Try matching JUST a three-digit or four-digit code
                    if( fourDigitPattern.matcher( number ).matches() || threeDigitPattern.matcher( number ).matches() )
                        courseNumbers3Digit.add( number );
                    else
                    {
                        // Whether or not the course number was ingested properly
                        boolean ingestSucceeded = false;

                        // Check the start of the title to see if the code is there
                        int dept;
                        if( -1 != (dept = findDepartmentCode( title )) )
                        {
                            String departmentString = DEPARTMENT_CODES[dept];
                            if( departmentString.equals( "PE" ) || departmentString.equals( "OUTED" ) )
                            {
                                // Grab the 4-digit code from the title, and shorten the title
                                Matcher mp = Pattern.compile( " *(\\d\\d\\d\\d).*").matcher( title.substring( departmentString.length() ) );
                                if( mp.matches() )
                                {
                                    courseNumbers4Digit.add( mp.group(1) );
                                    ingestSucceeded = true;
                                    title = title.substring( departmentString.length() + mp.end(1) ).trim();
                                }
                            }
                        }

                        // Raise an exception if this fails
                        if( !ingestSucceeded )
                            throw new Exception( "The entity spawned from " + catalogURL + " is invalid because its number, \"" + number + "\" is in a weird format" );
                    }
                }
            }

            // Set the course codes in the entity
            entity.setValueList( VALUEKEY_3DIGITCOURSENUMBERS, courseNumbers3Digit, "," );
            entity.setValueList( VALUEKEY_4DIGITCOURSENUMBERS, courseNumbers4Digit, "," );

            // Expand the cross-listings such as "CEE/M&AE/T&AM 455[4550]" into the more
            // easily parsable form "CEE 455[4550], M&AE 455[4550], T&AM 455[4550]".
            // "SOC 222[2220], PAM/D SOC/GOVT 222[2220], PHIL 195[1950]"
            //  -> "SOC 222[2220], PAM 222[2220], D SOC 222[2220], GOVT 222[2220], PHIL 195[1950]"
            // "PSYCH 431/631[4310/6310]"
            //  -> "PSYCH 431[4310], PSYCH 631[6310]"
            // "COM L/NES/JWST 256/RELST 213"
            //  -> "COM L 256, NES 256, JWST 256, RELST 213"

            // TODO: ISSUE:    <Title>Modern European Society and Politics GOVT/SOC 341)</Title>
            // ISSUE:   Psychology of Television (and Beyond)
            // ISSUE:

            // This splits the crosslistings by commas, which always denote changes in lists of departments,
            // or by slashes that are supposed to split lists of departments.  This special method, resplitEntries...Only
            // is used to solve the problem with crosslistings such as "COM L/NES/JWST 256/RELST 213" which are
            // not separated by commas.  The only way to identify a change in the list is by a "NUMBER/CHARACTER"
            // pattern, but we need to preserve the number and character fields, while getting rid of the slash.  This
            // method uses the first group of the regex as the only thing that is removed while splitting.
            String crosslistingArray[] = resplitEntriesByRemovingMatchingGroup1Only( crosslistings.split( "\\, *" ), "\\d(\\/)[^\\d]" );
            List<String> parsableCrosslistings = new ArrayList<String>();

            // Iterate through the array
            for( int i = 0; i < crosslistingArray.length; ++i )
            {
                // This pattern searches for the codes in this crosslisting.  It's slightly confusing because what
                // looks like an inner bracket set really isn't.  Here's the version without Java escaping:
                //
                //      \\d\\d[\\/\\[0123456789\\.]*\\d\\]?
                //            (+++++++++++++++++++)             <-- this is the inside group's location
                //
                // This pattern matches the group of any single three or four-digit course code, including decimals and brackets.
                // I explicitly scope the pattern/matcher here to prevent problems
                String courseCodeGroup = "";
                int courseCodeGroupStart = -1;
                {
                    Pattern courseCodeGroupWithBracketsPattern = Pattern.compile( "\\d\\d[\\/\\[0123456789\\.]*\\d\\]?" );
                    Matcher ccgwbpMatcher = courseCodeGroupWithBracketsPattern.matcher( crosslistingArray[i] );

                    // This should always find a match
                    if( !ccgwbpMatcher.find() )
                        throw new Exception( "Failed to find course code in " + crosslistingArray[i] + " for " + catalogURL );

                    // Assign the result of the search
                    courseCodeGroup = ccgwbpMatcher.group();
                    courseCodeGroupStart = ccgwbpMatcher.start();
                }

                // Break up this entry, if necessary
                String codesForEachDepartment[];
                Matcher tcbpMatcher = twoCodeBracketsPattern.matcher( courseCodeGroup );
                if( tcbpMatcher.matches() )
                {
                    // If this entry is hit, we have a course code format like 123/456[1230/4560],
                    // so generate two versions:  123[1230] and 456[4560]
                    codesForEachDepartment = new String[2];
                    codesForEachDepartment[0] = tcbpMatcher.group(1) + "[" + tcbpMatcher.group(3) + "]";
                    codesForEachDepartment[1] = tcbpMatcher.group(2) + "[" + tcbpMatcher.group(4) + "]";
                }
                else
                {
                    // If we get here, we have a single code in the format 123[1230] OR simply 123, if no 4-digit is given
                    codesForEachDepartment = new String[1];
                    codesForEachDepartment[0] = courseCodeGroup;
                }

                // TODO: all of these "for( int d = 0 ... )" loops can be combined into a single loop,
                // eliminating the need for a departments array at all.

                // Create entries in the departments array.  This array holds the text that contains department
                // codes paired with the course codes we found above.
                String departments[] = new String[codesForEachDepartment.length];
                for( int d = 0; d < codesForEachDepartment.length; ++d )
                    departments[d] = crosslistingArray[i].substring(0,courseCodeGroupStart).trim();

                // For each of these departments, replace all "/" instances with "<<code>>, " to make
                // it look like a list.   Since the last entry won't have a comma after it, simply
                // append one more "<<code>>" to the end.
                for( int d = 0; d < departments.length; ++d )
                    departments[d] = departments[d].replaceAll( "/", codesForEachDepartment[d] + ", " ) + codesForEachDepartment[d];

                // Add all of the department text to the parsable crosslistings collection
                for( int d = 0; d < departments.length; ++d )
                {
                    // Split up the departments by commas so that we can pull out each department/code pair
                    // as an individual string.
                    String crosslistingSubarray[] = departments[d].split( "\\, *" );

                    // Each of the strings we pulled out should be a valid crosslisting in a very rigidly
                    // defined format:
                    //
                    //  DEPT_CODE ###[####]  OR DEPT_CODE ###
                    //
                    // where ###/#### are three-digit and four-digit codes (respectively), which may contain decimal values
                    //
                    for( String parsableCrosslisting : crosslistingSubarray )
                        parsableCrosslistings.add( parsableCrosslisting );
                }
            }

            // Look through the parsable crosslistings string to find all of the crosslisted departments
            List<String> crosslistedCourseCodes = new ArrayList<String>();
            for( String parsableCrosslisting : parsableCrosslistings )
            {
                // Find the department
                int deptIndex = findDepartmentCode( parsableCrosslisting );

                // Make sure it was found
                if( deptIndex == -1 ) throw new Exception( "Unable to find department in " + parsableCrosslisting + " for " + catalogURL );

                // Grab the numbers in the format ###[####]
                String crosslistingCodeWithoutDept = parsableCrosslisting.substring( DEPARTMENT_CODES[deptIndex].length() ).trim();
                Matcher scbpMatcher = singleCodeBracketsPattern.matcher( crosslistingCodeWithoutDept );
                if( scbpMatcher.find() )
                {
                    // Create this crosslisting and add it to the crosslisting array in the format  "DEPT|THREE_DIGIT_CODE|FOUR_DIGIT_CODE"
                    crosslistedCourseCodes.add( DEPARTMENT_CODES[deptIndex] + "|" + scbpMatcher.group(1) + "|" + scbpMatcher.group(2) );
                }
                else
                {
                    // Add to the listing without a four-digit code
                    crosslistedCourseCodes.add( DEPARTMENT_CODES[deptIndex] + "|" + crosslistingCodeWithoutDept );

                    // Warn about this
                    //System.out.println( "Entity " + entity.getName() + " has a crosslisting with no 4-digit code: " + parsableCrosslisting );
                }
            }

            // If the crosslisting array doesn't exist, yet there is a course code AND number in the title,
            // warn about this object!
// TODO: re-enable this safety check
//          if( crosslistedCourseCodes.isEmpty() &&
//              -1 != findDepartmentCode( title ) && Pattern.compile( "\\d\\d\\d" ).matcher( title ).find() )
//              throw new Exception( "Potentially missing crosslistings for " + title + "; skipping (pulled from " + catalogURL + ")" );

            // Set the array of crosslisted course codes
            entity.setValueList( VALUEKEY_CROSSLISTED_COURSE_CODES, crosslistedCourseCodes, "," );

            // Assign labeled entity values
            entity.setValue( VALUEKEY_CATALOGURL, catalogURL );
            entity.setValue( VALUEKEY_DEPARTMENT, department );
            entity.setValue( VALUEKEY_COLLEGE, college );
            entity.setValue( VALUEKEY_TITLE, title );
            entity.setValue( VALUEKEY_DESCRIPTION, description );
            entity.setValue( VALUEKEY_UNINGESTABLE_ENTITY_XML, new UnusableEntityXML( xmlEntity, "Ingest failed during commit to VIVO" ).getXML() );

            // Return the entity we created
            return entity;
        }


        /**
         * This is new new version of next()
         * @return The next entity in the document, or 'null' if the document has no more
         *         available data
         */
        @Override
        public Entity next()
        {
            // Initialize the iterator, if necessary
            if( myRootIterator == null )
                myRootIterator = getXMLDocument().getRootElement().elementIterator();

            // Get the next element's data, and build an entity from it
            EntityImpl xmlEntity = null;
            Entity entityToReturn = null;
            while( null == entityToReturn )
            {
                // Make sure there is data to be returned
                if( !myRootIterator.hasNext() ) return null;

                // Build an entity that holds the data in a more accessible version of the XML file's structure
                xmlEntity = createEntity( myRootIterator.next() );

                // Try to parse the XML entity into something useful
                try { entityToReturn = parseXMLEntity( xmlEntity ); }
                catch( Exception e )
                {
                    // We couldn't get the returned entity
                    entityToReturn = null;

                    // Add this XML entity to the problems list
                    System.out.println( e.getMessage() );
                    myUnusableXMLEntities.add( new UnusableEntityXML( xmlEntity, e.getMessage() ) );
                }
            }

            // Return the entity we found
            return entityToReturn;
        }
    }
}
