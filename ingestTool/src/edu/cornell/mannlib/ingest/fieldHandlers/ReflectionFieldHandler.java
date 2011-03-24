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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Deals with all the single value fields in the Entity.
 * @author bdc34
 *
 */
public class ReflectionFieldHandler implements FieldHandler {
    HashMap <String,XPath> field2xpath;
    HashMap <String,String> field2default;
    HashMap <String,String> field2type;
    HashMap <String,String> field2TimeFormat;

    public String defaultDateFormat = "yyyy-MM-dd";
    public String defaultTimeStampFormat = "yyyy-MM-dd mm:ss";

    private static final Log log = LogFactory.getLog(ReflectionFieldHandler.class.getName());
    /**
     *
     * @param xpathMap - fieldName to xpath to get the value
     * @param defaultMap - fieldName to default value to use if xpath is missing,
     * or xpath evaluates to nothing.
     * @param typeMap - type the xpath value or defualt should be cast to for
     * the set method.  only fieldNames which expect something other than String
     * or int or Date/Timekey need to be set.
     * @param timeFormatMap - format to use for parseing Date/Timestamp fields.
     * defaults to "yyyy-MM-dd" and "yyyy-MM-dd mm:ss"
     */
    public ReflectionFieldHandler(HashMap <String,String> xpathMap,
                                  HashMap <String,String> defaultMap,
                                  HashMap <String,String> typeMap,
                                  HashMap <String,String> timeFormatMap){
        if( xpathMap == null && defaultMap == null ){
            log.error("xpathMap and defaults were null, cannnot map or set defaults");
        }else{
            if( xpathMap  == null )
                log.warn("Map of fields to xpaths is empty since a null was passed to the " +
                "constructor");
            if( defaultMap == null )
                log.info("No map of fields to defaults was set, no default values will " +
                "be used in the field setter.");
        }

        if( field2default == null )
            field2default = new HashMap<String,String>();
        else
            field2default = defaultMap;

        field2xpath = new HashMap<String,XPath>();
        for( String name : xpathMap.keySet() ){
            field2xpath.put( name,
                             DocumentHelper.createXPath(xpathMap.get(name)));
        }

        if( typeMap != null )
            this.field2type = typeMap;
        else
            this.field2type = new HashMap <String,String>();
        if( timeFormatMap != null)
            this.field2TimeFormat = timeFormatMap;
        else
            this.field2TimeFormat = new HashMap<String,String>();
    }

    public void setFields(Element ele, Object targetEnt) {
        Class cls = targetEnt.getClass();

        HashSet<String> fieldNames = new HashSet<String>();
        fieldNames.addAll( field2xpath.keySet());
        fieldNames.addAll( field2default.keySet());

        for( String fieldName : fieldNames ){
            XPath xpath = field2xpath.get(fieldName);
            String val = getXPathValue(ele, xpath );
            if( val == null ){
                val = field2default.get(fieldName);
                if( val == null )
                    continue;
            }

            //capitalize first letter
            String methodName = "get" +  fieldName.substring(0,1).toUpperCase()
                + fieldName.substring(1,fieldName.length());

            Method meth = null;

            try{
                //try to get a method for String, Integer, Date and Datetime
                if( (meth = getMethod(cls, methodName, "java.lang.String" )) != null ) {
                    meth.invoke(targetEnt, val );
                }else if( (meth = getMethod(cls, methodName, "java.lang.Integer")) != null){
                    Integer x = Integer.parseInt( val );
                    meth.invoke(targetEnt, x);
                }else if( (meth = getMethod(cls, methodName, "java.lang.Date")) != null){
                    String fmtStr = field2TimeFormat.get( fieldName );
                    if( fmtStr == null)
                        fmtStr = defaultDateFormat;
                    DateTimeFormatter fmt = DateTimeFormat.forPattern(fmtStr);
                    DateTime dt = fmt.parseDateTime( val );
                    meth.invoke(targetEnt, dt.toDate() );
                }else if(( meth = getMethod(cls, methodName, "java.lang.Datetime")) != null){
                    String fmtStr = field2TimeFormat.get( fieldName );
                    if( fmtStr == null)
                        fmtStr = defaultTimeStampFormat;
                    DateTimeFormatter fmt = DateTimeFormat.forPattern(fmtStr);
                    DateTime dt = fmt.parseDateTime( val );
                    // Here we are in 2006 passing the mills since epoc to construct
                    // a Timestamp when we have a Date
                    // meth.invoke(targetEnt, new Timestamp( dt.toDate() )); doesn't work
                    meth.invoke(targetEnt, new Timestamp( dt.getMillis() ));
                }
            }catch(Exception ex){
                log.warn("error attempting to set " + fieldName +
                        " to value '" + val +"'", ex);
            }
        }
        return;
    }


    protected String getXPathValue(Element ele, XPath xpath){
        if( xpath == null ) return null;

        Node node = null;
        try{
            node = xpath.selectSingleNode(ele);
        } catch(XPathException ex) {
            log.error(" error getting " + xpath, ex);
        }
        if( node != null )
            return node.getText();
        else
            return null;
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
            log.debug("could not find method " + methodName);
        }
        return meth;
    }

    public void endOfParsing() {
        // TODO Auto-generated method stub

    }
}
