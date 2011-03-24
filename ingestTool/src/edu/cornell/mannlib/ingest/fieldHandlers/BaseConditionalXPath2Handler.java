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
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.xml.xpath.*;
import java.util.*;

/**
 * This is the base class for field handlers that use xpath2.
 *
 * @author bdc34
 *
 */
public abstract class BaseConditionalXPath2Handler extends AbstractHandler implements FieldHandler {
        static XPathFactory xpf;
        private static final Logger log = Logger.getLogger(XPath2FieldHandler.class.getName());

        String baseUri;
        Configuration saxonConfig;
        XPath xpe;
        javax.xml.xpath.XPathExpression xp;
        javax.xml.xpath.XPathExpression xpb;
        javax.xml.xpath.XPathExpression xpc = null;
        String orgXpathStr;
        String bXpathStr;
        String cXpathStr;
        Set allowableBvalues;
        Set allowableCvalues;

        public BaseConditionalXPath2Handler(String xpathv2, String xpathv2b, String[] allowedBvalues, String xpathv2c, String[] allowedCvalues)
        throws XPathFactoryConfigurationException, XPathExpressionException{
            this("", xpathv2, xpathv2b, allowedBvalues, xpathv2c, allowedCvalues);
        }

        public BaseConditionalXPath2Handler( String baseUri, String xpathv2, String xpathv2b, String[] allowedBvalues, String xpathv2c, String[] allowedCvalues )
            throws XPathFactoryConfigurationException, XPathExpressionException{
            //constructors like this are not thread safe
            if( xpf == null ){
                //set up a saxon parser, get a factory and reset the system property
                String key = "javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON;

                System.setProperty(key, "net.sf.saxon.xpath.XPathFactoryImpl");
                xpf = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
                log.info("Loaded XPath Factory " + xpf.getClass().getName());
                xpe = xpf.newXPath();
                log.info("Loaded XPath Evaluator " + xpe.getClass().getName());
            } else {
                xpe = xpf.newXPath();
            }

            if( xpathv2 == null )
                log.warn("You attempted to pass a null xpath" );
            if( xpathv2b == null )
                log.warn("You attempted to pass a null second xpath");

            orgXpathStr = xpathv2;
            bXpathStr = xpathv2b;
            cXpathStr = xpathv2c;
            xp= string2xpath(xpathv2);
            xpb= string2xpath(xpathv2b);
            if (xpathv2c != null)
                xpc= string2xpath(xpathv2c);
            else
                xpc = null;

            this.baseUri = baseUri;
            this.saxonConfig = new Configuration();

            allowableBvalues = new HashSet();
            for (int i=0; i<allowedBvalues.length; i++)
                allowableBvalues.add(allowedBvalues[i]);

            allowableCvalues = new HashSet();
            if (allowedCvalues != null) {
                for (int i=0; i<allowedCvalues.length; i++)
                    allowableCvalues.add(allowedCvalues[i]);
            }

        }

        /**
         * Get the nodes from an xpath 2.0 query and return them as Strings.
         */
        @Override
        public Collection<String> getStrings(Element doc ) {
            return getStrings(doc, xp);
        }

        public XPathExpression string2xpath(String str) throws XPathExpressionException {
            return xpe.compile(str);
        }

        /**
         * get strings given an xpath
         * @param ele
         * @param xp
         * @return
         */
        protected Collection<String> getStrings(Element ele, javax.xml.xpath.XPathExpression xp){
            ArrayList<String> strs = new ArrayList<String>();
            Boolean noCpath = false;
            if( xp == null ){
                log.info("no first xpath was set");
                return strs;
            }
            if( xpb == null ){
                log.info("no second xpath was set");
                return strs;
            }
            if( xpc == null ){
                noCpath = true;
            }
            //get the xpath nodes
            List values = null;
            List bvalues = null;
            List cvalues = new ArrayList();
            try {
                values = (List)xp.evaluate(ele, XPathConstants.NODESET);
                bvalues = (List)xpb.evaluate(ele, XPathConstants.NODESET);
                if (!noCpath)
                    cvalues = (List)xpc.evaluate(ele, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                log.error(e);
                return strs;
            }

            Iterator it = values.iterator();
            Iterator bit = bvalues.iterator();
            Iterator cit = cvalues.iterator();

            while(it.hasNext()){
                Object obj = it.next();
                String objStr = null;
                String bobjStr = null;
                String cobjStr = null;
                if( obj instanceof Node )
                    objStr = ((Node)obj).getText();
                if( obj instanceof String )
                    objStr = (String) obj;
                boolean reject = false;
                try {
                    Object bobj = bit.next();
                    if ( bobj instanceof Node )
                        bobjStr = ((Node)bobj).getText();
                    if ( bobj instanceof String )
                        bobjStr = (String)bobj;
                    if (!allowableBvalues.contains(bobjStr)) {
                        reject = true;
                    } else if (!noCpath){
                        Object cobj = cit.next();
                        if ( cobj instanceof Node )
                            cobjStr = ((Node)cobj).getText();
                        if ( cobj instanceof String )
                            cobjStr = (String)cobj;
                        if (!allowableCvalues.contains(cobjStr))
                            reject=true;
                    }
                } catch (NoSuchElementException e) {
                    log.warn("Inconsistent xpath result sets");
                    reject = true;
                }
                if( !reject && objStr != null) {
                    strs.add( objStr );
                }
            }
            return strs;
        }
}
