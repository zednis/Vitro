package edu.cornell.mannlib.ingest.configurations;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.ingest.actions.OutputXmlToFile;
import edu.cornell.mannlib.ingest.actions.ParseToEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.BasicEntityHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.DataPropertyHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.EntityResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.GeoRelationResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.RelationResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.SaveEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.SubElementForField;
import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.ingest.interfaces.IngestParser;
import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.ingest.parser.XPathIteratorParser;
import edu.cornell.mannlib.ingest.processors.CleanInput;
import edu.cornell.mannlib.ingest.processors.CornellEmailSuffixer;
import edu.cornell.mannlib.ingest.processors.NewYorkCountyMapper;
import edu.cornell.mannlib.ingest.processors.Rejector;
import edu.cornell.mannlib.ingest.processors.USStateMapper;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.CumulativeDeltaModeler;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

/**
 * Class with main to extract USDA areas for 2009 CALS Impact statements and
 * make individuals of them.
 */
public class UsdaAreasForCals2008 {

    WebappDaoFactory wadf;
    IngestParser parser;
    CumulativeDeltaModeler tracker = null;
    String defaultCitation="";

    public UsdaAreasForCals2008(String filename, String connectionPropPath) throws Exception{
        if( filename == null )
            throw new Error("need a filename to process");

        //setup DB connection
        Model model = new JenaDataSourceSetupBase().makeDBModelFromPropertiesFile(connectionPropPath);
        OntModel prophylactic = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // we don't want to change the real database yet
        prophylactic.addSubModel(model);
        tracker = new CumulativeDeltaModeler(prophylactic);
        wadf = new WebappDaoFactoryJena(prophylactic);

        //setup citation string, only used when an impact report lacks a date
        DateTime dateT = new DateTime();
        defaultCitation = CALS_BASE_CITATION+" "+dateT.monthOfYear().getAsText()+" "+
        dateT.getDayOfMonth()+ ", " + dateT.getYear();

        ArrayList <IngestAction> actions = new ArrayList<IngestAction>();
        actions.add( makeUsdaAreas( wadf ) );

        // We need a XPathIteratorParser to break the XML into elements that 
        // need to be processed.  It will call each object in actions on 
        // each resulting element.          
        this.parser = new XPathIteratorParser(actions,"//USDA_AREA",
                filename, null);
    }
    
    /** Make a FieldHandler that will build new USDA areas.   */
    private ParseToEntity makeUsdaAreas(WebappDaoFactory wadf ){       
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
            fh = new EntityResolver(RDFS.label.getURI(), ".", wadf.getIndividualDao(), true, null, "en-US");
            handlersForUsdaAreas.add(fh);

            //handlersForUsdaAreas.add(new SaveEntity(wadf));
            
            return new ParseToEntity(handlersForUsdaAreas,wadf);
            
        } catch (Exception e) {
            log.error("Could not make USDA Impact Area handlers " + e);
        }
        return null;
    }
    
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
            UsdaAreasForCals2008 cii = new UsdaAreasForCals2008(inputFile, dbConnectionConfig);
            cii.parser.parse();
            System.out.println("Added "+cii.getCumulativeDeltaModeler().getAdditions().size()+" statements");
            System.out.println("Retracted "+cii.getCumulativeDeltaModeler().getRetractions().size()+" statements");
            File additionsFile = new File("/home/bdc34/src/semweb-align/ingestTool/usdaAreaAdditions.rdf");
            FileOutputStream additionsOutputStream = new FileOutputStream(additionsFile);
            File retractionsFile = new File("/home/bdc34/src/semweb-align/ingestTool/usdaAreaRetractions.rdf");
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
    private static final Log log = LogFactory.getLog(CalsImpactIngest2008.class.getName());
    private static String 
    CALS_BASE_CITATION = "From CALS annual faculty reporting. Imported on ",
    CURRENT_YEAR = "2008-2009";

    private static String REPORTING_NS = "http://vitro.mannlib.cornell.edu/ns/reporting#";
    private static String VIVO = "http://vivo.library.cornell.edu/ns/0.1#";


    private static final String 
    REPORTING_IMPACT_ID   = REPORTING_NS + "responseId",
    IMPACT_VCLASS_ID      = VIVO + "CALSImpactStatement",        
    SUBJECT_AREA_VCLASS_ID   = VIVO + "SubjectArea";

    ///////////////////////////////
    private static String MANN = "http://vivo.cornell.edu/ns/mannadditions/0.1#";    
    private static String ENT_NAME = RDFS.label.getURI(),
    NETID    = VIVO+"CornellemailnetId",
    CALS_IMPACT_DEPT_EXTID = VIVO+"CALSImpactDeptName";

    private static boolean domainside = true, rangeside = false, noQual= false;

    private static String AFFECTED_AREAS_PROPID    = VIVO + "hasGeographicFocus",
    AFFILIATED_WITH_PROPID   = MANN + "ImpactStatementRelatesToAcademicPriorityArea", // CHECK THIS
    AREA_OF_IMPACT_PROPID    = VIVO + "GenericAreaOfInterestAddressedByImpactStatement",
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

    private static String 
    PRIORITY_AREA_VCLASSID = VIVO + "AcademicPriorityArea",
    PROJECT_TYPE_VCLASSID = "http://www.aktors.org/ontology/portal#Generic-Area-Of-Interest",
    COUNTIES_VCLASSID = VIVO + "County",
    GEO_ENT_VCLASSID = "http://www.aktors.org/ontology/portal#Geographical-Region";
}
