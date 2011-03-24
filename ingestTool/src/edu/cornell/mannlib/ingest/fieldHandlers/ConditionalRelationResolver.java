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

import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

/**
 * Gets a value using an xpath2.0 expression, check externalIds for a row
 * where externalIdType and value match, get Entity with given id, makes
 * an Ents2Ents Object between the found Entity and the Target entity with
 * the given propId and the given targetEntdomainSide direction
 * _IF_ the corresponding value of conditionalXpath2 is in the set of
 * allowable values
 *
 * The object is not written to the DB, it is just added to the Ents2Ents
 * list on the targetEnt.  Use the Ents2EntsWriter later in the actions
 * list to write to the DB.
 *
 * @author bjl23
 *
 */
public class ConditionalRelationResolver extends BaseConditionalXPath2Handler {
    private static final Logger log = Logger.getLogger(RelationResolver.class.getName());
    String externalIdPropertyURI;
    String propertyURI;
    boolean targetEntDomainSide;
    boolean setQualifier;
    IndividualDao resolver;

    private Set <String> missingNames;

    public ConditionalRelationResolver(String xpath2, String externalIdPropertyURI, String propertyURI,
                            boolean targetEntDomainSide, boolean setQualifier,
                            IndividualDao resolver, String conditionXpath2, String[] allowableValues, String secondConditionXpath2, String[] secondAllowableValues)
    throws XPathException{
        super(xpath2,conditionXpath2,allowableValues,secondConditionXpath2, secondAllowableValues);
        if( resolver == null )
            throw new Error("You must pass a EntityResolverDao to the constructor");

        this.externalIdPropertyURI = externalIdPropertyURI;
        this.propertyURI = propertyURI;
        this.targetEntDomainSide = targetEntDomainSide;
        this.resolver = resolver;
        this.setQualifier = setQualifier;
        missingNames = new HashSet<String>();
    }

    @Override
    public void handleNode(String value, Element doc, Object targetEnt) {
        if( value == null || value.trim().length() == 0 )
            return;

        if( targetEnt == null )
            log.debug("targetEnt was null");

        if( !( targetEnt instanceof Individual )){
            log.error("targetEnt was not an Entity, it was of type " + targetEnt.getClass().getName());
        }
        Individual target = (Individual)targetEnt;
        log.debug("trying to get entity with exid of " + value + " and exidtype " + externalIdPropertyURI);
        List<Individual> inds = resolver.getIndividualsByDataProperty(externalIdPropertyURI, value);
        Individual otherEnt = (inds.size()>0) ? inds.get(0) : null;
        if( otherEnt == null ){
            log.warn("could not resolve externalid with value " +
                    value +" and type " + externalIdPropertyURI);
            missingNames.add(value);
            return;
        }else{
            log.debug("got enity with name " + otherEnt.getName());
        }
        ObjectPropertyStatement e2e = new ObjectPropertyStatementImpl();
        //TODO: update for semweb-align
        if(true) throw new Error("TODO: update for semweb-align");
        //e2e.setPropertyId(propertyId);
        if( setQualifier )
            e2e.setQualifier(value);
        if( targetEntDomainSide ){
            //TODO: update for semweb-align
            if(true) throw new Error("TODO: update for semweb-align");
            //e2e.setDomainId(target.getId());
            //e2e.setRangeId(otherEnt.getId());
            List <ObjectPropertyStatement> zz = target.getObjectPropertyStatements();
            if( zz == null ){
                zz = new ArrayList<ObjectPropertyStatement>();
                target.setObjectPropertyStatements(zz);
            }
            zz.add(e2e);
        }else{
            //TODO: update for semweb-align
            if(true) throw new Error("TODO: update for semweb-align");
//            //e2e.setRangeId(target.getId());
//            //e2e.setDomainId(otherEnt.getId());
//            List <ObjectPropertyStatement> zz = target.getRangeEnts2Ents();
//            if( zz == null ){
//                zz = new ArrayList<ObjectPropertyStatement>();
//                target.setRangeEnts2Ents(zz);
//            }
//            zz.add(e2e);
        }
    }

    public boolean displayBytesWhenMissing;

    @Override
    public void endOfParsing(){
        if( missingNames.size() > 0 ){
            log.info("Could not find entity with externalid type of "
                    + externalIdPropertyURI + " for use as " + (targetEntDomainSide?"domain":"range")
                    + " in property " + propertyURI);
            for( String str : missingNames){
                log.warn("'" + str + "'");
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