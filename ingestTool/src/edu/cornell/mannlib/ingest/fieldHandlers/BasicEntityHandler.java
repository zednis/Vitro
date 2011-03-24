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
import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Deals with all the single value fields in the Entity.
 * @author bdc34
 *
 */
public class BasicEntityHandler  implements FieldHandler {
    HashMap <String,XPath> field2xpath;
    HashMap <String,String> field2default;
    List<StringProcessor> defaultStringProcessors;
    HashMap<String,List<StringProcessor>> perPropertyStrProcessors;

    /* wouldn't it be nice if we could inspect the Entity
    class at compile time and build this list? */
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String MONIKER = "moniker";
    public static final String VCLASSID = "vClassURI";
    public static final String URL = "url";
    public static final String DESCRIPTION = "description";
    public static final String SUNRISE = "sunrise";
    public static final String SUNSET = "sunset";
    public static final String TIMEKEY = "timekey";
    public static final String IMAGEFILE = "imageFile";
    public static final String ANCHOR = "anchor";
    public static final String BLURB = "blurb";
    public static final String IMAGETHUMB = "imageThumb";
    public static final String CITATION = "citation";
    public static final String STATUSID = "statusId";
    public static final String STATUS = "status";
    public static final String FLAG1SET = "flag1Set";
    public static final String FLAG2SET = "flag2Set";

    private static final Log log = LogFactory.getLog(BasicEntityHandler.class.getName());

    public BasicEntityHandler(HashMap <String,String> xpathMap,
                              HashMap <String,String> defaultMap){
        if( xpathMap  == null )
            log.warn("Map of fields to xpaths is empty since a null was passed to the " +
                    "constructor");
        if( defaultMap == null )
            log.info("No map of fields to defaults was set, no default values will " +
                    "be used in the field setter.");

        if( defaultMap == null )
            field2default = new HashMap<String,String>();
        else
            field2default = defaultMap;

        field2xpath = new HashMap<String,XPath>();
        for( String name : xpathMap.keySet() ){
            field2xpath.put( name,
                             DocumentHelper.createXPath(xpathMap.get(name)));
        }
        defaultStringProcessors = new ArrayList<StringProcessor>();
        perPropertyStrProcessors = new HashMap<String,List<StringProcessor>> ();
    }

    public void setDefaultStrProcessor(List<StringProcessor> processors){
        if( processors != null )
            defaultStringProcessors = processors;
    }

    public void addPerPropertyStringProcessor(String property, StringProcessor sp){
        if( property == null || sp == null ) return;
        if(! perPropertyStrProcessors.containsKey(property) ){
            List<StringProcessor> list = new ArrayList<StringProcessor>();
            perPropertyStrProcessors.put(property,list);
            list.add(sp);
        }else {
            List<StringProcessor> list = perPropertyStrProcessors.get(property);
            list.add(sp);
        }
    }

    public void addPerPropertyStringProcessors(String property, List<StringProcessor> processors) {
        if( property == null || processors == null ) return;
        for( StringProcessor sp : processors){
            addPerPropertyStringProcessor(property, sp);
        }
    }

    public void setFields(Element ele, Object inObj) {
        String txt = null;

        Individual targetEnt = null;
        if( inObj instanceof Individual){
            targetEnt = (Individual)inObj;
        }else{
            log.error("Can only handle objects of type Entity but was passed " +
                    inObj.getClass().getName() );
            return;
        }

//        numb = getNodeInt(doc,ID,null);
//        if( numb != null )
//            targetEnt.setId( getNodeText ( doc,  ID ));

        targetEnt.setName( getNodeText( ele,  NAME ));
        targetEnt.setMoniker( getNodeText( ele,  MONIKER ));
        targetEnt.setVClassURI( getNodeText(ele, VCLASSID));

        targetEnt.setUrl( getNodeText( ele,  URL ));
        targetEnt.setAnchor( getNodeText( ele,  ANCHOR ));

        targetEnt.setBlurb( getNodeText( ele,  BLURB ));
        targetEnt.setDescription( getNodeText( ele,  DESCRIPTION ));
        targetEnt.setCitation( getNodeText( ele,  CITATION ));

//        targetEnt.setSunrise( getNodeText( doc,  SUNRISE ));
//        targetEnt.setSunset( getNodeText( doc,  SUNSET ));
//        targetEnt.setTimekey( getNodeText( doc,  TIMEKEY ));

        targetEnt.setImageFile( getNodeText( ele,  IMAGEFILE ));
        targetEnt.setImageThumb( getNodeText( ele,  IMAGETHUMB ));

//        targetEnt.setStatusId( getNodeText( doc,  STATUSID ));
//        targetEnt.setStatus( getNodeText( doc,  STATUS ));

        targetEnt.setFlag2Set( getNodeText(ele, FLAG2SET));
        targetEnt.setFlag1Set( getNodeText(ele, FLAG1SET));
    }


//    private Integer getNodeInt(Document doc, String field ){
//        String txt = getNodeText(doc,field);
//        if( txt == null ) return iDefault;
//        Integer rv=null;
//        try{
//            rv = Integer.parse( txt );
//        }catch(NumberFormatException ex){
//            System.out.println( "unable to parse value " + txt + " for field " + field);
//            return null;
//        }
//        return rv;
//    }

    /**
     * Gets the text of a node described by the given XPath or strDefault
     * if there are any problems.  The text will be trimmed.
     *
     * @returns text of node, or null if no default and no text or no node.
     */
    private String getNodeText(Element ele, String field ){
        String out = null;

        XPath xp = field2xpath.get(field);
        if( xp == null ){
            out = field2default.get(field);
        }else{
            Object obj = null;
            try{
                obj = xp.evaluate(ele);
            } catch(XPathException ex) {
                log.error(" getting " + field + " with " + xp, ex);
                return null;
            }
            if( obj != null )
                out = getText(obj);
            else
                out = field2default.get(field);
        }

        if( perPropertyStrProcessors.containsKey(field) ){
            List<StringProcessor> strProcessors = perPropertyStrProcessors.get(field);
            out = doStringProcessing(out, strProcessors);
        }

        log.debug("got " + out + " for field " + field);
        return out;
    }

    private String getText(Object obj){
        if( obj == null ) return null;
        if( obj instanceof Node) return ((Node)obj).getText();
        if( obj instanceof String) return (String)obj;
        if( obj instanceof List && ((List)obj).size() > 0)
            return getText(((List)obj).get(0));
        else
            return null;
    }

    /**
     * Executes all of the stringProcessors on a Node's text
     * @return
     */
    public String doStringProcessing(String str, List<StringProcessor>strProcessors){
        if( strProcessors == null || strProcessors.size() == 0)
            return str;

        for( StringProcessor strProc : strProcessors )
             str = strProc.process( str );
        return str;
    }

    public void endOfParsing() {
        // TODO Auto-generated method stub

    }


    /*


id
name
moniker
vClassId
vClass
url
description
sunrise
sunset
timekey
modTime
imageFile
anchor
blurb
imageThumb
citation
statusId
status

    private List <Property>propertyList = null;
    private List <DatatypeProperty>datatypePropertyList = null;
    private List <Ents2Data>ents2Data = null;
    private List <Link>linksList = null;
    private List<String> keywords=null;
    private List <Ents2Ents>domainEnts2Ents = null;
    private List <Ents2Ents>rangeEnts2Ents = null;

     */
}
