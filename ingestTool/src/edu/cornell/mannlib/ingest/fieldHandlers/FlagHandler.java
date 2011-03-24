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

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

public class FlagHandler implements FieldHandler {
    int[] propertyIds;
    boolean entityDomain = true;
    boolean push = false;

    private Logger log = Logger.getLogger(FlagHandler.class);

    public static final boolean PUSH = true;
    public static final boolean PULL = false;

    public static boolean DOMAIN=true;
    public static boolean RANGE=false;
    private IndividualDao entityWebappDao=null;

    /**
     * @param entityDomain - indicates if the entityId entity is on the domain
     * @param propertyIds
     * @param domainVitroFacade
     */
    public FlagHandler(int[] propertyIds, boolean entityDomain, IndividualDao dao) {
        super();
        if( propertyIds == null )
            throw new IllegalArgumentException("must pass a propertyIds array.");
        if( dao == null )
            throw new IllegalArgumentException("must pass an EntityDao.");
        this.propertyIds = propertyIds;
        this.entityWebappDao = dao;
        this.entityDomain = entityDomain;
        this.push = false;
    }

    public FlagHandler(int[] propertyIds, boolean entityDomain, IndividualDao dao, boolean push) {
        this(propertyIds, entityDomain,dao);
        this.push = push;
    }

    public void setFields(Element ele, Object targetEnt) {
        if( targetEnt == null )
            { log.warn("passed null target entity"); return;  }
        if(!( targetEnt instanceof Individual))
            { log.warn("passed non Entity target entity"); return; }
//        try {
//            if( push ){
////TODO: update for semweb-align
//                if(true) throw new Error("TODO: update for semweb-align");
//            //                entityWebappDao.pushFlagsToRelatedEntities(((Entity) targetEnt).getId(),
//            //                        propertyIds, entityDomain,true,false,false);
//            }else{
//                //TODO: update for semweb-align
//                if(true) throw new Error("TODO: update for semweb-align");
////                 entityWebappDao.setFlagsForEntityFromRelatedEntities(((Entity) targetEnt).getId(),
////                         propertyIds, entityDomain);
//            }
////        } catch(SQLException e) {
////            log.error("encountered SQL exception calling " +
////                    "setFlagsForEntityFromRelatedEntities():", e);
////        }
    }

    public void endOfParsing() {
    }
}
