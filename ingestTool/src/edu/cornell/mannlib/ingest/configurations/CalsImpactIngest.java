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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.ingest.actions.OutputXmlToFile;
import edu.cornell.mannlib.ingest.actions.ParseToEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.BasicEntityHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.DataPropertyHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.EntityResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.GeoRelationResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.RecordSkipper;
import edu.cornell.mannlib.ingest.fieldHandlers.RelationResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.SaveEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.TabRelationMaker;
import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.ingest.interfaces.IngestParser;
import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.ingest.parser.IngestSaxParser;
import edu.cornell.mannlib.ingest.processors.CalsFakeHtml;
import edu.cornell.mannlib.ingest.processors.CalsNYCountyMapper;
import edu.cornell.mannlib.ingest.processors.CleanInput;
import edu.cornell.mannlib.ingest.processors.CornellEmailSuffixer;
import edu.cornell.mannlib.ingest.processors.Country2Region;
import edu.cornell.mannlib.ingest.processors.GeoNameClean;
import edu.cornell.mannlib.ingest.processors.LeadingOrdinalNumberRemover;
import edu.cornell.mannlib.ingest.processors.NewYorkCountyMapper;
import edu.cornell.mannlib.ingest.processors.Rejector;
import edu.cornell.mannlib.ingest.processors.State2Region;
import edu.cornell.mannlib.ingest.processors.USStateMapper;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.CumulativeDeltaModeler;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

/* 2008-08-04 BJL23 updated to be semweb-align compatible */

/**
 * Each impact statement submitted by the faculty will be made into a
 * vivo entity.
 *

 ********************************************************************************
 Notes about importing the CALS faculty impact from March-2007, bdc34

 The xml format has been modified so this code has changed.  If you want
 the 2005 import, you will need to get an older version of this from subverison.

Problems with XML format:
    * Starts with three odd bytes, in hex these are "ef bb bf"
      removed by hand with emacs.
    * There are two closing </Impact_2006> elements, also removed
      with emacs.

Improvements to XML:
    * We could really use some line breaks.
      formated with xml_pp, a perl command line pretty printer
    * Root element could be more stable:
      <CALS_Impacts year="2006" revision="1">...
    * netIds have spaces following them some times.

Changes that need to be addressed by this ingest:
    * root element is now <Impact_2006>
    * NetIds have extra white space. Are they getting trimmed by the code already?
    * Collaborative_Research elements are gone?
    * tokenize countries by comma.
    * don't need to preprocess with xsl with the new <Person> local names.

===== process description =====
 sunset all existing impact statements.
 run the xml through xsl/impactNetId2person.xsl
 For all <report> elements {
    skip record if marked_for_delete or or on_hold or duplicate
    convert the xml to an Entity object, see makeImpactXml2Entity()
    see if entity exists already, based on response_id and add one if it doesn't
    add dataProperties: abstract, issue, response, impact, fundingSources, topic,
        collaborators, personel.  see makeDataPropertyHandlers()
    add relations: submittedBy, department, usStates, nyCounties, countries
        see: makeRelationHandlers();
    associate with tabs, see makeTabRelationsHandlers()
    save entity object.
 }

=== problems with responses in 2006 ===
Affected_Countries is frequently not a list of countries eg:
 "Although my research is performed at Cornell-Ithaca, it does not
  involve research on a particular location (state or nation). Also, it
  does have worldwide impact."
Anything not a country will get thrown out.


 *
 * Some of these values will need to be cleaned up:
 *
 * Some of these values will need to be used to match the impact
 * statement with a tab (use external ids):
 *
 * relations we will need to make:
 * submitted by
 * reporting organization
 * affiliated with
 * area of impact (topic)
 * affected areas (geographic)
 *
 * @author bdc34
 *
 */
public class CalsImpactIngest {
	
	private static String CURRENT_YEAR = "2007"; // too lazy to put this in the properties at the moment
	
    /* NOTES:
    Here's an xpath that extracts all nodes of depth 2 that have
    a non-empty child node <NetId>
    //child::node()/child::node()[not ( empty (./NetId))]
    good for:
    <Impact_20XX>
        <gsk22>
           <NetId>gsk22</NetId>

    */

    /* this is how to setup a spring context from a xml file */
    //          ApplicationContext ctx = new FileSystemXmlApplicationContext(filename);
    //          Ingester ingester = (Ingester)ctx.getBean("ingester");
    /* for now we hard code it for dev */

    /*
     * in xpath 2.0
     *   distinct-values( for $x in //Collaborative_Research return tokenize($x,",") )
     * will get all Collaborative_Research elements and tokenize their text by commas.
     */

	//SOME URIs
	private final static String CALS_IMPACT_RESPONSE_ID_TYPE = "http://vivo.library.cornell.edu/ns/0.1#CALSImpactstatementresponseId";
	

    WebappDaoFactory wadf;
    IngestParser parser;

    List <StringProcessor>standardStringProcessors;
    List <StringProcessor>blockStringProcessors;
    
    CumulativeDeltaModeler tracker = null;

    private static final Log log = LogFactory.getLog(CalsImpactIngest.class.getName());

    /**
     * Set up everything we need.
     */
    public CalsImpactIngest(InputStream input, String connectionPropPath) throws Exception{
        if( input == null )
            throw new Error(" CalsImpactIngest needs an InputStream");

        //setup db connection
        OntModel ontModel = new JenaDataSourceSetupBase().makeDBModelFromPropertiesFile(connectionPropPath);
        OntModel prophylactic = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // we don't want to change the real database yet
        prophylactic.addSubModel(ontModel);
        tracker = new CumulativeDeltaModeler(prophylactic);
        wadf = new WebappDaoFactoryJena(prophylactic);
        //TODO: update this for semweb-align
        //facade = new VitroFacade(vc);

        //setup citation string, only used when an impact report lacks a date
        DateTime dateT = new DateTime();
        DEFAULT_CITATION = CALS_BASE_CITATION+" "+dateT.monthOfYear().getAsText()+" "+
            dateT.getDayOfMonth()+ ", " + dateT.getYear();

        standardStringProcessors = new ArrayList<StringProcessor>(1);
        standardStringProcessors.add(new CleanInput());
        standardStringProcessors.add( new CalsFakeHtml());

        blockStringProcessors = new ArrayList<StringProcessor>(1);
        blockStringProcessors.add(new CleanInput());
        blockStringProcessors.add( new CalsFakeHtml() );
        blockStringProcessors.add( new pipesToBreaks() );

        /* SUNSET old impacts, right now */
        //TODO: update this for semweb-align
        //facade.sunsetEntities(IMPACT_VCLASS_ID, null, null, new Date() );

        // each of these actions will be performed on each record from the xml
        ArrayList <IngestAction> actions = new ArrayList<IngestAction>();
        actions.add( makeEntityParseAction( wadf ) );
        actions.add( new OutputXmlToFile("output.xml") );

        // we need a IngestSaxParser to break the xml into chunks.
        // this IngestSaxParser will call each object in actions on each resulting record.
        this.parser = new IngestSaxParser(actions,"Person",null, input );
    }

    public CumulativeDeltaModeler getCumulativeDeltaModeler() {
    	return tracker;
    }
    
    public static void main(String argv[]){
        if( argv == null || argv.length != 2){
            System.out.println("Usage: CalsImpactIngester connection.properties " +
                               "filetoingest.xml");
            return;
        }
        if( false == checkFiles(argv[0], argv[1]) ) return;
        String dbConnectionConfig = argv[0], inputFile = argv[1];
        try {
            //get the data from a local file
            FileInputStream fis  = null;
            try{
                File infile = new File( inputFile );
                fis = new FileInputStream( infile );
            }catch (java.io.FileNotFoundException ex){
                log.fatal("could not load file" , ex);
            }
            CalsImpactIngest cii = new CalsImpactIngest(fis, dbConnectionConfig);
            cii.parser.parse();
            System.out.println("Added "+cii.getCumulativeDeltaModeler().getAdditions().size()+" statements");
            System.out.println("Retracted "+cii.getCumulativeDeltaModeler().getRetractions().size()+" statements");
            File additionsFile = new File("/Users/bjl23/ingestData/additions.rdf");
            FileOutputStream additionsOutputStream = new FileOutputStream(additionsFile);
            File retractionsFile = new File("/Users/bjl23/ingestData/retractions.rdf");
            FileOutputStream retractionsOutputStream = new FileOutputStream(retractionsFile);
            cii.getCumulativeDeltaModeler().getAdditions().write(additionsOutputStream);
            cii.getCumulativeDeltaModeler().getRetractions().write(retractionsOutputStream);
        } catch (Exception e) { log.error(e); }
    }

    private static boolean checkFiles(String in, String connProps){
        File f = new File(in);
        if(! f.exists() ){
            System.out.println("The file " + in + " does not exist.");
            return false;
        }
        if( !f.canRead() ){
            System.out.println("The file " + in + " is not readable.");
            return false;
        }
        f = new File(connProps);
        if(! f.exists() ){
            System.out.println("The file " + connProps + " does not exist.");
            return false;
        }
        if( !f.canRead() ){
            System.out.println("The file " + connProps + " is not readable.");
            return false;
        }
        return true;
    }

    private  IngestAction makeEntityParseAction(WebappDaoFactory webappDaoFactory) throws Exception{
        //We need a list of fieldHandlers, each will be run onec on the xml tree
        //so we could have one that does the basic fields like name, etc.
        //and one that does external ids, and one for ents2ents, one for images etc.
        ArrayList <FieldHandler> hands = new ArrayList <FieldHandler>();

        //skip records that suck
        hands.addAll( makeRecordFilters() );

        //first we take the xml record and make a vivo impact statement entity
        hands.add( makeImpactXml2Entity() );

        //see if impact statement is in system already, make a new one if not.
        hands.add(new EntityResolver(this.CALS_IMPACT_RESPONSE_ID_TYPE,"//Response_Id", webappDaoFactory.getIndividualDao()));

        //then we add the objects to extract the data properties from the xml
        hands.addAll(makeDataPropertyHandlers());

        //then add the ents2ents relations
        hands.addAll(makeRelationHandlers(wadf));

        //then add entity 2 tab relations
        hands.addAll( makeTabRelationsHandlers(wadf) );

        //then save to the db, saves entity, ents2ents, ents2data, externalids
        hands.add( new SaveEntity(wadf) );

        //ParseToEntity will execute each FieldHandler on each record in the xml tree
        return new ParseToEntity( hands , webappDaoFactory);
    }

    /** Makes handlers that reject records.  */
    private List<FieldHandler> makeRecordFilters() throws Exception {
        List<FieldHandler>fhlist = new ArrayList<FieldHandler>();
        fhlist.add(new RecordSkipper("//Editor_Comments [ text() eq 'Can be Deleted'] ",
                "Comments of CALS editor for this record indicate it is marked for deletion."));
        fhlist.add(new RecordSkipper("//Edited_Status [ normalize-space(text()) eq 'On Hold' ] ",
            "Comments of CALS editor for this record indicate it is marked as on hold."));
        //uncomment this line if you want to reject second impacts by same netId.
        //Arron G. indicated that we should load all impacts.
        //fhlist.add(new RejectSecondImpactStmt("this arg is worthless") );
        return fhlist;
    }

    /**
     * Makes the objects that create ents2ents relations for the entity.
     *
     * @param facade
     * @return
     */
    private List<FieldHandler> makeRelationHandlers(WebappDaoFactory wadf){
        List<FieldHandler> hands = new ArrayList<FieldHandler>();
        try {
            RelationResolver rr = null;
            Rejector rejects = new Rejector(null);
            rejects.addReject("All U.S. states");
            rejects.addReject("All NY counties");

            // submitted by
            rr = new RelationResolver("//NetId", NETID,
                    SUBMITTED_BY_PROPID, domainside, noQual, wadf);
            rr.addStrProcessor(new CleanInput());
            rr.addStrProcessor(new CornellEmailSuffixer());
            hands.add( rr );

            // in department
            rr = new RelationResolver("//Dept", CALS_IMPACT_DEPT_EXTID,
                    REPORTING_ORG_PROPID, domainside, noQual, wadf);
            hands.add( rr );

            // affiliated with academic priority area
            String xp = "distinct-values(for $x in //Academic_Priorities return tokenize($x,','))";
            rr = new RelationResolver(xp, ENT_NAME,
                    AFFILIATED_WITH_PROPID, domainside, noQual, wadf, PRIORITY_AREA_VCLASSID);
            hands.add( rr );

            // area of impact - topic
            xp = "distinct-values(for $x in //Project_Types return tokenize(lower-case($x),','))";
            rr = new RelationResolver(xp, ENT_NAME, AREA_OF_IMPACT_PROPID,
                    domainside, noQual, wadf, PROJECT_TYPE_VCLASSID);
            hands.add( rr );

            // affected areas - states
            xp = "distinct-values(for $x in //Affected_US_States return tokenize($x,','))";
            rr = new RelationResolver(xp, ENT_NAME, AFFECTED_AREAS_PROPID,
                    domainside, noQual, wadf, GEO_ENT_VCLASSID);
            rr.addStrProcessor( rejects );
            rr.addStrProcessor(new USStateMapper());

            hands.add( rr );

            // affected areas - ny counties
            xp = "distinct-values(for $x in //Affected_NY_Counties return tokenize($x,','))";
            rr = new RelationResolver(xp, ENT_NAME, AFFECTED_AREAS_PROPID,
                    domainside, noQual, wadf,COUNTIES_VCLASSID);
            rr.addStrProcessor( rejects );
            rr.addStrProcessor(new NewYorkCountyMapper());
            hands.add( rr );

            // affected areas - countries
            xp = "distinct-values(for $p in (for $x in //Affected_Countries return tokenize($x,'\\|')) return tokenize($p,','))";
            rr = new GeoRelationResolver(xp, ENT_NAME, AFFECTED_AREAS_PROPID,
                    domainside, noQual, wadf);
            hands.add( rr );

            //reporting organization?

        } catch (XPathException e) {
            log.error("Could not make relation handlers " + e);
        }
        return hands;
    }

    /**
     * Makes the tabs2ents relations.  These get immediately inserted into the db
     */
    private List<FieldHandler> makeTabRelationsHandlers(WebappDaoFactory wadf)throws Exception{
        List<FieldHandler> hands = new ArrayList<FieldHandler>();
        Map<String,Integer> label2tabidMap = new HashMap<String,Integer>();

        //College Objectives
        label2tabidMap.put("Land-Grant Mission",425);
        label2tabidMap.put("Applied Social Sciences",423);
        label2tabidMap.put("Environmental Sciences",424);
        label2tabidMap.put("New Life Sciences",422);
        String xp = "distinct-values(for $x in //Academic_Priorities return tokenize($x,',') )";
        hands.add( new TabRelationMaker(xp,label2tabidMap,null,wadf) );

        //Project types
        label2tabidMap = new HashMap<String,Integer>();
        label2tabidMap.put("Teaching",408);
        label2tabidMap.put("Research",406);
        label2tabidMap.put("Extension/Outreach",407);
        xp = "distinct-values(for $x in //Project_Types return tokenize($x,',') )";
        hands.add( new TabRelationMaker(xp,label2tabidMap,null,wadf) );

        //Topic area tab
        label2tabidMap = new HashMap<String,Integer>();
        label2tabidMap.put("Enhance Economic Opportunities for Agricultural Producers",321);
        label2tabidMap.put("Enhance Protection and Safety of Agriculture and Food Supply",322);
        label2tabidMap.put("Protect and Enhance Natural Resource Base and Environment",325);
        label2tabidMap.put("Society Ready Graduates",323);
        label2tabidMap.put("Improve Nutrition and Health",325);
        label2tabidMap.put("Support Increased Economic Opportunities and Improved Quality of Life in Rural America",382);
        xp = "distinct-values(for $x in //USDA_Topic_Areas return tokenize($x,',') )";
        hands.add( new TabRelationMaker(xp,label2tabidMap,null,wadf));

        //funding tabs
        //first we need to map those crazy funding names
//        RegexMapper regexes = new RegexMapper();
//        regexes.addRegex("Private.*","Private");
//        regexes.addRegex("Other USDA.*","Other USDA");
//        regexes.addRegex("State or Municipal.*","State");
//        regexes.addRegex("Other Federal non-USDA.*","Other non-USDA");
//        regexes.addRegex("Academic Programs Instructional Support.*","Academic");
//        regexes.addRegex("Federal Formula Funds - Extension.*","FFF extension");
//        regexes.addRegex("Federal Formula Funds - Research.*","FFF research");
//        regexes.addRegex(".*",""); //ignore anything else
//
//        //then we need to map the normalized names to tabids
//        label2tabidMap = new HashMap<String,Integer>();
//        label2tabidMap.put("Private",383);
//        label2tabidMap.put("FFF extension",369);
//        label2tabidMap.put("FFF research",370);
//        label2tabidMap.put("Other non-USDA",374);
//        label2tabidMap.put("Other USDA",375);
//        label2tabidMap.put("State",404);
//        label2tabidMap.put("Academic",368);
//
//
//        xp = "//Funding_Sources";
//        TabRelationMaker fh = new TabRelationMaker(xp,label2tabidMap,null,facade){
//            public Collection<String> getStrings(Document doc){
//                Collection<String> inStr = super.getStrings(doc);//get the normal strings
//                List<String> outStr = new ArrayList<String>();
//                for( String st : inStr ) //break up the strings in a special way
//                    outStr.addAll( CalsImpactIngest.splitFundSrc(st));
//                return outStr;
//            }
//        };
//        fh.addStrProcessor(regexes);
//        hands.add( fh );

        //this is to deal with the new funding elements in 2006 impacts
        //hands.add( new SimpleTabRelationMaker("//Funding_Research",370,null,wadf) );
        //hands.add( new SimpleTabRelationMaker("//Funding_Extension",369,null,wadf) );
        //hands.add( new SimpleTabRelationMaker("//Funding_Instructional",368,null,wadf) );
        //hands.add( new SimpleTabRelationMaker("//Funding_Other_USDA",375,null,wadf) );
        //hands.add( new SimpleTabRelationMaker("//Funding_Federal_non-USDA",374,null,wadf) );
        //hands.add( new SimpleTabRelationMaker("//Funding_State_Municipal",404,null,wadf) );
        //hands.add( new SimpleTabRelationMaker("//Funding_Private",383,null,wadf) );

        //do the geo tabs
        hands.addAll( makeGeoTabRelator(wadf));
        return hands;
    }

    /**
     * Makes the objects that will pull values out of the xml and make
     * data properties in the Entity.
     *
     */
    private List<FieldHandler> makeDataPropertyHandlers( ){
       List<FieldHandler> hands = new ArrayList<FieldHandler>();
       List<StringProcessor> strProc = standardStringProcessors;
       List<StringProcessor> blockStrProc = blockStringProcessors;

       //abstract
       hands.add( new DataPropertyHandler("//Abstract[1]",
                                           null,
                                           CALS_ABSTRACT_DATAPROPID,
                                           blockStrProc));
       //issue
       hands.add( new DataPropertyHandler("//Issue[1]",
                                          "\\\\n",
                                          CALS_ISSUE_DATAPROPID,
                                          blockStrProc ));
       //response
       hands.add( new DataPropertyHandler("//Response[1]",
                                           null,
                                           CALS_RESPONSE_DATAPROPID,
                                           blockStrProc ));
       //impact
       hands.add( new DataPropertyHandler("//Impact[1]",
                                          "\\\\n",
                                          CALS_IMPACT_DATAPROPID,
                                          blockStrProc ));

       //in 2006 the structure of the xml for funding was changed
//       //funding sources, we need to break up the fields in a special way.
//       //should we break the getting of strings out to a different object that
//       //can be attached to a handler?
//       hands.add( new DataPropertyHandler("//Funding_Sources[1]",
//                                           null,
//                                           CALS_FUNDING_SRC_DATAPROPID,
//                                           strProc ) {
//           public Collection<String> getStrings(Document doc){
//               Collection<String> inStr = super.getStrings(doc);//get the normal strings
//               List<String> outStr = new ArrayList<String>();
//               for( String st : inStr ) //break up the strings in a special way
//                   outStr.addAll( CalsImpactIngest.splitFundSrc(st));
//               return outStr;
//           }
//       });

       String funds= "//Funding_Research |//Funding_Extension |" +
       "//Funding_Instructional |//Funding_Other_USDA |" +
       "//Funding_Federal_non-USDA |//Funding_State_Municipal |//Funding_Private";
       hands.add( new DataPropertyHandler(funds,",",CALS_FUNDING_SRC_DATAPROPID,strProc) );

       //other funding sources
       hands.add( new DataPropertyHandler("//Other_Funding_Sources",
                                          "\\|",
                                          CALS_FUNDING_SRC_DATAPROPID,
                                          strProc ));
       //topic description
       hands.add( new DataPropertyHandler("//Topic_Description",
                                          null,
                                          CALS_TOPIC_DESC_DATAPROPID,
                                          strProc ));
       //collaborators
       hands.add( new DataPropertyHandler(collaboratorXp,
                                          null,
                                          CALS_TOPIC_COLLAB_DATAPROPID,
                                          strProc ));

       //key personnel
       hands.add( new DataPropertyHandler(personnellXp,
                                          null,
                                          CALS_PERSONNEL_DATAPROPID,
                                          strProc ));
       return hands;
    }

    private List<FieldHandler> makeGeoTabRelator(WebappDaoFactory webappDaoFactory) throws Exception{
        List<FieldHandler> hands = new ArrayList<FieldHandler>();

        HashMap<String,Integer>label2tabidMap = new HashMap<String,Integer>();
        label2tabidMap.put("New York State",389);
        label2tabidMap.put("Other U.S.",390);
        label2tabidMap.put("International",391);
        label2tabidMap.put("Tompkins County",392);
        label2tabidMap.put("Metropolitan New York City",393);
        label2tabidMap.put("Other Upstate Counties",394);
        label2tabidMap.put("Northeastern U.S.",395);
        label2tabidMap.put("Midwestern U.S.",396);
        label2tabidMap.put("Southern U.S.",397);
        label2tabidMap.put("Western U.S.",398);
        label2tabidMap.put("Europe",399);
        label2tabidMap.put("Canada",400);
        label2tabidMap.put("Mexico, Central & South America",401);
        label2tabidMap.put("Africa",402);
        label2tabidMap.put("Asia",403);
        label2tabidMap.put("U.S. (general)",428);
        label2tabidMap.put("New York State (general)",429);
        label2tabidMap.put("International (general)",430);

        //let's do states first
        String xp = "distinct-values(for $x in //Affected_US_States return tokenize($x,',') )";
        TabRelationMaker trm = new TabRelationMaker(xp,label2tabidMap,null,webappDaoFactory);
        trm.addStrProcessor( new LeadingOrdinalNumberRemover() );
        trm.addStrProcessor( new USStateMapper() );
        trm.addStrProcessor( new State2Region() );
        hands.add( trm );

        //counties
        xp = "distinct-values(for $x in //Affected_NY_Counties return tokenize($x,',') )";
        trm = new TabRelationMaker(xp,label2tabidMap,null,webappDaoFactory);
        trm.addStrProcessor( new NewYorkCountyMapper() );
        trm.addStrProcessor( new CalsNYCountyMapper() );
        hands.add( trm );

        //countries
        xp = "distinct-values(for $x in //Affected_Countries return tokenize($x,'\\\\n') )";
        trm = new TabRelationMaker(xp, label2tabidMap,null,webappDaoFactory);
        trm.addStrProcessor(new GeoNameClean());
        trm.addStrProcessor(new Country2Region());
        hands.add( trm );

        return hands;
    }

    private BasicEntityHandler makeImpactXml2Entity(){
        //for the BasicEntityHandler we need a map of
        //field names to xpath queries to pull values out of the xml
        HashMap<String,String> field2xpath = new HashMap<String,String>();
        field2xpath.put("name","//Title[1]" );
        field2xpath.put("anchor","//Name[1]" );

        HashMap<String,String> defaultEntityValues = new HashMap<String,String>();
        defaultEntityValues.put("citation",DEFAULT_CITATION); //some impacts are missing a date so we need to put something
        defaultEntityValues.put("vClassId", String.valueOf(IMPACT_VCLASS_ID) );
        defaultEntityValues.put("moniker",CURRENT_YEAR+" Impact statement");
        defaultEntityValues.put("flag1Set", "6");
        defaultEntityValues.put("flag2Set", "CALS");

        BasicEntityHandler beh = new BasicEntityHandler( field2xpath, defaultEntityValues);
        beh.addPerPropertyStringProcessors("name", standardStringProcessors);
        beh.addPerPropertyStringProcessors("anchor", standardStringProcessors);

        return beh;
    }

    /**
     * We need to split the finding source by commas but there are ones
     * in parens that we need to ignore.  How would we do this with a regex?
     */
    public static List<String> splitFundSrc(String in){
        List<String> outStrs = new ArrayList<String>();
        if(in == null ) return outStrs;

        int lastEnd = 0; //keep track of back of last substring
        boolean deadtocommas = false;
        for(int i=0; i<in.length(); i++){
            char c = in.charAt(i);
            if( c == '('){
                deadtocommas = true;
                continue;
            }
            if( c == ')'){
                deadtocommas = false;
                continue;
            }
            if( deadtocommas )
                continue;
            if( c == ','){
                outStrs.add(in.substring(lastEnd, i));
                lastEnd = i + 1;
            }
        }
        return outStrs;
    }

    /**
     * Deal with the || that are faking breaks.
     */
    private class pipesToBreaks implements StringProcessor{
        public String process(String in) {
            in = in.replaceAll("\\|\\|","<br/>");
            in = in.replaceAll("\\|","<br/>");
          if( in.endsWith("|"))
              in= in.substring(0,in.length()-1);
            return in;
        }
    }

    /* **************** static and other values ************************/
    
    private static String VIVO = "http://vivo.library.cornell.edu/ns/0.1#";
    private static String MANN = "http://vivo.cornell.edu/ns/mannadditions/0.1#";
    
    private static String ENT_NAME = RDFS.label.getURI(),
    NETID    = VIVO+"CornellemailnetId",
    CALS_IMPACT_DEPT_EXTID = VIVO+"CALSImpactDeptName";

    private static boolean domainside = true, rangeside = false, noQual= false;

    private static String CALS_BASE_CITATION =
        "From CALS annual faculty reporting. Imported on ";
    String DEFAULT_CITATION="";

    //BAD HARDCODED JUNK:
    private static String 	AFFECTED_AREAS_PROPID    = VIVO + "hasGeographicFocus",
                       		AFFILIATED_WITH_PROPID   = MANN + "ImpactStatementRelatesToAcademicPriorityArea", // CHECK THIS
                       		AREA_OF_IMPACT_PROPID    = VIVO + "ImpactStatementAddressesGenericAreaOfInterest",
                       		SUBMITTED_BY_PROPID      = MANN + "ImpactStatementHasAcademicEmployeeSubmitter",
                       		REPORTING_ORG_PROPID     = VIVO + "ImpactStatementHasReportingOrganizedEndeavor";

    private static String CALS_ABSTRACT_DATAPROPID     = "http://vivo.mannlib.cornell.edu/ns/ThomsonWOS/0.1#abstract",
                       CALS_ISSUE_DATAPROPID        = MANN + "issue",
                       CALS_RESPONSE_DATAPROPID     = MANN + "response",
                       CALS_IMPACT_DATAPROPID       = MANN + "impact",
                       CALS_FUNDING_SRC_DATAPROPID  = VIVO + "fundingSourceDescription",
                       CALS_TOPIC_DESC_DATAPROPID   = VIVO + "topicDescription",
                       CALS_PERSONNEL_DATAPROPID    = MANN + "keyPersonnel",
                       CALS_TOPIC_COLLAB_DATAPROPID = VIVO + "collaborator";

    private static String IMPACT_VCLASS_ID = VIVO + "CALSImpactStatement",
        PRIORITY_AREA_VCLASSID = VIVO + "AcademicPriorityArea",
        PROJECT_TYPE_VCLASSID = "http://www.aktors.org/ontology/portal#Generic-Area-Of-Interest",
        COUNTIES_VCLASSID = VIVO + "County",
        GEO_ENT_VCLASSID = "http://www.aktors.org/ontology/portal#Geographical-Region";

    /** xpath to get all the personnell */
    private String personnellXp=
        "//Personnel_1 | "+
        "//Personnel_2 | "+
        "//Personnel_3 | "+
        "//Personnel_4 | "+
        "//Personnel_5 | "+
        "//Personnel_6 | "+
        "//Personnel_7 | "+
        "//Personnel_8 | "+
        "//Personnel_9 | "+
        "//Personnel_10";

    private String collaboratorXp=
        "//Collaborator_1 | " +
        "//Collaborator_2 | " +
        "//Collaborator_3 | " +
        "//Collaborator_4 | " +
        "//Collaborator_5 | " +
        "//Collaborator_6 | " +
        "//Collaborator_7 | " +
        "//Collaborator_8 | " +
        "//Collaborator_9 | " +
        "//Collaborator_10";
}