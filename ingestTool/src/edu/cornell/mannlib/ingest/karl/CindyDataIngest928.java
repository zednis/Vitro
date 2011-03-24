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
import java.util.Set;
import java.util.TreeSet;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;


class WriteCindyDataToVivo extends WriteIngestEntityToVivo
{
    /// The URI for a course VCLASS
    public static final String COURSE_VCLASS_URI = "http://vivo.library.cornell.edu/ns/0.1#CourseSection";

    /// The URI for the group of which the VClass is a member.  This is the "events" group
    public static final String COURSE_VCLASS_GROUP_URI = "http://vivo.library.cornell.edu/ns/0.1#group3";

    /// URIs for course properties
    public static final String COURSE_PROPERTY_HASPREREQOF_URI = "http://vivo.library.cornell.edu/ns/0.1#HasPrereqOf";
    public static final String COURSE_PROPERTY_ISPREREQFOR_URI = "http://vivo.library.cornell.edu/ns/0.1#IsPrereqFor";

    /// Just a list of course URIs so I can link things up...for funsies
    protected Set<String> myCourseURIs = new TreeSet<String>();

    @Override
    public void process( IngestEntity entity )
    {
        ArrayList<String> netIDArray = entity.getProperty("Ee Netid");
        if( netIDArray != null )
        {
            // Get the net ID of the person who teaches this course
            String netID = netIDArray.get(0);

            // Use the net ID to access the URI of the entity to which it corresponds
            //myDaoFactory.getIndividualDao().getIndividualByExternalId(externalIdType, netID);
        }

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

        // Set this entity's course name
        vivoEntity.setBlurb( entity.getProperty( "Course Title" ).get(0) );

        vivoEntity.setFlag1Set("1"); // set the portal for this entity

        // Update this entity
        myDaoFactory.getIndividualDao().updateIndividual(vivoEntity);

        // Get the URI of another course
        String otherCourseURI = (String)myCourseURIs.toArray()[(int)(Math.random()*(myCourseURIs.size()-1))];

        // Give this entity a property
        ObjectPropertyStatement stmt = new ObjectPropertyStatementImpl();
        stmt.setSubjectURI(vivoEntity.getURI());
        stmt.setPropertyURI(COURSE_PROPERTY_HASPREREQOF_URI);
        stmt.setObjectURI(otherCourseURI);

        // Insert this statement
        myDaoFactory.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(stmt);

        stmt = new ObjectPropertyStatementImpl();
        stmt.setSubjectURI(otherCourseURI);
        stmt.setObjectURI(vivoEntity.getURI());
        stmt.setPropertyURI(COURSE_PROPERTY_ISPREREQFOR_URI);
        myDaoFactory.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(stmt);
    }

    /**
     * Generates a course URI from the course entity
     * @return URI
     */
    public String courseEntityURI( IngestEntity entity )
    {
        // Get this course's ID number (it should be unique)
        String cid = entity.getProperty( "Cid" ).get(0);
        if( cid.isEmpty() ) return null;

        // Build the URI for this course
        String uri = "http://vivo.mann.library.cornell.edu/vivo#course" + cid;

        // Insert into our set
        myCourseURIs.add( uri );

        // Return the URI
        return uri;
    }

    /**
     * Gets the name of the course specified by the given entity
     */
    public String courseName( IngestEntity entity )
    {
        // Get the course name from this entity
        return entity.getProperty( "Course Title" ).get(0);
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

        // Get the entity from the database by first looking for its URI
        Individual vivoEntity = myDaoFactory.getIndividualDao().getIndividualByURI( uri );

        // If we could find it, great!
        if( vivoEntity != null ) return vivoEntity;

        // If we couldn't find the entity, we should make one
        vivoEntity = new IndividualImpl();
        vivoEntity.setName( courseName( entity ) );
        vivoEntity.setDescription( "Taught" ); // change me!
        vivoEntity.setURI( uri );
        vivoEntity.setVClassURI( COURSE_VCLASS_URI );
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
            vclass.setHiddenFromDisplayBelowRoleLevel(BaseResourceBean.RoleLevel.PUBLIC);
            //vclass.setHidden( "false" );

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
            op.setHiddenFromDisplayBelowRoleLevel(BaseResourceBean.RoleLevel.PUBLIC);
            //op.setHidden("false");
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

public class CindyDataIngest928 extends TabFileIngestDocument
{
    public static String SOURCE_FILE = "c:/Documents and Settings/Vivo/My Documents/VIVO/FA07.txt";

    /**
     * Entry point to the ingester.  This file takes the data provided by Cindy (via Jon)
     * and parses out the course information.  This is a tab-delimited file, but I have
     * no idea what it contains at this point.
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
            WriteCindyDataToVivo processor = new WriteCindyDataToVivo();

            // Create a new ingester
            CindyDataIngest928 ingester = new CindyDataIngest928( processor );

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
    public CindyDataIngest928( IngestEntityProcessor processor )
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
        actions.put( "row", new ParseRow() );

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
            IngestEntity entity = new IngestEntity( "entries");

            // Add parsing actions to the table
            actions.put( "Lastname", new ParseField( "Lastname", entity, formatter ) );
            actions.put( "Firstname", new ParseField( "Firstname", entity, formatter ) );
            actions.put( "Unit Employed", new ParseField( "Unit Employed", entity, formatter ) );
            actions.put( "Dept Employed", new ParseField( "Dept Employed", entity, formatter ) );
            actions.put( "Emplid", new ParseField( "Emplid", entity, formatter ) );
            actions.put( "Ee Netid", new ParseField( "Ee Netid", entity, formatter ) );
            actions.put( "Cid", new ParseField( "Cid", entity, formatter ) );
            actions.put( "Year", new ParseField( "Year", entity, formatter ) );
            actions.put( "Term", new ParseField( "Term", entity, formatter ) );
            actions.put( "College Dept", new ParseField( "College Dept", entity, formatter ) );
            actions.put( "Num", new ParseField( "Num", entity, formatter ) );
            actions.put( "Sect Type", new ParseField( "Sect Type", entity, formatter ) );
            actions.put( "Sect Num", new ParseField( "Sect Num", entity, formatter ) );
            actions.put( "Course Title", new ParseField( "Course Title", entity, formatter ) );
            actions.put( "Salplandescr", new ParseField( "Salplandescr", entity, formatter ) );
            actions.put( "Isacademic", new ParseField( "Isacademic", entity, formatter ) );
            actions.put( "Isprofessorial", new ParseField( "Isprofessorial", entity, formatter ) );
            actions.put( "Isexecutive", new ParseField( "Isexecutive", entity, formatter ) );
            actions.put( "Isassistant", new ParseField( "Isassistant", entity, formatter ) );
            actions.put( "Authconfirm", new ParseField( "Authconfirm", entity, formatter ) );


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





