package edu.cornell.mannlib.ingest.fieldHandlers;

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

import edu.cornell.mannlib.ingest.configurations.ThompsonWosIngest;
import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
//import edu.cornell.mannlib.vitro.dao.db.NameMatch;
//import edu.cornell.mannlib.vitro.dao.db.VitroConnection;
//import edu.cornell.mannlib.vitro.webapp.dao.VitroFacade;
//import edu.cornell.mannlib.vitro.webapp.dao.db.EntityWebappDaoDb;
//import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
//import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import net.sf.saxon.om.NamespaceConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.xpath.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.*;

// NEEDS MAJOR UPDATING to remove dependencies on dao.db.* classes

/**
 * Gets the author elements from the WOS xml and attempts to match them
 * with folks in vivo.  If matches are found, make ents2ents rows.
 * If no match is found for an author, make an 'unmatched author'
 * entity.
 *
 * THIS CLASS IS A MESS.  It could easily be broken into two or three.
 * The name matching is general use and could be put in a different class.
 *
 * @author bdc34
 *
 */

/*
  example of authors element:
  <authors count="5">
                <primaryauthor>Valla, T</primaryauthor>
                <fullauthorname>
                    <AuRole>Author, Reprint Author</AuRole>
                    <AuLastName>Valla</AuLastName>
                    <AuFirstName>T.</AuFirstName>
                    <AuCollectiveName>Valla, T.</AuCollectiveName>
                </fullauthorname>
                <author key="2079040">Fedorov, AV</author>
                <fullauthorname>
                    <AuRole>Author</AuRole>
                    <AuLastName>Fedorov</AuLastName>
                    <AuFirstName>A. V.</AuFirstName>
                    <AuCollectiveName>Fedorov, A. V.</AuCollectiveName>
                </fullauthorname>
                <author key="4056939">Lee, J</author>
                <fullauthorname>
                    <AuRole>Author</AuRole>
                    <AuLastName>Lee</AuLastName>
                    <AuFirstName>Jinho</AuFirstName>
                    <AuCollectiveName>Lee, Jinho</AuCollectiveName>
                </fullauthorname>
                <author key="1570098">Davis, JC</author>
                <fullauthorname>
                    <AuRole>Author</AuRole>
                    <AuLastName>Davis</AuLastName>
                    <AuFirstName>J. C.</AuFirstName>
                    <AuCollectiveName>Davis, J. C.</AuCollectiveName>
                </fullauthorname>
                <author key="2675525">Gu, GD</author>
                <fullauthorname>
                    <AuRole>Author</AuRole>
                    <AuLastName>Gu</AuLastName>
                    <AuFirstName>G. D.</AuFirstName>
                    <AuCollectiveName>Gu, G. D.</AuCollectiveName>
                </fullauthorname>
            </authors>

    Sometimes there will be no <fullauthorname> elements in
    the <authors> element and the only author info is in the
    <reprint> element:
            <authors count="1">
                <primaryauthor>Johnson, S</primaryauthor>
            </authors>
            <reprint>
                <rp_author>Johnson, S</rp_author>
                <rp_address>Cornell Univ, Ithaca, NY 14853 USA</rp_address>
                <rp_organization>Cornell Univ</rp_organization>
                <rp_city>Ithaca</rp_city>
                <rp_state>NY</rp_state>
                <rp_country>USA</rp_country>
                <rp_zips count="1">
                    <rp_zip location="AP">14853</rp_zip>
                </rp_zips>
            </reprint>

     Here is yet another way you might find the <authors> element:
       <authors count="3">
          <primaryauthor>Cohen, CT</primaryauthor>
          <author key="1270550">Chu, T</author>
          <author key="1321555">Coates, GW</author>
       </authors>
 */
public class WosAuthorMatch implements FieldHandler {
//    static XPathFactory xpf;
//    XPath xpe;
//    NameMatch nameMatcher;
//
//    private HashSet <WosAuthorData>matchedNames;
//    private HashSet <WosAuthorData>unmatchedNames;
//
//    private static String PRIMARY_AUTHOR_XP= "//primaryauthor/text()";
//    private static String ALL_AUTHORS_XP= "//item/authors/*";
//    private static String KEY_XP= "//@key";
//
//    public static int PUB_TO_AUTHOR_PROP = 105;
//    public static int PERSON_VCLASSID =28;
//
//    public static int WOS_AUTHOR_DATAPROPID = 45; //<<<<<<<<<<<<<<<<<<<< make this dataprop
//    public static int WOS_AUTHORKEY_DATAPROPID = 51; //<<<<<<<<<<<<<<<<<< make this dataprop
//
//    public static int VISIBLE_TO_CURATORS = 2;
//    public static int VISIBLE_TO_ALL = 0;
//
//    private XPathExpression xpPrimaryAuth;
//    private XPathExpression xpAllAuthors;
//
//    private XPathExpression fullAuElementNodes ;
//    private XPathExpression reprintElementNodes;
//    private XPathExpression authorElementsOnly;
//    private XPathExpression primaryauthorElementsOnly;
//
//    private String fullAuElementXP ="//item/authors/fullauthorname";
//    private String reprintElementXP="//item/reprint/rp_author";
//    private String authorElementOnlyXP="//item/authors/author";
//    private String primaryauthorElementOnlyXP="//item/authors/primaryauthor";
//
//    //these are only to collect stats
//    private List<Integer> matchedScores;
//    private List<Integer> closeCounts;
//    private List<Float> scoreToCloseRatio;
//    private int totalAuthorChecks = 0;
//    private int totalEmailMatches = 0;
//    private int match = 0;
//    private int missmatch = 0;
//    private boolean makeEntForMismatch = true;
//
//    private static final int UNKNOWN_AUTHOR_FORMAT = -1,
//       FULLAU_EXIST = 1, REPRINT_ONLY = 2, AUTHOR_ELEMENTS_ONLY = 3;
//
//    //used to pull list of names from db. bad style, could be fixed by adding name stuff to a dao
//    private VitroConnection con;
//
//    private VitroFacade facade;
//    private static final Log log = LogFactory.getLog(WosAuthorMatch.class.getName());
//    public String debugDir = "debug";
//
//    public WosAuthorMatch(VitroConnection con, VitroFacade facade)
//    throws XPathFactoryConfigurationException, XPathExpressionException{
//        //constructors like this are not thread safe
//        if( xpf == null ){
//            //set up a saxon parser, get a factory and reset the system property
//            String key = "javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON;
//
//            System.setProperty(key, "net.sf.saxon.xpath.XPathFactoryImpl");
//            xpf = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
//            log.info("Loaded XPath Factory " + xpf.getClass().getName());
//            xpe = xpf.newXPath();
//            log.info("Loaded XPath Evaluator " + xpe.getClass().getName());
//        } else {
//            xpe = xpf.newXPath();
//        }
//        xpPrimaryAuth = xpe.compile(PRIMARY_AUTHOR_XP);
//        xpAllAuthors = xpe.compile(ALL_AUTHORS_XP);
//        fullAuElementNodes = xpe.compile(fullAuElementXP);
//        reprintElementNodes= xpe.compile(reprintElementXP);
//        authorElementsOnly = xpe.compile(authorElementOnlyXP);
//        primaryauthorElementsOnly = xpe.compile(primaryauthorElementOnlyXP);
//
//        this.con = con;
//        this.facade = facade;
//        this.nameMatcher = new NameMatch();
//        matchedScores = new LinkedList<Integer>();
//        closeCounts = new LinkedList<Integer>();
//        scoreToCloseRatio = new LinkedList<Float>();
//        matchedNames = new HashSet<WosAuthorData>();
//        unmatchedNames = new HashSet<WosAuthorData>();
//    }
//
//    public WosAuthorMatch(VitroConnection con, VitroFacade facade, boolean makeEntForMismatch)
//    throws XPathFactoryConfigurationException, XPathExpressionException{
//        this(con,facade);
//        this.makeEntForMismatch = makeEntForMismatch;
//    }
//
//    /* This is the method that get executed for each article xml */
    public void setFields(Element ele, Object targetEnt) {
//        if( targetEnt == null || !(targetEnt instanceof IndividualWebapp))
//            return;
//        IndividualWebapp entity = (IndividualWebapp)targetEnt;
//
//        //just stick the primary author in as a dataprop
//        makePrimaryAuthorDataProp(doc,entity);
//
//        //get the authors from the xml
//        List<WosAuthorData>authors = getAuthors( doc );
//
//        //match the authors with vivo
//        boolean foundAtLeastOne = false;
//        for( WosAuthorData ad : authors ){
//            int matchedId = -1;
//            matchedId = this.getBestExistingMatch( ad );
//            if( matchedId > 0){
//                //TODO: update for semweb-align
//                if(true) throw new Error("TODO: update for semweb-align");
//            //matchAuthorWithExistingEntity(ad, entity.getId(), matchedId);
//                foundAtLeastOne = true;
//                matchedNames.add(ad);
//            }else{
//                unmatchedNames.add(ad);
//                //TODO: update for semweb-align
//                if(true) throw new Error("TODO: update for semweb-align");
//            //makeNewAuthorEntity(ad, entity.getId());
//            }
//        }
//
//        if(!foundAtLeastOne ){ //hide if we didn't find any author matches
//            entity.setStatusId(VISIBLE_TO_CURATORS);
//            //log.warn("Entity " + entity.getId() + " lacked author matches, hidden using statusId");
//        }else
//            entity.setStatusId(VISIBLE_TO_ALL);
//
//        //add authors to pub as dataprops
//        for( WosAuthorData ad : authors ){
//            DataPropertyStatement e2d = new DataPropertyStatement();
//            //TODO: update for semweb-align
//            if(true) throw new Error("TODO: update for semweb-align");
//            //e2d.setEntityId(entity.getId());
////             e2d.setData( ad.toJson() );
////             e2d.setDatapropId( WOS_AUTHOR_DATAPROPID );
////             facade.insertNewEnts2Data(e2d);
//        }
    }
//
//    private void matchAuthorWithExistingEntity(WosAuthorData author, int pubEntId, int personId){
//        //check to see if we have a authorkey.  very inefficient
//        Individual entity = new Individual();
//        //TODO: fix to work with new data access objs
//        facade.getCoreDaoFactory().getEnts2DataDao().fillExistingDataPropertyStatementsForIndividual(entity);
//        boolean hasWosKey = false;
//        for( DataPropertyStatement row : entity.getDataPropertyStatements() ){
////TODO: update for semweb-align
//            if(true) throw new Error("TODO: update for semweb-align");
////             if( row.getDatapropId() == WOS_AUTHOR_DATAPROPID){
////                 hasWosKey = true;
////                 break;
////             }
//        }
//
//        doAuthorAssociation(author, pubEntId, personId);
//    }
//
//    private void makeNewAuthorEntity(WosAuthorData author, int pubEntId){
//        if( !makeEntForMismatch ) return;
//
//        IndividualWebapp ent = new IndividualWebapp();
//        ent.setName(author.collectiveName);
//        ent.setMoniker("unmatched article author");
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
//            //ent.setVClassId(PERSON_VCLASSID);
////         ent.setStatusId(VISIBLE_TO_CURATORS);
////         ent.setDescription("This entity should be visible only to curators. ");
//
//        // int newEntId = facade.insertNewEntity(ent);
////         doAuthorAssociation(author, pubEntId, newEntId);
//
//        //make dataprop from whole WosAuthorData struct
////         Ents2Data e2d = new Ents2Data();
////         e2d.setEntityId( newEntId );
////         e2d.setData( author.toJson() );
////         e2d.setDatapropId(WOS_AUTHOR_DATAPROPID);
////         facade.insertNewEnts2Data(e2d);
//
//        //add to in memory list of names
//    }
//
//    /**
//     * Take a author entity, move data to person entity,
//     */
////    private void assignAuthorEntityToPersonEntity(VitroFacade facade, int aurhtoEntId, int personEntId){
//        //copy ents2ents from author to person
//        //copy nameSynonym data prop to person
//        //update flags of article
//
//        //sunset author entity
//        //sunset all of authors ents2ents
////        or maybe delete?
//
//  //  }
//
//    private void doAuthorAssociation(WosAuthorData nameData, int pubEntId, int personId){
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
//
////             Entity author = facade.entityById(personId);
////         //add synonym dataProp? no need to, it is the name of the entity.
////         if( author.getName() != null && !author.getName().equals( nameData.collectiveName ) ){
////             Ents2Data e2d = new Ents2Data();
////             e2d.setEntityId(author.getId());
////             e2d.setData(nameData.collectiveName);
////             e2d.setDatapropId(3); // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< make this dataprop
////             facade.insertNewEnts2Data(e2d);
////         }
//
////         //make Ents2Ents pub -> author property id 105
////         Ents2Ents e2e = new Ents2Ents();
////         e2e.setPropertyId(PUB_TO_AUTHOR_PROP);
////         e2e.setDomainId(pubEntId);
////         e2e.setRangeId( author.getId() );
////         facade.insertNewEnts2Ents(e2e);
//    }
//
//
    public void endOfParsing() {
//        //these are only to collect stats
////        private List<Integer> matchedScores;
////        private List<Integer> closeCounts;
////        private List<Float> scoreToCloseRatio;
////        private int totalAuthorChecks = 0;
////        private int totalEmailMatches = 0;
//
//        log.info("total Author checks: " + this.totalAuthorChecks);
//        int sumMatched = 0;
//        for( int i : matchedScores){
//            if( i > 0 )
//                sumMatched = sumMatched + i;
//        }
//        log.info("average match score: " +  (float) sumMatched/matchedScores.size()  );
//
//        int sumCloseCounts=0;
//        for( int i : closeCounts ){
//            sumCloseCounts = sumCloseCounts + i;
//        }
//        log.info("average close counts: " + (float) sumCloseCounts/closeCounts.size()  );
//
//        Float sum = 0.0f;
//        for( Float r : scoreToCloseRatio){
//            sum = sum + r;
//        }
//        log.info("average score to close count ratio: " + (float) sum/scoreToCloseRatio.size()  );
//
//        log.info("total email matches: " + this.totalEmailMatches);
//        log.info("total matched: " + this.match);
//        log.info("total unmatched: " + this.missmatch);
//        log.info("ratio of match/missmatch: " + (float) this.match/this.missmatch );
//
//        DateTime d = new DateTime();
//        String fname = "WosAuthorMatch"+d.toDateTimeISO()+".txt";
//        try{
//            File f = new File(debugDir,fname);
//            f.createNewFile();
//            BufferedWriter w = new BufferedWriter(new FileWriter(f));
//            w.write("\\*********************** matched names: ***************************\\\n" );
//            w.write("[\"matched\": [\n");
//            for(WosAuthorData name : matchedNames){
//                w.write(name.collectiveName + "\t\t" + name.toJson() + "\n" );
//            }
//            w.write("]\n");
//            w.write("\\*********************** unmatched names: *************************\\\n" );
//            w.write("\"matched\": [\n");
//            for(WosAuthorData name : unmatchedNames){
//                w.write(name.collectiveName + "\t\t" + name.toJson()+"\n");
//            }
//            w.write("]]\n");
//            w.close();
//        }catch(Exception ex){
//            log.error("Error writing file " + debugDir + fname + " " + ex);
//        }
    }
//
//    private void makePrimaryAuthorDataProp(Document doc, Individual entity){
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
//        //get the primary author string first, this doesn't always exactly match
//        //any string found in the <fullauthorname> elements.
//        //ex primaryAuth = "Chow, CH"  fullauthorname = "Chow, Chang-Huan"
////         Collection <String>pauths = getStrings(doc,xpPrimaryAuth);
////         if( pauths != null && pauths.size() > 0 ){
////             for( String auth : pauths){
////                 Ents2Data e2d = new Ents2Data();
////                 e2d.setData(auth);
////                 e2d.setDatapropId(ThompsonWosIngest.WOS_PRIMARYAUTHOR_DATAPROPID);
////                 e2d.setEntityId(entity.getId());
////                 if( entity.getEnts2Data() == null )
////                     entity.setEnts2Data(new ArrayList<Ents2Data>());
////                 entity.getEnts2Data().add( e2d );
////             }
////         }
//    }
//
//    /* This method is public only for testing, do not use in other classes! (java is great) */
//    public List<WosAuthorData> getAuthors(Document doc ){
//        List<WosAuthorData> authors = null;
//        try {
//            int authorFormat = checkAuthorsFormat(doc);
//            switch ( authorFormat ){
//                case FULLAU_EXIST: authors = getFullAuElementAuths(doc); break;
//                case REPRINT_ONLY: authors = getReprintElementAuths(doc); break;
//                case AUTHOR_ELEMENTS_ONLY: authors = getAuthorElementAuths(doc); break;
//                case UNKNOWN_AUTHOR_FORMAT:
//                    authors = null;
//                    log.error("Unable to find authors in document");
//            }
//        } catch (Exception e) {
//            log.error("error while trying to get authors: " + e);
//            e.printStackTrace();
//        }
//        if( authors != null ){
//            addEmailsToAuthors(doc, authors);
//            addReprintAddressToAuthors(doc, authors);
//        }
//        return authors;
//    }
//
//    /**
//     * Use this when there are text values of author elements but no other
//     * useful author data.  This method will also add the name found in
//     * any primaryauthor element to the author list.
//     * @param doc
//     * @return
//     * @throws XPathExpressionException
//     */
//    private List<WosAuthorData> getAuthorElementAuths(Document doc) throws XPathExpressionException {
//        List nodes = (List)this.authorElementsOnly.evaluate(doc, XPathConstants.NODESET);
//        nodes.addAll( (List)this.primaryauthorElementsOnly.evaluate(doc, XPathConstants.NODESET) );
//
//        List<WosAuthorData> authors = new ArrayList<WosAuthorData>();
//        HashSet<String> names = new HashSet<String>();
//        for( Object obj : nodes ){
//            if( obj instanceof Node){
//                Node n = (Node)obj;
//                String name = n.getText();
//                if( !names.contains( name ) ){
//                    names.add(name);
//                    WosAuthorData ad = makeStructFromAuthorNode(n);
//                    if ( name.equals("primaryauthor"))
//                        ad.primary = true;
//                    authors.add(ad);
//                }
//            }
//        }
//        return authors;
//    }
//
//    private List<WosAuthorData> getReprintElementAuths(Document doc) throws XPathExpressionException {
//        List nodes = (List)this.reprintElementNodes.evaluate(doc, XPathConstants.NODESET);
//        List<WosAuthorData> authors = new ArrayList<WosAuthorData>();
//        for( Object obj : nodes ){
//            if( obj instanceof Node){
//                Node n = (Node)obj;
//                WosAuthorData ad = makeStructFromReprintNode(n);
//                authors.add(ad);
//            }
//        }
//        return authors;
//    }
//
//    private List<WosAuthorData> getFullAuElementAuths(Document doc) throws XPathExpressionException{
//            List values = (List)xpAllAuthors.evaluate(doc, XPathConstants.NODESET);
//            List<WosAuthorData> authors = new LinkedList<WosAuthorData>();
//            String nextAuthorsKey = null;
//            boolean nextAuthorPrimary = false;
//            for( Object obj : values ){
//                if( obj instanceof Node){
//                    Node n = (Node)obj;
//                    String name = n.getName();
//                    if ( name.equals("author")){
//                        nextAuthorPrimary = false;
//                        Node key = ((Element)n).selectSingleNode( KEY_XP );
//                        if(  key != null )
//                            nextAuthorsKey = key.getText();
//                        else
//                            nextAuthorsKey = null;
//                    } else if ( name.equals("primaryauthor")){
//                        nextAuthorPrimary = true;
//                        Node key = ((Element)n).selectSingleNode( KEY_XP );
//                        if(  key != null )
//                            nextAuthorsKey = key.getText();
//                        else
//                            nextAuthorsKey = null;
//                    } else if( name.equals("fullauthorname")){
//                        WosAuthorData ad = makeStructFromFullAuNode(n);
//                        ad.primary = nextAuthorPrimary;
//                        ad.authorKey = nextAuthorsKey;
//                        authors.add( ad );
//                        nextAuthorPrimary = false;
//                        nextAuthorsKey = null;
//                    }
//                }
//            }
//          return authors;
//    }
//
//
//
//    private int checkAuthorsFormat(Document doc) throws XPathExpressionException{
//
//        List nodes = (List)fullAuElementNodes.evaluate(doc, XPathConstants.NODESET);
//        int fullAuthorElementCount = (nodes==null?0:nodes.size());
//
//        if( fullAuthorElementCount > 0 ) //if there are fullauthor elements just use them
//            return FULLAU_EXIST;
//
//        nodes = (List)reprintElementNodes.evaluate(doc, XPathConstants.NODESET);
//        int reprintElementCount = (nodes==null?0:nodes.size());
//        nodes = (List)authorElementsOnly.evaluate(doc, XPathConstants.NODESET);
//        int authorElementCount = (nodes==null?0:nodes.size());
//
//        if( reprintElementCount > 0 && authorElementCount == 0 ) // only have a reprint author
//            return REPRINT_ONLY;
//
//        if( authorElementCount > 0 ) //we only have the text from author elements
//            return AUTHOR_ELEMENTS_ONLY;
//
//        return UNKNOWN_AUTHOR_FORMAT;
//    }
//
//    private WosAuthorData makeStructFromReprintNode(Node in){
//        if( in == null ) return null;
//        WosAuthorData ad = new WosAuthorData();
//        if( in.getNodeType() == Node.ELEMENT_NODE &&
//                ( ((Element)in).getName().equals("rp_author") )){
//                ad.collectiveName= in.getText();
//                ad.role="reprint author";
//        }else
//            log.error("reprint node had no author");
//        return ad;
//    }
//
//    private WosAuthorData makeStructFromAuthorNode(Node in){
//        if( in == null ) return null;
//        WosAuthorData ad = new WosAuthorData();
//        String nodeName = ((Element)in).getName();
//
//        if( in.getNodeType() == Node.ELEMENT_NODE &&
//            ( "author".equals( nodeName ) ||
//              "primaryauthor".equals( nodeName) )){
//                ad.collectiveName= in.getText();
//                ad.primary =  "primaryauthor".equals(nodeName);
//        }else
//            log.error("reprint node had no author");
//        return ad;
//    }
//
//    /** works on a <fullauthorname>  node */
//    private WosAuthorData makeStructFromFullAuNode(Node in){
//        if( in == null )
//            return null;
//        WosAuthorData ad = new WosAuthorData();
//        List nodes = ((Element)in).elements();
//
//        for(Object o : nodes){
//            Node n = (Node)o;
//
//            if( n.getNodeType() == Node.ELEMENT_NODE &&
//                    ((Element)n).getName().equals("AuRole")){
//                ad.role = n.getText();
//                continue;
//            }
//
//            if( n.getNodeType() == Node.ELEMENT_NODE &&
//                    ((Element)n).getName().equals("AuLastName")){
//                ad.lastName= n.getText();
//                continue;
//            }
//            if( n.getNodeType() == Node.ELEMENT_NODE &&
//                    ((Element)n).getName().equals("AuFirstName")){
//                ad.firstName= n.getText();
//                continue;
//            }
//            if( n.getNodeType() == Node.ELEMENT_NODE &&
//                ( ((Element)n).getName().equals("AuCollectiveName") )){
//                ad.collectiveName= n.getText();
//                continue;
//            }
//            if( n.getNodeType() == Node.ELEMENT_NODE &&
//                    ( ((Element)n).getName().equals("rp_author") )){
//                    ad.collectiveName= n.getText();
//                    continue;
//            }
//        }
//        return ad;
//    }
//
//
//
//    /**
//     * Try to find the email address for these authors
//     */
//    private List<WosAuthorData>addEmailsToAuthors(Document doc, List<WosAuthorData>authors){
//        List nodes = doc.selectNodes("//item/emails/email");
//
//        //get all emails
//        class Email{ String name; String email;  }
//        ArrayList <Email> emails = new ArrayList<Email>();
//        for( Object obj : nodes ){
//            Node n = (Node)obj;
//            if( n.getName().equals("email") ){
//                Node adr = n.selectSingleNode("//email_addr");
//                Node name = n.selectSingleNode("//name");
//                Email e = new Email();
//                e.name = name.getText();
//                e.email = adr.getText();
//                emails.add(e);
//            }
//        }
//        log.debug("found " + emails.size() + " email addresses" );
//
//        //match up with authors.
//        for( Email email : emails){
//            WosAuthorData maxMatchAuth = null;
//            int maxScore = 0;
//            for(WosAuthorData au : authors){
//                int score = NameMatch.scoreNameMatch(email.name, au.collectiveName);
//                if( score > maxScore){
//                    maxScore = score;
//                    maxMatchAuth = au;
//                }
//            }
//            if( maxScore >= (nameMatcher.ACCETABLE_NAME_MATCH - 10 ) )
//                maxMatchAuth.email = email.email;
//        }
//        return authors;
//    }
//
//    /**
//     * Sometimes the authors have addresses.  These are in the
//     * <reprint> element.  Check for a reprint element and attempt
//     * to match the address with a author element from the article.
//     */
//    private List<WosAuthorData>addReprintAddressToAuthors(Document doc,
//            List<WosAuthorData>authors){
//        Node authorNode = doc.selectSingleNode("//item/reprint/rp_author");
//        Node addressNode = doc.selectSingleNode("//item/reprint/rp_address");
//        if( authorNode == null || addressNode == null ) return authors;
//        String author = authorNode.getText();
//        String address = addressNode.getText();
//
//        if( address != null && author != null ){
//            int maxScore = 0;
//            WosAuthorData maxMatchAuth = null;
//            for(WosAuthorData au : authors){
//                int score = NameMatch.scoreNameMatch(author, au.collectiveName);
//                if( score > maxScore){
//                    maxScore = score;
//                    maxMatchAuth = au;
//                }
//            }
//            if( maxScore >= nameMatcher.ACCETABLE_NAME_MATCH )
//                maxMatchAuth.address = address;
//        }
//        return authors;
//    }
//
//    /**
//     * get strings given an xpath
//     * @param doc
//     * @param xp
//     * @return
//     */
//    protected Collection<String> getStrings(Document doc, javax.xml.xpath.XPathExpression xp){
//        ArrayList<String> strs = new ArrayList<String>();
//        if( xp == null ){
//            log.info("no xpath was set");
//            return strs;
//        }
//        //get the xpath nodes
//        List values = null;
//        try {
//            values = (List)xp.evaluate(doc, XPathConstants.NODESET);
//        } catch (XPathExpressionException e) {
//            log.error(e);
//            return strs;
//        }
//
//        Iterator it = values.iterator();
//        while(it.hasNext()){
//            Object obj = it.next();
//            if( obj instanceof Node)
//                strs.add( ((Node)obj).getText() );
//            if( obj instanceof String)
//                strs.add((String)obj);
//        }
//        return strs;
//    }
//
//
//    private int getBestExistingMatch( WosAuthorData data ){
//        this.totalAuthorChecks++;
//        int maxMatchScore = 0;
//        String maxMatchEntURI = null;
//
//        //check for email match first
//        if( data != null && data.email != null ){
//            Individual found =
//                facade.getIndividualByExternalId(EntityWebappDaoDb.CORNELL_NET_ID_TYPE,data.email);
//            if( found != null ){
//                log.debug("author match by netid "+ found.getName() +" to " +
//                        data.email );
//                maxMatchScore=200;
//                maxMatchEntURI = found.getURI();
//                this.totalEmailMatches++;
//            }else{
//                found = facade.getIndividualByExternalId(EntityWebappDaoDb.NON_CORNELL_EMAIL,data.email);
//                if( found != null ){
//                    log.debug("author match by non-cornell email"+ found.getName() +" to " +
//                            data.email );
//                    maxMatchScore=200;
//                    maxMatchEntURI = found.getURI();
//                    this.totalEmailMatches++;
//                }
//            }
//        }
//
//        //check reprint address
//        int addressModifier = 0;
//        boolean foundAddress = false;
//        if( data.address != null){
//            foundAddress = true;
//            String address = data.address.toLowerCase();
//            if( address.indexOf("cornell") >= 0 ||
//                address.indexOf("wiell") >= 0 )
//                addressModifier = 8;
//            else
//                addressModifier = -8;
//        }
//
//        //if no email match, compare with all names in list.
//        if( maxMatchEntURI == null){
//            //TODO: update for semweb-align
//            if(true) throw new Error("TODO: update for semweb-align");
//            //maxMatchEntURI = nameMatcher.getBestExistingMatch( data.collectiveName );
//            //maxMatchScore = nameMatcher.maxMatchScore;
//        }
//
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
////         if( maxMatchEntURI != -1 ){
////             int vclassId = getVclass(maxMatchEntURI);
////             if( vclassId != -1 ){
////                 if( foundAddress ){
////                     if( isCornellFaculty(vclassId) )      {
////                         maxMatchScore = (maxMatchScore + addressModifier);
////                         log.debug("cornel faculty address modifier: " + addressModifier
////                                 + " for " + data.address);
////                     }else{
////                         maxMatchScore = maxMatchScore - addressModifier;
////                         log.debug("non-cornell faculty address modifier: " + addressModifier
////                                 + " for " + data.address);
////                     }
////                 }
////                 if( !isAcademic( vclassId ) )  //if it's not a vclass that usually authors, lower score
////                     maxMatchScore = maxMatchScore - 4;
////             }
////         }
//
////         data.nearMatch = this.nameMatcher.closeMatches.size();
////         data.matchScore = maxMatchScore;
//
////         if( maxMatchScore  < nameMatcher.ACCETABLE_NAME_MATCH  ){
////             this.missmatch++;
////             if( log.isDebugEnabled())
////                 log.debug("unable to match " + data.collectiveName + " closest: " + data.matchName
////                         + " close:" + data.nearMatch + " score: " + data.matchScore);
////             return -1;
////         }else{
////             this.match++;
////             data.entityId   = maxMatchEntId;
//
////             if( data.nearMatch == 0) data.nearMatch = 1; //a match should be a near match,but isNonCornellFaculty and isAcademic can bump scores.
////             this.scoreToCloseRatio.add( new Float(data.matchScore/data.nearMatch) );
////             this.matchedScores.add( data.matchScore );
////             this.closeCounts.add( data.nearMatch );
//
////             if(log.isDebugEnabled()){
////                 log.debug("matched " + data.collectiveName + " with " +
////                         data.entityId + " close: " + data.nearMatch
////                         +" score: " + data.matchScore );
////             }
////             return maxMatchEntId;
////         }
//        return -1;
//    }
//
//    private int getVclass( int entId){
////TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
////         if( entId < 0 )
////             return -1;
////         Entity ent = facade.entityById(entId);
////         if( ent.getVClass() != null )
////             return ent.getVClass().getId();
////         else
////             return -1;
//        return -112233;
//    }
//
//    private boolean isCornellFaculty(int vclassid) {
//        return vclassid != 327;
//    }
//
//    /** Checks if vclass of entity is something that would write an article */
//    /* academic emp, cornell academic staff, cornell faculty, cornell librarian, non-cornell faculty*/
//    private int[] academicVClassIds = {232, 331, 259, 296, 295, 327};
//    private boolean isAcademic(int vclassId) {
//        for(int i=0; i< academicVClassIds.length; i++){
//            if( academicVClassIds[i] == vclassId )
//                return true;
//        }
//        return false;
//    }
//
//    /* ****************************** WosAuthorData ******************************** */
//
//    /** Intended to hold info from Wos XML for an author */
//    public static class WosAuthorData {
//        public String role; //auRole
//        public String lastName; //auLastName
//        public String firstName; //auFirstName
//        public String collectiveName;  //auColletiveName
//        public String authorKey; // <author key="x">
//        public String authorText; // <author>x</author>
//        public String email; // <email_addr>x</email_addr>
//        public String address; // <address>x</address> or <rp_address>x</rp_address>
//        boolean primary;
//
//        public String matchName ="";
//        public int matchScore=-1;
//        public int entityId=-1;
//        public int nearMatch = 0;
//
//        public WosAuthorData(){}
//
//        public WosAuthorData(String in){
//            this();
//            try {
//                JSONObject obj;
//                obj = new JSONObject(in);
//                this.role = obj.getString("role");
//                this.lastName= obj.getString("lastName");
//                this.role = obj.getString("fisrtName");
//                this.collectiveName= obj.getString("collectiveName");
//                this.authorText= obj.getString("authorText");
//                this.authorKey= obj.getString("authorKey");
//                this.email= obj.getString("email");
//                this.primary =obj.getBoolean("primary");
//                this.matchScore =obj.getInt("matchScore");
//                this.entityId =obj.getInt("entityId");
//                this.nearMatch=obj.getInt("nearMatch");
//                this.address = obj.getString("address");
//                this.matchName = obj.getString("matchName");
//            } catch (JSONException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//        public String toJson(){
//            String[] names = {"role","lastName","fisrtName","collectiveName",
//                    "authorKey","authorText","email","primary","entityId",
//                    "matchScore","nearMatch","address","matchName"};
//            JSONObject json = new JSONObject();
//            Class c = this.getClass();
//            for (int i = 0; i < names.length; i += 1) {
//                try {
//                    String name = names[i];
//                    Field field = c.getField(name);
//                    Object value = field.get(this);
//                    json.put(name, value);
//                } catch (Exception e) {/* forget about it */ }
//            }
//            return json.toString();
//        }
//    }
}