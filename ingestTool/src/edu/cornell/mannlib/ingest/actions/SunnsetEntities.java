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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Sunsets all entities of a vclass where the sunnset is
 * in the given window.
 *
 * @author bdc34
 *
 */
public class SunnsetEntities implements IngestAction {

    Date early;
    Date late;
    int vclassid;
    WebappDaoFactory webappDaoFactory;
    Date newSunset;

    private static final Log log = LogFactory.getLog(SunnsetEntities.class.getName());

    public SunnsetEntities(int vclassid, Date early, Date late, Date newSunset,
            WebappDaoFactory webappDaoFactory) {
        this.early = early;
        this.late = late;
        this.newSunset = newSunset;
        this.vclassid = vclassid;
        this.webappDaoFactory = webappDaoFactory;
    }

    public void doAction(Element record) {
        //TODO: fix this
        //int updatedRows = facade.sunsetEntities(vclassid, early, late, newSunset);
        //log.info("Updated the sunset on " + updatedRows + " to " + newSunset);
    }

    public void endOfParsing() {
        // TODO Auto-generated method stub

    }

}
