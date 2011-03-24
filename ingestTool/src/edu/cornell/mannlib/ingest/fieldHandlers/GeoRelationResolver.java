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

import javax.xml.xpath.XPathException;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.ingest.processors.GeoNameClean;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Grabs an XPath query value, checks to see if it has a different name
 * based on some code from the Load2005ImpactStatements.
 *
 *
 * @author bdc34
 *
 */
public class GeoRelationResolver extends RelationResolver {

    public GeoRelationResolver(String xpath2, String externalIdPropertyURI,
            String propertyURI, boolean targetEntDomainSide,
            boolean setQualifier, WebappDaoFactory webappDaoFactory,
            String vclassURIOfOtherEnt, StringProcessor sp, String datatypeUri,
            String lang) throws XPathException {
        super(xpath2, externalIdPropertyURI, propertyURI, targetEntDomainSide,
                setQualifier, webappDaoFactory, vclassURIOfOtherEnt, sp, datatypeUri,
                lang,true);
        addStrProcessor( new GeoNameClean() );
    }

    public GeoRelationResolver(String xpath2, String externalIdPropertyURI,
            String propertyURI, boolean targetEntDomainSide,
            boolean setQualifier, WebappDaoFactory webappDaoFactory,
            String vclassURIOfOtherEnt, StringProcessor sp)
            throws XPathException {
        super(xpath2, externalIdPropertyURI, propertyURI, targetEntDomainSide,
                setQualifier, webappDaoFactory, vclassURIOfOtherEnt, sp);
        addStrProcessor( new GeoNameClean() );
    }

    public GeoRelationResolver(String xpath2, String externalIdPropertyURI,
            String propertyURI, boolean targetEntDomainSide,
            boolean setQualifier, WebappDaoFactory webappDaoFactory,
            String vclassURIOfOtherEnt) throws XPathException {
        super(xpath2, externalIdPropertyURI, propertyURI, targetEntDomainSide,
                setQualifier, webappDaoFactory, vclassURIOfOtherEnt);
        addStrProcessor( new GeoNameClean() );
    }

    public GeoRelationResolver(String xpath2,
            String externalIdPropURI, String propURI, boolean targetEntDomainSide,
            boolean setQualifier, WebappDaoFactory resolver) throws XPathException {
        super(xpath2, externalIdPropURI, propURI, targetEntDomainSide, setQualifier,
                resolver);
        addStrProcessor( new GeoNameClean() );
    }

//    /**
//     * Just intercept the string and see if we have a replacement for it,
//     * then call super handleNode.
//     */
//    @Override
//    public void handleNode(String value, Document doc, Object targetEnt){
//        if( value == null ) return;
//
//        if (value.equals("New York")) {
//            value = "New York State";
//        } else if (value.equals("U.S.") || value.equals("USA") ||
//                value.equals("All fifty states.") || value.indexOf("Many of the states")>0) {
//            value = "United States";
//        } else if (value.equals("Northeast U.S.") || value.equals("Northeast U.S") ||
//                value.equals("Northeast States")) {
//            value = "northeastern U.S.";
//        } else if (value.equals("UK") || value.equals("U.K.")) {
//            value = "United Kingdom";
//        } else if (value.equals("Global") || value.equals("World") || value.equals("International")) {
//            value = "international";
//        } else if (value.equalsIgnoreCase("Washington State")) {
//            value = "Washington";
//        } else if (value.equalsIgnoreCase("Washington, D.C.")) {
//            value = "Washington D.C.";
//        } else if (value.equalsIgnoreCase("Pennsylvania.")) {
//            value = "Pennsylvania";
//        }
//
//        super.handleNode(value,doc,targetEnt);
//    }
}
