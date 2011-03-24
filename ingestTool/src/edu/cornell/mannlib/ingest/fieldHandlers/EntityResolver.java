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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;

/**
 * This handler will take
 * the incomming targetEnt and try to figure out if it is
 * already in the vitro system as an Entity.  If it is
 * the values in the targetEnt will be overwriten with
 * the values from the db.
 *
 * If no Entity is found that matches this one then a new
 * Entity will be created.
 *
 *
 * @author bdc34
 *
 */
public class EntityResolver implements FieldHandler {

    String externalIdPropertyURI = null;
    IndividualDao wEntDao;
    XPath xpath;
    String xpathStr;
    boolean createNewEntityWhenNoneFound=true;
    private String datatypeUri;
    private String lang; 

    private static final Log log = LogFactory.getLog(EntityResolver.class.getName());

    /**
     *
     * @param externalIdType - external id type from the table externalidtypes
     * notice that cornell_netid will get a '@cornell.edu' appended if not found.
     * @param xpath2value - xpath to some text that will be searched for in the
     * table externalid in the column value
     */
    public EntityResolver(String externalIdPropertyURI, String xpath2value, IndividualDao resolver){
        if( resolver == null )
            throw new Error("you must set a VitroFacade for a EntityResolver");
        if( xpath2value == null )
            throw new Error("You must set an xpath to the externalid value");

        this.wEntDao = resolver;
        this.externalIdPropertyURI = externalIdPropertyURI;
        this.xpathStr = xpath2value;
        this.xpath = DocumentHelper.createXPath( xpath2value );
    }

    public EntityResolver(String externalIdPropertyURI, String xpathStr, IndividualDao resolver,
            boolean createNewEntityWhenNoneFound) {
        this(externalIdPropertyURI,xpathStr,resolver);
        this.createNewEntityWhenNoneFound = createNewEntityWhenNoneFound;
    }

    public EntityResolver(String externalIdPropertyURI, String xpathStr, IndividualDao resolver,
            boolean createNewEntityWhenNoneFound, String datatypeUri, String lang) {
        this(externalIdPropertyURI,xpathStr,resolver,createNewEntityWhenNoneFound);
        if( lang != null && datatypeUri != null )
            throw new Error("Only one of lang or datatypeUri may be set.");
        this.datatypeUri = datatypeUri;
        this.lang = lang;        
    }

    public void setFields(Element ele, Object targetEnt) {
        if( !(targetEnt instanceof Individual)){
            log.warn("Object passed to EntityResolve was not a "+Individual.class.getName()+" object." +
                    "  It was of type " + targetEnt.getClass().getName());
            return;
        }

        List nodes = xpath.selectNodes( ele );
        String value = null;

        if( nodes == null || nodes.size() == 0){
            log.warn("No value was selected for " + xpathStr);
            return;
        }
        try {
            value = ((Node)nodes.get(0)).getText();
        } catch (ClassCastException e) {
            if (nodes.get(0) instanceof String)
                value = (String) nodes.get(0);
        }

        if( value == null || value.length() == 0 ){
            log.warn("No value was selected for " + xpathStr);
            return;
        }
        //value = value.replaceAll("\n", "");
        //value = value.replaceAll(" ", "");      // bdc34: WHAT IS THAT ABOUT?! We want to strip spaces?
        //sounds like a very bad idea if externalIdType is not netid
        //maybe we should have a subclass that is just for netid?
        //This should be done by a StringProcessor

        Individual ent = null;
        
        log.debug("checking for externalId type " + externalIdPropertyURI + " with value '" + value + "'");
        List<Individual> inds = wEntDao.getIndividualsByDataProperty(externalIdPropertyURI, value, datatypeUri, lang);
        if (inds.size()>0) {
        	ent = inds.get(0);
        	if (inds.size()>1) {
        		log.warn(inds.size()+" individuals retrieved for external ID property "+externalIdPropertyURI+" value "+value);
        	}
        }       
        
        if( ent == null ){
            if( createNewEntityWhenNoneFound){
                //FIXME: BUG!!! this doesn't work since getEntityByExternalId() returns null if there are more than one
                //entities with the given external id
            	String uri = null;
            	try {
            		uri = wEntDao.insertNewIndividual((Individual)targetEnt );
            	} catch (InsertException ie) {
            		log.error("Error inserting new individual: ",ie);
            	}
                log.debug("inserted a new entity with URI " + uri);    
                ((Individual)targetEnt).setURI(uri);
                //TODO: should we also insert the externalId here too?
            }
        } else {
            Individual wEnt = null;
            //TODO: update for semweb-align
            wEnt = wEntDao.getIndividualByURI(ent.getURI()); // TODO: check if this is what we really want
            shallowCopy(wEnt,(Individual)targetEnt);
            log.debug("found existing entity for key " + value + " entity.id " + ((Individual)targetEnt).getURI());
        }
    }

    private void shallowCopy(Individual from, Individual to){
      to.setURI(from.getURI());
      to.setName(from.getName());
      to.setDescription(from.getDescription());
      /* bdc34: more might be needed here */
   }
    
    public void endOfParsing() {
        // TODO Auto-generated method stub
    }
}
