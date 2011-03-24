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
import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This is the base class for field handlers that use xpath2.
 *
 * @author bdc34
 *
 */
public abstract class BaseXPath2Handler extends AbstractHandler implements FieldHandler {
        static XPathFactory xpf;
        private static final Log log = LogFactory.getLog(XPath2FieldHandler.class.getName());

        String baseUri;
        Configuration saxonConfig;
        XPath xpe;
        javax.xml.xpath.XPathExpression xp;
        String orgXpathStr;

        public BaseXPath2Handler(String xpathv2 )
        throws XPathFactoryConfigurationException, XPathExpressionException{
            this("", xpathv2);
        }

        public BaseXPath2Handler( String baseUri, String xpathv2 )
        throws XPathFactoryConfigurationException, XPathExpressionException{
            XPathFactory fact = getXPath2Factory();
            xpe = fact.newXPath();
            log.debug("Loaded XPath Evaluator " + xpe.getClass().getName());

            if( xpathv2 == null )
                log.warn("You attempted to pass a null xpath" );

            orgXpathStr = xpathv2;
            xp= string2xpath(xpathv2);

            this.baseUri = baseUri;
            this.saxonConfig = new Configuration();
        }

        public static synchronized XPathFactory  getXPath2Factory() 
        throws XPathFactoryConfigurationException{
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

        /**
         * Get the nodes from an xpath 2.0 query and return them as Strings.
         */
        @Override
        public Collection<String> getStrings(Element element ) {
            return getStrings(element, xp);
        }

        public XPathExpression string2xpath(String str) throws XPathExpressionException {
            return xpe.compile(str);
        }

        /**
         * get strings given an xpath
         * @param element
         * @param xp
         * @return
         */
        protected Collection<String> getStrings(Element element, javax.xml.xpath.XPathExpression xp){
            ArrayList<String> strs = new ArrayList<String>();
            if( xp == null ){
                log.info("no xpath was set");
                return strs;
            }
            //get the xpath nodes
            List values = null;
            try {
                values = (List)xp.evaluate(element, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                log.error(e);
                return strs;
            }

            Iterator it = values.iterator();
            while(it.hasNext()){
                Object obj = it.next();
                if( obj instanceof Node)
                    strs.add( ((Node)obj).getText() );
                if( obj instanceof String)
                    strs.add((String)obj);
            }
            return strs;
        }
}
