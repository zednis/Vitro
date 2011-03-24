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


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetup;

public class Example {

    private static OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_DL_MEM; // no additional entailment reasoning
    private static OntModel memModel;
    private static OntModel dbModel;

    public static void main(String[] argv){
        System.out.println("using " + argv[0] + " for connection properties");
        WebappDaoFactory wdf = makeDao( argv[0] );

        System.out.println("memModel has " + memModel.size() + " statements");
        System.out.println("dbModel has " + dbModel.size() + " statements");

        //creating and saving an entity
        Individual ent = new IndividualImpl();
        ent.setURI("http://example.com/vitro#1111");
        ent.setVClassURI("http://vivo.cornell.edu/ns/mannadditions/0.1#CornellFaculty");
        ent.setName("Jade");
        ent.setDescription("this is a test entity");
        try {
        	wdf.getIndividualDao().insertNewIndividual(ent);
        } catch (InsertException ie) {
        	ie.printStackTrace();
        }

        Individual ent2 = new IndividualImpl();
        ent2.setURI("http://example.com/vitro#2222");
        ent2.setVClassURI("http://vivo.cornell.edu/ns/mannadditions/0.1#CornellFaculty");
        ent2.setName("Frank");
        ent2.setDescription("this is a test entity #2");
        try {
        	wdf.getIndividualDao().insertNewIndividual(ent2);
        } catch (InsertException ie) {
        	ie.printStackTrace();
        }

        //making an objectProperty
        ObjectPropertyStatement e2e = new ObjectPropertyStatementImpl();
        e2e.setObjectURI(ent.getURI());
        e2e.setPropertyURI("http://example.com/vitro#boughtLunchFor");
        e2e.setSubject(ent2);
        wdf.getObjectPropertyStatementDao().insertNewObjectPropertyStatement(e2e);
    }

    public static WebappDaoFactory makeDao(String propFilename){
        JenaDataSourceSetup jdss = new JenaDataSourceSetup();

        //make connection to model in db
        Model tmp = jdss.makeDBModelFromPropertiesFile(propFilename);

        dbModel = ModelFactory.createOntologyModel(ONT_MODEL_SPEC,tmp);
        //JenaBaseDao.addWritableOntModel(dbModel);
        //JenaBaseDao.setPersistentOntModel(dbModel);

        //make in-memory model
        tmp = ModelFactory.createDefaultModel();
        memModel = ModelFactory.createOntologyModel(ONT_MODEL_SPEC,tmp);
        memModel.add(dbModel);
        //JenaBaseDao.setOntModel(memModel);
        //JenaBaseDao.addWritableOntModel(memModel);

        WebappDaoFactory wadf = new WebappDaoFactoryJena(memModel);
        //JenaBaseDao.setCoreDaoFactory(wadf.getCoreDaoFactory());

        try {
            //JenaBaseDao.initCoreConvenienceVocabulary();
            //JenaBaseWebappDao.initConvenienceVocabulary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wadf;
    }


}
