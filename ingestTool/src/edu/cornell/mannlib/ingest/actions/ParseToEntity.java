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

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Action that attempts to process the xml element into
 * a Vitro entity.
 *
 * @author bdc34
 *
 */
public class ParseToEntity implements IngestAction {
    List <FieldHandler> fieldHandlers;
    WebappDaoFactory webappDaoFactory;
    private static final Log log = LogFactory.getLog(ParseToEntity.class.getName());

    public ParseToEntity( List <FieldHandler> fieldHandlers, WebappDaoFactory webappDaoFactory)
    throws IOException{
        if( webappDaoFactory == null )
            throw new Error("You must pass a WebappDaoFactory");
        if( fieldHandlers == null  || fieldHandlers.size() == 0)
            log.info("no FieldHandlers passed");

        this.fieldHandlers = fieldHandlers;
        this.webappDaoFactory = webappDaoFactory;
    }

    public void doAction(Element input) {
        if( input == null ){
            log.warn("doAction passed a null input, skipping");
            return;
        }
        try{
            //run all the fieldHandlers so we have a entity setup from the input
            Individual targetEnt = new IndividualImpl();
            for( FieldHandler handler : fieldHandlers){
                handler.setFields(input, targetEnt);
                //Hay what happens at the end?
                //You need to have your last handlers do something
                //with the entity that was created
                //check out fieldHandlers.SaveEntity.
            }
         }catch(Exception ex){
            log.error( ex.toString() );
            ex.printStackTrace();
        }

    }

    public void endOfParsing() {
        for(FieldHandler fh : fieldHandlers){
            fh.endOfParsing();
        }
    }

}
