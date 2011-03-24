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

import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * This will get some values from xpath and for each node returned
 * it will make an external id for the entity.
 *
 * It handles xpath statements that return multiple nodes by making multiple
 * external ids on the entity.
 *
 * If a a separatorRegex is passed to the constructor then the result of the
 * xpath query will be split() using the separatorRegex.  Each resulting token
 * will be added as an ExternalId.  Pass null as separatorRegex if you don't
 * want splitting.
 *
 * The prefix and suffix strings will be affixed to the xpath values before insertion
 * as external ids.
 *
 * @author bjl23
 *
 */
public class ExternalIdHandler extends MultiNodeHandler {
    int externalIdType;
    String separator;
    private static final Logger log = Logger.getLogger(DataPropertyHandler.class.getName());

    public ExternalIdHandler(XPath xpath, String separatorRegex, int externalIdType){
        super(xpath);
        this.externalIdType = externalIdType;
        this.separator = separatorRegex;
    }

    public ExternalIdHandler(String xpathStr, String separatorRegex, int dataPropId){
        this(DocumentHelper.createXPath( xpathStr ), separatorRegex, dataPropId);
    }

    public ExternalIdHandler(String xpathStr,
                               String separatorRegex,
                               int externalIdType,
                               List<StringProcessor>stringProcessors){
        this(DocumentHelper.createXPath( xpathStr ), separatorRegex, externalIdType);
        if( stringProcessors != null){
            for( StringProcessor sp : stringProcessors){
                this.addStrProcessor(sp);
            }
        }
    }

    @Override
    public void handleNode(String text, Element ele, Object targetEnt) {

        if( targetEnt == null || text == null)
            return;

        if( !(targetEnt instanceof Individual)){
            log.error("ExternalIdHandler must be passed a Entity object");
            return;
        }

        Individual ent = (Individual) targetEnt;

        if( separator != null ){
            String[] tokens = text.split(separator);
            //StringTokenizer st = new StringTokenizer(node.getText(), seperator);
            for(int i=0; i< tokens.length; i++){
                String token = tokens[i];
                if( token == null || token.length() == 0)
                    continue;
                DataPropertyStatement extid = new DataPropertyStatementImpl();
                //TODO: update for semweb-align
                if(true) throw new Error("TODO: update for semweb-align");
                //extid.setURI(ent.getURI());
                //extid.setValue(token);
                //extid.setExternalIdType(externalIdType);
                if( ent.getExternalIds() == null ){
                    List <DataPropertyStatement> l = new ArrayList<DataPropertyStatement>();
                    l.add( extid);
                    ent.setExternalIds(l);
                }else{
                    ent.getExternalIds().add(extid);
                }
            }
        }else{
            if( text.length() == 0 ) {
                return;
            }
            DataPropertyStatement extid = new DataPropertyStatementImpl();
            //TODO: update for semweb-align
            if(true) throw new Error("TODO: update for semweb-align");
            //extid.setEntityURI(ent.getURI());
            extid.setData(text);
            //extid.setExternalIdType(externalIdType);
            if( ent.getExternalIds() == null )
                ent.setExternalIds( new ArrayList<DataPropertyStatement>() );
            ent.getExternalIds().add(extid);
        }
    }
}
