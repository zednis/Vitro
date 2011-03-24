package edu.cornell.mannlib.ingest.fieldHandlers;

import java.io.StringReader;
import java.util.HashMap;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import edu.cornell.mannlib.ingest.fieldHandlers.ReflectionFieldHandler;
import edu.cornell.mannlib.vitro.beans.Individual;

public class ReflectionFieldHandlerTest extends TestCase {
   
    Document dom4jDoc;
    private SAXReader xmlReader;    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();               
        xmlReader = new org.dom4j.io.SAXReader(); 
        StringReader sr = new StringReader( xmlForTest );
        dom4jDoc = xmlReader.read( sr );
    }
  
    public void testXPath(){        
        try {
            HashMap <String,String> xpathMap = new HashMap<String,String>();
            xpathMap.put("name","//Name");
            
            HashMap <String,String> defaultMap = new HashMap<String,String>();
            defaultMap.put("description","this is the default description");
            
            HashMap <String,String> typeMap = new HashMap<String,String>();
            HashMap <String,String> timeFormatMap = new HashMap<String,String>();
            
            ReflectionFieldHandler fh = 
                new ReflectionFieldHandler(xpathMap,defaultMap,typeMap,timeFormatMap);
                        
            Individual ent = new Individual();
            fh.setFields( dom4jDoc, ent);
                        
            assertEquals("Caruso, Brian",ent.getName());
            assertEquals("this is the default description",ent.getDescription());            
            
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
            
    public static void main(String[] args) {
        junit.textui.TestRunner.run( ReflectionFieldHandlerTest.class );
    }

    public static String xmlForTest =
        "<?xml version='1.0' encoding='UTF-8'?>" +
        "<response>" +
        "        <Response_Id>d9f1aa8a-8c23-4e06-9d7e-155ca1b510c4</Response_Id>" +
        "        <NetId>tes2</NetId>" +
        "        <Dept>Plant Pathology (GNVA)</Dept>" +
        "        <Name>Caruso, Brian</Name>" +
        "        <First>Brian</First>" +
        "        <Middle>David</Middle>" +
        "        <Last>Caruso</Last>" +
        "        <Last_Modified/>" +
        "        <Editor_Comments/>" +
        "        <Edited_Status/>" +
        "        <Status>new</Status>" +
        "        <Acad_Priority>Land Grant Mission</Acad_Priority>" +
        "        <Keywords>Plant Pahology|Vegetable Diseases|Integratted Pest Management|Root Diseases|Soil Health|Internaional Agriculture</Keywords>" +
        "        <Collaborative_Research>plant pathology</Collaborative_Research>" +
        "        <Area_Concentration>Vegetable Pathology|Soil Health</Area_Concentration>" +
        "        <Web>http://www.nysaes.cornell.edu/pp|http://www.nysaes.cornell.edu/pp/faculty/abawi/index.html</Web>" +
        "        <Other/>" +
        "        <Comments/>" +
        "</response>" ;
}
