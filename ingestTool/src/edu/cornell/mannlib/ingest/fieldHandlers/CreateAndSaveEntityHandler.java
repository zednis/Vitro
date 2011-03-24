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

import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Make a new entity with a name from an XPath value and
 * save it to the database.
 *
 * This will take a value, and look for it as the name of an entity.
 * If that entity exists nothing will happen.
 * If that entity doesn't exist then it will be created immediately in
 * the DB.  It will have the name from the XPath expression.
 *
 * @author bdc34
 *
 */
public class CreateAndSaveEntityHandler extends BaseXPath2Handler {
    WebappDaoFactory webappDaoFactory;
    Individual defaultEntity;
    List<FieldHandler> fieldHandlers;

    private static final Log log = LogFactory.getLog(CreateAndSaveEntityHandler.class.getName());
    /**
     *
     * @param xpathv2
     * @param facade - used to access db
     * @param defaultEntity - will be used as a template to make new entities.  All of
     * the values copied by Entity.shallowCopy will be used in the save to the db
     * except the name and the id.
     * @throws XPathFactoryConfigurationException
     * @throws XPathExpressionException
     */
    public CreateAndSaveEntityHandler(String xpathv2, WebappDaoFactory webappDaoFactory, Individual defaultEntity)
            throws XPathFactoryConfigurationException, XPathExpressionException {
        super(xpathv2);
        if( webappDaoFactory == null ){
            log.error("webappDaoFactory must not be null");
            throw new IllegalArgumentException("facade must not be null");
        }
        if( defaultEntity == null ){
            log.error("defaultEntity must not be null");
            throw new IllegalArgumentException("defaultEntity must not be null");
        }
        this.webappDaoFactory = webappDaoFactory;
        this.defaultEntity= defaultEntity;
    }

    public CreateAndSaveEntityHandler(
            String xpathv2, 
            WebappDaoFactory webappDaoFactory, 
            Individual defaultEntity,
            List<FieldHandler> fieldHandlers)
    throws XPathFactoryConfigurationException, XPathExpressionException {
        this(xpathv2,webappDaoFactory,defaultEntity);
        this.fieldHandlers = fieldHandlers;
    }
    
    @Override
    public void handleNode(String node, Element ele, Object targetEnt) {
        Individual ent = null;
        
        List<Individual> inds = webappDaoFactory.getIndividualDao().getIndividualsByDataProperty(RDFS.label.getURI(),node);
        ent = (inds.size()>0) ? inds.get(0) : null;
        if(ent != null ) //entity already exists so just return.
            return;

        Individual ewa= new IndividualImpl();
        //defaultEntity.shallowCopy(ewa);//copy default values into ewa TODO: examine
        ewa.setName(node);
        String uri = null;
        try {
        	uri = webappDaoFactory.getIndividualDao().insertNewIndividual(ewa);
        } catch (InsertException ie) {
        	log.error("Unable to insert individual: ",ie);
        }
         if(log.isDebugEnabled()){
             log.debug("Created new entity: " + node + " uri: " + uri);
         }
    }

}
