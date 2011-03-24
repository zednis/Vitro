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
import java.util.ArrayList;

import org.dom4j.Element;
/**
 * For Thompson Web Of Scienc you need to be able to download
 * large numbers of articles.  The service is limited to only
 * getting 100 article records per a request.  So you have to
 * make a searchRetrieve request followed by frequest for
 * groups of article id (Thompson's "ut" field), followed by actual
 * article requests.
 *
 *
 * @author bdc34
 *
 */
public class ThompsonWSRecordSource extends WebServiceRecordSource {
    private String requestFilename;
    private String requestUrl;
    private String requestFront; //first half of request
    private String requestBack;  //second half of request, key goes between the two.

    public ThompsonWSRecordSource(String url, String wsscFilename) throws IOException {
        super(url, wsscFilename);
        // TODO Auto-generated constructor stub
    }

    public ThompsonWSRecordSource(String queryUrl, String queryFilename,
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

        //break the request in half at the KEY_MARKER
        //later we will sandwich the keys List values between these halves.
        int endOfFront = sb.indexOf(KEY_MARKER);
        int startOfBack = endOfFront + KEY_MARKER.length();
        if( endOfFront < 0 )
            throw new Error("There was no key marker, " + KEY_MARKER + ", found in the request wssc");
        requestFront = sb.substring(0,endOfFront);
        requestBack = sb.substring(startOfBack);
    }

    @Override
    public ArrayList getKeysFromResult(String results) {
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
    @Override
    public String keyToSoapRequest(String key) { return requestFront + key + requestBack;  }

    /**
     * Here we will take the result of a call to keyToSoapRequest()
     * and turn it into a Dom4J Document object.
     */
    @Override
    public Element toElement(String lastResult) {
        // TODO Auto-generated method stub
        return null;
    }

    public static final String KEY_MARKER = "----key----";
}
