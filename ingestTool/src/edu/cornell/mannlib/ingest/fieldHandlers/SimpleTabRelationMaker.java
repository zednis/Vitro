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

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Gets a value using xpath 2.0, if value is non-null and non-zero-length
 * then make the tab relation.
 * @author bdc34
 *
 */
public class SimpleTabRelationMaker extends BaseXPath2Handler {
    WebappDaoFactory webappDaoFactory;
    Tab tab;

    private static final Log log = LogFactory.getLog(TabRelationMaker.class.getName());

    /**
     * @param xpath2
     * @param strProcessors
     * @param tabId id of tab to link to.
     * @throws XPathExpressionException
     * @throws XPathFactoryConfigurationException
     */
    public SimpleTabRelationMaker(String xpath2,
            int tabId,
            List<StringProcessor> strProcessors,
            WebappDaoFactory webappDaoFactory)
    throws XPathFactoryConfigurationException, XPathExpressionException{
        super(xpath2);

        addAllStrProcessors(strProcessors);
        this.webappDaoFactory = webappDaoFactory;

        if(webappDaoFactory == null )
            log.error(this.getClass().getName()+" was passed a null WebappDaoFactory");
        if( (this.tab = getTab( tabId, webappDaoFactory)) == null )
            throw new Error("there is no tab with id " + tabId);
    }

    /**
     * Take the incomming String, if string is non-zero-length and
     * if that tabid and the tab exists, look for a tab2ents between that tab and this ent.
     * if that does not exist, make it.
     */
    @Override
    public void handleNode(String node, Element ele, Object targetEnt) {
        if( targetEnt == null ) return;
        if( node == null || node.trim().length() == 0 )
            return;
        if( ! (targetEnt instanceof Individual)){
            log.error("Must be passed an Individual object");
            return;
        }
        Individual ent = (Individual) targetEnt;
        if( ent.getURI() == null) {
        	log.error("individual had no URI");
            return;
        }
        webappDaoFactory.getTabs2EntsDao().insertTabIndividualRelation(tab, ent);
    }

    public Tab getTab(int tabId, WebappDaoFactory webappDaoFactory){
        Tab tab = null;
        tab = webappDaoFactory.getTabDao().getTab(tabId);
        return tab;
    }

}
