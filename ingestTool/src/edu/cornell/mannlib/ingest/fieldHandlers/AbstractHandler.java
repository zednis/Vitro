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
import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is the base of handlers that extract some set of strings and do
 * an action with them.  This class allows us to add features that will
 * be inherited by all the subclasses.
 *
 * @author bdc34
 *
 */
public abstract class AbstractHandler implements FieldHandler {
    private List <StringProcessor> strProcessors;

    /* *********** Abstract Methods ****************** */
    /**
     * Does something with node.  Override this to make your
     * subclass do something useful such as take the node
     * and stick it in the name field of the targetEnt.
     * @param node
     */
    public abstract void handleNode(String node, Element doc, Object targetEnt);

    /**
     * Somehow goes and gets the values from the document.
     * Override this to do something useful like an xpath query.
     * @param doc
     * @return
     */
    public abstract Collection<String> getStrings(Element doc );


    /* ************** Implemented Methods *************** */
    /**
     * Gets called by the parser for each xml record.
     * Gets a Collection of Strings from getStrings(), and for
     * each String, calls each StringProcessor and then
     * handleNode()
     */
    public void setFields(Element doc, Object targetEnt) {
        Collection <String> theStrings = getStrings(doc);
        if( theStrings != null ){
            for( String str : theStrings ){
                str = doStringProcessing(str);
                handleNode(str, doc, targetEnt);
            }
        }
    }

    /**
     * Add a StringProcessor that will be called on the
     * text from the node before it is passed to handleNode.
     * These will be called in the order they are added.
     *
     * @param strProc
     */
    public void addStrProcessor(StringProcessor strProc){
        if( strProcessors == null ){
            strProcessors = new ArrayList<StringProcessor>();
        }
        strProcessors.add(strProc);
    }

    public void addAllStrProcessors(Collection<StringProcessor> listOfProcessors){
        if( listOfProcessors == null ) return;
        for(StringProcessor st : listOfProcessors){
            addStrProcessor(st);
        }
    }

    /**
     * Executes all of the stringProcessors on a Node's text
     * @param node
     * @return
     */
    public String doStringProcessing(String str){
        if( strProcessors == null || strProcessors.size() == 0)
            return str;

        for( StringProcessor strProc : strProcessors )
             str = strProc.process( str );
        return str;
    }

    /**
     * Called when parsing is over.  Intended for when the
     * handler has to do some house keeping such as close
     * files or db connections.
     * Optionally override to do something useful.
     */
    public /*semi-abstract*/ void endOfParsing(){}

}
