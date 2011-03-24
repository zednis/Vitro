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
import java.util.TreeMap;

import javax.xml.xpath.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Gets a value using an xpath2.0 expression, check externalIds for a row
 * where externalIdType and value match, get Entity with given id, makes
 * an Ents2Ents Object between the found Entity and the Target entity with
 * the given propId and the given targetEntdomainSide direction.
 *
 * The object is not written to the DB, it is just added to the Ents2Ents
 * list on the targetEnt.  Use the Ents2EntsWriter later in the actions
 * list to write to the DB.
 *
 * @author bdc34
 *
 */
public class RelationResolver extends BaseXPath2Handler {
    private static final Log log = LogFactory.getLog(RelationResolver.class.getName());
    String externalIdPropertyURI;
    String propertyURI;
    boolean targetEntDomainSide;
    boolean setQualifier;
    WebappDaoFactory webappDaoFactory;
    IndividualDao resolver;
    String vclassURIOfOther = null;
    StringProcessor preprocess;
    String datatypeUri = null;
    String lang = null;
    TreeMap<String,String> value2Uri = null;
    boolean useCaching = true;
    
    private Set <String> missingNames;

    /**
     *
     * @param xpath2 - xpath(v2.0) to the values
     * @param externalIdType - type of external id. Look in EntityDao for valid values.
     * Note that EntityDao.ENTITY_NAME as a externalIdType will match against the values
     * found in entities.name not in externalids.value.
     * @param propId - property Id to use if a canidate for the relation is found
     * @param targetEntDomainSide - if true the targetEnt will be the domain and the
     * canidate entity found via externalId will be the range of the ents2ents relation.
     * @param setQualifier - qualifier to put in ents2ents.qualifier
     * @param resolver - object to use to make db calls.
     * @throws XPathException
     */
    public RelationResolver(String xpath2, String externalIdPropertyURI, String propertyURI,
                            boolean targetEntDomainSide, boolean setQualifier,
                            WebappDaoFactory resolver)
    throws XPathException{
        this(xpath2,externalIdPropertyURI,propertyURI,targetEntDomainSide,setQualifier,resolver,null);
    }

    /**
     * Same as other constructor except vclassOfOtherEnt
     *
     * @param xpath2
     * @param externalIdType
     * @param propId
     * @param targetEntDomainSide
     * @param setQualifier
     * @param resolver
     * @param vclassOfOtherEnt - specify the vclass of the entity that is being
     * looked up by externalid. a 0 will indicate that any vclass will do.
     * @throws XPathException
     */
    public RelationResolver(String xpath2, String externalIdPropertyURI, String propertyURI,
            boolean targetEntDomainSide, boolean setQualifier,
            WebappDaoFactory webappDaoFactory, String vclassURIOfOtherEnt) throws XPathException {
        this(xpath2,externalIdPropertyURI,propertyURI,targetEntDomainSide,
                setQualifier,webappDaoFactory,vclassURIOfOtherEnt,null);
    }

    public RelationResolver(String xpath2, String externalIdPropertyURI, String propertyURI,
            boolean targetEntDomainSide, boolean setQualifier,
            WebappDaoFactory webappDaoFactory, String vclassURIOfOtherEnt,
            StringProcessor sp) throws XPathException {
        this(xpath2,externalIdPropertyURI,propertyURI,targetEntDomainSide,
                setQualifier,webappDaoFactory,vclassURIOfOtherEnt,null, null, null,true);
    }
    
    public RelationResolver(String xpath2, String externalIdPropertyURI, String propertyURI,
            boolean targetEntDomainSide, boolean setQualifier,
            WebappDaoFactory webappDaoFactory, String vclassURIOfOtherEnt,
            StringProcessor sp,String datatypeUri, String lang, boolean useCaching) throws XPathException {
        super(xpath2);
        if (webappDaoFactory == null)
            throw new Error(
                    "You must pass a WebappDaoFactory to the constructor");
        if( lang != null && datatypeUri != null)
            throw new Error("You may pass lang or datatypeUri, not both.");
        
        this.externalIdPropertyURI = externalIdPropertyURI;
        this.propertyURI = propertyURI;
        this.targetEntDomainSide = targetEntDomainSide;
        this.webappDaoFactory = webappDaoFactory;
        this.resolver = webappDaoFactory.getIndividualDao();
        this.setQualifier = setQualifier;
        this.missingNames = new HashSet<String>();
        this.vclassURIOfOther = vclassURIOfOtherEnt;
        this.preprocess = sp;
        this.lang = lang;
        this.datatypeUri = datatypeUri;
        this.value2Uri = new TreeMap<String,String>();
        this.useCaching = useCaching;
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
            log.error("targetEnt was not an Individual; it was of type " + targetEnt.getClass().getName());
        }
        Individual target = (Individual)targetEnt;
        log.debug("trying to get individual with exid of " + value + " and exidtype " + externalIdPropertyURI +
                " and "  + ( vclassURIOfOther==null ? "any vclass": "vclass of " +vclassURIOfOther + " or subclass") );
        Individual otherEnt = null;
        
        // The new DAO method doesn't let us limit to a class subtree, so we'll have to do it ourselves.
        Set<String> allowableVClassURIs = new HashSet<String>();
        if (vclassURIOfOther != null) {
	        allowableVClassURIs.add(vclassURIOfOther);
	        List<String> subclasses = webappDaoFactory.getVClassDao().getSubClassURIs(vclassURIOfOther);
	        allowableVClassURIs.addAll(subclasses);
        }
        
        if( useCaching && value2Uri.containsKey(value) ){
            otherEnt = resolver.getIndividualByURI(value2Uri.get(value));
            log.debug("found in cache");
        }else{
            List<Individual> inds = resolver.getIndividualsByDataProperty(externalIdPropertyURI, value, datatypeUri,lang);
            if( inds != null ){
                if (vclassURIOfOther == null) {
                    if( inds.size() > 1 ) { //multiple values with same id
                        //check to see if all statements have same subject.
                        boolean allSameSub = true;
                        otherEnt = inds.get(0);
                        String subUri = otherEnt.getURI();
                        for(Individual ind : inds ){
                            if( ind == null || ind.getURI() == null || "".equals(ind.getURI())){
                                continue; //should we ignore bnodes?
                            } else if( !subUri.equals(ind.getURI()) ){
                                allSameSub = false;
                                break;
                            }
                        }
                        if( allSameSub ){
                            otherEnt = inds.get(0);
                        }else{
                            log.debug("Skiping: Found multiple values with external id "
                                    + externalIdPropertyURI + " and value " + value);
                            otherEnt = null;
                        }
                    }else if (inds.size() == 1 ){
                        otherEnt = inds.get(0);                
                    }
                } else {
                    for (Individual ind : inds) {
                        for (VClass vclass : ind.getVClasses()) {
                            if (allowableVClassURIs.contains(vclass.getURI())) {
                                otherEnt = ind;
                                break;
                            }
                        }
                    }
                    if( log.isDebugEnabled() && inds != null && inds.size() > 0 && otherEnt == null ){
                        log.debug("Found " + inds.size() + " individuals with extId " +
                        		"but none were of type " + vclassURIOfOther);                    
                    }
                }
            }
            if( useCaching && otherEnt != null && otherEnt.getURI() != null ){
                value2Uri.put(value, otherEnt.getURI());            
                log.debug("added to cache");
            }
        }
        
        if( otherEnt == null ){
            log.debug("could not resolve externalid with value " +
                       value +" and type " + externalIdPropertyURI);
            String mes="Value: " + value + " exidType: " + externalIdPropertyURI +
                " vclassOfOther: " + ( vclassURIOfOther==null ? "any vclass": vclassURIOfOther + " or subclass");
            missingNames.add(mes);
            return;
        }else{
            log.debug("got individual" +
            		" with name " + otherEnt.getName());
        }
        ObjectPropertyStatement e2e = new ObjectPropertyStatementImpl();
        e2e.setPropertyURI(propertyURI);
        if( setQualifier )
            e2e.setQualifier(value);
        if( targetEntDomainSide ){
            e2e.setSubjectURI(target.getURI());
            e2e.setObjectURI(otherEnt.getURI());
            List <ObjectPropertyStatement> zz = target.getObjectPropertyStatements();
            if( zz == null ){
                zz = new ArrayList<ObjectPropertyStatement>();
                target.setObjectPropertyStatements(zz);
            }
            zz.add(e2e);
        }else{  // TODO what to do about this
            //e2e.setObjectURI(target.getURI());
            //e2e.setSubjectURI(otherEnt.getURI());
            //List <ObjectPropertyStatement> zz = target.getRangeEnts2Ents();
            //if( zz == null ){
            //    zz = new ArrayList<ObjectPropertyStatement>();
            //    target.setRangeEnts2Ents(zz);
            //}
            //zz.add(e2e);
        }
    }

    public boolean displayBytesWhenMissing;

    @Override
    public void endOfParsing(){
        if( missingNames.size() > 0 ){
            log.info("Could not find entity with externalid type of "
                    + externalIdPropertyURI + " for use as " + (targetEntDomainSide?"domain":"range")
                    + " in property " + propertyURI + " superVClassURI: " + vclassURIOfOther);
            for( String str : missingNames){
                log.info("Problem finding entity for name '" + str + "', not found or multiple entities with same name.");
                if( displayBytesWhenMissing ){
                    byte bytes[] = str.getBytes();
                    for( int i=0; i< str.length(); i++){
                        System.out.println( "'" + str.charAt(i) +"' " +  bytes[i]  );
                    }
                }
            }
        }
    }
}
