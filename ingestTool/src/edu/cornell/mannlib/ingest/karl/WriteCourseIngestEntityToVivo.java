package edu.cornell.mannlib.ingest.karl;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;

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



public class WriteCourseIngestEntityToVivo extends WriteIngestEntityToVivo
{

    public void process( IngestEntity entity )
    {
        // Get the entity that corresponds to the one we are being given
        Individual vivoEntity = grabVivoEntity( entity );

        // Do more updating here
        //vivoEntity.
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
        return "http://vivo.mann.library.cornell.edu/vivo#course" + cid;
    }

    /**
     * Gets the name of the course specified by the given entity
     */
    public String courseName( IngestEntity entity )
    {
        String dept = entity.getProperty( "Course Dept" ).get(0);
        String num = entity.getProperty( "Course Num" ).get(0);

        // Concatenate the department / course numbers
        return dept + num;
    }

    /**
     * Obtains the Vivo entity representation of the given course IngestEntity.
     * If the course hasn't been added to the database yet, this method will
     * create the course and fill it with as much information as possible.
     * @return The VIVO course corresponding to the provided entity
     */
    public Individual grabVivoEntity( IngestEntity entity )
    {
        // Get the entity from the database by first looking for its URI
    	Individual vivoEntity = myDaoFactory.getIndividualDao().getIndividualByURI( courseEntityURI( entity ) );

        // If we could find it, great!
        if( vivoEntity != null ) return vivoEntity;

        // If we couldn't find the entity, we should make one
        vivoEntity = new IndividualImpl();
        vivoEntity.setName( courseName( entity ) );
        vivoEntity.setDescription( "A course at Cornell" ); // change me!

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
        // TODO Auto-generated method stub
        return null;
    }
}
