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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;


// make courses  SemesterCourse, moniker "Fall 2007 Course"
// set sunrise/sunset to beginning/end of semester
// map object property that links to Fall 07 Academic Semester
// heldInSemester property -> Time Entity : Time Interval : Academic Semester : { Fall 07 Academic Semester }
class WriteDownloadedCourseCatalogIngestEntityToVivo extends WriteIngestEntityToVivo
{
    /// The URI for a course VCLASS
    //public static final String COURSE_VCLASS_URI = "http://vivo.library.cornell.edu/ns/0.1#CourseSection";
    public static final String COURSE_VCLASS_URI = "http://vivo.library.cornell.edu/ns/0.1#SemesterCourse";

    /// The URI for the group of which the VClass is a member.  This is the "events" group
    public static final String COURSE_VCLASS_GROUP_URI = "http://vivo.library.cornell.edu/ns/0.1#group3";

    /// URIs for course properties
    public static final String COURSE_PROPERTY_HASPREREQOF_URI = "http://vivo.library.cornell.edu/ns/0.1#HasPrereqOf";
    public static final String COURSE_PROPERTY_ISPREREQFOR_URI = "http://vivo.library.cornell.edu/ns/0.1#IsPrereqFor";

    //
    public static final String COURSE_PROPERTY_OCCURSINSEMESTER_URI = "http://vivo.library.cornell.edu/ns/0.1#SemesterCourseOccursInSemester";

    public static String CURRENT_SEMESTER_ENTITY_URI = "http://vivo.library.cornell.edu/ns/0.1#Fall07AcademicSemester";
    public static String ACADEMIC_SEMESTER_VCLASS_URI = "http://vivo.library.cornell.edu/ns/0.1#AcademicSemester";

    public static String COURSE_ID_DATAPROPERTY_URI = "http://vivo.library.cornell.edu/ns/0.1#CourseID";
    public static String COURSE_CID_DATAPROPERTY_URI = "http://vivo.library.cornell.edu/ns/0.1#CourseCID";

    public WriteDownloadedCourseCatalogIngestEntityToVivo()
    {
        super();

        // Add the data property for course IDs
        if( null == myDaoFactory.getDataPropertyDao().getDataPropertyByURI(COURSE_ID_DATAPROPERTY_URI))
        {
            DataProperty dtp = new DataProperty();
            dtp.setDomainClassURI(COURSE_VCLASS_URI);
            dtp.setURI(COURSE_ID_DATAPROPERTY_URI);
            dtp.setName("Course ID");
            dtp.setPublicName("Course ID");
            dtp.setDescription("Department ID + 3-digit course ID");
            try {
            	myDaoFactory.getDataPropertyDao().insertDataProperty(dtp);
            } catch (InsertException ie) {
            	ie.printStackTrace();
            }
        }

        // Add the data property for unique CIDs
        if( null == myDaoFactory.getDataPropertyDao().getDataPropertyByURI(COURSE_CID_DATAPROPERTY_URI))
        {
            DataProperty dtp = new DataProperty();
            dtp.setDomainClassURI(COURSE_VCLASS_URI);
            dtp.setURI(COURSE_CID_DATAPROPERTY_URI);
            dtp.setName("Course CID");
            dtp.setPublicName("Course CID");
            dtp.setDescription("Numeric, unique course identifier");
            try {
            	myDaoFactory.getDataPropertyDao().insertDataProperty(dtp);
            } catch (InsertException ie) {
            	ie.printStackTrace();
            }
        }
    }

    @Override
    public void process(IngestEntity entity) {

        // Make sure the VClass exists
        try {
            ensureVClassExists( COURSE_VCLASS_URI );
        } catch( Exception e )
        {
            // Tell the user about the problem
            System.out.println( "Unable to insert course VClass:" );

            // Have the exception let us know where it came from
            e.printStackTrace();

            // Just exist the method
            return;
        }

        // Get the entity that corresponds to the one we are being given
        Individual vivoEntity = grabVivoEntity( entity );

        // Process the entity, if it could be found
        if( vivoEntity != null )
        {
            // Set the moniker for this course
            vivoEntity.setMoniker( "Fall 2007 Course" );

            // Set this entity's course name
            //vivoEntity.setBlurb( getFirstProperty( entity, "Title", "" ) );

            // Set the portal
            vivoEntity.setFlag1Numeric(1); // set the portal for this entity
            vivoEntity.setFlag1Set("1");

            // Update this entity
            myDaoFactory.getIndividualDao().updateIndividual(vivoEntity);
/*
            Individual vivoSemesterEntity = grabSemesterEntity();

            // Create the Occurs In Semester property statement
            ObjectPropertyStatement stmt = new ObjectPropertyStatement();
            stmt.setSubjectURI(vivoEntity.getURI());
            stmt.setPropertyURI(COURSE_PROPERTY_OCCURSINSEMESTER_URI);
            stmt.setObjectURI(vivoSemesterEntity.getURI());

            // Insert this statement
            myDaoFactory.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(stmt);

            // Add the course key (ex. AEM220, ENGRD230, MATH294) as a data property
            addDataProperty( vivoEntity, COURSE_ID_DATAPROPERTY_URI, courseKey(entity) );*/
        }

    }
    public void addDataProperty( Individual entity, String dataURI, String value )
    {
        DataPropertyStatement stmt = new DataPropertyStatementImpl();
        stmt.setIndividualURI(entity.getURI());
        stmt.setDatapropURI(dataURI);
        stmt.setData(value);
        myDaoFactory.getDataPropertyStatementDao().insertNewDataPropertyStatement(stmt);
    }

    public String courseKey( IngestEntity courseEntity )
    {
        String title = getFirstProperty( courseEntity, "Title", null );
        if( title == null ) return null;

        // Get rid of all of the spaces
        title = title.toUpperCase().replaceAll("\\s","");

        Pattern titlePattern = Pattern.compile( "([\\D]*[\\d]*)" );
        Matcher titleMatcher = titlePattern.matcher( title );

        return titleMatcher.find() ? titleMatcher.group(1) : null;
    }

    /**
     * Generates a course URI from the course entity
     * @return URI
     */
    public String courseEntityURI( IngestEntity entity )
    {
        String key = courseKey( entity );

        if( key != null )
        {
            String uri = "http://vivo.mann.library.cornell.edu/vivo#course" + key;
            return uri;
        }
        else
            return null;
    }

    /**
     * Gets the name of the course specified by the given entity
     */
    public String courseName( IngestEntity entity )
    {
        // Get the course name from this entity
        return getFirstProperty( entity, "Title", "" );
    }


    public Individual grabSemesterEntity()
    {
        // Get the semester entity
        Individual vivoEntity = myDaoFactory.getIndividualDao().getIndividualByURI( CURRENT_SEMESTER_ENTITY_URI );

        // If it exists, return it
        if( vivoEntity != null ) return vivoEntity;

        // If we couldn't find the entity, we should make one
        vivoEntity = new IndividualImpl();
        vivoEntity.setName( "Fall 07 Semester" );
        //vivoEntity.setDescription(  ); // change me!
        vivoEntity.setURI( CURRENT_SEMESTER_ENTITY_URI );
        vivoEntity.setVClassURI( ACADEMIC_SEMESTER_VCLASS_URI );
        vivoEntity.setFlag1Set("1"); // set the portal for this entity

        // Add this entity to the database
        try {
        	myDaoFactory.getIndividualDao().insertNewIndividual( vivoEntity );
        } catch (InsertException ie) {
        	ie.printStackTrace();
        }

        // Return this entity
        return vivoEntity;

    }

    /**
     * Obtains the Vivo entity representation of the given course IngestEntity.
     * If the course hasn't been added to the database yet, this method will
     * create the course and fill it with as much information as possible.
     * @return The VIVO course corresponding to the provided entity
     */
    public Individual grabVivoEntity( IngestEntity entity )
    {
        // Get the URI for this entity
        String uri = courseEntityURI( entity );
        if( uri == null )
        {
            System.out.println( "Unable to insert entity:  " + entity );
            return null;
        }

        // Get the entity from the database by first looking for its URI
        Individual vivoEntity = myDaoFactory.getIndividualDao().getIndividualByURI( uri );

        // If we could find it, great!
        if( vivoEntity != null ) return vivoEntity;

        // If we couldn't find the entity, we should make one
        vivoEntity = new IndividualImpl();
        vivoEntity.setName( courseName( entity ) );
        vivoEntity.setDescription( getFirstProperty( entity, "Description", "" ) ); // change me!
        vivoEntity.setURI( uri );
        vivoEntity.setVClassURI( COURSE_VCLASS_URI );
        vivoEntity.setFlag1Numeric(1);
        vivoEntity.setFlag1Set("1"); // set the portal for this entity

        // Add this entity to the database
        try {
        	String newURI = myDaoFactory.getIndividualDao().insertNewIndividual( vivoEntity );
        } catch (InsertException ie) {
        	ie.printStackTrace();
        }

        // Return this entity
        return vivoEntity;
    }

    public static String getFirstProperty( IngestEntity entity, String desc, String failResponse )
    {
        ArrayList<String> al = entity.getProperty(desc);
        if( al != null && !al.isEmpty() ) return al.get(0);
        else return failResponse;
    }


    @Override
    public VClass initializeNewVClass(String uri) {
        
        // Create the vclass object we are going to fill
        VClass vclass = new VClass();
        vclass.setURI(uri);

        // Give this class a URI
        if( uri.equals( COURSE_VCLASS_URI ) )
        {
            // Initialize the vClass
            vclass.setName( "Course" );
            vclass.setGroupURI( COURSE_VCLASS_GROUP_URI );
            vclass.setShortDef( "A Course at Cornell" );
            vclass.setExample( "ECE 210:  Introduction to Circuits for Electrical and Computer Engineers" );
            vclass.setDescription( "Represents any of Cornell's courses, including relavent links to other information" );
            vclass.setDisplayLimit( 10 );
            vclass.setDisplayRank( 1 );
            vclass.setHidden( "false" );

            edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty op;

            // Add an object property for the VClass->VClass prereq mappings
            op = new edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty();
            op.setDomainVClassURI( COURSE_VCLASS_URI );
            op.setRangeVClassURI( COURSE_VCLASS_URI );
            op.setURI( COURSE_PROPERTY_HASPREREQOF_URI );
            op.setURIInverse( COURSE_PROPERTY_ISPREREQFOR_URI );
            op.setDomainSide( "CourseIsPrereqFor" ); // I'm assuming this is the domain-side name?
            op.setDomainPublic( "Course Is Prereq For" );
            op.setRangeSide( "CourseHasPrereqOf" );     // And this one's a range-side name?
            op.setRangePublic( "Course Has Prereq Of" );
            op.setLocalName( "HasPrereqOf" );
            op.setLocalNameInverse( "IsPrereqFor" );
            op.setDomainDisplayTier("-1");
            op.setRangeDisplayTier("-1");
            op.setParentURI("");
            op.setDomainDisplayLimit(5);
            op.setRangeDisplayLimit(5);
            op.setHidden("false");
            op.setExample("MATH 191 is a prerequisite for MATH 293");
            op.setDescription("Designates a course that is a prerequisite for another course");
            try {
            	myDaoFactory.getObjectPropertyDao().insertObjectProperty(op);
            } catch (InsertException ie) {
            	ie.printStackTrace();
            }


            // Return the class that was created and initialized
            return vclass;
        }
        else
            return null;
    }
}


public class WriteDownloadedCourseCatalogToVivo extends XMLFileIngestDocument
{
    public static String SOURCE_FILE = "C:\\Documents and Settings\\Karl\\My Documents\\Work - Vivo\\courses.txt";

    /**
     * This program takes the courses that were downloaded from the online course catalog and puts them
     * into the vivo.
     * @param args
     */
    public static void main( String[] args )
    {
        // This block of code executes the ingestion
        try {

            // Generate an input stream for the ingester using the refactored source file
            // that was generated above.  Hopefully this will be correctly tab-delimited.
            InputStream input = new BufferedInputStream( new FileInputStream( SOURCE_FILE ) );

            // Right now, we're just going to output to the console
            //OutputEntityProperties processor = new OutputEntityProperties();
            WriteDownloadedCourseCatalogIngestEntityToVivo processor = new WriteDownloadedCourseCatalogIngestEntityToVivo();

            // Create a new ingester
            WriteDownloadedCourseCatalogToVivo ingester = new WriteDownloadedCourseCatalogToVivo( processor );

            // Ingest the document
            ingester.ingest( input );

        } catch( Exception e ) {
            System.out.println( "Unable to complete parsing:  " + e );
            e.printStackTrace();
        }
    }

    /**
     *  Initializes this ingest tool with a processor
     * @param processor Defines what to do with entities
     */
    public WriteDownloadedCourseCatalogToVivo( IngestEntityProcessor processor )
    {
        super( processor );
    }

    /**
     *  Takes the given input stream and parses it for entities
     */
    public void ingest( InputStream input ) throws Exception
    {
        // Get the data stream
        open( input );

        // Get the string of parsing actions
        //System.out.println( buildParseFieldActions( "actions", "entity", "entries", "formatter" ) );

        // Build the primary action list for top-level objects
        HashMap<String,IngestAction> actions = new HashMap<String,IngestAction>();
        actions.put( "Course", new ParseRow() );

        // Read the document
        parse( actions );
    }


    class ParseRow implements IngestAction
    {
        public void perform( IngestDocument document ) throws Exception
        {
            // The formatter for entries in this row
            IngestFormatter formatter = new IngestFormatter.StringFormatter();
            HashMap<String,IngestAction> actions = new HashMap<String,IngestAction>();
            IngestEntity entity = new IngestEntity( "entries" );

            // Add parsing actions to the table
            actions.put( "Title", new ParseField( "Title", entity, formatter ) );
            actions.put( "Info", new ParseField( "Info", entity, formatter ) );
            actions.put( "Description", new ParseField( "Description", entity, formatter ) );

            // Handle this entity
            try {
                parse( actions );
                process( entity );
            }
            catch( Exception e )
            {
                System.out.println( "Row-parsing problem:  " + e );
                e.printStackTrace();
                throw e;
            }
        }
    }
}