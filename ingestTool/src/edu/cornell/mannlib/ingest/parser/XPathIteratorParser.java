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

package edu.cornell.mannlib.ingest.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.ingest.interfaces.IngestParser;

public class XPathIteratorParser extends IngestParser {
    private String xsltFilename;    
    private String xpathSelectorStr;
    private String filename;
    
    public XPathIteratorParser(List<IngestAction> actions,
            String xpathSelectorStr,
            String filename) throws IOException,  SAXException{
        if( xpathSelectorStr == null )
            throw new Error("No xpath was set");
        this.xpathSelectorStr = xpathSelectorStr;
        
        if( actions == null || actions.size() == 0 )
            log.warn("No actions were passed to constructor");
        setActions(actions);
        
        if( filename == null || filename.length() == 0) 
            throw new Error("no file name was provided.");       
        File f = new File(filename);
        if( ! f.canRead() )
            throw new Error("cannot read file " + filename);
        this.filename = filename;
    }

    public XPathIteratorParser(List<IngestAction> actions,
                                String xpathSelectorStr,
                                String filename,
                                String xsltFilename) 
    throws IOException, SAXException{
        this(actions,xpathSelectorStr,filename);
       this.xsltFilename = xsltFilename;        
   }
   
   public void parse( ){
       List<Element> results = null;
       SAXReader  xml2domReader =  new SAXReader();
       try {
           Document doc = null;                                
           if (xsltFilename != null ){  //we have an xslt we need to run before the parse
               log.debug("attempting to load xsl transform for ingest filtering");
               //force use of the saxon xsl library
               System.setProperty("javax.xml.transform.TransformerFactory",
                   "net.sf.saxon.TransformerFactoryImpl");
               TransformerFactory tfactory = TransformerFactory.newInstance();
               Transformer trans = tfactory.newTransformer( new StreamSource(new File(xsltFilename )));
               Document docOrg = xml2domReader.read( new File(this.filename) );
               DocumentSource source = new DocumentSource( docOrg );
               DocumentResult result = new DocumentResult();
               trans.transform(source, result);
               doc = result.getDocument();
               
               log.debug("loaded xsl for filtering");
           } else {                              
               doc = xml2domReader.read( new File(this.filename) );
           }
           
           XPath xpathSelector = DocumentHelper.createXPath(this.xpathSelectorStr);           
           results = xpathSelector.selectNodes( doc );
       } catch (Exception ex){
           log.error("Could not parse xml",ex);
       }
       try{
           if( results != null ){
               log.debug("doing ingest actions");
               for( Element element : results ){        
                   doIngestActions( element );
               }
           }else{
               log.warn("There were no results for the xpath \"" + xpathSelectorStr + "\"");
           }
       }catch(Exception ex ){ 
           log.error("error while doing ingest actions",ex);
       }

   }
   private static final Log log = LogFactory.getLog(XPathIteratorParser.class.getName());

}


