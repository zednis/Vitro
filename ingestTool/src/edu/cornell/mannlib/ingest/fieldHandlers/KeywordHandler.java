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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Associate targetEnt with a Keyword entity.  If the
 * keyword entity does not exist, create the entity.
 *
 *  The vclassOfKeyword indicates which vclass the keyword
 *  should be from.  The different vclasses can be used to
 *  group the keywords into different groups.  Either to
 *  avoid name conflicts or to indicate that the given names
 *  came from a given ontology.
 *
 *
 * @author bdc34
 */
public class KeywordHandler extends BaseXPath2Handler {

    protected WebappDaoFactory webappDaoFactory;
    protected String vclassOfKeyword = null;
    protected StringProcessor preprocess;
    protected int propertyId;
    protected boolean targetEntDomainSide = true;
    protected String externalIdType = RDFS.label.getURI();

    protected Set<String> missingKeywords;
    private static final Log log = LogFactory.getLog(KeywordHandler.class.getName());

    /**
     *
     * @param xpathv2 - xpath to get the name of the keyword entity.
     * @param facade - use to interact with db
     * @param vclassOfKeyword -The vclassOfKeyword indicates which vclass the keyword
     *  should be from.  The different vclasses can be used to
     *  group the keywords into different groups.  Either to
     *  avoid name conflicts or to indicate that the given names
     *  came from a given ontology.
     * @param preprocess - process the value of xpathv2.  Set to null if not needed.
     * @param propertyId - Id of property to use between entity and keyword.
     * Entity will be domain side.
     *
     * @throws XPathFactoryConfigurationException
     * @throws XPathExpressionException
     */
    public KeywordHandler(String xpathv2, WebappDaoFactory webappDaoFactory, String vclassOfKeyword,
            StringProcessor preprocess, int propertyId)
    throws XPathFactoryConfigurationException, XPathExpressionException {
        super(xpathv2);
        this.webappDaoFactory = webappDaoFactory;
        this.vclassOfKeyword = vclassOfKeyword;
        this.preprocess = preprocess;
        this.propertyId = propertyId;
        this.missingKeywords = new HashSet<String>();
    }


    @Override
    public void handleNode(String value, Element ele, Object targetEnt) {
        if( value == null || value.trim().length() == 0 )
            return;
        if( preprocess != null )
            value = preprocess.process(value);

        if( targetEnt == null )
            log.debug("targetEnt was null");

        if( !( targetEnt instanceof Individual )){
            log.error("targetEnt was not an Entity, it was of type " + targetEnt.getClass().getName());
        }

        log.debug("trying to get entity with exid of " + value + " and exidtype " + externalIdType +
                " and "  + ( vclassOfKeyword==null ? "any vclass": "vclass of " +vclassOfKeyword + " or subclass") );

        Individual target = (Individual)targetEnt;
        Individual otherEnt = getEntityForKeyword(value,this.externalIdType,this.vclassOfKeyword);

        if( otherEnt == null ){
            missingKeywords.add(value);
            return;
        }else
            log.debug("got enity with name " + otherEnt.getName());

        associateTargetAndKeyword(target,otherEnt, propertyId, this.targetEntDomainSide);
    }

    protected void associateTargetAndKeyword(Individual target, Individual kwEnt, int propertyId2,
            boolean targetEntDomainSide2) {
        ObjectPropertyStatement e2e = new ObjectPropertyStatementImpl();
        //TODO: update for semweb-align
        if(true) throw new Error("TODO: update for semweb-align");
            //e2e.setPropertyId(propertyId);

        if( targetEntDomainSide ){
            //TODO: update for semweb-align
            if(true) throw new Error("TODO: update for semweb-align");
            //e2e.setDomainId(target.getId());
            //TODO: update for semweb-align
            if(true) throw new Error("TODO: update for semweb-align");
            //e2e.setRangeId(kwEnt.getId());
            List <ObjectPropertyStatement> zz = target.getObjectPropertyStatements();
            if( zz == null ){
                zz = new ArrayList<ObjectPropertyStatement>();
                target.setObjectPropertyStatements(zz);
            }
            zz.add(e2e);
        }else{
//            //TODO: update for semweb-align
//            if(true) throw new Error("TODO: update for semweb-align");
//            //e2e.setRangeId(target.getId());
//            e2e.setSubjectURI(kwEnt.getURI());
//            List <ObjectPropertyStatement> zz = target.getRangeEnts2Ents();
//            if( zz == null ){
//                zz = new ArrayList<ObjectPropertyStatement>();
//                target.setRangeEnts2Ents(zz);
//            }
//            zz.add(e2e);
        }
    }


    /**
     * Gets entity id of keyword.
     *
     * @param Value
     * @param externalIdType
     * @param vclassOfKeyword
     * @return
     */
    protected Individual getEntityForKeyword(String value, String externalIdPropertyURI, String vclassOfKeywordURI){
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
//        //int count = facade.entityExists(externalIdType, value, vclassOfKeyword);
//        int count = 0;
//        if( count > 1 ){
//            log.error("Two keywords exist in the vclass " + vclassOfKeyword + " with the name " + value);
//            return null;
//        }
//        if( count == 0 ){
//            int id = makeNewKeyword(value, vclassOfKeyword, this.facade);
//            //TODO: update for semweb-align
//            if(true) throw new Error("TODO: update for semweb-align");
//            //return facade.entityById(id);
//            return null;
//        } else {
////TODO: update for semweb-align
//            if(true) throw new Error("TODO: update for semweb-align");
//            //return facade.getEntityByExternalId(externalIdType, value, vclassOfKeyword);
//            //return null;//TODO: update for semweb-align
//        }
        return null;
    }

    protected String makeNewKeyword(String value, int vclassOfKeyword2, WebappDaoFactory facade) {
        Individual newKeyword = new IndividualImpl();
        newKeyword.setName(value);
        //TODO: update for semweb-align
        if(true) throw new Error("TODO: update for semweb-align");
            //newKeyword.setVClassId(vclassOfKeyword2);
        String uri = null;
        try {
        	uri = facade.getIndividualDao().insertNewIndividual(newKeyword);
        } catch (InsertException ie) {
        	log.error("Unable to insert individual: ",ie);
        }
        log.debug("made new keyword entity " + value + " entId: " + uri);
        return uri;
    }

    @Override
    public void endOfParsing(){
        if( missingKeywords.size() > 0 ){
            log.info("Could not find keyword entity "  +
                    " for use as " + (targetEntDomainSide?"domain":"range")
                    + " in property " + propertyId + " superVClassId: " + vclassOfKeyword);
            for( String str : missingKeywords){
                log.info("Problem finding Keyword entity for name '" + str
                        + "', not found or multiple entities with same name.");
            }
        }
    }

}
