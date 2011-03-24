package edu.cornell.mannlib.ingest.configurations;

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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.ingest.actions.OutputXmlToFile;
import edu.cornell.mannlib.ingest.actions.ParseToEntity;
import edu.cornell.mannlib.ingest.actions.WosPublisherMaker;
import edu.cornell.mannlib.ingest.fieldHandlers.ArticleResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.BasicEntityHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.DataPropertyHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.ExternalIdHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.FlagHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.KeywordHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.RelationResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.SaveEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.WosAuthorMatch;
import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.ingest.interfaces.IngestParser;
import edu.cornell.mannlib.ingest.interfaces.RecordSource;
import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.ingest.parser.IngestSaxParser;
import edu.cornell.mannlib.ingest.parser.PullParser;
import edu.cornell.mannlib.ingest.processors.CleanInput;
import edu.cornell.mannlib.ingest.record.WOSRecordSource;
//import edu.cornell.mannlib.vitro.dao.db.EntityDaoDb;
//import edu.cornell.mannlib.vitro.dao.db.VitroConnection;
//import edu.cornell.mannlib.vitro.webapp.dao.VitroFacade;
//import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
//import edu.cornell.mannlib.vitro.webapp.dao.db.WebappDaoFactoryDb;
import edu.cornell.mannlib.vitro.webapp.utils.TitleCase;

/**
 *
 * @author bdc34
 *
 */
public class ThompsonWosIngest {
	
// TODO : update for semweb-align
	
//    RecordSource wosRecords;
//    IngestParser parser;
//    List<IngestAction> actions;
//    private VitroFacade facade;
//
//    List <StringProcessor>standardStringProcessors;
//
//    private static final Log log = LogFactory.getLog(ThompsonWosIngest.class.getName());
//
//    public ThompsonWosIngest(String connectionProp) throws Exception{
//        //setup db connection
//        VitroConnection vc = new VitroConnection(connectionProp);
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
//        //facade = new VitroFacade(vc);
//
//        wosRecords = new WOSRecordSource(WOSRecordSource.default_query,"2006-2006");
//        actions = makeActions();
//        parser = new PullParser(actions, wosRecords);
//
//        standardStringProcessors = new ArrayList<StringProcessor>(1);
//        standardStringProcessors.add(new CleanInput());
//    }
//
//    public ThompsonWosIngest(String connectionProp, File xmlIn) throws Exception{
//        //      setup db connection
//        VitroConnection vc = new VitroConnection(connectionProp);
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
//            //facade = new VitroFacade(vc);
//        actions = makeActions();
//
//        InputStream inputStream = new FileInputStream(xmlIn);
//        parser = new IngestSaxParser(actions, "REC",null, inputStream);
//        standardStringProcessors = new ArrayList<StringProcessor>(1);
//        standardStringProcessors.add(new CleanInput());
//    }
//
//    public List<IngestAction> makeActions() throws Exception{
//        ArrayList<IngestAction> acts = new ArrayList<IngestAction>();
//        ArrayList<FieldHandler> hands = new ArrayList<FieldHandler>();
//
//        acts.add(new OutputXmlToFile( "/home/bdc34/workspace/Vitro2/ingestTool/wosIngestOut.txt"));
//
//        //make publisher if not found in vivo
//        acts.add(new WosPublisherMaker(facade, PUBLISHER_VCLASSID, PUBLISHER_XPATH));
//
//        //the UT element seems to be a Thompson key of some sort.
//        hands.add(new ExternalIdHandler(WOS_UT_XPATH,null,wosUt_externalIdType));
//
//        //get title, vclass, moniker, citation
//        hands.add( makeArticle2Entity() );
//
//        //get the data properties
//        hands.addAll( makeDataPropertyHandlers () );
//
//        //check if article exists
//        //hands.add(new EntityResolver(doi_externalIdType,DOI_XPATH,facade));
//        hands.add(new ArticleResolver(facade));
//
//        //find authors, make ents2ents, sets entity statusId if no authors are found.
//        hands.add( new WosAuthorMatch(new VitroConnection(), facade ));
//
//        //associate article with keyword Entities
//        hands.add(new KeywordHandler(KEYWORD_PLUS_XPATH,facade,VCLASSID_KEYWORDS_PLUS,null,KEYWORD_PROPID));
//        hands.add(new KeywordHandler(KEYWORD_XPATH,facade,VCLASSID_KEYWORDS_AUTH,null,KEYWORD_PROPID));
//
//        //article -> publisher e2e
//        hands.add( new RelationResolver(PUBLISHER_XPATH,
//                EntityDaoDb.ENTITY_NAME,ARTICLE_HAS_PUBLISHER_PROPID,true,false,facade,
//                PUBLISHER_VCLASSID, new TitleCaseProcessor()));
//        //set flags for publishers? na, we don't need more stuff checked into portals.
//
//        hands.add( new SaveEntity(facade));
//
//        //set flags from authors
//        int propIds[] = {105};
//
//        hands.add( new FlagHandler(propIds, true, facade.getWebappDaoFactory().getEntityWebappDao() , false));
//
//        acts.add( new ParseToEntity(hands,facade));
//        return acts;
//    }
//
//    /**
//     * Makes the objects that will pull values out of the xml and make
//     * data properties in the Entity.
//     *
//     */
//    private List<FieldHandler> makeDataPropertyHandlers( ){
//       List<FieldHandler> hands = new ArrayList<FieldHandler>();
//       List<StringProcessor> strProc = standardStringProcessors;
//
//       hands.add(new DataPropertyHandler("//item/ut[1]",
//               null,
//               WOS_UT_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler(DOI_XPATH,
//               null,
//               DOI_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/i_ckey[1]",
//               null,
//               WOS_I_CKEY_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/i_cid[1]",
//               null,
//               WOS_I_CID_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/source_title[1]",
//               null,
//               WOS_SOURCE_TITLE_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/source_abbrev[1]",
//               null,
//               WOS_SOURCE_ABBREV_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/item_title[1]",
//               null,
//               WOS_ITEM_TITLE_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/bib_id[1]",
//               null,
//               WOS_BIB_ID_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/article_nos[1]",
//               null,
//               WOS_ARTICLE_NOS_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/bib_pages[1]",
//               null,
//               WOS_BIB_PAGES_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/bib_issue[1]/@year",
//               null,
//               WOS_BIB_ISSUE_YEAR_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/bib_issue[1]/@vol",
//               null,
//               WOS_BIB_ISSUE_VOL_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/doctype[1]",
//               null,
//               WOS_DOCTYPE_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/editions[1]",
//               null,
//               WOS_EDITIONS_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/languages[1]",
//               null,
//               WOS_LANGUAGES_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/primaryauthor[1]",
//               null,
//               WOS_PRIMARYAUTHOR_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/authors",
//               null,
//               WOS_AUTHOR_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/emails",
//               null,
//               WOS_EMAIL_DATAPROPID));
//       hands.add(new DataPropertyHandler(KEYWORD_XPATH,
//               null,
//               WOS_KEYWORD_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler(KEYWORD_PLUS_XPATH,
//               null,
//               WOS_KEYWORD_PLUS_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/research_addrs/research/rs_address",
//               null,
//               WOS_RESEARCH_ADDRS_DATAPROPID,
//               strProc));
//       hands.add(new DataPropertyHandler("//item/abstract/*",
//               null,
//               WOS_ABSTRACT_DATAPROPID,
//               strProc));
//       return hands;
//    }
//
//    public void parse(){ parser.parse(); }
//
//    public static void main(String argv[]) throws Exception{
//        ThompsonWosIngest twi = null;
//        if( argv.length == 1 )
//            twi = new ThompsonWosIngest( argv[0] );
//        else if( argv.length == 2 )
//            twi = new ThompsonWosIngest( argv[0], new File(argv[1]));
//        else {
//            System.out.println("Error, need at least on parameter\n" +
//                    "ThompsonWosIngest connection.properties [xmlfiletoIngest]");
//            return;
//        }
//        twi.parse();
//    }
//
//    private BasicEntityHandler makeArticle2Entity(){
//        //for the BasicEntityHandler we need a map of
//        //field names to xpath queries to pull values out of the xml
//        HashMap<String,String> field2xpath = new HashMap<String,String>();
//        field2xpath.put("name","//item/item_title[1]" );
//        field2xpath.put("moniker","//item/doctype[1]" );
//
//        HashMap<String,String> defaultEntityValues = new HashMap<String,String>();
//        defaultEntityValues.put("citation",DEFAULT_CITATION); //some impacts are missing a date so we need to put something
//        defaultEntityValues.put("vClassId", String.valueOf(ARTICLE_VCLASS_ID) );
//        defaultEntityValues.put("moniker","publication");
//
//        //If you can't find your article entities in vivo it's probably because of the entity's flags or status
//        defaultEntityValues.put("flag1Set", "");
//        defaultEntityValues.put("flag2Set", "");
//
//        HashMap<String,List<StringProcessor>> processors = new HashMap<String,List<StringProcessor>>();
//        processors.put("name",standardStringProcessors);
//        processors.put("anchor",standardStringProcessors);
//
//        BasicEntityHandler beh = new BasicEntityHandler( field2xpath, defaultEntityValues);
//        beh.addPerPropertyStringProcessors("name", standardStringProcessors);
//        beh.addPerPropertyStringProcessors("anchor", standardStringProcessors);
//        //beh.addPerPropertyStringProcessor("citation", new Prepender(THOMP));
//
//        return beh;
//    }
//
//    /**************** inner classes ******************** */
//    private class TitleCaseProcessor implements StringProcessor{
//        public String process(String in) {return TitleCase.toTitleCase(in);}
//    }
//
//    /* ************** constants ********************** */
//    static int doi_externalIdType = 12;
//    static int wosUt_externalIdType = 13;
//
//    private static String DOI_XPATH = "//item/article_nos/article_no[starts-with(text(),'DOI ')]";
//    private static String WOS_UT_XPATH = "//item/ut";
//    private static String PUBLISHER_XPATH = "//item/source_title";
//
//    //private static int DOI_EXTERNALID_ID = 12;
//
//    private static int ARTICLE_VCLASS_ID = 318;
//    private static int PUBLISHER_VCLASSID = 433;
//
//    private static int ARTICLE_HAS_PUBLISHER_PROPID = 198;// AbstractCommunicationHasAgentAsPublisher
//
//    private static String DEFAULT_CITATION  = "This citation has been made " +
//            "available courtesy of Thomson WOS, 3501 Market Street, Philadelphia, " +
//            "PA 19104.";
//
//    public static int WOS_UT_DATAPROPID = 31;
//    public static int WOS_I_CKEY_DATAPROPID = 32;
//    public static int WOS_I_CID_DATAPROPID = 33;
//    public static int WOS_SOURCE_TITLE_DATAPROPID = 34;
//    public static int WOS_SOURCE_ABBREV_DATAPROPID = 35;
//    public static int WOS_ITEM_TITLE_DATAPROPID = 36;
//    public static int WOS_BIB_ID_DATAPROPID = 37;
//    public static int WOS_ARTICLE_NOS_DATAPROPID = 38;
//    public static int WOS_BIB_PAGES_DATAPROPID = 39;
//    public static int WOS_BIB_ISSUE_DATAPROPID = 40; //might not be in use any more.
//    public static int WOS_DOCTYPE_DATAPROPID = 41;
//    public static int WOS_EDITIONS_DATAPROPID = 42;
//    public static int WOS_LANGUAGES_DATAPROPID = 43;
//    public static int WOS_PRIMARYAUTHOR_DATAPROPID = 44;
//    public static int WOS_AUTHOR_DATAPROPID = 45;
//    public static int WOS_EMAIL_DATAPROPID = 46;
//    public static int WOS_KEYWORD_DATAPROPID = 47;
//    public static int WOS_KEYWORD_PLUS_DATAPROPID = 48;
//    public static int WOS_RESEARCH_ADDRS_DATAPROPID =49 ;
//    public static int WOS_ABSTRACT_DATAPROPID =50 ;
//    public static int WOS_BIB_ISSUE_YEAR_DATAPROPID = 53;
//    public static int WOS_BIB_ISSUE_VOL_DATAPROPID = 54;
//
//    public static String KEYWORD_PLUS_XPATH = "//item/keywords/keyword";
//    public static String KEYWORD_XPATH = "//item/keywords_plus/keyword";
//
//    public static int VCLASSID_KEYWORDS_PLUS =436;
//    public static int VCLASSID_KEYWORDS_AUTH =435;
//    public static int KEYWORD_PROPID = 609;
//
//    public static int DOI_DATAPROPID = 55;

}
