import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import edu.cornell.mannlib.vitro.biosis.beans.Article;
import edu.cornell.mannlib.vitro.biosis.db.dao.ArticleDao;



/**
 * This class is a sax content handler that will extract individual
 * articles out of the input file and then call a dom4j parser
 * on them to build up an article object that can be stored
 * to the vitro system.
 * @author unknown
 */
class PMContentHandler implements ContentHandler {

    private Locator locator;
    private boolean inArticle               = false;
    private String article                  = "";
    private String blockName                = "must be set in parameter file";    
    private int articleNum                  = 0;
    private String parameterFileName = "not_set";
    private String logfileName = "./PMContentHandler.log";
    private PrintWriter logfile;
    private boolean test = false; //if true, do a dry run.
    private ArticleDao articleDao;
    
    private static int debug = 0;

    /**
     * @param propertiesFileName name of the properties file to use for parsing
     * @param test if true do a dry run.
     */
    public PMContentHandler(Connection con, String propertiesFileName, boolean test)
        throws IOException{
        this.parameterFileName = propertiesFileName;
        getParameters(propertiesFileName);
        logfile = new PrintWriter( new FileWriter( logfileName ) );
        this.test = test;
        this.articleDao = new ArticleDao(con);
    }

    public void getParameters(String paramFile)throws IOException
    {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(paramFile);
        props.load(in);
        blockName = props.getProperty("PMContentHandler.articleBlock");        
        logfileName = props.getProperty("PMContentHandler.logfile");        
        in.close();
    }

    public void setDocumentLocator(Locator locator) {
        //System.out.println("    * setDocumentLocator() called");
        this.locator = locator;
    }

    public void startDocument() throws SAXException {
        System.out.println("Parsing begins...");
        try {
            getParameters( parameterFileName );
        } catch (IOException e) {
            throw new SAXException("error accessing properties file" +
                                   parameterFileName  + "\n" + e );
        }        
    }

    public void endDocument() throws SAXException {
        System.out.println("\n\n...Parsing ends.");        
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
            System.out.println("PI: Target:" + target + " and Data:" + data);
    }


    public void startPrefixMapping(String prefix, String uri) {
        System.out.println("Mapping starts for prefix " + prefix +
                           " mapped to URI " + uri);
    }

    public void endPrefixMapping(String prefix) {
        System.out.println("Mapping ends for prefix " + prefix);
    }


    public void startElement(String namespaceURI, String localName,
                             String rawName, Attributes atts)
        throws SAXException {

        if (localName.equals(blockName)) {
            inArticle = true;
            if(debug > 0)
                System.out.println("BEGIN SAX parse of "+blockName+" "+articleNum);
        }
        if (inArticle){
            article += "<" + localName;
            for (int i=0; i<atts.getLength(); i++)
                article += " " + atts.getLocalName(i)+"=\""+atts.getValue(i)+"\"";
            article += ">\n";
        }
    }

    public void endElement(String namespaceURI, String localName,
                           String rawName)
        throws SAXException {

        if (inArticle) {
            article += "</" + localName + ">\n";
        }

        if (localName.equals(blockName)) {
            if(debug > 0)
                System.out.println("END SAX xml extraction of " + blockName +
                                   " " + articleNum);
            articleNum++;
            /*Writes each block to a serpate xml file (output for debugging)
            try {
                String outfile = "./output/output" + articleNum + ".xml";
                outwriter = new FileWriter(outfile);
                outwriter.write(article);
                outwriter.close();
            } catch (IOException e) {
                System.out.println("Error writing output file.");
            }*/

            inArticle = false;
            StringReader sr = new StringReader(article);
            org.dom4j.Document doc = null;
            try {
                SAXReader xmlReader = new SAXReader();
                doc = xmlReader.read( sr );
            } catch (DocumentException ex){
                System.out.println("Error converting an article "+ articleNum +
                                   " to a DOM document : " + ex);
                article = "";
                return;
            }

            /* FIXME (more like IMPROVEME)
               this might need to be a different class based on a string
               in the properties file so that we can handle odd ball
               xml formats.  This would be especially true for odd
               date and author formats.  Do some interfaces etc. yada-yada

               Oh, or we could have xstl in the property file to transform
               odd ball formats to a pubmed/medline like format.
               That might be slick.  No java object mess.
            */
            DOM4JArticleHandler articleHandler = null;
            try{
                articleHandler = getOurHandler( parameterFileName );
            }catch (IOException ex){
                System.out.println("error loading properties file "+ parameterFileName + "\n" + ex);
                article = "";
                return;
            }
            
            /* this converst the DOM to an object that we can use */
            String status = "not inserted or updated";
            Article articleObj = articleHandler.parse( doc );
            if( articleObj != null ){
                try {
                    status = articleDao.doArticle2Pub(!test, articleObj, logfile );
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            System.out.println("article "+ articleNum +" insert result : " +
                               status);
            article = "";
        }

    }

    private DOM4JArticleHandler ourHandler = null;
    /**
        Gets a article handler.
        It would be poor form to keep making these
    */
    private DOM4JArticleHandler getOurHandler(String props) throws IOException{
        if( ourHandler == null){
            ourHandler = new DOM4JArticleHandler(props);
        }
        return ourHandler;
    }


    public void characters(char[] ch, int start, int end)
        throws SAXException {

        if (inArticle){

            String s =  new String(ch, start, end);

            for(int i=start; i < end; i++){
                if (ch[i] == '<'){
                    s = s.substring(0,i) + "&lt;" + s.substring(i+1);
                }
            }

            for(int i=start; i < end; i++){
                if (ch[i] == '>'){
                    s = s.substring(0,i) + "&gt;" + s.substring(i+1);
                }
            }

            for(int i=start; i < end; i++){
                if (ch[i] == '&'){
                    s = s.substring(0,i) + "&amp;" + s.substring(i+1);
                }
            }

            article += s;
        }


    }

    public void ignorableWhitespace(char[] ch, int start, int end)
        throws SAXException {

       // String s = new String(ch, start, end);
       // System.out.println("ignorableWhitespace: [" + s + "]");
    }
    public void skippedEntity(String name) throws SAXException {
        System.out.println("Skipping entity " + name);
    }

}


