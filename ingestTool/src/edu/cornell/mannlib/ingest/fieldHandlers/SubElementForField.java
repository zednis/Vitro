package edu.cornell.mannlib.ingest.fieldHandlers;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;

public class SubElementForField extends AbstractSubElementHandler{    
    private List<FieldHandler> fieldHandlers;
   
    public SubElementForField(String xpathStr,  List<FieldHandler> fieldHandlers) 
    throws XPathFactoryConfigurationException, XPathExpressionException{
        super(xpathStr);
        this.fieldHandlers = fieldHandlers;        
    }
     
    /**
     * For each node, run all the field handlers. 
     */
    public void handleNode(Element subElement, Element ele, Object thisIsIgnored) {
        try{
            //run all the fieldHandlers so we have a entity setup from the input
            Individual targetEnt = new IndividualImpl();
            for( FieldHandler handler : fieldHandlers){                
                handler.setFields( subElement , targetEnt);
            }
        }catch(Exception ex){
            log.error( ex.toString() );
            ex.printStackTrace();
        }
    }
    
    public void endOfParsing() {/*nothing*/};
   
    public static Log log = LogFactory.getLog(SubElementForField.class);
}