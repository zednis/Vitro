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
import net.sf.saxon.dom4j.DocumentWrapper;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NamespaceConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import javax.xml.xpath.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

public class XPath2FieldHandler implements FieldHandler {
    static XPathFactory xpf;
    private static final Log log = LogFactory.getLog(XPath2FieldHandler.class.getName());

    String baseUri;
    Configuration saxonConfig;
    HashMap<String,XPathExpression>field2xpath;
    HashMap<String,String>field2default;
    XPath xpe;

    public XPath2FieldHandler( String baseUri )
    throws XPathFactoryConfigurationException{
        //constructors like this are not thread safe
        if( xpf == null ){
            //set up a saxon parser, get a factory and reset the system property
            String key = "javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON;

            System.setProperty(key, "net.sf.saxon.xpath.XPathFactoryImpl");
            xpf = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
            log.info("Loaded XPath Factory " + xpf.getClass().getName());
            xpe = xpf.newXPath();
            log.info("Loaded XPath Evaluator " + xpe.getClass().getName());
        }else{
            xpe = xpf.newXPath();
        }


        this.baseUri = baseUri;
        this.saxonConfig = new Configuration();
        this.field2default = new HashMap<String,String>();
        this.field2xpath  = new HashMap<String,XPathExpression>();
    }

    public void addField2XPath(String fieldName, String xpath, String defaultVal )
    throws XPathExpressionException{
        if( fieldName == null )
            log.warn("You attempted to pass a null fieldName; xpath: " + xpath + " default: " + defaultVal);

        if( xpath != null ){
            XPathExpression xp= xpe.compile(xpath);
            field2xpath.put(fieldName, xp);
        }
        if( defaultVal != null ){
            field2default.put(fieldName, defaultVal);
        }
    }

    public void setFields(Element element, Object targetEnt) {
        try {
            //collect all the fields that should be attempted
            HashSet<String> fieldNames = new HashSet<String>();
            fieldNames.addAll( field2xpath.keySet() );
            fieldNames.addAll( field2default.keySet() );

            for( String name : fieldNames ){
                log.debug("key/fieldName" + name);
                setField( element, name, targetEnt);
            }
        } catch (Exception e) {
            log.error( e.toString() );
            e.printStackTrace();
        }
    }

    private void setField( Element ele, String fieldName, Object targetEnt )
    throws Exception{
        String value = null;

        XPathExpression xp = field2xpath.get(fieldName);
        if ( xp != null ) {
            value = (String)xp.evaluate(ele, XPathConstants.STRING);
        }
        if( value == null || value.length() == 0 ){
            value = field2default.get(fieldName);
        }
        if( value != null ){
            callSetter( fieldName, value, targetEnt );
        }
    }

    private void callSetter(String fieldName, String value , Object targetEnt) throws  Exception{
        Class cls = targetEnt.getClass();

        //capitalize first letter
        String methodName = "set" +  fieldName.substring(0,1).toUpperCase()
            + fieldName.substring(1,fieldName.length());

        Method meth = null;
        log.debug("trying to call Getter " + methodName + " " + meth);


            //try to get a method for String, Integer, Date and Datetime
            if( (meth = getMethod(cls, methodName, "java.lang.String" )) != null ) {
                meth.invoke(targetEnt,value  );
            }else if( (meth = getMethod(cls, methodName, "java.lang.Integer")) != null){
                Integer x = Integer.parseInt( value );
                meth.invoke(targetEnt, x);
            }

    }

    /**
     * Attempt to get a method with the given name and a single
     * parameter of the given type.
     *
     * @param name
     * @param firstParameter
     * @return null if not found.
     */
    public Method getMethod(Class objClass, String methodName, String firstParameter){
        Method meth = null;
        try {
            //class lookups by name might be slow, consider saving in hashmap
            meth = objClass.getMethod(methodName, Class.forName(firstParameter));
        } catch (Exception e) {
            log.error("could not find method " + methodName);
        }
        return meth;
    }

    public void endOfParsing() {
        // TODO Auto-generated method stub

    }
}
