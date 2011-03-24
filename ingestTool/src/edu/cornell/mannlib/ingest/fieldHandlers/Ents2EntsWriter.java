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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Attempt to write any Ents2Ents for the targetEnt to the db.
 * @author bdc34
 *
 */
public class Ents2EntsWriter implements FieldHandler {
    private static final Log log = LogFactory.getLog(Ents2EntsWriter.class.getName());
    private WebappDaoFactory webappDaoFactory;
    int propertyId = 0;
    boolean doDomainSide = true;
    boolean doRangeSide = true;

    public Ents2EntsWriter(WebappDaoFactory webappDaoFactory){
        this.webappDaoFactory = webappDaoFactory;
    }

    /**
     *
     * @param facade
     * @param propertyId - only attempt to save ents2ents with this propertyId,
     * 0 indicates save all properties.
     * @param doDomainSide
     * @param doRangeSide
     */
    public Ents2EntsWriter(WebappDaoFactory webappDaoFactory, String propertyURI,
            boolean doDomainSide, boolean doRangeSide) {
        super();
        this.webappDaoFactory = webappDaoFactory;
        this.propertyId = propertyId;
        this.doDomainSide = doDomainSide;
        this.doRangeSide = doRangeSide;
    }

    public void setFields(Element ele, Object targetEnt) {
        if( targetEnt == null ){
            return;
        }
        if( !(targetEnt instanceof Individual )){
            log.warn("Only accepts Entity objects.");
            return;
        }
        Individual ent = (Individual) targetEnt;
        if( ent.getURI() == null ){
            log.warn("invalid entity URI of null");
            return;
        }
        if( doDomainSide )
            saveEnts2Ents(ent.getObjectPropertyStatements(), propertyId);
        //if( doRangeSide )
            //saveEnts2Ents( ent.getRangeEnts2Ents(), propertyId );
    }

    @SuppressWarnings("unchecked")
    public void saveEnts2Ents( List ents2ents, int propertyId){
        if( ents2ents == null ) return;
        List f = null;
        if( propertyId == 0 ){
            f = ents2ents;
        } else {
            f = new ArrayList( ents2ents.size() );
            ObjectPropertyStatement e2e = null;
            Iterator it = ents2ents.iterator();
            while(it.hasNext()){
                e2e=(ObjectPropertyStatement)it.next();
                //TODO: update for semweb-align
                if(true) throw new Error("TODO: update for semweb-align");
            //if( e2e != null && e2e.getPropertyId() == propertyId )
            //      f.add(e2e);
            }
        }
        throw new Error(this.getClass().getName()+" needs updating for semweb-align");
        //webappDaoFactory.getObjectPropertyStatementDao().updateEnts2Ents(f);
    }

    public void endOfParsing() {
        // TODO Auto-generated method stub

    }
}
