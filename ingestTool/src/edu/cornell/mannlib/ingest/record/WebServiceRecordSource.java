package edu.cornell.mannlib.ingest.record;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.cornell.mannlib.ingest.interfaces.RecordSource;
import edu.cornell.mannlib.ingest.util.NetUtils;

/**
 * Gets the result of a web service call.  The call is made using a
 * supplied wssc file.  The contents of the file will be posted to
 * given address.
 *
 * @author bdc34
 *
 */
public class WebServiceRecordSource implements RecordSource {
    Iterator keyIterator;
    ArrayList keys;
    String initialQueryResult;
    String lastResult;
    private String requestFilename;
    private String requestUrl;
    private String requestFront; //first half of request
    private String requestBack;  //second half of request, key goes between the tw
    private SAXReader saxReader;

    private static final Log log = LogFactory.getLog(WebServiceRecordSource.class.getName());

    public WebServiceRecordSource(String queryUrl, String queryFilename,
                                  String requestUrl, String requestFilename)
    throws IOException{
        this(queryUrl, queryFilename);
        this.requestFilename = requestFilename;
        this.requestUrl = requestUrl;

        File file = new File(requestFilename);
        if( !file.exists() )
            throw new IOException("the file " +file.getCanonicalPath() + " does not exist.");
        if( !file.canRead() )
            throw new IOException("the file " + file.getCanonicalPath() + " is not readable.");
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String buffer = null;
        StringBuilder sb = new StringBuilder();
        while( (buffer = reader.readLine() ) != null ){
            sb.append(buffer).append('\n');
        }

//      break the request in half at the KEY_MARKER
//      later we will sandwich the keys List values between these halves.
        int endOfFront = sb.indexOf(KEY_MARKER);
        int startOfBack = endOfFront + KEY_MARKER.length();
        if( endOfFront < 0 )
            throw new Error("There was no key marker, " + KEY_MARKER + ", found in the request wssc");
        requestFront = sb.substring(0,endOfFront);
        requestBack = sb.substring(startOfBack);
    }

    protected WebServiceRecordSource(String url, String wsscFilename) throws IOException{
        if( url == null )
            throw new IllegalArgumentException("url must not be null");
        if( wsscFilename == null )
            throw new IllegalArgumentException("wsscFilename must not be null");

        File wssc = new File(wsscFilename);
        if( !wssc.exists() )
            throw new IOException("the file " + wssc.getCanonicalFile() + " does not exist.");
        if( !wssc.canRead() )
            throw new IOException("the file " + wssc.getCanonicalPath() + " is not readable.");

        initialQueryResult = NetUtils.postSoapFromFile(url,"action",wsscFilename);
        if( initialQueryResult == null )
            throw new IOException("SOAP request returned an empty string");

        String fault = "no fault";
        if( (fault = checkForFault( initialQueryResult )) != null )
            throw new IOException("SOAP request returned a fault: " + fault );

        keys = getKeysFromResult(initialQueryResult);
        if( keys == null )
            throw new IOException("Unable to get keys from SOAP response");
        else
            System.out.println("keys found: " + keys.size() );

        keyIterator = keys.iterator();

        saxReader = new SAXReader();
    }

    public Element next() /* throws Error */ {
        if( keyIterator.hasNext() == false)
            return null;
        String key = (String)keyIterator.next();
        String request = keyToSoapRequest(key);
        try{
            lastResult = NetUtils.postSoapFromStr(requestUrl, REQUEST_OPERATION, request);
        }catch(IOException ioe){
            throw new Error(ioe); //<<< Error gets thrown here.
        }
        return toElement(lastResult);
    }

    /**
     * @return null if not fault, a description otherwise.
     */
    protected String checkForFault(String in){
        if( in == null ) return null;
        if( in.matches(".*<faultstring>.*</faultstring>.*") ){
            String rt = null;
            rt = in.replaceFirst(".*<faultstring>","");
            rt = rt.replaceFirst("</faultstring>.*","");
            return rt;
        } else {
            return null;
        }
    }

    /**
     * Converts the result of the initial query into a list of keys
     * that will be used in the keyToSoapRequest() method.
     * Should return a empty list if there are no keys in the result.
     */
    protected ArrayList getKeysFromResult(String results) {
        String keyStart = "&lt;KEY&gt;";
        String keyEnd   = "&lt;/KEY&gt;";

        //since Thompson is returning fake xml we'll just extract the trings
        if( results == null || results.length() == 0 )
            return new ArrayList();

        //get rid of SOAP header
        StringBuilder sb = new StringBuilder(results.replaceFirst(".*&lt;RECORDS&gt;",""));

        int i = sb.indexOf(keyStart) + keyStart.length();
        int j = sb.indexOf(keyEnd);
        String key = null;
        ArrayList keys = new ArrayList();
        while( j > -1 ){
            key = sb.substring(i,j);
            if( key != null && key.length() > 0)
                keys.add(key);
            sb.delete(0,j+keyEnd.length());

            i = sb.indexOf(keyStart) + keyStart.length();
            j = sb.indexOf(keyEnd);
        }
        return keys;
    }

    /**
     * Here we need to take a key String and run a SOAP
     * request with it, then return the results.
     */
    protected String keyToSoapRequest(String key) {
        return requestFront + key + requestBack;
    }

    /**
     * Here we will take the result of a call to keyToSoapRequest()
     * and turn it into a Dom4J Document object.  This expects to only get
     * one article at a time.
     *
     * The result will look like this:
     * <?xml version="1.0" encoding="UTF-8"?><soapenv:Envelope>xml etc. ...
     * <sopeenv:Bodn>...<retrieveReturn xsi:type="xsd:string">
     * &lt;RECORDS&gt; ... escaped xml for record etc. ...
     * </retrieveReturn>...</soapenv:Envelope>
     * @throws DocumentException
     *
     */
    public Element toElement(String result) {
        if( result == null ) return null;

        int start = -1, end = -1;
        StringReader sr=null;
        Element ele=null;

        try{
            start = result.indexOf(RESULT_START );
            end = result.indexOf(RESULT_END )+ RESULT_END.length();
            sr = new StringReader(StringEscapeUtils.unescapeXml(result.substring(start,end)));
            ele = null;
        }catch(Exception ex){
            log.error("Error extracting xmlfrom result:" + ex);
            return null;
        }

        if( sr == null ){
            log.error("Result Error: " + result);
            return null;
        }

        try {
            Document doc = saxReader.read(sr);
            return doc.getRootElement();
        } catch (DocumentException e) {
           log.error("Error reading xml from result:" + e);
           return null;
        }       
    }

    public String getInitialQueryResult(){
        return initialQueryResult;
    }

    public ArrayList getKeys(){
        return keys;
    }

    public boolean hasNext() {
        return keyIterator.hasNext();
    }

    public void remove() {}

    /* ******************** Static Properties ******************************* */
    public static final String KEY_MARKER = "----keys----";
    public static final String REQUEST_OPERATION = "retrieve";
    public static final String RESULT_START="&lt;RECORDS&gt;";
    public static final String RESULT_END = "&lt;/RECORDS&gt;";
    /* ************************* Abstract Methods *************************** */
    /**
     * Converts the result of a key request to a Document.
     */
//    abstract public Document toDocument(String lastResult) ;

    /**
     * Turns a single key into a Soap request.
     */
  //  abstract public String keyToSoapRequest(String key);

}
