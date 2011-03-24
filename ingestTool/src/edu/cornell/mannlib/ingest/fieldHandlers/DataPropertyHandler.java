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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * This will get some values from xpath and for each node returned
 * it will make a datatype property for the entity.
 *
 * It handles xpath statements that return multiple nodes by making multiple
 * data propertys on the entity.
 *
 * If a a separatorRegex is passed to the constructor then the result of the
 * xpath query will be split() using the separatorRegex.  Each resulting token
 * will be added as a dataProperty.  Pass null as separatorRegex if you don't
 * want splitting.
 *
 * @author bdc34
 *
 */
public class DataPropertyHandler extends MultiNodeHandler {
    String dataPropertyURI;
    String separator;
    
    public DataPropertyHandler(XPath xpath, String separatorRegex, String dataPropertyURI ){
        super(xpath);
        this.dataPropertyURI = dataPropertyURI;
        this.separator = separatorRegex;
    }

    public DataPropertyHandler(String xpathStr,
                               String separatorRegex,
                               String dataPropertyURI,
                               List<StringProcessor>stringProcessors){
        this(DocumentHelper.createXPath( xpathStr ), separatorRegex, dataPropertyURI);
        if( stringProcessors != null){
            for( StringProcessor sp : stringProcessors){
                this.addStrProcessor(sp);
            }
        }
    }

    public DataPropertyHandler(String xpathStr, String dataPropertyURI ){
        this(DocumentHelper.createXPath(xpathStr),null,dataPropertyURI);
    }

    public DataPropertyHandler(String xpathStr, String separatorRegex, String dataPropertyURI){
        this(DocumentHelper.createXPath( xpathStr ), separatorRegex, dataPropertyURI);
    }

    @Override
    public void handleNode(String text, Element doc, Object targetEnt) {
        if( targetEnt == null || text == null)
            return;

        if( !(targetEnt instanceof Individual)){
            log.error("DataPropertyHandler must be passed a Entity object");
            return;
        }

        Individual ent = (Individual) targetEnt;

        if( separator != null ){
            String[] tokens = text.split(separator);
            for(int i=0; i< tokens.length; i++){
                String token = tokens[i];
                if( token == null || token.length() == 0)
                    continue;
                DataPropertyStatement data = new DataPropertyStatementImpl(ent);
                data.setData(token);
                data.setDatapropURI(dataPropertyURI);
                if( ent.getDataPropertyStatements() == null )
                    ent.setDataPropertyStatements( new ArrayList<DataPropertyStatement>() );
                ent.getDataPropertyStatements().add(data);
                log.debug("found data: " + data.getData() );
            }
        } else {
            if( text.length() == 0 )
                return;
            if( text.trim().length() == 0 )
                return;
            DataPropertyStatement data = new DataPropertyStatementImpl(ent);
            data.setData(text);
            data.setDatapropURI(dataPropertyURI);
            if( ent.getDataPropertyStatements() == null )
                ent.setDataPropertyStatements( new ArrayList<DataPropertyStatement>() );
            log.debug("found data: " + data.getData() );
            ent.getDataPropertyStatements().add(data);
        }
    }
    private static final Log log = LogFactory.getLog(DataPropertyHandler.class.getName());
}
