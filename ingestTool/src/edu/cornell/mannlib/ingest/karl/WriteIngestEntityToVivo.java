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

//All of this stuff is for Vivo
//------------------------------
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

//------------------------------

public abstract class WriteIngestEntityToVivo extends IngestEntityProcessor
{
    private static final String CONNECTION_PROP_FILE = "C:\\Documents and Settings\\Karl\\My Documents\\Work - Vivo\\code\\webapp\\config\\connection.properties";
    private static final OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_DL_MEM;
    private static final String ONTOLOGIES[] = { "C:\\Program Files\\Apache Software Foundation\\Tomcat 6.0\\webapps\\vitro\\WEB-INF\\ontologies\\system\\vitro-0.7.owl",
    											 "C:\\Program Files\\Apache Software Foundation\\Tomcat 6.0\\webapps\\vitro\\WEB-INF\\ontologies\\auth\\vivo-users.owl" };

    protected WebappDaoFactory myDaoFactory;

    /**
     * Initializes the DAO factory for this processor.  This class is abstract
     * because it doesn't implement the process() method.
     */
    public WriteIngestEntityToVivo()
    {
        // Generate the database object
        myDaoFactory = makeDao( CONNECTION_PROP_FILE );
    }

    private static String JENA_DB_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";

    /**
     * Creates a Data Access Object for VIVO by establishing database connections.
     */
    public static WebappDaoFactory makeDao( String propFilename )
    {
        JenaDataSourceSetup jdss = new JenaDataSourceSetup();
        OntModel dbModel = null;

        try {
        
        // Connect to the database
        dbModel = jdss.makeDBModelFromPropertiesFile(propFilename, JENA_DB_MODEL, ONT_MODEL_SPEC);
        //loadModelOntologies( dbOntModel );
        
        } catch( Exception e )
        {
            System.out.println("Could not open Jena DB model named "+JENA_DB_MODEL);
        }
/*
        //make connection to model in db
        Model dbModel = jdss.makeDBModelFromPropertiesFile(propFilename);
        loadModelOntologies( dbModel );
        OntModel dbOntModel = ModelFactory.createOntologyModel(ONT_MODEL_SPEC,dbModel);
        JenaBaseDao.addWritableOntModel(dbOntModel);
        JenaBaseDao.setPersistentOntModel(dbOntModel);*/

        //make in-memory model
        Model memModel = ModelFactory.createDefaultModel();
        memModel.add( dbModel );
        //loadModelOntologies( memModel );
        OntModel ontModel = ModelFactory.createOntologyModel(ONT_MODEL_SPEC,memModel);
        ontModel.prepare();
        /*JenaBaseDao.setOntModel(ontModel);
        JenaBaseDao.addWritableOntModel(ontModel);*/

        WebappDaoFactory wadf = new WebappDaoFactoryJena(ontModel,dbModel);
        //JenaBaseDao.setCoreDaoFactory(wadf.getCoreDaoFactory());

        // Everything should be initialized here...
        System.out.println( "DAO initialized" );

        return wadf;
    }


    /**
     * Load each of the ontologies specified
     * @param model
     */
    static void loadModelOntologies( Model model )
    {
        try
        {
            for( int i = 0; i < ONTOLOGIES.length; ++i )
            {
                try
                {
                    model.read( new FileInputStream( ONTOLOGIES[i] ), null );
                } catch( FileNotFoundException e )
                {
                    System.out.println( "Error while reading ontology \"" + ONTOLOGIES[i] + "\"\n" );
                    throw e;
                }
            }
        } catch( FileNotFoundException e )
        {
            // Warn about this error
            System.out.println( "Ontology wasn't found!" );

            // Output the stack
            e.printStackTrace();
        }
    }


    /**
     * Makes sure that the VClass specified exists in the VClass table.  If it does, this
     * class returns success; otherwise, this class calls the getVClassProperties abstract
     * method to get the derived class to stuff some data into the VClass.
     * @param uri The URI of the VClass for which to search
     * @throws Exception If the VClass needs to be created, but couldn't be
     */
    public void ensureVClassExists( String uri ) throws Exception
    {
        // Make sure the VClass for courses exists
        VClassDao vclassDao = myDaoFactory.getVClassDao();
        if( vclassDao.getVClassByURI(uri) == null )
        {
            // Use the abstract method to fill the vClass with data
            VClass vclass = initializeNewVClass( uri );

            // Insert the course VClass
            if( vclass != null )
                vclassDao.insertNewVClass( vclass );
            else
                throw new Exception( "vClass \"" + uri + "\" was not properly initialized" );

            Thread.sleep(1000);

            // Make sure this worked
            if( vclassDao.getVClassByURI(uri) == null )
                throw new Exception( "Unable to insert VClass at \"" + uri + "\" into table" );
        }
    }

    /**
     * This method must call the following methods on a new VClassWebapp object, and return
     * that object.
     *  setURI( uri )
     *  setName
     *  setGroupURI
     *  setShortDef
     *  setExample
     *  setDescription
     *  setDisplayLimit
     *  setDisplayRank
     *  setHidden
     * @param vClass The vClass to initialize
     * @return The newly created VClass representation
     */
    public abstract VClass initializeNewVClass( String uri );
}
