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
import org.dom4j.Node;
import org.dom4j.XPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A field handler that can deal with a xpath 1.0 that returns
 * multiple nodes.  To use this you will have to implement/override
 * the handleNode method.
 *
 * @author bdc34
 *
 */
public abstract class MultiNodeHandler extends AbstractHandler {
    protected XPath xpath;

    public MultiNodeHandler(){    }
    public MultiNodeHandler(XPath xpath){
        this.xpath = xpath;
    }

    /**
     * gets the Dom4j nodes that will be passed to handleNode(), turns
     * them into String objects.
     *
     * @param ele
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getStrings(Element ele){
        List <String> strings = new ArrayList<String>();
        if( xpath == null )
            return strings;

        List<Node> nodes = (List<Node>)xpath.selectNodes( ele );
        for( Node n : nodes ){
            strings.add( n.getText() );
        }
        return strings;
    }
}
