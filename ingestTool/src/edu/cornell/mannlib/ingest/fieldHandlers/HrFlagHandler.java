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

import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

public class HrFlagHandler implements FieldHandler {

    int[] propertyIds = {506,507,512,396,374};
    IndividualDao entityDao ;

    HrFlagHandler( IndividualDao ewd ){
        entityDao = ewd;
    }

    public void setFields(Element ele, Object obj) {
//        try {
//            EntityWebapp theEnt = (EntityWebapp) obj;
//            try {
//                //TODO: update for semweb-align
//                if(true) throw new Error("TODO: update for semweb-align");
//            //entityDao.setFlagsForEntityFromRelatedEntities( theEnt.getId(),
//            //      propertyIds, false);
//            } catch (SQLException e) {
//                // log.error("HrFlagHandler encountered SQL exception calling setFlagsForEntityFromRelatedEntities()
//            }
//        } catch (ClassCastException e) {
//            // log.warn("HrFlagHandler expects an Entity object");
//        }
    }


    public void endOfParsing() {
        // ?
    }
}
