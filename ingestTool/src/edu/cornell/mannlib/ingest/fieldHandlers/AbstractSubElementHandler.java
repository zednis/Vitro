package edu.cornell.mannlib.ingest.fieldHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import net.sf.saxon.om.NamespaceConstant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;

/**
 * Perform an abstract method on sub-elements that are specified by a
 * XPath 2.0 expression.
 * 
 * @author bdc34
 */
public abstract class AbstractSubElementHandler implements FieldHandler{
    private static XPathFactory xpf;    
    private XPath xpe;
    private XPathExpression xp;

    public AbstractSubElementHandler(String xpathStr) 
    throws XPathFactoryConfigurationException, XPathExpressionException{            
        this.xpe = getXPath2Factory().newXPath();
        this.xp = xpe.compile(xpathStr);
    }

    @Override
    public void setFields(Element ele, Object targetEnt) {
        List nodes= getXpathEval(ele);        
        if( nodes != null ){
            for( Object obj : nodes){
                if( obj instanceof Element){
                    handleNode((Element)obj, ele, targetEnt);
                }
            }
        }
    }
    
    protected List getXpathEval(Element ele){
        if( xp == null ){
            log.info("no xpath was set");
            return Collections.EMPTY_LIST;
        }
        try {
            return (List)xp.evaluate(ele, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            log.error(e);
            return Collections.EMPTY_LIST;
        }
    }
    
    protected static synchronized XPathFactory  getXPath2Factory() throws XPathFactoryConfigurationException{
        //not thread safe, there has got to be a better way to do this.
        if( xpf == null ){
            //set up a saxon parser, get a factory and reset the system property
            String key = "javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON;
            System.setProperty(key, "net.sf.saxon.xpath.XPathFactoryImpl");
            xpf = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
            log.debug("Loaded XPath Factory " + xpf.getClass().getName());
        }
        return xpf;
    }
 
    public abstract void handleNode(Element subNode, Element ele, Object targetEnt) ;
    @Override
    public abstract void endOfParsing() ;
    
    private static Log log = LogFactory.getLog(AbstractSubElementHandler.class);
}