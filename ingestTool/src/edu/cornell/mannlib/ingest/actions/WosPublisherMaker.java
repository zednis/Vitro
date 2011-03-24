package edu.cornell.mannlib.ingest.actions;

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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.utils.TitleCase;

/**
 * attempt so make a publisher for any publisher listed in
 * the document.
 * @author bdc34
 *
 */
public class WosPublisherMaker implements IngestAction {
    String xpath;
    WebappDaoFactory webappDaoFactory;
    int vclass;

    private static final Log log = LogFactory.getLog(WosPublisherMaker.class.getName());

    public WosPublisherMaker(WebappDaoFactory webappDaoFactory, int vclass, String xpath){
        this.xpath = xpath;
        this.webappDaoFactory = webappDaoFactory;
        this.vclass = vclass;
    }

    public void doAction(Element record) {
        log.debug("doing WosPublisherMaker");
        if( record == null )
            return;
        List nodes = record.selectNodes(xpath);
        if( nodes == null ) {
            log.debug("no publishers found");
            return;
        }

        Iterator it = nodes.iterator();
        while(it.hasNext()){
            Object obj = it.next();
            String name = null;
            if( obj instanceof Node){
                Node n =(Node)obj;
                name = n.getText();
            }
            if( obj instanceof String )
                name = (String)obj;

            name = TitleCase.toTitleCase(name);

            //if( facade.entityExists(EntityDaoDb.ENTITY_NAME, name, vclass)==0 ){
            //TODO: update for semweb-align
            String vclassStr = "fixme";
            if(true) throw new Error("update "+this.getClass().getName()+" for semweb-align"); 
            //if( webappDaoFactory.individualExists(EntityDaoDb.ENTITY_NAME, name, vclassStr)==0 ){
                //Individual newEnt = new Individual();
                //todo set class
                //newEnt.setVClassId(vclassStr);
                //newEnt.setVClassURI(vclassStr);

                //newEnt.setName(name);
                //int id = facade.insertNewIndividual(newEnt);
                //log.debug("inserted new entity with id " + id +
                //        " for " + name);
            //}else{
            //    log.debug("found entity with name " + name );
            //}
        }

    }

    public void endOfParsing() {}

}
