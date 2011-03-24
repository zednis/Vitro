import java.io.*;
import java.net.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import java.nio.charset.Charset;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;

/**
 * Get a set of articles from the pubmed web service.
 * The properties file has the settings like the service
 * urls and the query strings.
 *
 * Retrieving the article citations is a two step process.
 * First a search must be done to get a list of article
 * id's.
 * Second these citations must be fetched from the server.
 *
 * We use the history feature of the pubmed system where we can
 * do a query and then refer to the results of that query for
 * the fetch.
 * @author bdc34
 * @date 2005-03-23
 */

public class Fetcher{

    private static String propertiesFile = null;
    private static String countXPath = null;
    private static String webEnvXPath = null;
    private static String queryKeyXPath = null;

    private static String searchQuery = null;
    private static String searchURL = null;
    private static String fetchQuery = null;
    private static String fetchURL = null;

    private static String startMark = "<<<<start";
    private static String retryMark = "<<<<retrymax";
    private static String webEnvMark = "<<<<webenv";
    private static String queryKeyMark = "<<<<querykey";
    private static int retmax = 500;

    private static String outfile = "fetch.xml";

    private static void getProperties(String propertiesFile)
        throws IOException {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream( propertiesFile );
        props.load(in);
        searchURL = props.getProperty("Fetcher.searchURL");
        fetchURL = props.getProperty("Fetcher.fetchURL");
        searchQuery = props.getProperty("Fetcher.searchQuery");
        fetchQuery = props.getProperty("Fetcher.fetchQuery");

        countXPath = props.getProperty("Fetcher.countXPath");
        webEnvXPath = props.getProperty("Fetcher.webEnvXPath");
        queryKeyXPath = props.getProperty("Fetcher.queryKeyXPath");
        outfile = props.getProperty("Fetcher.outfile");
    }

    public static void main(String args[]){
        if(args.length > 1 || args.length == 0 ){
            System.out.println("Usage: java Fetcher fetch.properties");
            return;
        }
        propertiesFile = args[0];

        int count = 0;
        String webEnv = null;
        String queryKey = null;
        Number temp = null;
        try{
            getProperties(propertiesFile);
        }catch (IOException ex){
            System.out.println("could not read properties file\n" + ex);
        }
        String httpReq = searchURL + searchQuery;
        try{
            URL url = new URL(httpReq);
            System.out.println(url);
            Document doc = stream2Dom( url.openStream() );

            webEnv = doc.valueOf(webEnvXPath);
            queryKey = doc.valueOf(queryKeyXPath);
            count = doc.numberValueOf(countXPath).intValue();
            System.out.println("articles to fetch: " + count);

            String chunk;
            File outputFile = new File( outfile );
            FileOutputStream fos = new FileOutputStream( outputFile );
            OutputStreamWriter osw =
                new OutputStreamWriter(fos, Charset.forName("UTF-8"));

            System.out.println("out encoding: " + osw.getEncoding());

            for(int restart=0; restart < count; restart += retmax){
                System.out.println("getting: "+ restart);

                String query=fetchQuery.replaceAll(startMark, ""+restart );
                query = query.replaceAll( retryMark, ""+retmax );
                query = query.replaceAll( webEnvMark, webEnv );
                query = query.replaceAll( queryKeyMark, queryKey );

                System.out.println("query: " + query);

                URL fetchUrl= new URL(fetchURL + query);
                BufferedReader in =
                    new BufferedReader(new InputStreamReader( fetchUrl.openStream() ));
                while( (chunk = in.readLine()) != null ){
                    //here we have to check for the </pubmedArticleSet> and remove it
                    if( restart != 0 ){ //if not first batch remove this stuff
                        if( chunk.matches("<\\?xml.*") ||
                            chunk.matches("<.DOCTYPE.*") ||
                            chunk.matches("<PubmedArticleSet>.*") )
                            continue;
                    }
                    if( restart + retmax < count ){//if not last batch remove this stuff
                        if( chunk.matches("</PubmedArticleSet>.*") )
                            continue;
                    }
                    osw.write(chunk);
                    osw.write("\n");
                }
                in.close();
            }
            osw.close();

        }catch (Exception ex){
            System.out.println(ex);
        }
    }


    public static Document stream2Dom(InputStream in)throws DocumentException {
        InputStreamReader isr = new InputStreamReader( in );
        org.dom4j.Document doc = null;
        SAXReader xmlReader = new SAXReader();
        doc = xmlReader.read( isr );
        return doc;
    }
}


// # example properties file:
// # Properties file for Fetcher.java
// # bdc34@cornell.edu
// # 2005-03-23
// #

// ####
// # some pubmed webservice parameters:
// # db=PubMed
// # usehistory=y
// # tool=Fetcher
// # email=bdc34@cornel.edu
// # term=asthma[mh]+OR+hay+fever[mh]
// # reldate=90
// # retmode=xml

// #### base of the pubmed search webservice
// Fetcher.searchURL=http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?
// # pubmed query string
// Fetcher.searchQuery=db=PubMed&usehistory=y&reldate=90&term=cornell[Affiliation]+AND+hasabstract[text]

// #### xpaths to get the values from the search result that we need
// #xpath to the count as a number
// Fetcher.countXPath=number(/eSearchResult/Count)

// #xath to the webenv as a string
// Fetcher.webEnvXPath=string(/eSearchResult/WebEnv)

// #xath to the query key as a string
// Fetcher.queryKeyXPath=string(/eSearchResult/QueryKey)

// #### fetch properties
// # base of the pubmed fetch webservice
// Fetcher.fetchURL=http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?

// # fetch query string
// # <<<<start, <<<<retrymax, <<<<webenv, <<<<querykey will be replaced
// # with the values from the search result.
// Fetcher.fetchQuery=db=PubMed&retmode=xml&WebEnv=<<<<webenv&query_key=<<<<querykey&restart=<<<<start&retmax=<<<<retrymax

// ### output file
// Fetcher.outfile=pubmed.fetch.xml


