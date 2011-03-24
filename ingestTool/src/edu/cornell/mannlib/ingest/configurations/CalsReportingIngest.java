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

/* TODO: Update for semweb-align */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.ingest.actions.ParseToEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.CreateAndSaveEntityHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.EntityResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.Ents2EntsWriter;
import edu.cornell.mannlib.ingest.fieldHandlers.FlagHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.RelationResolver;
//import edu.cornell.mannlib.ingest.interfaces.Download;
import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.ingest.interfaces.IngestParser;
import edu.cornell.mannlib.ingest.parser.IngestSaxParser;
//import edu.cornell.mannlib.vitro.dao.db.EntityDaoDb;
//import edu.cornell.mannlib.vitro.dao.db.VitroConnection;
//import edu.cornell.mannlib.vitro.webapp.beans.IndividualWebapp;
//import edu.cornell.mannlib.vitro.webapp.dao.VitroFacade;
//import edu.cornell.mannlib.vitro.webapp.dao.db.EntityWebappDaoDb;

/**
 This is a ingest that will process the Cals Faculty Reporting
 xml format.

 Notice that the reporting data from 2006 gets  processed in 2007,
 2005 get processed in 2006.  They collect the data over the last months
 of a year and then we get it at the beginning of the next year.

********************************************************************************
 * Notes about importing the Cals Faculty Reporting(CFR) from Nov-2006, bdc34 *

 === What will be extracted from the CFR? ===
 Collaborative_Research - There are 112 distinct values.  They look good and
     may have come from a pick-list.
 Area_Concentration - 770 values which seem to be free text.
 Keywords - 2537 values which are free text. These have to be tokeized
     by pipe and then by comma and then by semicolon.
     try:
     distinct-values
     (for $i in
       (for $o in
       (for $p in
       (for $n in //Keywords
        return tokenize( $n , '\|'))
        return tokenize($p, ','))
        return tokenize($o, ';'))
     return normalize-space(lower-case($i)) )

 === What will not be extracted: ===
 Graduate-Field-List - only one value.
 Focus - one value and it includes Lorem Ipsum
 Honors-Awards - No values
 Publications - Only one value, it's for W.Fry.
 Activities - Only one value, W.Fry has "...Technical feasibility of anti-crop
   agro-terrorism. Seminar on Agro-Terrorism, Peace Studies Program,
   Cornell University, November 2000..."

=== Process description, 2005 ==
for each <Person> element
    get <NetId> and resolve as a externalId to a vivo entity Id (do not make a new entity if none found)

    get <Collaborative_Research>, tokenize by |
        For each Collaborative_Research token
            resolve as extenralid on name, if none, make a new one
            check for duplicate, current ents2ents
            if none
                make ents2ents for netid.entityId to collaborative_research.entitId

    get Area_Concentration
        check for duplicate, current ents2data for Area_Concentration
        if none
            Make ents2data for Area_Concentration

    get Keywords, tokeize by | , and ;
        For each token
           Check for duplicate, current ents2data
           if none
               Make ents2data for netid.entityId to token

Unresloved questions:
Should we sunset keywords?
Should we sunset existing ents2data rows for Area_Concentration that are not in the report?
Should we sunset existing ents2ents rows for Collaborative_research that are not in the report?

=== Current status of this Ingest
what is implemented?
  - creates entities for all CALS Collaborative Research Areas (CCRA)
  - associated faculty with CCRAs
what is not implemented?
  - add dataproperty to faculty entities for keywords.

*/
public class CalsReportingIngest {

// TODO: update for semweb-align
	
//    IngestParser CcraParser; //Cals Collaborative Research Area Parser
//    private static VitroFacade facade;
//
//    private static final Log log = LogFactory.getLog(CalsReportingIngest.class.getName());
//    private static final String calsCitation ="Gathered from CALS Faculty Reporting.";
//
//    /**
//     * Set up everything we need.
//     * @throws Exception
//     *
//     */
//    public CalsReportingIngest(InputStream input, String connectionProp) throws Exception{
//        if( input == null || connectionProp == null ){
//            if( input == null ) log.error("no input file");
//            if( connectionProp == null ) log.error("no connection properties file set" );
//            throw new Error(" CalsImpactIngest need both Download and a connection properties file.");
//        }
//
//        //setup db connection
//        facade = getDbFacade(connectionProp);
//
//        //parser that will make sure that all of the CCRAs have entities.
//        ArrayList <IngestAction> actions = new ArrayList<IngestAction>();
//        actions.add( makeCcraParseAction() );
//        this.CcraParser=
//            new IngestSaxParser(actions, "Person", null, input);
//    }
//
//    public void ingest(){ CcraParser.parse();   }
//
//    /** makes a parser that will create an entity for any CCRA that is not in the system */
//    private static  IngestAction makeCcraParseAction() throws IOException, Exception{
//        ArrayList <FieldHandler> hands = new ArrayList <FieldHandler>();
//        hands.add( new EntityResolver(EntityWebappDaoDb.CORNELL_NET_ID_TYPE,"//NetId",getDbFacade(null),false) );
//
//        //these three handlers expose a pattern: we want to evaluate an xpath and run multiple actions on the result
//        hands.add( getCCRAMaker() );
//        hands.add( getCCRAResolver() );
//        hands.add( new Ents2EntsWriter(getDbFacade(null),PERSON_IN_AREA_PROP_ID,true,false));
//
//        //now we have a person entity and we want to push their flags to the related CCRA
//        int[] propIds = { PERSON_IN_AREA_PROP_ID };
//        hands.add( new FlagHandler(propIds, FlagHandler.DOMAIN,
//                facade.getWebappDaoFactory().getEntityWebappDao(), FlagHandler.PUSH)); //set the flags of the CCRAs
//
//        return new ParseToEntity(hands ,getDbFacade(null) );
//    }
//
//    private final static String CCRA_XPATH2 =
//        "distinct-values " +
//        "(for $i in " +
//        "(for $n in //Collaborative_Research " +
//        "return tokenize( $n , ',')) " +
//        "return lower-case(normalize-space($i)) )";
//
//
//    /** makes a handler that will create an entity for any CCRA that is not in the system */
//    private static FieldHandler getCCRAMaker() throws XPathFactoryConfigurationException, XPathExpressionException {
//        IndividualWebapp templateEnt = new IndividualWebapp();
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
//        //templateEnt.setVClassId( RESEARCH_AREA_VCLASSID );
//        templateEnt.setMoniker( CCRA_MONIKER );
//        //templateEnt.setFlag1Set("2,3,4,5");
//        templateEnt.setCitation(calsCitation);
//        return new CreateAndSaveEntityHandler(CCRA_XPATH2,facade,templateEnt);
//    }
//
//    /** makes ents2ents for faculty to CCRA */
//    private static FieldHandler getCCRAResolver() throws XPathException {
//        boolean domainSide = true;  //person=domain area=range
//        boolean noQualifier = false;
//
//        RelationResolver aar =
//            new RelationResolver(CCRA_XPATH2,
//                                 EntityDaoDb.ENTITY_NAME,
//                                 PERSON_IN_AREA_PROP_ID,
//                                 domainSide,
//                                 noQualifier,
//                                 getDbFacade(null),
//                                 RESEARCH_AREA_VCLASSID);
//        return aar;
//    }
//
//    private static VitroFacade getDbFacade(String filename){
//        if( facade == null ){
//            //setup db connection
//            VitroConnection vc = new VitroConnection(filename);
//            //TODO: update for semweb-align
//            if(true) throw new Error("TODO: update for semweb-align");
//            //facade = new VitroFacade(vc);
//        }
//        return facade;
//    }
//
////    private static FieldHandler makeDataPropHandler(){
////        XPath xpath = DocumentHelper.createXPath("//Keywords");
////        int TEST_DP_ID = 1;
////        return new DataPropertyHandler(xpath,"|",TEST_DP_ID);
////    }
//
//    /**
//     * makes a FieldHandler that will pull keywords out of the xml and
//     * add them to the entity.
//     * @return
//     */
//    //private static FieldHandler makeKeywordHandler(){
//        /* notice that we have to escape the pipe char for xpath */
////        XPath xpath = DocumentHelper.createXPath("//Keywords[1]");
////        return new MultiNodeHandler(xpath) {
////            @Override
////            public void handleNode(Node node, Document doc, Object targetEnt) {
////                /* append node text to entity's keywords list */
////                if( targetEnt instanceof Entity){
////                    Entity ent = (Entity)targetEnt;
////                    List <String> keywords = ent.getKeywords();
////                    if( keywords == null )
////                        ent.setKeywords(keywords = new ArrayList<String>());
////                    String txt = node.getText();
////                    if( txt != null && txt.length() > 0 )
////                        keywords.add( txt );
////                }
////            }
////        };
////        return null;
////    }
//
//    private static boolean checkFiles(String in, String connProps){
//        File f = new File(in);
//        if(! f.exists() ){
//            System.out.println("The file " + in + " does not exist.");
//            return false;
//        }
//        if( !f.canRead() ){
//            System.out.println("The file " + in + " is not readable.");
//            return false;
//        }
//        f = new File(connProps);
//        if(! f.exists() ){
//            System.out.println("The file " + connProps + " does not exist.");
//            return false;
//        }
//        if( !f.canRead() ){
//            System.out.println("The file " + connProps + " is not readable.");
//            return false;
//        }
//        return true;
//    }
//
//    public static void main(String argv[]){
//        if( argv == null || argv.length != 2){
//            System.out.println("Usage: CalsImpactIngester dbconfig.properties filetoingest.xml ");
//            return;
//        }
//        if( false == checkFiles(argv[0], argv[1]) )
//            return;
//
//        String dbConnectionConfig = argv[0];
//        String inputFile          = argv[1];
//        try {
//            FileInputStream fis  = null;
//            try{
//                File infile = new File( inputFile );
//                fis = new FileInputStream( infile );
//            }catch (java.io.FileNotFoundException ex){
//                log.fatal("Could not load file",ex);
//            }
//
//            CalsReportingIngest cri = new CalsReportingIngest(fis, dbConnectionConfig);
//            cri.ingest();
//        } catch (Exception e) {
//            log.error(e);
//            e.printStackTrace();
//            return;
//        }
//    }
//
//    private static final int PERSON_IN_AREA_PROP_ID = 459;
//    private static final int RESEARCH_AREA_VCLASSID = 174;
//    private static final String CCRA_MONIKER = "CALS Collaborative Research Area";
//

}
