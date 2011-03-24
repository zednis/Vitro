package edu.cornell.mannlib.ingest.fieldHandlers;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.parser.SkipRecord;

/*
 * Skips record if no result is found for XPath.
 */
public class SkipIfElementEmpty  implements FieldHandler{
    XPathExpression xpExpression;
    String message;
    
    public SkipIfElementEmpty(String xpath2, String message) 
    throws XPathFactoryConfigurationException, XPathExpressionException{
        XPathFactory xpf = BaseXPath2Handler.getXPath2Factory();
        XPath xp = xpf.newXPath();
        this.xpExpression = xp.compile(xpath2);
        this.message = message;
    }

    public void setFields(Element element, Object targetEnt) {
         List values = null;
        try {
            values = (List)xpExpression.evaluate(element, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            log.error("exception while evaluating xpath expression",e);
        }
         if( values == null || values.size() == 0 )
             throw new SkipRecord(message);
         /* else do nothing */
    }

    public void endOfParsing() {     /* do nothing */     }
    
    private static final Log log = LogFactory.getLog(SkipIfElementEmpty.class.getName());
}
