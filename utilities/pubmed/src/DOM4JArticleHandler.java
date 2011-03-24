import java.io.*;
import java.util.*;

import java.net.URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPathException;
import org.dom4j.io.SAXReader;

import edu.cornell.mannlib.vitro.biosis.beans.Article;

/**
 * This class is for handling a single article element.
 * Where this handler extracts different features from
 * is controled by xpath strings that get loaded from
 * a properties file.
 * @author bdc34
 */

public class DOM4JArticleHandler {

    /** this is the root tag of the document, all other xpaths will be children
        of this node */
    private static String rootXPath = "/MustBeSetInPropertiesFile";
    /** xpath to the unique id of this article */
    private static String pmidXPath = "/MustBeSetInPropertiesFile";
    /** xpath to article title */
    private static String articleTitleXPath = "/MustBeSetInPropertiesFile";

    /** xpath to journal number node */
    private static String journalXPath ="/none";
    /** xpath to volume number node */
    private static String volumeXPath = "/none";
    /** xpath to issue number node */
    private static String issueXPath =  "/none";
    /** xpath to pagination node, the node's text should be page the range */
    private static String pageXPath =   "/none";
    /** xpath to the year this article was published */
    private static String pubYearXPath = "/none";

    /** xpath to author or authors, this will work properly if
        the xpath resolves to several nodes */
    private static String authorsXPath = "/none";

    /** xpath to the abstract, not required */
    private static String abstractXPath = "/none";

    /** xpath to language of article, not required */
    private static String langXPath = "/none";
    /** default language if none is found */
    private static String defaultLang = "ENG";

    /** xpath to the update code, which is a date.  This will work properly
        if the xpath resolves to several nodes.  The date that is the most
        receint will be used.*/
    private static String updateXPath = "set this is the properties file";

    private static String textLinkUrl = "/none";
    private static String textLinkIdXPath = "/none";
    private static String textLinkIdToken=
        "token must be set in properties file";

    /** text to be used as citation */
    private static String citationText =
        "This citation and abstract is copyrighted by its publisher.";
    private static int citationOrigin = 0;

    /** other origins, not currently used */
    private static int authorOrigin = 0;
    private static int editorOrigin = 0;
    private static int meetingOrigin = 0;
    private static int sourceOrigin = 0;
    private static int addressOrigin = 0;

    protected static String statusXPath =
        "/PubmedArticle[1]/MedlineCitation/attribute::Status";

    /**
     * setup with the properties from the given file.
     */
    public DOM4JArticleHandler(String propertyFileName) throws IOException{
        getParameters(propertyFileName);
    }

    /** This is a local article object so that a new one will not have to be
        created every time this parses something.    */
    private Article ourArticle = null;

    /** reset ourArticle to a clean state */
    private Article getCleanArticle(){
        if( ourArticle == null)
            ourArticle = new Article();
        else
            ourArticle.clearAllFields();
        return ourArticle;
    }

    /**
     * Parse a single article using the properties
     * that have been set on this handler.
     *
     * @param doc should have one element, the children of
     * which should represent an article.
     * @returns an article filled in as much as it can be with
     * the info from the given doc.
     */
    public Article parse (Document doc) {
        Article theArticle=getCleanArticle();

        if(doc==null){
            System.out.println("no document was passed to the DOM4JArticleHandler");
            return null;
        }
        Element root =doc.getRootElement();

        String status = doc.valueOf(statusXPath);
        if( "In-Data-Review".equals(status) ||
            "In-Process".equals(status) ) {
            System.out.println("article rejected because of status: " + status);
            return null;
        }

        /******** ID **********/
        theArticle.accessionNumber = getNodeText(doc, pmidXPath, null);
        if( theArticle.accessionNumber == null){
            System.out.println("no ID by xpath" + pmidXPath);
            return null;
        }
        System.out.println("ID=" + theArticle.accessionNumber);

        /******* article title *********/
        theArticle.titleStr = getNodeText(doc, articleTitleXPath, null);
        if(theArticle.titleStr == null ){
            System.out.println("ArticleTitle not found using xpath " + articleTitleXPath);
            return null;
        }

        /********* publication year ********/
        theArticle.pubYear = getNodeText(doc, pubYearXPath, null);
        if( theArticle.pubYear ==  null )
            System.out.println("no pub year found using the xpath " +
                               pubYearXPath);

        /********* the authors *********/
        List authors = doc.selectNodes( authorsXPath );
        //System.out.println("author list: " + authors );

        if(authors == null) {
            System.out.println("author list not found using xpath " +
                               authorsXPath);
            return null;
        }
        for( Iterator i = authors.iterator(); i.hasNext(); ){
            Node author = (Node)i.next();
            addAuthorNodeToArticle(author, theArticle);
        }

        /******** Abstract *********/
        theArticle.abstractStr =
            getNodeText(doc, abstractXPath, "No abstract provided" );
        if( theArticle.abstractStr == null )
            System.out.println("no abstract found in article using the xpath: "
                               + abstractXPath);

        /******** language *********/
        theArticle.languageStr = getNodeText(doc, langXPath, defaultLang);

        /****** Source ********/
        theArticle.sourceStr = makeSourceLine(doc);

        /****** Update Code (date of last update) ***/
        theArticle.updateCodeStr = findUpdateCode(doc);

        /**** make text link url ******/
        theArticle.addTextLink( findTextLink(doc), "NIH Pubmed" );

        /**** set the citation ****/
        theArticle.citationText = citationText;
        theArticle.citationOrigin = citationOrigin;

        //System.out.println(theArticle.toString());
        return theArticle;
    }

    /**
     * take the textLinkUrl and replace the first occurrence
     * of textLinkIdToken with the text from the node
     * indicated by textLinkIdXPath.
     */
    String findTextLink(Document doc){
        String id = getNodeText(doc, textLinkIdXPath, null);
        if( id == null || textLinkUrl == null || textLinkIdToken == null)
            return null;
        return textLinkUrl.replaceFirst( textLinkIdToken, id );
    }

    /**
     * Place the author represented by the node on the author list of the
     * article. This expects an author node with a <LastName> node
     * and a <FirstName> node.
     */
    public void addAuthorNodeToArticle(Node author, Article article){
        StringBuffer sb = new StringBuffer("");
        Node lNode = author.selectSingleNode("LastName");
        Node fNode = author.selectSingleNode("FirstName");
        if( fNode == null )
            fNode = author.selectSingleNode("ForeName");
        if( lNode != null && fNode != null){
            sb.append( trim(lNode.getText()) );
            sb.append(", ").append( nameCase( trim(fNode.getText()) )  );
            article.addAuthor(sb.toString());
        }
        return;
    }

    /** convert from BRIAN or brian or bRiAn to Brian */
    public String nameCase(String name){
        if( name == null || name.length() < 1 ||
            name.matches("([A-Z][a-z]*-[A-Z][a-z]*)") || //match Maria-Clare
            name.matches("([A-Z][a-z]+).*")) //starts decently
            return name;

        if( name.indexOf(' ') > -1){
            StringBuffer sb = new StringBuffer("");
            String chunk [] = name.split("[\\s]");
            for(int i=0; i < chunk.length; i++){
                sb.append( nameCase(chunk[i]) );
                sb.append(' ');
            }
            return sb.toString().trim();
        } //else

        return name.substring(0,1).toUpperCase() +
            name.substring(1).toLowerCase();
    }

    /**
     * Everything that comes out of a node should be trimmed
     * unless there is whitespace that you want.  We use
     * this instead of String.trim() so we can check for
     * null somewhere else and keep the code readable.
     * @returns if in was a string, then in.trim() else, null.
     */
    private String trim(String in){
        if(in!=null)
            return in.trim();
        else
            return in;
    }

    /**
     * Gets the text of a node described by the given XPath or strDefault
     * if there are any problems.  The text will be trimmed.
     *
     * @returns text of node, or null if no default and no text or no node.
     */
    private String getNodeText(Document doc, String xPath, String strDefault){
        Node node = null;
        try{
             node = doc.selectSingleNode(xPath);
        } catch(XPathException ex) {
            System.out.println(xPath +'\n');
            throw ex;
        }
        String out = null;
        if( node != null ){
            out = trim( node.getText() );
        } else {
            out = strDefault;
        }
        return out;
    }

    /**
     * Looks for the update code in the <history> branch
     * and returns the most receint one.
     * @returns date as a String in the YYYYMMDD format
     */
    public String findUpdateCode(Document doc){
        List dates = doc.selectNodes( updateXPath );
        int max = 00000000; //YYYYMMDD

        for(Iterator it= dates.iterator(); it.hasNext(); ){
            Node dNode = (Node)it.next();
            String date = dateNode2Str(dNode);
            int current =0;
            try{
            current = Integer.parseInt( date );
            }catch(NumberFormatException ex){
                System.out.println("dat string: " + date + " datenode: \n" + dNode.asXML() + "\n");
            }
            if( current > max )
                max = current;
        }

        if( max < 10000000)
            return "00000000";
        else
            return Integer.toString( max );
    }

    /**
     * This will parse a date node into an date string
     * @param date a node in the format:
                            <PubMedPubDate PubStatus="pubmed">
                                <Year>2004</Year>
                                <Month>6</Month>
                                <Day>19</Day>
                                <Hour>5</Hour>
                                <Minute>0</Minute>
                            </PubMedPubDate>
            All attributes and units more percise than Days
            will be ignored.  This function must be able to
            handle Months as numbers or abbreviations.
       @return a date string like 20041230
     */
    public String dateNode2Str(Node date )  {
        Node yearN = date.selectSingleNode("Year");
        Node monthN = date.selectSingleNode("Month");
        Node dayN = date.selectSingleNode("Day");

        StringBuffer retv = new StringBuffer("");
        //what should we do if there is bad date data?
        retv.append(yearN != null && yearN.getText() != null?
                    yearN.getText().trim() : "2000");
        retv.append(monthN != null ? month2number(monthN.getText()):"01");
        retv.append(dayN != null ? twoCharDay(dayN.getText()): "01" );
        return  retv.toString();
    }

    private String twoCharDay(String in){
        if( in == null) return "01";
        in = in.trim();
        if( in.length() == 1)
            return "0" + in;
        else
            return in;
    }

    private HashMap monthMap = null;
    private void setupMonthMap (){
        monthMap = new HashMap (24);
        monthMap.put("Jan","01");
        monthMap.put("Feb","02");
        monthMap.put("Mar","03");
        monthMap.put("Apr","04");
        monthMap.put("May","05");
        monthMap.put("Jun","06");
        monthMap.put("Jul","07");
        monthMap.put("Aug","08");
        monthMap.put("Sep","09");
        monthMap.put("Oct","10");
        monthMap.put("Nov","11");
        monthMap.put("Dec","12");

        monthMap.put("January","01");
        monthMap.put("February","02");
        monthMap.put("March","03");
        monthMap.put("April","04");
        monthMap.put("May","05");
        monthMap.put("June","06");
        monthMap.put("July","07");
        monthMap.put("August","08");
        monthMap.put("September","09");
        monthMap.put("October","10");
        monthMap.put("November","11");
        monthMap.put("December","12");

        monthMap.put("1","01");
        monthMap.put("2","02");
        monthMap.put("3","03");
        monthMap.put("4","04");
        monthMap.put("5","05");
        monthMap.put("6","06");
        monthMap.put("7","07");
        monthMap.put("8","08");
        monthMap.put("9","09");
        monthMap.put("10","10");
        monthMap.put("11","11");
        monthMap.put("12","12");
    }
    /* is there a better way to do this? */
    private String month2number (String month){
        if( monthMap == null)
            setupMonthMap();
        if(month == null ) return "01";
        String retV = (String)monthMap.get(month.trim());
        if( retV != null)
            return retV;
        else{
            System.out.println("could not parse month: '" + month + "'");
            return "01";
        }
    }

    /**
     * Tries to make a line to use as the source similar to biosis field SO.
     *
     * An example from biosis:
     *   Journal-of-Food-Science. 2004; 69(8): M207-M214
     *     journal name.         year; vol(issue): page-pagend
     *
     * @returns As much of this that can be found in the xml document will be put into
     * the return string. If no journel name can be found, just return "";
     */
    public String makeSourceLine(Document doc){
        StringBuffer sb = new StringBuffer ("");
        String jname = getNodeText(doc, journalXPath, "");
        String jyear = getNodeText(doc, pubYearXPath, "");
        String jvol = getNodeText(doc, volumeXPath, "");
        String jIssue = getNodeText(doc, issueXPath, "");
        String jPageination = getNodeText(doc, pageXPath, "");

        if( jname.length() == 0){
            System.out.println("no journal name found using xpath " + journalXPath);
            System.out.println("so the source of this article will be left blank. ");
            return "";
        }

        sb.append( jname ).append(". ");
        if( jyear.length() > 0)
            sb.append(jyear).append("; ");
        if( jvol.length() > 0){
            sb.append(jvol);
            if( jIssue.length() > 0)
                sb.append("(").append(jIssue).append(")");
            if( jPageination.length() > 0)
                sb.append(": ").append( jPageination );
        }
        return sb.toString();
    }

    /**
     * gets properties from the indicated property file.
     * This will not overwrite the values if there is nothing
     * in the properties file.
     */
    public void getParameters(String propertiesFile) throws IOException
    {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream( propertiesFile );
        props.load(in);
        String temp = null;
        /* there has got to be a better way to do this */
        temp = props.getProperty("DOM4JArticleHandler.rootXPath");
        if( temp != null && temp.length() > 0 )
            {       rootXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.pmidXPath");
        if( temp != null && temp.length() > 0 )
            {       pmidXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.articleTitleXPath");
        if( temp != null && temp.length() > 0 )
            {       articleTitleXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.journalXPath");
        if( temp != null && temp.length() > 0 )
            {       journalXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.volumeXPath");
        if( temp != null && temp.length() > 0 )
            {       volumeXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.issueXPath");
        if( temp != null && temp.length() > 0 )
            {       issueXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.pageXPath");
        if( temp != null && temp.length() > 0 )
            {       pageXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.pubYearXPath");
        if( temp != null && temp.length() > 0 )
            {       pubYearXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.authorsXPath");
        if( temp != null && temp.length() > 0 )
            {       authorsXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.abstractXPath");
        if( temp != null && temp.length() > 0 )
            {       abstractXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.langXPath");
        if( temp != null && temp.length() > 0 )
            {       langXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.defaultLang");
        if( temp != null && temp.length() > 0 )
            {       defaultLang = temp; }
        temp = props.getProperty("DOM4JArticleHandler.updateXPath");
        if( temp != null && temp.length() > 0 )
            {       updateXPath = temp; }
        temp = props.getProperty("DOM4JArticleHandler.textLinkUrl");
        if( temp != null && temp.length() > 0 )
            {       textLinkUrl = temp; }
        temp = props.getProperty("DOM4JArticleHandler.textLinkIdToken");
        if( temp != null && temp.length() > 0 )
            {       textLinkIdToken = temp; }
        temp = props.getProperty("DOM4JArticleHandler.textLinkIdXPath");
        if( temp != null && temp.length() > 0 )
            {   textLinkIdXPath  = temp; }
        temp = props.getProperty("DOM4JArticleHandler.citationText");
        if( temp != null && temp.length() > 0 )
            {   citationText = temp; }

        temp = props.getProperty("DOM4JArticleHandler.citationOrigin");
        if( temp != null && temp.length() > 0 ) {
            try{
                int numb = Integer.parseInt(temp);
                citationOrigin = numb;
            } catch(NumberFormatException ex){
                System.out.println("In property file, error parsing " + temp + " to a number");
            }
        }

        temp = props.getProperty("DOM4JArticleHandler.editorOrigin");
        if( temp != null && temp.length() > 0 )
            try{
                int numb = Integer.parseInt(temp);
                editorOrigin = numb;
            } catch(NumberFormatException ex){
                System.out.println("In property file, error parsing " + temp + " to a number");
            }

        temp = props.getProperty("DOM4JArticleHandler.addressOrigin");
        if( temp != null && temp.length() > 0 )
            try{
                int numb = Integer.parseInt(temp);
                addressOrigin = numb;
            } catch(NumberFormatException ex){
                System.out.println("In property file, error parsing " + temp + " to a number");
            }

        temp = props.getProperty("DOM4JArticleHandler.meetingOrigin");
        if( temp != null && temp.length() > 0 )
            try{
                int numb = Integer.parseInt(temp);
                meetingOrigin = numb;
            } catch(NumberFormatException ex){
                System.out.println("In property file, error parsing " + temp + " to a number");
            }

        temp = props.getProperty("DOM4JArticleHandler.sourceOrigin");
        if( temp != null && temp.length() > 0 )
            try{
                int numb = Integer.parseInt(temp);
                sourceOrigin = numb;
            } catch(NumberFormatException ex){
                System.out.println("In property file, error parsing " + temp + " to a number");
            }

        in.close();
    }

    public static void main(String[] argv){
        System.out.println("In DOM4JArticleHandler.java TEST CASE");
        System.out.println("This will just test this parser on the hardcoded testcase.");
        System.out.println("building document from getTestPubmedXml()");
        StringReader sr = new StringReader(getTestPubmedXml());

        try {
            Document doc = null;
            SAXReader reader = new SAXReader(false);
            doc = reader.read(sr);
            System.out.println("build of the doc using a SAXReader successful.");

            DOM4JArticleHandler articleHandler = new DOM4JArticleHandler("pubmed.properties");
            articleHandler.parse( doc );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTestPubmedXml(){
        return
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
            "<!-- edited with XML Spy v4.4 U (http://www.xmlspy.com) by jc55 (D-LIT Cornell University) -->"+
            "<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st November 2003//EN\" \"http://www.ncbi.nlm.nih.gov/entrez/query/DTD/pubmed_031101.dtd\">"+
            "    <PubmedArticle>" +
            "       <MedlineCitation Status=\"Publisher\">" +
            "           <PMID>15205349</PMID>" +
            "           <DateCreated>" +
            "               <Year>2004</Year>" +
            "               <Month>Jun</Month>" +
            "               <Day>18</Day>" +
            "           </DateCreated>" +
            "           <Article>" +
            "               <Journal>" +
            "                   <ISSN>0008-5472</ISSN>" +
            "                   <JournalIssue>" +
            "                       <Volume>64</Volume>" +
            "                       <Issue>12</Issue>" +
            "                       <PubDate>" +
            "                           <Year>2004</Year>" +
            "                           <Month>Jun</Month>" +
            "                           <Day>15</Day>" +
            "                       </PubDate>" +
            "                   </JournalIssue>" +
            "               </Journal>" +
            "               <ArticleTitle>p14ARF Expression Increases Dihydrofolate Reductase Degradation and Paradoxically Results in Resistance to Folate Antagonists in Cells with Nonfunctional p53.</ArticleTitle>" +
            "               <Pagination>" +
            "                   <MedlinePgn>4338-4345</MedlinePgn>" +
            "               </Pagination>" +
            "               <Abstract>" +
            "                   <AbstractText>The p14(ARF) protein, the product of an alternate reading frame of the INK4A/ARF locus on human chromosome 9p21, disrupts the ability of MDM2 to target p53 for proteosomal degradation and causes an increase in steady-state p53 levels, leading to a G(1) and G(2) arrest of cells in the cell cycle. Although much is known about the function of p14(ARF) in the p53 pathway, not as much is known about its function in human tumor growth and chemosensitivity independently of up-regulation of p53 protein levels. To learn more about its effect on cellular proliferation and chemoresistance independent of p53 up-regulation, human HT-1080 fibrosarcoma cells null for p14(ARF) and harboring a defective p53 pathway were stably transfected with p14(ARF) cDNA under the tight control of a doxycycline-inducible promoter. Induction of p14(ARF) caused a decrease in cell proliferation rate and colony formation and a marked decrease in the level of dihydrofolate reductase (DHFR) protein. The effect of p14(ARF) on DHFR protein levels was specific, because thymidylate kinase and thymidylate synthase protein levels were not decreased nor were p53 or p21WAF1 protein levels increased. The decrease in DHFR protein was abolished when the cells were treated with the proteasome inhibitor MG132, demonstrating that p14(ARF) augments proteasomal degradation of the protein. Surprisingly, induction of p14(ARF) increased resistance to the folate antagonists methotrexate, trimetrexate, and raltitrexed. Depletion of thymidine in the medium reversed this resistance, indicating that p14(ARF) induction increases the reliance of these cells on thymidine salvage.</AbstractText>" +
            "               </Abstract>" +
            "               <Affiliation>Joan and Sanford I. Weill Graduate School of Medical Sciences of Cornell University, New York, New York.</Affiliation>" +
            "               <AuthorList>" +
            "                   <Author>" +
            "                       <LastName>Magro</LastName>" +
            "                       <FirstName>Pellegrino G.</FirstName>" +
            "                       <Initials>PG</Initials>" +
            "                   </Author>" +
            "                   <Author>" +
            "                       <LastName>Russo</LastName>" +
            "                       <FirstName>Angelo J.</FirstName>" +
            "                       <Initials>AJ</Initials>" +
            "                   </Author>" +
            "                   <Author>" +
            "                       <LastName>Li</LastName>" +
            "                       <FirstName>Wei-Wei</FirstName>" +
            "                       <Initials>WW</Initials>" +
            "                   </Author>" +
            "                   <Author>" +
            "                       <LastName>Banerjee</LastName>" +
            "                       <FirstName>Debabrata</FirstName>" +
            "                       <Initials>D</Initials>" +
            "                   </Author>" +
            "                   <Author>" +
            "                       <LastName>Bertino</LastName>" +
            "                       <FirstName>Joseph R.</FirstName>" +
            "                       <Initials>JR</Initials>" +
            "                   </Author>" +
            "               </AuthorList>" +
            "               <Language>ENG</Language>" +
            "               <PublicationTypeList>" +
            "                   <PublicationType>JOURNAL ARTICLE</PublicationType>" +
            "               </PublicationTypeList>" +
            "           </Article>" +
            "           <MedlineJournalInfo>" +
            "               <Country/>" +
            "               <MedlineTA>Cancer Res</MedlineTA>" +
            "               <NlmUniqueID>2984705R</NlmUniqueID>" +
            "           </MedlineJournalInfo>" +
            "       </MedlineCitation>" +
            "       <PubmedData>" +
            "           <History>" +
            "               <PubMedPubDate PubStatus=\"pubmed\">" +
            "                   <Year>2004</Year>" +
            "                   <Month>6</Month>" +
            "                   <Day>19</Day>" +
            "                   <Hour>5</Hour>" +
            "                   <Minute>0</Minute>" +
            "               </PubMedPubDate>" +
            "               <PubMedPubDate PubStatus=\"medline\">" +
            "                   <Year>2004</Year>" +
            "                   <Month>6</Month>" +
            "                   <Day>19</Day>" +
            "                   <Hour>5</Hour>" +
            "                   <Minute>0</Minute>" +
            "               </PubMedPubDate>" +
            "           </History>" +
            "           <PublicationStatus>ppublish</PublicationStatus>" +
            "           <ArticleIdList>" +
            "               <ArticleId IdType=\"pubmed\">15205349</ArticleId>" +
            "               <ArticleId IdType=\"doi\">10.1158/0008-5472.CAN-03-1045</ArticleId>" +
            "               <ArticleId IdType=\"pii\">64/12/4338</ArticleId>" +
            "           </ArticleIdList>" +
            "       </PubmedData>" +
            "   </PubmedArticle>";
    }
}


/* these are xpaths that would work for the previous test xml: */

//  /** this is the root tag of the document, all other xpaths will be children of this node */
//  private static String rootXPath = "MedlineCitation";

//  /** xpath to the unique id of this article */
//  private static String pmidXPath = "/PubmedArticle[1]/MedlineCitation/PMID";

//  /** xpath to article */
//  private static String articleXPath = "Article";

//  /** xpath to article title */
//  private static String articleTitleXPath = "/PubmedArticle[1]/MedlineCitation/Article/ArticleTitle";


//  /** xpath to journal number node */
//  private static String journalXPath ="/PubmedArticle[1]/MedlineCitation/MedlineJournalInfo/MedlineTA";
//  /** xpath to volume number node */
//  private static String volumeXPath = "/PubmedArticle[1]/MedlineCitation/Article/Journal/JournalIssue/Volume";
//  /** xpath to issue number node */
//  private static String issueXPath =  "/PubmedArticle[1]/MedlineCitation/Article/Journal/JournalIssue/Issue";
//  /** xpath to pagination node, the node's text should be page the range */
//  private static String pageXPath =   "/PubmedArticle[1]/MedlineCitation/Article/Pagination/MedlinePgn";

//  /** xpath to the year this article was published */
//  private static String pubYearXPath = "/PubmedArticle[1]/MedlineCitation/Article/Journal/JournalIssue/PubDate/Year";

//  /** xpath to author or authors, this will work properly if
//      the xpath resolves to several nodes */
//  private static String authorsXPath = "/PubmedArticle[1]/MedlineCitation/Article/AuthorList/Author";

//  /** xpath to the abstract, not required */
//  private static String abstractXPath = "/PubmedArticle[1]/MedlineCitation/Article/Abstract/AbstractText";

//  /** xpath to language of article, not required */
//  private static String langXPath = "/PubmedArticle[1]/MedlineCitation/Article/Language";
//  /** default language if none is found */
//  private static String defaultLang = "ENG";

//  /** xpath to the update code, which is a date.  This will work properly if the
//      xpath resolves to several nodes.  The date that is the most receint will be used.*/
//  private static String updateXPath = "/PubmedArticle[1]/PubmedData/History/PubMedPubDate";
