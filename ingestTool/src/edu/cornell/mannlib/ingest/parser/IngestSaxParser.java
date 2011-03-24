package edu.cornell.mannlib.ingest.parser;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.ingest.interfaces.IngestParser;

/**
 * Class to take a XML InputStream and parse it by breaking
 * the subtrees off based on Elements with local name blockName
 * and then passing those subtrees to the actions List.
 *
 * The interesting method is parse().  It takes an InputStream and
 * uses an sax parser to gather a String buffer of one whole xml
 * element with the local name of blockName.  Once one of these
 * elements is in the buffer, it is passed off to each object
 * on the actions List.  The action objects do something with the
 * data, write it to a file, parse it more, add xml nodes,
 * send it to a database, etc.
 *
 * If you are expecting to get characters such as '>' but are getting
 * "&gt;" then see the method characters().
 *
 * Notice: since this uses a SAX parser, no in memory model
 * is created of the xml.  This method only fills a StringBuilder with
 * the contents of an Element with the local name blockName.  Then, at
 * the closing tag of that Element a dom4j Document object is created.
 * This Document has the root Element of blockName and is only comprised
 * of only subtree of the original InputStream.
 *
 * The reason for this appraoch is that we would like to be able to process very
 * large input streams.  By operating in this fashion this class can
 * operate on manageable Elements without loading the whole file as a
 * Document into memory.
 *
 * There is an option to transform the Document objects that build from
 * blockName Elements with an XSL.  This is usually very slow.
 *
 * @author bdc34
 */
public class IngestSaxParser extends IngestParser implements ContentHandler,ErrorHandler {
    private boolean inInterestingNode       = false;
    private StringBuilder currentBlock      = null;
    /**
     * defines Element local name to use to break the file into records.
     * Needs to be improved to take into account namespaces.
     */
    private String blockName                = "must be set";
    private int blockNumber                  = 0;

    /** Actions to perform with each dom we can parse out of the xml */
    protected List <IngestAction> actions  = null;

    /** just a sax reader we use to convert our string to a dom */
    private SAXReader xmlReader;
    private Locator locator;
    private String xsltFilename;
    private InputStream input;
    private XMLReader xml2domReader;

    private static final Log log = LogFactory.getLog(IngestSaxParser.class.getName());

    public IngestSaxParser(List<IngestAction> actions,
            String blockName,
            InputStream input) throws IOException,  SAXException{

        setup(actions,blockName,input);
        xml2domReader = XMLReaderFactory
            .createXMLReader("org.apache.xerces.parsers.SAXParser");
    }

    public IngestSaxParser(List<IngestAction> actions,
                           String blockName,
                           String xsltFilename,
                           InputStream input) throws IOException, TransformerConfigurationException, SAXException{
       setup(actions, blockName, input);

       this.xsltFilename = xsltFilename;
       if (xsltFilename != null ){  //we have an xslt we need to run before the parse
           log.debug("attempting to load xsl transform for ingest filtering");
           //force use of the saxon xsl library
           System.setProperty("javax.xml.transform.TransformerFactory",
                              "net.sf.saxon.TransformerFactoryImpl");
           TransformerFactory tfactory = TransformerFactory.newInstance();
           if( tfactory.getFeature(SAXSource.FEATURE)) {
               xml2domReader = ((SAXTransformerFactory) tfactory)
                    .newXMLFilter(new StreamSource(new File(xsltFilename)));
               log.debug("loaded xsl for filtering");
           } else {
               throw new Error("the input was to be run through a xslt before "+
                             " the parse but the TrnasformerFactory does not" +
                             " support SAX features. aborting");
           }
       } else {
           xml2domReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
       }
   }

   private void setup(List<IngestAction> actions,String blockName, InputStream input){
       xmlReader = new SAXReader();

       if( blockName == null )
           throw new Error("No blockName was set");

       if( actions == null || actions.size() == 0 )
           log.warn("No actions were passed to constructor");

       this.blockName = blockName;
       setActions( actions );
       this.input = input;
   }

   public void parse( ){
       try {
           InputSource is = new InputSource( this.input );
           xml2domReader.setContentHandler( this );
           xml2domReader.setErrorHandler( this );
           xml2domReader.parse(is);
       } catch (IOException e) {
           log.error("Error reading xml" , e);
       } catch (SAXException e) {
           log.error("Error while attempting to parse ",e);
       } catch (Exception ex){
           log.error(ex);
       }
   }

   /* =======================  SAX ContentHandler Methods ============================== */
   /* most of these are trivial.  The interesting ones are startElement and endElement */

   public void startElement(String namespaceURI, String localName,
           String rawName, Attributes atts)
   throws SAXException {

       if (localName.equals(blockName)) {
           if( inInterestingNode ){
               log.error(" on line " + locator.getLineNumber() + " " +
                       blockName + " element nested within " +
                       "other element " + blockName + ". IngestSaxParser parser " +
                       "is not designed to work with this type of structure.");
           }

           inInterestingNode = true;
           log.debug("BEGIN SAX parse of "+blockName+" "+blockNumber + " on line " + locator.getLineNumber());
       }
       if (inInterestingNode){
           currentBlock.append( '<' ).append( localName );
           for (int i=0; i<atts.getLength(); i++){
               currentBlock.append(' ').append( atts.getLocalName(i) );
               currentBlock.append("=\"").append( atts.getValue(i) ).append('"');
           }
           currentBlock.append(">");
       }
   }

   /**
    * When we come to a close tag, check if it is the end of an
    * Article.  If it is we turn our current article string into a
    * dom and pass it off to DOM4JArticleHandler which will give us
    * back an Article.
    */
   public void endElement(String namespaceURI, String localName,
           String rawName)
   throws SAXException {

       if (inInterestingNode) {
           currentBlock.append("</").append(localName).append(">");
       }

       if (localName.equals(blockName)) {
           log.debug("END SAX xml extraction of " + blockName +
                   " " + blockNumber + " line " + locator.getLineNumber());
           blockNumber++;

           inInterestingNode = false;
           StringReader sr = new StringReader(currentBlock.toString());
           org.dom4j.Document doc = null;
           try {
               doc = xmlReader.read( sr );
           } catch (DocumentException ex){
               log.error("Error converting element " + blockName +
                       " "+ blockNumber +" to a DOM document, no IngestActions performed " + ex);
               badElement(blockNumber, blockName, currentBlock.toString());
               currentBlock.delete(0,currentBlock.length()); //clear out currentBlock
               return;
           }
           currentBlock.delete(0,currentBlock.length()); //clear out currentBlock
           doIngestActions( doc.getRootElement() );
       }
   }

   public void badElement(int num, String elementName, String block){
       try {
           File f = new File("bad" + blockName + blockNumber + "xml");
           f.createNewFile();
           BufferedWriter out = new BufferedWriter(new FileWriter(f.getName()));
           out.write(block);
           out.close();
           log.info("Wrote bad element to file " + f.getCanonicalPath());
       } catch (IOException e) {
          log.error("could not write bad block to a file ");
       }
   }

   public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public void startDocument() throws SAXException {
        log.debug("Parsing begins");
        currentBlock = new StringBuilder(2048);
    }

    public void endDocument() throws SAXException {
        log.debug("Parsing ends");
        this.endOfParsing();
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
            log.debug("PI: Target:" + target + " and Data:" + data);
    }

    public void startPrefixMapping(String prefix, String uri) {
        log.debug("Mapping starts for prefix " + prefix +
                           " mapped to URI " + uri);
    }

    public void endPrefixMapping(String prefix) {
        log.debug("Mapping ends for prefix " + prefix);
    }

    public void characters(char[] ch, int start, int end)
        throws SAXException {

        if (inInterestingNode){

            String s =  new String(ch, start, end);

            /* bdc34:  is this going to make a mess? how are things that are already escaped handled?

            /* bdc34: the sax parser seems to convert these things from &amp; to & before passing to
             * the char[] to this method */

            //we need to keep track of the the chars we add to s
            // i is index to ch[]
            // si is an index into the new String s, it grows as we move along ch[] and add escapes
            for(int i=start, si = start ; i < end; i++, si++){
                if (ch[i] == '<'){
                    s = s.substring(0,si) + "&lt;" + s.substring(si+1);
                    si = si + 3; //need to move point over to account for addition of "lt;"
                }
            }
            for(int i=start, si = start; i < end; i++, si++){
                if (ch[i] == '>'){
                    s = s.substring(0,si) + "&gt;" + s.substring(si+1);
                    si = si + 3; //need to move point over to account for addition of "gt;"
                }
            }
            for(int i=start, si = start; i < end; i++, si++){
                if (ch[i] == '&'){
                    s = s.substring(0,si) + "&amp;" + s.substring(si+1);
                    si = si + 4; //need to move point over to account for additon of "amp;"
                }
            }
            for(int i=start, si = start; i < end; i++, si++){
                if (ch[i] == '"'){
                    s = s.substring(0,si) + "&quot;" + s.substring(si+1);
                    si = si + 5; //need to move point over to account for additon of "quot;"
                }
            }
            for(int i=start, si = start; i < end; i++, si++){
                if (ch[i] == '\''){
                    s = s.substring(0,si) + "&apos;" + s.substring(si+1);
                    si = si + 5; //need to move point over to account for additon of "apos;"
                }
            }
            currentBlock.append(s);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int end)
        throws SAXException {
       // String s = new String(ch, start, end);
       // log.debug("ignorableWhitespace: [" + s + "]");
    }
    public void skippedEntity(String name) throws SAXException {
        log.debug("Skipping entity " + name);
    }

    /* ==================== Error Handler methods ============================*/
    /**
     * <p>
     * This will report a warning that has occurred; this indicates
     *   that while no XML rules were "broken", something appears
     *   to be incorrect or missing.
     * </p>
     *
     * @param exception <code>SAXParseException</code> that occurred.
     * @throws <code>SAXException</code> when things go wrong
     */
    public void warning(SAXParseException exception)
    throws SAXException {
        log.warn("**Parsing Warning**" +
                "  Line:    " +
                exception.getLineNumber() +
                "  URI:     " +
                exception.getSystemId() ,exception);
        throw new SAXException("Warning encountered");
    }

    /**
     * <p>
     * This will report an error that has occurred; this indicates
     *   that a rule was broken, typically in validation, but that
     *   parsing can reasonably continue.
     * </p>
     *
     * @param exception <code>SAXParseException</code> that occurred.
     * @throws <code>SAXException</code> when things go wrong
     */
    public void error(SAXParseException exception)
    throws SAXException {
        log.error("**Parsing Error** " +
                "  Line:    " +
                exception.getLineNumber() +
                "  URI:     " +
                exception.getSystemId(), exception);
        throw new SAXException("Error encountered");
    }

    /**
     * <p>
     * This will report a fatal error that has occurred; this indicates
     *   that a rule has been broken that makes continued parsing either
     *   impossible or an almost certain waste of time.
     * </p>
     *
     * @param exception <code>SAXParseException</code> that occurred.
     * @throws <code>SAXException</code> when things go wrong
     */
    public void fatalError(SAXParseException exception)
    throws SAXException {
        log.fatal("**Parsing Fatal Error**" +
                "  Line:    " +
                exception.getLineNumber() +
                "  URI:     " +
                exception.getSystemId() , exception);
        throw new SAXException("Fatal Error encountered");
    }
}


