package edu.cornell.mannlib.ingest.interfaces;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.parser.SkipRecord;

/**
 * This is the interface to a parser that takes a InputStream and
 * does something with that.
 *
 * @author bdc34
 *
 */
public abstract class IngestParser {
    List<IngestAction> actions;
    private static final Log log = LogFactory.getLog(IngestParser.class.getName());

    public abstract void parse( );

    public void setActions(List<IngestAction> actions){
        this.actions = actions;
    }
    public List<IngestAction>getActions(){
        return this.actions;
    }
    public void doSingleRecord( Element doc ){
        doIngestActions(doc);
    }

    public void endOfParsing(){
        for( IngestAction action : actions){
            try{
                action.endOfParsing();
            }catch(Throwable thr){
                log.error("Exception in endOfParsing() : " + thr);
            }
        }
    }

    /**
     * Try to do everything in the actions list.
     *
     * @param doc
     */
    public void doIngestActions( Element doc){
        if( actions == null ){
            log.warn("actions list was null, nothing will be done");
            return;
        }
        for( IngestAction action : actions){
            try{
                action.doAction( doc );
            }catch (SkipRecord sr){
                log.warn("Record skipped " + sr.getMessage());
            }catch ( Throwable thr){
                log.error("Exception in IngestAction of class " +
                        action.getClass().getName() + ", " + action.toString() ,
                        thr);
            }
        }
    }
}
