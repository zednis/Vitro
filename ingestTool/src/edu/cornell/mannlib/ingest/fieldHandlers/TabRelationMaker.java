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
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Gets a value using xpath 2.0, uses label2tabId to map the value to a
 * tabid, looks for a tab with the tabid,
 * if it exists, try to make a tab2ent2 row.
 *
 * @author bdc34
 *
 */
public class TabRelationMaker extends BaseXPath2Handler {
    WebappDaoFactory webappDaoFactory;
    Map<String,Integer> label2tabId;

    private static final Log log = LogFactory.getLog(TabRelationMaker.class.getName());

    /**
     * @param xpath2
     * @param tabName2tabId - map of strings to the tab ids they should be manually linked with.
     * @param strProcessors
     * @throws XPathExpressionException
     * @throws XPathFactoryConfigurationException
     */
    public TabRelationMaker(String xpath2,
            Map<String,Integer> label2tabId,
            List<StringProcessor> strProcessors,
            WebappDaoFactory webappDaoFactory)
    throws XPathFactoryConfigurationException, XPathExpressionException{
        super(xpath2);

        addAllStrProcessors(strProcessors);
        this.webappDaoFactory = webappDaoFactory;
        this.label2tabId = label2tabId;

        if( label2tabId == null )
            log.warn("No label2tabId mapping passed, nothing can be done.");
        if( webappDaoFactory == null )
            log.error("TabRelationMaker is was passed a null DAO factory");
    }

    /**
     * Take the incomming String, look for a tabId with that label in the map,
     * if that tabid and the tab exists, look for a tab2ents between that tab and this ent.
     * if that does not exist, make it.
     */
    @Override
    public void handleNode(String node, Element ele, Object targetEnt) {
        if( targetEnt == null ) return;
        if( node == null || node.trim().length() == 0 ){
            if( log.isDebugEnabled() )
                log.debug("Incomming string was empty, skipping");
            return;
        }
        if( ! (targetEnt instanceof Individual)){
            log.error("Must be passed an Entity object");
            return;
        }
        Individual ent = (Individual) targetEnt;
        
        if( ent.getURI() == null ) {
             log.error("entity had no URI: " + ent.getURI());
             return;
        }

        if( label2tabId.containsKey(node)){
            int tabid = label2tabId.get(node);
            Tab tab = getTab(tabid, webappDaoFactory);
            if( tab == null )
                log.error("no tab with id '" + tabid + "' could be found");

            webappDaoFactory.getTabs2EntsDao().insertTabIndividualRelation(tab, ent);
        } else
            log.warn("the label " + node + " could not be mapped to a tabid");
    }

    public Tab getTab(int tabId, WebappDaoFactory wadf){
        Tab tab = null;
        tab = wadf.getTabDao().getTab(tabId);
        return tab;
    }

}
