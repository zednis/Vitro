package edu.cornell.mannlib.ingest.util;

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

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class NetUtils {
    public static String postSoapFromFile(String strURL, String strSoapAction, String strXMLFilename )
    throws IOException{
        /* This is from commons-httpclient example code */
        /* http://svn.apache.org/viewvc/jakarta/commons/proper/httpclient/trunk/src/examples/PostSOAP.java?revision=480424&view=markup */

        File input = new File(strXMLFilename);
        // Prepare HTTP post
        PostMethod post = new PostMethod(strURL);
        // Request content will be retrieved directly
        // from the input stream
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        post.setRequestEntity(entity);
        // consult documentation for your web service
        post.setRequestHeader("SOAPAction", strSoapAction);
        // Get HTTP client
        HttpClient httpclient = new HttpClient();
        String out = null;
        // Execute request
        try {
            int result = httpclient.executeMethod(post);
            // Display status code
            System.out.println("Response status code: " + result);
            // Display response
            //System.out.println("Response body: ");
            //System.out.println(post.getResponseBodyAsString());
            out = post.getResponseBodyAsString();
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }
        return out;
    }

    public static String postSoapFromStr(String strURL, String strSoapAction, String soapContent )
    throws IOException{
        /* This is from commons-httpclient example code */
        /* http://svn.apache.org/viewvc/jakarta/commons/proper/httpclient/trunk/src/examples/PostSOAP.java?revision=480424&view=markup */

        // Prepare HTTP post
        PostMethod post = new PostMethod(strURL);
        // Request content will be retrieved directly
        // from the input stream
        RequestEntity entity = new StringRequestEntity(soapContent,null,null);
        post.setRequestEntity(entity);
        // consult documentation for your web service
        post.setRequestHeader("SOAPAction", strSoapAction);
        // Get HTTP client
        HttpClient httpclient = new HttpClient();
        String out = null;
        // Execute request
        try {
            int result = httpclient.executeMethod(post);
            // Display status code
            System.out.println("Response status code: " + result);
            // Display response
            //System.out.println("Response body: ");
            //System.out.println(post.getResponseBodyAsString());
            out = post.getResponseBodyAsString();
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }
        return out;
    }
}
