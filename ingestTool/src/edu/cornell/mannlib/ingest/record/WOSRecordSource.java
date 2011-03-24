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
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.isinet.esti.soap.search.SearchResults;
import com.isinet.esti.soap.search.SearchRetrieve;
import com.isinet.esti.soap.search.SearchRetrieveService;
import com.isinet.esti.soap.search.SearchRetrieveServiceLocator;

import edu.cornell.mannlib.ingest.interfaces.RecordSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.dom4j.DocumentFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Steps:
 * 1) make the initial query to get recordsFound and first maxNumRec "ut" keys
 * 2) get all the "ut" keys by incrementing firstRec.
 * 3) setup an iterator so hasNext() and next() work as expected
 * 4) whenever next() is called, go out to WOS and get a full record using the "ut" key.
 *
 * Timeframe (what thompson calls depth):
A depth may be either an empty string, one of the codes below or a blank space separated list of
years or year ranges. A year is any four digit integer greater than 1945 and less than or equal to
the current year, such as '2001' or '1990'. A year range is express as two years joined with a '-'
symbol. It includes all years greater than or equal to the first year and less than or equal to the
second year. Some examples of year ranges are: '1945-2004', '1990-2000' and '2001-'2004'. If
depth is null or an empty string, then the default behavior is to search all subscribed years. In
addition the bibliographic data for the chemistry records is added special chem. depths when new
records are added to the chemistry database. These chem. weeks should be added to weekly
depths when ever one of the chem. editions (IC or CCR) is selected.
Code                     Description
1week                    The latest week of data.
2week                    The latest two weeks of data.
4week                    The latest four weeks of data.
1week_chem               The latest week of chem. data.
2week_chem               The two weeks of chem. data.
4week_chem               The four weeks of chem. data.
 *
 * This Class defaults to loading the last week of articles using the string "1week"
 *
 * @author bdc34
 *
 */
public class WOSRecordSource implements RecordSource {

    private SearchRetrieveService service;
    private SearchRetrieve searchWos;
    private String query;
    private String timeframe;
    private List<String> keys;
    private Iterator<String> keyIterator;
    private SAXReader saxReader = new SAXReader();
    private static final Log log = LogFactory.getLog(WOSRecordSource.class.getName());

    public WOSRecordSource() throws ServiceException, RemoteException, DocumentException {
        this(default_query, default_depth);
    }

    public WOSRecordSource(String query, String timeframe) throws ServiceException, RemoteException, DocumentException {
        this.service = new SearchRetrieveServiceLocator(); //uses url endpoint from wsdl
        this.searchWos = service.getSearchRetrieve();
        this.query = query;
        this.timeframe = timeframe;
        this.keys = getKeyList(this.query, this.timeframe);
        this.keyIterator = this.keys.iterator();
    }

    private List<String> getKeyList(String query, String timeframe) throws RemoteException, DocumentException {
        List<String> utList = new LinkedList<String>();
        SearchResults result = this.searchWos.searchRetrieve(
                WOS_DB_ID, query, timeframe, null, null, 1, maxNumRec, KEY_FIELD);

        if (result == null) {
            System.out.println("first result was NULL");
            return utList;
        } else {
            System.out.println("first result, occurancesFound:" + result.getOccurancesFound() + " recordsFound:" + result.getRecordsFound());
        }

        int recordsFound = result.getRecordsFound();
        List<String> utFromResult = getUtKeysFromString(result.getRecords());
        utList.addAll(utFromResult);
        int utCount = utFromResult.size();

        while (utCount < recordsFound) {
            result = this.searchWos.searchRetrieve(
                    WOS_DB_ID, query, timeframe, null, null, utCount + 1, maxNumRec, KEY_FIELD);
            if (result == null) {
                log.error("WOS webservice returned a null result");
                break;
            }
            utFromResult = getUtKeysFromString(result.getRecords());
            if (utFromResult.size() < 1) //bust out on 0 records to avoid inf loops
            {
                break;
            }
            utCount += utFromResult.size();
            utList.addAll(utFromResult);
        }
        //log.debug("Total record keys from WOS: " + utList.size());
        System.out.println("Total record keys from WOS: " + utList.size());
        return utList;
    }

    private List<String> getUtKeysFromString(String xmlStr) throws DocumentException {
        LinkedList<String> utList = new LinkedList<String>();
        Document doc = saxReader.read(new StringReader(xmlStr));
        List nodes = doc.selectNodes("//ut");
        for (Object obj : nodes) {
            String ut = null;
            if (obj instanceof Node) {
                ut = ((Node) obj).getText();
            } else {
                log.warn("non Node result from selectNodes()");
            }
            if (ut != null) {
                utList.add(ut);
            }
        }
        return utList;
    }

    public boolean hasNext() {
        return this.keyIterator.hasNext();
    }

    public Element next() {
        // this could be improved by retrieve records in batches
        // primaryKey in searchWos.retrieve can be formated like:
        // "234234234234 2342342320002 020020203993 0203992039002 02399023920 ..."
        String utKey = this.keyIterator.next();
        return getRecordFromUtKey(utKey);
    }

    /**
     * Go to WOS web service and get the full record for the ut key.
     */
    public Element getRecordFromUtKey(String utKey) {
        Document doc = null;
        try {
            String result = this.searchWos.retrieve(WOS_DB_ID, utKey, null, ALL_FIELDS);
            doc = this.saxReader.read(new StringReader(result));
        } catch (Exception e) {
            log.error("could not get record from WOS web service: " + e);
        }
        return doc.getRootElement();
    }

    public void remove() {
        // TODO Auto-generated method stub
    }

    /**
     * For testing.
     * @param argv
     */
    public static void main(String argv[]) {
        try {            
            String query = default_query;
            String depth = default_depth;
            if( argv != null && argv.length == 1 ){
                query = argv[0];
            }else if( argv != null && argv.length == 2){
                query = argv[0];
                depth = argv[1];
            }
            WOSRecordSource wrs = new WOSRecordSource(query,depth);

            int count = 0;
            while (wrs.hasNext()) {

                System.out.println(++count);
                Element ele = wrs.next();  

                String filename =  count + ".xml";
                FileOutputStream fos = new FileOutputStream(filename);
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(fos, format);

                Document doc = DocumentFactory.getInstance().createDocument();
                doc.add(ele);
                writer.write(doc.asXML());
                
                writer.flush();
                writer.close();
            }
        } catch (Exception ex) {
            System.out.println("Error in main WOSRecordSource " + ex);
            ex.printStackTrace();
        }
    }
    public static final String default_query = "ad=(cornell*)";
    public static final String default_depth = "1week";
    private static final String WOS_DB_ID = "WOS";
    private static final String KEY_FIELD = "ut";
    private static final String ALL_FIELDS = null; //null indicates all fields to web service
    private static final int maxNumRec = 100;
}
