/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cornell.mannlib.ingest.fieldHandlers;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import java.util.ArrayList;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 *
 * @author bdc34
 */
public class Xpath2DataPropertyHandler extends BaseXPath2Handler {
    String dataPropetyURI = null;
    private static final Log log = LogFactory.getLog(Xpath2DataPropertyHandler.class.getName());

    public Xpath2DataPropertyHandler(String xpStr, String dpropUri)
            throws XPathFactoryConfigurationException, XPathExpressionException{
        super(xpStr);
        dataPropetyURI = dpropUri;
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

        if( text.length() == 0 )
            return;
        if( text.trim().length() == 0 )
            return;
        DataPropertyStatement data = new DataPropertyStatementImpl(ent);
        data.setData(text);
        data.setDatapropURI(dataPropetyURI);
        if( ent.getDataPropertyStatements() == null )
            ent.setDataPropertyStatements( new ArrayList<DataPropertyStatement>() );
        log.debug("found data: " + data.getData() );
        ent.getDataPropertyStatements().add(data);
    }
}
