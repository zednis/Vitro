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

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Runs some other handler if a condition is true.  I'd love
 * to be able to pass some kind of simple anonymous function,
 * no I have to pass a staticly typed something or other.
 *
     *
     * ex, Only do otherFH when targetEnt is a FatDog object:
     *  FieldHandler fh = new ConditionalHandler(otherFH){
     *   condition(Document doc, Object targetEnt ){
     *      if(doc!= null && targetEnt instanceOf FatDog)
     *          return true;
     *      else
     *          return false;
     *   }
     * };
 * @author bdc34
 *
 */
public abstract class ConditionalHandler implements FieldHandler {
    protected FieldHandler handler;
    public ConditionalHandler(FieldHandler handler){
        this.handler = handler;
    }

    /**
     * Override this for the condition

     */
    public abstract boolean condition(Element ele, Object targetEnt);


    public void setFields(Element ele, Object targetEnt) {
        if( handler != null && condition(ele,targetEnt))
            handler.setFields(ele, targetEnt);
    }


    public void endOfParsing() {
        // TODO Auto-generated method stub
    }
}
