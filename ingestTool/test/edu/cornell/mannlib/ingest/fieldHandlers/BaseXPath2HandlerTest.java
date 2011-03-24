package edu.cornell.mannlib.ingest.fieldHandlers;

import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import edu.cornell.mannlib.ingest.fieldHandlers.BaseXPath2Handler;
import edu.cornell.mannlib.vitro.beans.Individual;


public class BaseXPath2HandlerTest extends TestCase {
    Document dom4jDoc;
    private SAXReader xmlReader; 
    private static final Log log = LogFactory.getLog(BaseXPath2HandlerTest.class.getName());
    protected void setUp() throws Exception {
        super.setUp();
                      
        xmlReader = new org.dom4j.io.SAXReader(); 
        StringReader sr = new StringReader( xmlForTest );
        dom4jDoc = xmlReader.read( sr );
    }

    /**
     * Gather keywords, append them with $$$ and stick in description.     
     */
    public void testXPath(){
        BaseXPath2Handler xp2fh =null;
        try {
            
            log.info("about to make a thinger");
            xp2fh = new BaseXPath2Handler("","tokenize(//Keywords,'\\|')"){
                @Override
                public void handleNode(String node, Document doc, Object targetEnt) {
                    Individual e = ((Individual)targetEnt); 
                    e.setDescription(e.getDescription() + "$$$" + node);
                }                               
            };            
            
            Individual ent = new Individual();
            ent.setDescription("");
            xp2fh.setFields( dom4jDoc, ent);
                        
            assertEquals("$$$Plant Pahology$$$Vegetable Diseases$$$" +
                    "Integratted Pest Management$$$Root Diseases$$$Soil Health" +
                    "$$$Internaional Agriculture",
                    ent.getDescription());            
            
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run( BaseXPath2HandlerTest.class );
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
