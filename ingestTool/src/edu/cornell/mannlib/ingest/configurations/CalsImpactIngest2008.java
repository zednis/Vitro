    package edu.cornell.mannlib.ingest.configurations;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.ingest.actions.OutputXmlToFile;
import edu.cornell.mannlib.ingest.actions.ParseToEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.BaseXPath2Handler;
import edu.cornell.mannlib.ingest.fieldHandlers.BasicEntityHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.DataPropertyHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.EntityResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.GeoRelationResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.MultiNodeHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.RelationResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.RelationResolverWithUri;
import edu.cornell.mannlib.ingest.fieldHandlers.SaveEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.SkipIfElementEmpty;
import edu.cornell.mannlib.ingest.fieldHandlers.SubElementForField;
import edu.cornell.mannlib.ingest.fieldHandlers.TabRelationMaker;
import edu.cornell.mannlib.ingest.fieldHandlers.Xpath2DataPropertyHandler;
import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.ingest.interfaces.IngestParser;
import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.ingest.parser.XPathIteratorParser;
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
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.CumulativeDeltaModeler;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import java.util.Collections;
import javax.xml.xpath.XPathFactory;

/**
 * Class with main to perform ingest of 2009 CALS impact statements that
 * were extracted from Activity Insight.
 *
 * The general form of the xml is:
 * <Report>
 *   <Survey @username="someNetId">
 *     <IMPACT_STATEMENT>
 *       (useful data in here)
 *
 * Some of the Survey elements have more than one IMPACT_STATEMENT elements.
 * As of 2009-06-10 the plan is to just import all of the IMPACT_STATEMENT
 * objects, associate them with people and decide on what to do about the
 * duplicates later.
 */

/*
 * TODO: 
 * Decisions to discus with Jon:
   
   I didn't find a public/private flag.
   
   Not associating departments with Impact Statements.
   
   A lot of impact statements have "All U.S. states" as involved state, what should we do with that?
   Right now we are doing nothing.
   
   Need to make a class for USDA_AREA_VCLASS_ID.
   
   Right now this looks for NetIDs in IMPACT_STATEMENT_INVEST/DEP but does
   nothing with IMPACT_STATEMENT_INVEST that do not have NetIDs.  What
   should we do with them?
   
   Not associating Impact Statements with any tabs.
 */
public class CalsImpactIngest2008 {
    private final WebappDaoFactory wadf;
    private IngestParser parser;
    private CumulativeDeltaModeler tracker = null;
    private String defaultCitation="";

    public CalsImpactIngest2008(String filename, String connectionPropPath) throws Exception{
        if( filename == null )
            throw new Error(" CalsImpactIngest2009 needs an filename to process");

        //setup DB connection
        Model model = new JenaDataSourceSetupBase().makeDBModelFromPropertiesFile(connectionPropPath);
        OntModel prophylactic = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // we don't want to change the real database yet
        prophylactic.addSubModel(model);
        tracker = new CumulativeDeltaModeler(prophylactic);
        wadf = new WebappDaoFactoryJena(prophylactic);

        //make sure that we have a impact statement reply id property
        DataProperty reply = new DataProperty();
        reply.setURI(REPORTING_IMPACT_ID);
        reply.setPublicName("activity insight reporting id");
        reply.setExternalId(true);
        wadf.getDataPropertyDao().insertDataProperty(reply);
        
        //make sure that we have the impact statement user id property
        DataProperty userid = new DataProperty();
        userid.setURI(REPORTING_USER_ID);
        userid.setPublicName("activity insight user id");
        userid.setExternalId(true);
        wadf.getDataPropertyDao().insertDataProperty(userid);
        
        //setup citation string, only used when an impact report lacks a date
        DateTime dateT = new DateTime();
        defaultCitation = CALS_BASE_CITATION+" "+dateT.monthOfYear().getAsText()+" "+
            dateT.getDayOfMonth()+ ", " + dateT.getYear();

        // each of these actions will be performed on select elements from the XML
        ArrayList <IngestAction> actions = new ArrayList<IngestAction>();
        actions.add( new OutputXmlToFile("output.xml"));
        actions.add( makeEntityParseAction( wadf ) );        

        /* This XSLT will be executed before the ingest.  It adds the NetID from
         * the attributes of the Survey element to the attributes of the 
         * IMPACT_STATEMENT elements.  */
        String xsltPreprocess = "ingestTool/xsl/addNetIdToImpacts.xsl";
        
        // We need a XPathIteratorParser to break the XML into elements that 
        // need to be processed.  It will call each object in actions on 
        // each resulting element.          
        this.parser = new XPathIteratorParser(actions,"//IMPACT_STATEMENT",
                filename, xsltPreprocess);
    }

    /**
     * The Survey/@username and Survey/@userId need to be processed to associate
     * Activity Insight userIds with individuals in the VIVO model.
     *
     * Process the XML to careat @userId external ids in the delta modeler.
     */
    private void doFirstPass(String filename) throws Exception{
        final WebappDaoFactory innerWdf = this.wadf;
        final CumulativeDeltaModeler fff = this.getCumulativeDeltaModeler();
        ArrayList <FieldHandler> hands = new ArrayList <FieldHandler>();
        hands.add( new FieldHandler() {
            public void setFields(Element doc, Object targetEnt) {
                String userId = doc.valueOf("@userId");
                String username = doc.valueOf("@username");
                if( userId == null || userId.length() == 0){
                    log.debug("Survey lacks @userId");
                    return;
                }
                if( username == null || username.length() == 0){
                    log.debug("Survey lacks @username");
                    return;
                }

                if( ! username.endsWith("@cornell.edu"))
                    username = username + "@cornell.edu";
                List<Individual> people = innerWdf.getIndividualDao()
                        .getIndividualsByDataProperty(VIVO_NET_ID_URI, username);

                if( people == null || people.size() == 0){
                    log.debug("Could not find person with netid of " + username);
                    return;
                }
                if( people.size() > 1 )
                    log.debug("found multiple people with netid of " + username + " using first on list.");
                
                DataPropertyStatement stmt = new DataPropertyStatementImpl();
                stmt.setIndividualURI(people.get(0).getURI());
                stmt.setDatapropURI(REPORTING_USER_ID);
                stmt.setData(userId);                
                innerWdf.getDataPropertyStatementDao().insertNewDataPropertyStatement(stmt);
                log.debug("Attempted to add " + userId + " as user id for " + username);
            }
            public void endOfParsing() {/* Do nothing special at end. */ }
        });

        List<IngestAction> ias=(Collections.singletonList(((IngestAction)new ParseToEntity(hands,wadf))));
        IngestParser firstPassParser =
                new XPathIteratorParser(ias,"//Survey", filename, null);
        firstPassParser.parse();
    } 

    private  IngestAction makeEntityParseAction(WebappDaoFactory webappDaoFactory) throws Exception{
        //We need a list of fieldHandlers, each will be run once on the XML tree
        //so we could have one that does the basic fields like name, etc.
        //and one that does external IDs, and one for ents2ents, one for images etc.
        ArrayList <FieldHandler> hands = new ArrayList <FieldHandler>();

        //skip anything with an empty title since it is just test data
        hands.add( new SkipIfElementEmpty("TITLE", "no TITLE element found"));
        
        //first we take the XML record and make a VIVO impact statement entity
        hands.add( makeImpactXml2Entity() );

        //see if impact statement is in system already, make a new one if not.
        hands.add(new EntityResolver(REPORTING_IMPACT_ID,"@id", webappDaoFactory.getIndividualDao()));

        //then we add the objects to extract the data properties from the XML
        hands.addAll(makeDataPropertyHandlers());

        //handler to make new USDA impact areas, relation gets built later.
        hands.addAll( makeUsdaAreas( wadf ));
        
        //then add the ents2ents relations
        hands.addAll(makeRelationHandlers(wadf));

        //then add entity 2 tab relations
        hands.addAll( makeTabRelationsHandlers(wadf) );

        //then save to the DB, saves entity, ents2ents, ents2data, externalids
        hands.add( new SaveEntity(wadf) );

        //ParseToEntity will execute each FieldHandler on each record in the XML tree
        return new ParseToEntity( hands , webappDaoFactory);
    }

    private List<FieldHandler> makeTabRelationsHandlers(WebappDaoFactory wadf)throws Exception{
        List<FieldHandler> hands = new ArrayList<FieldHandler>();
        Map<String,Integer> label2tabidMap = new HashMap<String,Integer>();

        //College Objectives
        label2tabidMap.put("Land Grant Mission",425);
        label2tabidMap.put("Applied Social Sciences",423);
        label2tabidMap.put("Environmental Sciences",424);
        label2tabidMap.put("New Life Sciences",422);
        String xp = "PRIORITY_AREA";
        hands.add( new TabRelationMaker(xp,label2tabidMap,null,wadf) );

        //Project types
        label2tabidMap = new HashMap<String,Integer>();
        label2tabidMap.put("Teaching",408);
        label2tabidMap.put("Research",406);
        label2tabidMap.put("Extension/Outreach",407);
        xp = "CONAREA";
        hands.add( new TabRelationMaker(xp,label2tabidMap,null,wadf) );

        //Topic area tab
        label2tabidMap = new HashMap<String,Integer>();
        label2tabidMap.put("Enhance Economic Opportunities for Agricultural Producers",321);
        label2tabidMap.put("Enhance Protection and Safety of Agriculture and Food Supply",322);
        label2tabidMap.put("Improve Nutrition and Health",325); 
        //other
        label2tabidMap.put("Protect and Enhance Natural Resource Base and Environment",325);
        label2tabidMap.put("Society Ready Graduates",323);        
        label2tabidMap.put("Support Increased Economic Opportunities and Improved Quality of Life in Rural America",382);         
        xp = "USDA_AREA";
        hands.add( new TabRelationMaker(xp,label2tabidMap,null,wadf));
        
        hands.addAll( makeGeoTabRelator(wadf));
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
        String xp = "INVOLVED_STATE";
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
        field2xpath.put("name","TITLE" );
        field2xpath.put("anchor","Name[1]" );

        HashMap<String,String> defaultEntityValues = new HashMap<String,String>();
        defaultEntityValues.put("citation",defaultCitation); //some impacts are missing a date so we need to put something
        defaultEntityValues.put("vClassURI", String.valueOf(IMPACT_VCLASS_ID) );
        defaultEntityValues.put("moniker",CURRENT_YEAR+" Impact statement");
        defaultEntityValues.put("flag1Set", "6");
        defaultEntityValues.put("flag2Set", "CALS");

        BasicEntityHandler beh = new BasicEntityHandler( field2xpath, defaultEntityValues);
        return beh;
    }

    private List<FieldHandler> makeDataPropertyHandlers() throws XPathExpressionException, XPathFactoryConfigurationException{
        List<FieldHandler> fh = new ArrayList<FieldHandler>();
        
        List<StringProcessor> stringCleanups = new ArrayList<StringProcessor>(1);
        stringCleanups.add(new StringProcessor(){
            public String process(String in) {
                if( in != null )
                    return in.replaceAll("\\\\n", "; ");
                else
                    return in;
            } 
        });
        
        fh.add( new DataPropertyHandler("@username", REPORTING_NS +"netid"));       
        //response id
        fh.add( new DataPropertyHandler("@id", REPORTING_IMPACT_ID));
        //impact IMPACT
        fh.add( new DataPropertyHandler("IMPACT", null, CALS_IMPACT_DATAPROPID, stringCleanups));
        //impact issue ISSUE
        fh.add( new DataPropertyHandler("ISSUE", null, CALS_ISSUE_DATAPROPID, stringCleanups));
        //impact response RESPONSE
        fh.add( new DataPropertyHandler("RESPONSE", null, CALS_RESPONSE_DATAPROPID, stringCleanups));
        //impact summary SUMMARY
        fh.add( new DataPropertyHandler("SUMMARY", null, CALS_ABSTRACT_DATAPROPID, stringCleanups));
        //Federal research funding other DP
        fh.add( new DataPropertyHandler("FUNDING_FEDRCHOTHER", null, CALS_FUNDING_SRC_DATAPROPID, stringCleanups));
        //private funding other DP
        fh.add( new DataPropertyHandler("FUNDING_PRIVATEOTHER", null, CALS_FUNDING_SRC_DATAPROPID, stringCleanups));
        //funding type FUNDTYPE skip? DP

        //investigators lacking Activity Insight FACULTY_NAME element
        String INVEST_lacking_FACULTY_NAME =
                "//IMPACT_STATEMENT_INVEST[ string-length( string( FACULTY_NAME )) = 0 ]/";
        String nonemptyDepTest = "string-length(string( DEP )) gt 0";        
        String nameAndDep = "concat( LNAME , ', ', FNAME, ' (' , DEP ,')' )";
        String name       = "concat( LNAME , ', ', FNAME )";

        String investXP =
                INVEST_lacking_FACULTY_NAME +
                "( if( " + nonemptyDepTest + ")" +
                " then " + nameAndDep +
                " else " + name + ")";
        fh.add( new Xpath2DataPropertyHandler( investXP, INVEST_COLLAB_PROP_URI ));

        //start date time
        fh.add( new DataPropertyHandler("START_START", REPORTING_NS+"startDateTime"));
        //start day
        fh.add( new DataPropertyHandler("DTD_START", REPORTING_NS+"dtD_Start"));
        //start month
        fh.add( new DataPropertyHandler("DTM_START", REPORTING_NS+"dtM_Start"));
        //start year
        fh.add( new DataPropertyHandler("DTY_START", REPORTING_NS+"dtY_Start"));
        //end datetime
        fh.add( new DataPropertyHandler("END_START", REPORTING_NS+"endDateTime"));
        //end day
        fh.add( new DataPropertyHandler("DTD_END", REPORTING_NS+"dtD_End"));
        //end month
        fh.add( new DataPropertyHandler("DTM_END", REPORTING_NS+"dtM_End"));
        //end year
        fh.add( new DataPropertyHandler("DTY_END", REPORTING_NS+"dtY_End"));
        return fh;
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
            /*  contribution area conarea OP */
            
   /*
    * USDA area OP
    * USDA funding FUNDING_USDA OP
    * USDA area other OP or DP
     * Academic funding FUNCING_ACAD OP
    * Academic funding ohter FUNDING_ACADOTHER OP
    * Priority area other PRIORITY_AREAOTHER OP or DP
    *
    * Federal extension funding FUDING_FEDEXT OP
    * Federal extension funding other FUNDING_FEDEXTOTHER
    * Federal research funding FUNDING_FEDRCH OP
    *
         * other federal funding FUNDING_OTHERFED OP
         * private funding FUNDING_PRIVATE OP but filter out 'other'?
         * state funding FUNDING_STATE OP
         *
         * has investigator stub
         * impact statement entity

*/
            RelationResolver rr = null;
            String xp = null;
            Rejector rejects = new Rejector(null);
            rejects.addReject("All U.S. states");
            rejects.addReject("All NY counties");
            rejects.addReject("All NY Counties");

           // submitted by
            rr = new RelationResolver("@username", NETID,
                    SUBMITTED_BY_PROPID, domainside, noQual, wadf);
            rr.addStrProcessor(new CleanInput());
            rr.addStrProcessor(new CornellEmailSuffixer());
            hands.add( rr );

            // in department
//            rr = new RelationResolver("//Dept", CALS_IMPACT_DEPT_EXTID,
//                    REPORTING_ORG_PROPID, domainside, noQual, wadf);
//            hands.add( rr );

            // affiliated with academic priority area            
            rr = new RelationResolver("PRIORITY_AREA", ENT_NAME,
                    AFFILIATED_WITH_PROPID, domainside, noQual, wadf, PRIORITY_AREA_VCLASSID);
            rr.addStrProcessor( new PriorityAreaMapper());
            hands.add( rr );

            // affected areas - states
            xp = "INVOLVED_STATE";
            rr = new RelationResolver(xp, ENT_NAME, AFFECTED_AREAS_PROPID,
                    domainside, noQual, wadf, GEO_ENT_VCLASSID);
            rr.addStrProcessor( rejects );
            rr.addStrProcessor(new USStateMapper());
            hands.add( rr );

            // affected areas - NY counties
            xp = "INVOLVED_COUNTY";
            rr = new RelationResolver(xp, ENT_NAME, AFFECTED_AREAS_PROPID,
                    domainside, noQual, wadf,COUNTIES_VCLASSID);
            rr.addStrProcessor( rejects );
            rr.addStrProcessor(new NewYorkCountyMapper());
            hands.add( rr );

            // affected areas - countries
            xp = "INVOLVED_COUNTRY";
            rr = new GeoRelationResolver(xp, ENT_NAME, AFFECTED_AREAS_PROPID,
                    domainside, noQual, wadf, COUNTRY_VCLASSID);
            hands.add( rr );
            
            //USDA subject areas
            xp = "USDA_AREA";
            rr = new RelationResolver(xp, ENT_NAME, IMPACT_ADDRESSES_AREA,
                    domainside, noQual, wadf,null);
            hands.add( rr );
                
            //Additional investigator on project if a NetId is found 
            /* XPath to get all NetIDs from IMPACT_STATEMENT_INVEST elements. */ 
            xp = "for $txt in " 
                   +"//IMPACT_STATEMENT_INVEST/DEP[matches (./text(),\"^*.@cornell.edu.*\")]"  
                   +"return replace($txt, \"^.* (\\w*@cornell.edu)\", \"$1\")";
            rr = new RelationResolver(xp, NETID, INVEST_PROP_URI,
                    domainside, noQual, wadf);
            hands.add(rr);            

            //Additional investigator on project if a userid is found
            xp = "//IMPACT_STATEMENT_INVEST/FACULTY_NAME";
            rr = new RelationResolver(xp, REPORTING_USER_ID, INVEST_PROP_URI,
                    domainside, noQual, wadf);
            hands.add(rr);

            xp = "IndexValue/@value";            
            RelationResolverWithUri rrwu = new RelationResolverWithUri(xp, IMPACT_HAS_DEPARTMENT,null,wadf);
            rrwu.addStrProcessor(new DepartmentMapper());
            hands.add(rrwu);            
        } catch (Exception e) {
            log.error("Could not make relation handlers " + e);
        }
        return hands;
    }

    /** Make a FieldHandler that will build new USDA areas.   */
    private List<FieldHandler> makeUsdaAreas(WebappDaoFactory wadf ){
        List<FieldHandler> hands = new ArrayList<FieldHandler>();        
        try {
            List<FieldHandler> handlersForUsdaAreas = new ArrayList<FieldHandler>();            
            FieldHandler fh = null;
            
            /* make an entity for the USDA area */            
            HashMap<String,String> field2xpath = new HashMap<String,String>();
            HashMap<String,String> defaultEntityValues = new HashMap<String,String>();
            field2xpath.put("name","text()" );            
            defaultEntityValues.put("citation", defaultCitation); 
            defaultEntityValues.put("vClassURI", SUBJECT_AREA_VCLASS_ID );
            defaultEntityValues.put("moniker", "USDA subject area");
            defaultEntityValues.put("flag1Set", "6");
            defaultEntityValues.put("flag2Set", "CALS");
            fh = new BasicEntityHandler(field2xpath,defaultEntityValues);
            handlersForUsdaAreas.add(fh);
            
            /* save this if it is new */
            fh = new EntityResolver(RDFS.label.getURI(), "text()", wadf.getIndividualDao(),true);
            handlersForUsdaAreas.add(fh);
            
            //handlersForUsdaAreas.add(new SaveEntity(wadf));
            
            String xpForUsdaAreas = "USDA_AREA";
            fh = new SubElementForField(xpForUsdaAreas,handlersForUsdaAreas);
            hands.add(fh);
        } catch (Exception e) {
            log.error("Could not make USDA Impact Area handlers " + e);
        }
        return hands;
    }
    
    /** Make a FieldHandler that will deal with IMPACT_STATEMENT_INVEST
     * elements that are for faculty. */
//    private List<FieldHandler> makeImpactStatementEntities(WebappDaoFactory wadf ){
//        List<FieldHandler> hands = new ArrayList<FieldHandler>();        
//        try {
//            List<FieldHandler> handlersForISIs = new ArrayList<FieldHandler>();            
//            FieldHandler fh = null;
//                        
//            HashMap<String,String> field2xpath = new HashMap<String,String>();
//            HashMap<String,String> defaultEntityValues = new HashMap<String,String>();
//            field2xpath.put("name","text()" );            
//            defaultEntityValues.put("citation", defaultCitation); 
//            defaultEntityValues.put("vClassURI", SUBJECT_AREA_VCLASS_ID );            
//            defaultEntityValues.put("flag1Set", "6");
//            defaultEntityValues.put("flag2Set", "CALS");
//            fh = new BasicEntityHandler(field2xpath,defaultEntityValues);
//            handlersForISIs.add(fh);
//            
//            /* save this if it is new */
//            fh = new EntityResolver(VitroVocabulary.label, "text()", wadf.getIndividualDao());
//            handlersForISIs.add(fh);
//            
//            String xpForUsdaAreas = "IMPACT_STATEMENT_INVEST";
//            fh = new SubElementForField(xpForUsdaAreas,handlersForISIs);
//            hands.add(fh);
//        } catch (Exception e) {
//            log.error("Could not make USDA Impact Area handlers " + e);
//        }
//        return hands;
//    }
    
    public static void main(String argv[]){
        if( argv == null || argv.length != 2){
            System.out.println("Usage: CalsImpactIngester connection.properties " +
                               "filetoingest.xml");
            return;
        }
        if( false == checkFiles(argv[0], argv[1]) ) return;
        String dbConnectionConfig = argv[0], 
               inputFile = argv[1];
        try {
            CalsImpactIngest2008 cii = new CalsImpactIngest2008(inputFile, dbConnectionConfig);
            cii.doFirstPass(inputFile);
            cii.parser.parse();
            System.out.println("Added "+cii.getCumulativeDeltaModeler().getAdditions().size()+" statements");
            System.out.println("Retracted "+cii.getCumulativeDeltaModeler().getRetractions().size()+" statements");
            File additionsFile = new File("/home/bdc34/src/semweb-align/ingestTool/additions.rdf");
            FileOutputStream additionsOutputStream = new FileOutputStream(additionsFile);
            File retractionsFile = new File("/home/bdc34/src/semweb-align/ingestTool/retractions.rdf");
            FileOutputStream retractionsOutputStream = new FileOutputStream(retractionsFile);
            cii.getCumulativeDeltaModeler().getAdditions().write(additionsOutputStream);
            cii.getCumulativeDeltaModeler().getRetractions().write(retractionsOutputStream);
        } catch (Exception e) { log.error(e); }
    }

    private CumulativeDeltaModeler getCumulativeDeltaModeler() {
        return tracker;
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

    /**
     * Sunsets all individuals that are impact statements.
     */
    private static void sunsetExistingImpacts(WebappDaoFactory wdf){
        VClass impactVC = wdf.getVClassDao().getVClassByURI(IMPACT_VCLASS_ID);
        List<Individual> individuals = wdf.getIndividualDao().getIndividualsByVClass(impactVC);
        Date now = new Date();
        for( Individual ind : individuals ){            
            ind.setSunset(now);
            wdf.getIndividualDao().updateIndividual(ind);
        }
        
    }
    
    private class PriorityAreaMapper implements StringProcessor {
        public String process(String in) {            
            if( in == null ) return null;
            String value = in.trim().toLowerCase();            
            if (value.startsWith("applied"))
                return "Applied Social Sciences";
            else if( value.startsWith("environmental"))
                return "Environmental Sciences";
            else if( value.startsWith("land"))
                return "Land-Grant Mission";
            else if (value.startsWith("new"))
                return "New Life Sciences";
            else
                return in;
        }
    }

//    private class SkipRecord extends BaseXPath2Handler implements FieldHandler{
//        public SkipRecord(String xpathv2)
//                throws XPathFactoryConfigurationException,XPathExpressionException {
//            super(xpathv2);
//        }
//        public void handleNode(String node, Element doc, Object targetEnt) {
//            if( )
//            
//        }        
//    }
    
    private class DepartmentMapper implements StringProcessor {
        Map<String,String> deptMap;
        public DepartmentMapper(){
            deptMap = buildDepartmentMap();
        }
        public String process(String in) {
            if( in == null || in.trim().length() ==0){
                return in;            
            }else if( deptMap.containsKey(in)){
                return deptMap.get(in);
            }else{
                log.error("Unknown department name in DepartmentMapper: " + in );
                return "";
            }
        }
    }
       
    private static final Log log = LogFactory.getLog(CalsImpactIngest2008.class.getName());
    private static String 
        CALS_BASE_CITATION = "From CALS annual faculty reporting. Imported on ",
        CURRENT_YEAR = "2008";
    
    private static String REPORTING_NS = "http://vitro.mannlib.cornell.edu/ns/reporting#";
    private static String VIVO = "http://vivo.library.cornell.edu/ns/0.1#";
    
    
    private static final String 
        REPORTING_IMPACT_ID   = REPORTING_NS + "responseId",
        REPORTING_USER_ID   = REPORTING_NS + "userId",
        IMPACT_VCLASS_ID      = VIVO + "CALSImpactStatement",        
        SUBJECT_AREA_VCLASS_ID   = VIVO + "SubjectArea";
    
    private Map<String,String> buildDepartmentMap(){
        Map<String,String> deptMap = new TreeMap<String, String>();
        deptMap.put("Animal Science", "http://vivo.library.cornell.edu/ns/0.1#individual873");
        deptMap.put("Applied Economics & Mgmt", "http://vivo.library.cornell.edu/ns/0.1#individual551");
        deptMap.put("Bio and Envir Engineering", "http://vivo.library.cornell.edu/ns/0.1#individual154");
        deptMap.put("Biological Statistics & Computational Biology", "http://vivo.library.cornell.edu/ns/0.1#individual379");
        deptMap.put("CALS CCE-Integrated Pest Mgmt", "http://vivo.library.cornell.edu/ns/0.1#individual5456");
        deptMap.put("CALS Coop Extension Areas Spec", "http://vivo.library.cornell.edu/ns/0.1#individual759");
        deptMap.put("CALS Cooperative Extension Adm", "http://vivo.library.cornell.edu/ns/0.1#individual759");
        deptMap.put("Collective Bargaining, Labor Law and Labor History", "http://vivo.library.cornell.edu/ns/0.1#individual5634");
        deptMap.put("College of Agriculture & Life Sciences", "http://vivo.library.cornell.edu/ns/0.1#individual134");
        deptMap.put("College of Arts & Sciences", "http://vivo.library.cornell.edu/ns/0.1#individual249");
        deptMap.put("Communication", "http://vivo.library.cornell.edu/ns/0.1#individual7243");
        deptMap.put("Cooperative Extension (HE)", "http://vivo.library.cornell.edu/ns/0.1#individual759");
        deptMap.put("Cooperative Extension (NYC)", "http://vivo.library.cornell.edu/ns/0.1#individual7975");
        deptMap.put("Cooperative Extension NYC(HE)", "http://vivo.library.cornell.edu/ns/0.1#individual7975");
        deptMap.put("Crop and Soil Sciences", "http://vivo.library.cornell.edu/ns/0.1#individual874");
        deptMap.put("CU Community & Rural Dev", "http://vivo.library.cornell.edu/ns/0.1#individual11195");
        deptMap.put("Department of Horticulture", "http://vivo.library.cornell.edu/ns/0.1#individual416");
        deptMap.put("Development Sociology", "http://vivo.library.cornell.edu/ns/0.1#individual426");
        deptMap.put("Earth and Atmospheric Sciences AG", "http://vivo.library.cornell.edu/ns/0.1#individual5208");
        deptMap.put("Ecology and Evolutionary Biology AG", "http://vivo.library.cornell.edu/ns/0.1#individual195");
        deptMap.put("Ecology and Evolutionary Biology AS", "http://vivo.library.cornell.edu/ns/0.1#individual195");
        deptMap.put("Education", "http://vivo.library.cornell.edu/ns/0.1#individual5652");
        deptMap.put("Entomology", "http://vivo.library.cornell.edu/ns/0.1#individual257");
        deptMap.put("Ext-Conflict Resolution", "http://vivo.library.cornell.edu/ns/0.1#individual21112");
        deptMap.put("Ext-Employment & Disability Institute", "http://vivo.library.cornell.edu/ns/0.1#individual11479");
        deptMap.put("Ext-Labor and Employment Law", "http://vivo.library.cornell.edu/ns/0.1#individual21826");
        deptMap.put("Ext-Labor and Unions", "http://vivo.library.cornell.edu/ns/0.1#individual21827");
        deptMap.put("Ext-Management Development and Human Resources Management", "http://vivo.library.cornell.edu/ns/0.1#individual11468");
        deptMap.put("Ext-Workforce Industry and Economic Development", "http://vivo.library.cornell.edu/ns/0.1#individual21845");
        deptMap.put("Farmworker Program (Development Sociology)", "http://vivo.library.cornell.edu/ns/0.1#individual426");
        deptMap.put("Food Science", "http://vivo.library.cornell.edu/ns/0.1#individual241");
        deptMap.put("Food Science & Technology", "http://vivo.library.cornell.edu/ns/0.1#individual739");
        deptMap.put("Geneva Entomology", "http://vivo.library.cornell.edu/ns/0.1#individual743");
        deptMap.put("Geneva Plant Pathology", "http://vivo.library.cornell.edu/ns/0.1#individual746");
        deptMap.put("Horticultural Sciences", "http://vivo.library.cornell.edu/ns/0.1#individual745");
        deptMap.put("International Programs", "http://vivo.library.cornell.edu/ns/0.1#individual5332");
        deptMap.put("Johnson Graduate School of Management", "http://vivo.library.cornell.edu/ns/0.1#individual577");
        deptMap.put("Laboratory of Ornithology", "http://vivo.library.cornell.edu/ns/0.1#individual5548");
        deptMap.put("Landscape Architecture", "http://vivo.library.cornell.edu/ns/0.1#individual6966");
        deptMap.put("Microbiology", "http://vivo.library.cornell.edu/ns/0.1#individual264");
        deptMap.put("Molecular Biology and Genetics AG", "http://vivo.library.cornell.edu/ns/0.1#individual131");
        deptMap.put("Molecular Biology and Genetics AS", "http://vivo.library.cornell.edu/ns/0.1#individual131");
        deptMap.put("Natural Resources", "http://vivo.library.cornell.edu/ns/0.1#individual5224");
        deptMap.put("Neurobiology & Behavior AG", "http://vivo.library.cornell.edu/ns/0.1#individual253");
        deptMap.put("Neurobiology & Behavior AS", "http://vivo.library.cornell.edu/ns/0.1#individual253");
        deptMap.put("Nutritional Sciences (CALS)", "http://vivo.library.cornell.edu/ns/0.1#individual365");
        deptMap.put("Nutritional Sciences (CHE)", "http://vivo.library.cornell.edu/ns/0.1#individual365");
        deptMap.put("Plant Biology AG", "http://vivo.library.cornell.edu/ns/0.1#individual157");
        deptMap.put("Plant Biology AS", "http://vivo.library.cornell.edu/ns/0.1#individual157");
        deptMap.put("Plant Breeding", "http://vivo.library.cornell.edu/ns/0.1#individual133");
        deptMap.put("Plant Breeding & Genetics", "http://vivo.library.cornell.edu/ns/0.1#individual133");
        deptMap.put("Plant Pathology and Plant-Microbe Biology", "http://vivo.library.cornell.edu/ns/0.1#individual293");
        deptMap.put("Polson Institute (Development Sociology)", "http://vivo.library.cornell.edu/ns/0.1#individual11094");
        deptMap.put("School of Industrial and Labor Relations", "http://vivo.library.cornell.edu/ns/0.1#individual5249");
        deptMap.put("Sea Grant / Lead NY", "http://vivo.library.cornell.edu/ns/0.1#individual6293");
        deptMap.put("Undergraduate Biology", "http://vivo.library.cornell.edu/ns/0.1#individual263");
        deptMap.put("Dean's Office", ""); // this value is found in test data only
        deptMap.put("Finance & Admininstration", ""); //this value is found in test data only
        deptMap.put("Information Systems", "");//this value is found in test data only
        return deptMap;
    }
    
///////////////////////////////
    private static String MANN = "http://vivo.cornell.edu/ns/mannadditions/0.1#";    
    private static String ENT_NAME = VitroVocabulary.RDFS + "label",    
                            NETID    = VIVO+"CornellemailnetId",
                            CALS_IMPACT_DEPT_EXTID = VIVO+"CALSImpactDeptName";

    private static boolean domainside = true, rangeside = false, noQual= false;

    private static String AFFECTED_AREAS_PROPID    = VIVO + "hasGeographicFocus",
                            AFFILIATED_WITH_PROPID   = MANN + "ImpactStatementRelatesToAcademicPriorityArea", // CHECK THIS
                            IMPACT_ADDRESSES_AREA    = VIVO + "ImpactStatementAddressesGenericAreaOfInterest",
                            SUBMITTED_BY_PROPID      = MANN + "ImpactStatementHasAcademicEmployeeSubmitter",
                            REPORTING_ORG_PROPID     = VIVO + "ImpactStatementHasReportingOrganizedEndeavor",
                            INVEST_PROP_URI          = MANN + "hasInvestigator",
                            INVEST_COLLAB_PROP_URI   = MANN + "investigationCollaborator";

    private static String CALS_ABSTRACT_DATAPROPID     = "http://vivo.mannlib.cornell.edu/ns/ThomsonWOS/0.1#abstract",
                       CALS_ISSUE_DATAPROPID        = MANN + "issue",
                       CALS_RESPONSE_DATAPROPID     = MANN + "response",
                       CALS_IMPACT_DATAPROPID       = MANN + "impact",
                       CALS_FUNDING_SRC_DATAPROPID  = VIVO + "fundingSourceDescription",
                       CALS_TOPIC_DESC_DATAPROPID   = VIVO + "topicDescription",
                       CALS_PERSONNEL_DATAPROPID    = MANN + "keyPersonnel",
                       CALS_TOPIC_COLLAB_DATAPROPID = VIVO + "collaborator",
                       IMPACT_HAS_DEPARTMENT        = VIVO + "ImpactStatementHasReportingOrganizedEndeavor";

    private static String 
        PRIORITY_AREA_VCLASSID = VIVO + "AcademicPriorityArea",
        PROJECT_TYPE_VCLASSID = "http://www.aktors.org/ontology/portal#Generic-Area-Of-Interest",
        COUNTIES_VCLASSID = VIVO + "County",
        GEO_ENT_VCLASSID = "http://www.aktors.org/ontology/portal#Geographical-Region",
        STATE_VCLASSID = VIVO + "StateOrProvince",
        COUNTRY_VCLASSID = VIVO + "Country",
        VIVO_NET_ID_URI = VIVO + "CornellemailnetId";
    
}
