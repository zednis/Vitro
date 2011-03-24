package edu.cornell.mannlib.ingest.fieldHandlers;

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

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Saves an entity, it's ents2data, ents2ents, externalids
 * Right now it is very basic but it could be smarter.
 * Maybe the logic belongs in EntityDao? Then anyone can do
 * a deep entity update to the db.
 *
 * @author bdc34
 *
 */
public class SaveEntity implements FieldHandler {
    WebappDaoFactory webappDaoFactory;

    public SaveEntity(WebappDaoFactory webappDaoFactory){
        log = Logger.getLogger(SaveEntity.class);
        this.webappDaoFactory = webappDaoFactory;
    }
    public void setFields(Element ele, Object targetEnt) {
        String uriOfEntToSave = null;
        if( targetEnt == null ){
            log.warn("passed null target individual");
            return;
        }
        if(!( targetEnt instanceof Individual)){
            log.warn("passed non-Individual target individual");
            return;
        }
        Individual entity = (Individual)targetEnt;
//        if( entity.getId() < 1  ){
//            log.warn("Entity had an invalid id of " + entity.getId());
//            return;
//        }

        if( entityIsNew( entity ) ){
        	try {
        		uriOfEntToSave = webappDaoFactory.getIndividualDao().insertNewIndividual(entity);
        	} catch (InsertException ie) {
        		log.error("Unable to insert new individual: ",ie);
        	}
            entity.setURI(uriOfEntToSave);
            log.debug("entity URI " + uriOfEntToSave + " was added with name " + entity.getName() );
        }else{
            log.debug("updating entity (" + entity.getURI() + ") " + entity.getName());
            uriOfEntToSave = entity.getURI();
            webappDaoFactory.getIndividualDao().updateIndividual(entity);
        }
        if( entity.getObjectPropertyStatements() != null ){
            for(ObjectPropertyStatement e2e : entity.getObjectPropertyStatements()){
                e2e.setSubjectURI(uriOfEntToSave);
                if( ents2entsIsNew(e2e) )
                    webappDaoFactory.getPropertyInstanceDao().insertProp(e2e.toPropertyInstance());
                else
                	throw new Error("SaveEntity.java : updating property instances is no longer supported. We'll need the original value.");
                	//webappDaoFactory.getPropertyInstanceDao().updateProp(e2e.toPropertyInstance());
            }
        }
        //if( entity.getRangeEnts2Ents() != null ){
        //    for(ObjectPropertyStatement e2e : entity.getRangeEnts2Ents()){
        //        //TODO: update for semweb-align
        //        if(true) throw new Error("TODO: update for semweb-align");
        //   //e2e.setRangeId(idOfEntToSave);
        //    //     if( ents2entsIsNew(e2e) )
//      //               facade.insertProp(e2e.toPropertyInstance());
//      //           else
        //            //facade.updateProp(e2e.toPropertyInstance());
        //    }
        //}
        if (entity.getDataPropertyStatements() != null) {
            for(DataPropertyStatement e2d : entity.getDataPropertyStatements()){
                e2d.setIndividualURI(uriOfEntToSave);
                if( ents2dataIsNew(e2d) ){
                    e2d.setIndividualURI(uriOfEntToSave);
                    webappDaoFactory.getDataPropertyStatementDao().insertNewDataPropertyStatement(e2d);
                }else{
                    throw new Error("Updating of DataPropertyStatements not supported in semweb-align");
                    //log.warn("updating of ents2data is not yet supported");
                }
            }
        }
        if (entity.getExternalIds() != null) {
            for(DataPropertyStatement extidStmt : entity.getExternalIds()){
            	extidStmt.setIndividualURI(uriOfEntToSave);
                if( externalIdIsNew(extidStmt) ){
                    webappDaoFactory.getDataPropertyStatementDao().insertNewDataPropertyStatement(extidStmt);
                } else {
                    throw new Error("Updating of DataPropertyStatements not supported in semweb-align");
                    //log.warn("updating of ExternalId is not yet supported");
                }
            }
        }
    }

    /**
     *  assumes that the thing is new,
     * OVERRIDE to get something different.
     */
    public boolean entityIsNew(Individual entity){
         if( entity.getURI() != null )
             return false;
         else
             return true;
    }

    /**
     *  assumes that the thing is new,
     * OVERRIDE to get something different.
     */
    public boolean ents2entsIsNew(ObjectPropertyStatement e2e){
        return true;
    }

    /**
     *  assumes that the thing is new,
     * OVERRIDE to get something different.
     */
    public boolean ents2dataIsNew(DataPropertyStatement e2d){
        return true;
    }

    public boolean externalIdIsNew(DataPropertyStatement extid){
         if( extid.getIndividualURI() != null )
             return false;
         else
             return true;
    }


    Logger log;

    public void endOfParsing() {
        // TODO Auto-generated method stub

    }
}
