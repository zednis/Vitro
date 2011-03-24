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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.parser.SkipRecord;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;

/**
 * Check to see if the article already exists in the system.
 * Things to check:
 *   article/entity name
 *   other ids?
 *
 * @author bdc34
 *
 */
public class ArticleResolver implements FieldHandler {
    IndividualDao resolver;

    private static final Log log = LogFactory.getLog(ArticleResolver.class.getName());

    public ArticleResolver(IndividualDao resolver){
        this.resolver = resolver;
    }

    public void setFields(Element doc, Object targetEnt) {
        if( !(targetEnt instanceof Individual)){
            log.error("targetEnt was not a Entity object");
            return;
        }
        Individual ent = (Individual)targetEnt;
        int count = 0;
        //get by article name
        //TODO: update for semweb-align
        if(true) throw new Error("TODO: update for semweb-align");
            //count = resolver.entityExists(EntityDaoDb.ENTITY_NAME,
            //  ent.getName(),PUBLICATION_VCLASS_ID);
        if( count == 1 ){
            throw new SkipRecord("Article already exist with the name '"+ ent.getName() +"'");
        } else if( count == 0 ){
        	String uri = null;
        	try {
        		uri = resolver.insertNewIndividual(ent);
        	} catch (InsertException ie) {
        		log.error("Unable to insert new individual: ",ie);
        	}
            log.debug("inserted a new entity with id " + uri);
            //ent.setId(id);
        } else if( count > 0){
            log.debug(count + " entities already exist with the name '"+ ent.getName() +"'");
            throw new SkipRecord(count + " entities already exist with the name '"+ ent.getName() +"'");
        }
    }

    public static int PUBLICATION_VCLASS_ID = 130;
    public void endOfParsing() {}
}
