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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;
import java.lang.Integer;

import javax.sql.DataSource;
import javax.xml.xpath.XPathException;

import org.apache.commons.dbcp.BasicDataSource;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import edu.cornell.mannlib.ingest.actions.OutputXmlToFile;
import edu.cornell.mannlib.ingest.actions.ParseToEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.BasicEntityHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.DataPropertyHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.EntityResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.ConditionalRelationResolver;
import edu.cornell.mannlib.ingest.fieldHandlers.SaveEntity;
import edu.cornell.mannlib.ingest.fieldHandlers.HrMonikerHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.HrVclassHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.HrFlagHandler;
import edu.cornell.mannlib.ingest.fieldHandlers.ExternalIdHandler;
import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.ingest.interfaces.IngestAction;
import edu.cornell.mannlib.ingest.interfaces.IngestParser;
import edu.cornell.mannlib.ingest.interfaces.RecordSource;
import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.ingest.parser.PullParser;
import edu.cornell.mannlib.ingest.processors.CleanInput;
import edu.cornell.mannlib.ingest.processors.Appender;
import edu.cornell.mannlib.ingest.processors.HrNameProcessor;
import edu.cornell.mannlib.ingest.sql.SqlXmlUtils;
//import edu.cornell.mannlib.vitro.dao.db.VitroConnection;
//import edu.cornell.mannlib.vitro.webapp.dao.VitroFacade;

public class HumanResourcesIngest {

// superseded by RDF-based ingest
	
//    VitroFacade facade;
//    IngestParser parser;
//
//    private JdbcTemplate jdbcTemplate = null;
//
//    private final static int DEFAULT_MAXWAIT = 10, //ms
//    DEFAULT_MAXACTIVE = 10,
//    DEFAULT_MAXIDLE = 10;
//    private final static String DEFAULT_VALIDATIONQUERY = "SELECT 1";
//    private final static boolean DEFAULT_TESTONBORROW = true,
//    DEFAULT_TESTONRETURN = true;
//
//    private final static int HR_LASTNAME_DATAPROPID = 20;
//    private final static int HR_FIRSTNAME_DATAPROPID = 21;
//    private final static int HR_NAMEPREFIX_DATAPROPID = 9;
//    private final static int HR_NAMESUFFIX_DATAPROPID = 10;
//    private final static int HR_NAME_DATAPROPID = 11;
//    private final static int HR_PREFNAME_DATAPROPID = 12;
//    private final static int HR_WORKINGTITLE_DATAPROPID = 13;
//    private final static int HR_CAMPUSPHONE_DATAPROPID = 14;
//    private final static int HR_PHONE_DATAPROPID = 15;
//    private final static int HR_ADDRESS1_DATAPROPID = 16;
//    private final static int HR_ENDOWCHAIRCODEDESC_DATAPROPID = 17;
//    private final static int HR_HRINGESTDATE_DATAPROPID = 22;
//
//    List<StringProcessor> standardStringProcessors;
//
//    public HumanResourcesIngest(String hrConnectionProp, String vitroConnectionProp) throws Exception {
//
//        standardStringProcessors = new ArrayList<StringProcessor>(1);
//        standardStringProcessors.add(new CleanInput());
//
//        // set up connection to hr extract db
//        DataSource hrds = establishHrDataSourceFromProperties(hrConnectionProp);
//        jdbcTemplate = new JdbcTemplate(hrds);
//
//        // set up connection to vitro db
//           //setup db connection
//        VitroConnection vc = new VitroConnection(vitroConnectionProp);
//        //TODO: update for semweb-align
//        if(true) throw new Error("TODO: update for semweb-align");
//        //facade = new VitroFacade(vc);
//
//        //TODO: fix to work with new data access objs
//        //String deptIdQuery = "SELECT DISTINCT value FROM externalids WHERE externalIdType IN (202,204)";
//        //List<String> deptIds = VitroConnection.getJdbcTemplate().queryForList(deptIdQuery, String.class);
//
//        //TODO: fix to work with new data access objs
//        //System.out.println("Importing faculty, academic, and professional staff from "+deptIds.size()+" departments...");
//
//        //TODO: fix to work with new data access objs
//        //RecordSource recs = new HrRecordSource(hrds,deptIds);
//        ArrayList <IngestAction> actions = new ArrayList<IngestAction>();
//        actions.add( new OutputXmlToFile("/output.xml"));
//        actions.add( makeEntityParseAction(facade));
//        //TODO: fix to work with new data access objs
//        //this.parser = new PullParser(actions, recs);
//    }
//
//    public static void main (String[] args){
//        //for testing:
//        args[0] = "/usr/local/works4/Vitro3/ingestTool/config/hrconnection.properties";
//        args[1] = "/usr/local/works4/Vitro3/ingestTool/config/connection.properties";
//
//        if (args == null || args.length != 2){
//            System.out.println("Usage: HumanResourcesIngester hrconnection.properties connection.properties");
//            return;
//        }
//        if (checkFiles(args[0], args[1]) == false)
//            return;
//        String hrConnectionConfig = args[0];
//        String vtConnectionConfig = args[1];
//        try {
//            HumanResourcesIngest hri = new HumanResourcesIngest(hrConnectionConfig, vtConnectionConfig);
//            hri.parser.parse();
//        } catch (Exception e) {
//            System.out.println("exception running HumanResourcesIngest or parse");
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//            // log.error(e);
//            return;
//        }
//    }
//
//    private static boolean checkFiles(String hrConnProps, String connProps){
//        File f = new File(hrConnProps);
//        if(! f.exists() ){
//            System.out.println("The file " + hrConnProps + " does not exist.");
//            return false;
//        }
//        if( !f.canRead() ){
//            System.out.println("The file " + hrConnProps + " is not readable.");
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
//    /**
//     * Sets up a DataSource and db Connection using values from
//     * a properties file.
//     */
//    public final static DataSource establishHrDataSourceFromProperties(final String filename){
//        DataSource ds = null;
//        if (filename == null || filename.length() <= 0) {
//            throw new Error(
//                    "To establish the DataSource and Db Connection you MUST set the "
//                    + "filename to the location of a "
//                    + "connection.properties file with the database connection parameters.");
//        }
//
//        File propF = new File(filename );
//        InputStream is;
//        try {
//            is = new FileInputStream(propF);
//        } catch (FileNotFoundException e) {
//            throw new Error("Could not load file " + filename
//                    + '\n' + e.getMessage());
//        }
//
//        Properties dbProps = new Properties();
//        try {
//            dbProps.load(is);
//        } catch (IOException e) {
//            throw new Error("Could not properties from file " + filename + '\n'
//                    + e.getMessage());
//        }
//
//        ds = getHrDataSourceFromProperties(dbProps);
//        return ds;
//    }
//
//       private static DataSource getHrDataSourceFromProperties(Properties props){
//            String driverClass = props.getProperty("VitroConnection.DataSource.driver");
//
//            String userName = props.getProperty("VitroConnection.DataSource.username");
//            String dbUrl = props.getProperty("VitroConnection.DataSource.url");
//            String passwd = props.getProperty("VitroConnection.DataSource.password");
//
//            String maxActiveStr = props.getProperty("VitroConnection.DataSource.MaxActive");
//            System.out.println("DEBUG: maxActiveStr= "+maxActiveStr);
//            int maxActive = Integer.parseInt(maxActiveStr,DEFAULT_MAXACTIVE);
//
//            String maxIdleStr = props.getProperty("VitroConnection.DataSource.MaxIdle");
//            System.out.println("DEBUG: maxIdleStr= "+maxIdleStr);
//            int maxIdle = Integer.parseInt(maxIdleStr,DEFAULT_MAXIDLE);
//
//            String maxWaitStr = props.getProperty("VitroConnection.DataSource.MaxWait");
//            System.out.println("DEBUG: maxWaitStr= "+maxWaitStr);
//            int maxWait = Integer.parseInt(maxWaitStr,DEFAULT_MAXWAIT);
//
//            String query = props.getProperty("VitroConnection.DataSource.ValidationQuery",
//                                             DEFAULT_VALIDATIONQUERY);
//
//            String onBorrowStr = props.getProperty("VitroConnection.DataSource.TestOnBorrow");
//            String onReturnStr = props.getProperty("VitroConnection.DataSource.TestOnReturn");
//            boolean onBorrow = ( onBorrowStr != null ? "true".equalsIgnoreCase(onBorrowStr) :
//                                                       DEFAULT_TESTONBORROW );
//            boolean onReturn = ( onReturnStr != null ? "true".equalsIgnoreCase(onReturnStr) :
//                                                       DEFAULT_TESTONRETURN );
//
//            BasicDataSource ds = new BasicDataSource();
//            ds.setDriverClassName(driverClass);
//            ds.setUrl(dbUrl);
//            ds.setUsername(userName);
//            ds.setPassword(passwd);
//            ds.setMaxActive(maxActive);
//            ds.setMaxIdle(maxIdle);
//            ds.setMaxWait(maxWait); //in msec
//            ds.setValidationQuery(query);
//            ds.setTestOnBorrow(onBorrow);
//            ds.setTestOnReturn(onReturn);
//            ds.setMinEvictableIdleTimeMillis( 60*1000*5 );
//
//            //these should only be used in testing:
//            ds.setRemoveAbandoned(true);
//            ds.setLogAbandoned(true);
//            ds.setRemoveAbandonedTimeout( 120 );//sec
//
//            try{
//                System.out.println("Attempting to connecto to "
//                                   + userName + "@" + dbUrl + "...");
//                ds.getConnection().close(); //connect and close, to test conection
//                System.out.println("Successfully connected to DataSource: " + dbUrl);
//            } catch (SQLException e) {
//               System.out.println("*** VitroConntection: could not connecto to "
//                                  +"database, is the db server running? *** "
//                                  + e.toString());
//            }
//            return ds;
//        }
//
//
//
//       private class HrRecordSource implements RecordSource {
//
//           // private final String HR_QUERY = "SELECT person.*, job.* FROM person, job WHERE person.emplId = job.emplId ORDER BY person.emplId";
//           private final String HR_QUERY = "SELECT DISTINCT person.* FROM person, job WHERE person.emplid=job.emplid AND (job.eeo6codeldesc='Faculty' OR job.eeo6codeldesc='Professional Nonfaculty' OR job.eeo6CodeLdesc = 'Executive/Admin/Managerial') AND job.flsastatusldesc='No FLSA Required'";
//           private String deptWhereClause = " AND job.Deptid IN (";
//
//           private JdbcTemplate jdbcTemplate = null;
//           private SqlRowSet hrRowSet = null;
//           private DocumentFactory docFactory = new DocumentFactory();
//
//
//           public HrRecordSource(DataSource ds, List<String> deptIds){
//               if (ds != null) {
//                   Iterator deptIt = deptIds.iterator();
//                   while (deptIt.hasNext()) {
//                       deptWhereClause += "'"+((String)deptIt.next())+"'";
//                       if (deptIt.hasNext())
//                           deptWhereClause += ", ";
//                   }
//                   deptWhereClause += ")";
//                   jdbcTemplate = new JdbcTemplate(ds);
//                   String query = HR_QUERY + deptWhereClause;
//                   hrRowSet = jdbcTemplate.queryForRowSet(query);
//                   hrRowSet.beforeFirst();
//               }
//           }
//
//           public boolean hasNext(){
//               return (!(hrRowSet.isLast() || hrRowSet.isAfterLast()));
//           }
//
//           public Document next() {
//               Calendar cal = Calendar.getInstance();
//               Date date = cal.getTime();
//               Document nextDoc = docFactory.createDocument();
//               Element rootElt = nextDoc.addElement("person");
//               Element dateElt = rootElt.addElement("VITRO_HR_INGEST_DATE");
//               dateElt.addText(date.toString());
//               hrRowSet.next();
//               // fill elements for the person row:
//               SqlXmlUtils.fillElementFromRow(nextDoc.getRootElement(),hrRowSet);
//               double currEmplId = hrRowSet.getDouble(1);
//               // run a query and add a <job> element for each job
//               String jobQuery = "SELECT job.* FROM job WHERE job.emplId="+currEmplId+" ORDER BY jobindicatorldesc";
//               SqlRowSet jobRowSet = jdbcTemplate.queryForRowSet(jobQuery);
//               jobRowSet.beforeFirst();
//               while (!jobRowSet.isLast()){
//                   jobRowSet.next();
//                   Element jobElt = nextDoc.getRootElement().addElement("job");
//                   SqlXmlUtils.fillElementFromRow(jobElt,jobRowSet);
//               }
//               return nextDoc;
//           }
//
//           public void remove (){
//               throw new UnsupportedOperationException("HrRecordSource does not support remove()");
//           }
//
//       }
//
//       private IngestAction makeEntityParseAction(VitroFacade facade) throws IOException {
//           ArrayList<FieldHandler> hands = new ArrayList<FieldHandler>();
//           hands.add(makeHrXml2Entity());
//           hands.add( new EntityResolver(101,"concat(//person/Netid,'@cornell.edu')", facade));
//           hands.add( new HrMonikerHandler(jdbcTemplate));
//           hands.add( new HrVclassHandler());
//           List<StringProcessor> sp = new ArrayList();
//           sp.add(new Appender("@cornell.edu"));
//           hands.add( new ExternalIdHandler("//person/Netid", null, 101, sp));
//           hands.addAll(makeDataPropertyHandlers());
//           hands.addAll(makeRelationHandlers(facade));
//           System.out.println(makeRelationHandlers(facade).size()+" relation handlers");
//           hands.add( new SaveEntity(facade) );
//            //TODO: fix to work with new data access objs
//           //hands.add( new HrFlagHandler() );
//           return new ParseToEntity (hands, facade);
//       }
//
//       private BasicEntityHandler makeHrXml2Entity(){
//           HashMap<String,String> field2xpath = new HashMap<String,String>();
//           field2xpath.put ("name","//person/PrefName");
//           field2xpath.put ("moniker","//person/WorkingTitle");
//
//           HashMap<String,String> defaultEntityValues = new HashMap<String,String>();
//           // this is only temporary:
//           defaultEntityValues.put("vClassId","295");
//           defaultEntityValues.put("moniker","Cornell faculty member");
//
//           HashMap<String,List<StringProcessor>> processors = new HashMap<String,List<StringProcessor>>();
//           List<StringProcessor> nameStringProcessors = new ArrayList<StringProcessor>();
//           nameStringProcessors.addAll(standardStringProcessors);
//           nameStringProcessors.add(new HrNameProcessor());
//
//           BasicEntityHandler beh = new BasicEntityHandler(field2xpath,defaultEntityValues);
//           beh.addPerPropertyStringProcessors("name",nameStringProcessors);
//           beh.setDefaultStrProcessor(standardStringProcessors);
//           return beh;
//       }
//
//       private List<FieldHandler> makeDataPropertyHandlers() {
//           List<FieldHandler> hands = new ArrayList<FieldHandler>();
//           List<StringProcessor> strProc = standardStringProcessors;
//
//           //lastName
//           hands.add ( new DataPropertyHandler("//person/LastName",
//                   null,
//                   HR_LASTNAME_DATAPROPID,
//                   strProc));
//
//           //firstName
//           hands.add ( new DataPropertyHandler("//person/FirstName",
//                   null,
//                   HR_FIRSTNAME_DATAPROPID,
//                   strProc));
//
//           //namePrefix
//           hands.add ( new DataPropertyHandler("//person/NamePrefix",
//                   null,
//                   HR_NAMEPREFIX_DATAPROPID,
//                   strProc));
//
//           //nameSuffix
//           hands.add ( new DataPropertyHandler("//person/NameSuffix",
//                   null,
//                   HR_NAMESUFFIX_DATAPROPID,
//                   strProc));
//
//           //name
//           hands.add ( new DataPropertyHandler("//person/Name",
//                   null,
//                   HR_NAME_DATAPROPID,
//                   strProc));
//
//           //prefName
//           hands.add ( new DataPropertyHandler("//person/PrefName",
//                   null,
//                   HR_PREFNAME_DATAPROPID,
//                   strProc));
//
//           //workingTitle
//           hands.add ( new DataPropertyHandler("//person/WorkingTitle",
//                   null,
//                   HR_WORKINGTITLE_DATAPROPID,
//                   strProc));
//
//           //campusPhone
//           hands.add ( new DataPropertyHandler("//person/CampusPhone",
//                   null,
//                   HR_CAMPUSPHONE_DATAPROPID,
//                   strProc));
//
//           //phone
//           hands.add ( new DataPropertyHandler("//person/Phone",
//                   null,
//                   HR_PHONE_DATAPROPID,
//                   strProc));
//
//           //address
//           hands.add ( new DataPropertyHandler("//person/Address1",
//                   null,
//                   HR_ADDRESS1_DATAPROPID,
//                   strProc));
//
//           //endowChair
//           hands.add ( new DataPropertyHandler("//person/EndowChairCodeDesc",
//                   null,
//                   HR_ENDOWCHAIRCODEDESC_DATAPROPID,
//                   strProc));
//
//           //hrIngestDate
//           hands.add ( new DataPropertyHandler("//person/VITRO_HR_INGEST_DATE",
//                   null,
//                   HR_HRINGESTDATE_DATAPROPID,
//                   strProc));
//
//           return hands;
//
//       }
//
//       private List<FieldHandler> makeRelationHandlers(VitroFacade facade) {
//           List<FieldHandler> hands = new ArrayList<FieldHandler>();
//           try {
//               ConditionalRelationResolver rr = null;
//               // faculty member in
//               String[] FacultyFamilies = {"Professorial"};
//               rr = new ConditionalRelationResolver("//person/job/Deptid",202,506,false,false,facade,"//person/job/JobFamilyLdesc", FacultyFamilies, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               String[] OtherFacultyJobcodes = {"Prof Adj Asst", "Prof Adj", "Prof Adj Assoc", "Prof Courtesy", "Professor Assistant Courtesy",
//                       "Prof Assoc Vis", "Prof Asst Visit", "Professor Associate Courtesy", "Fellow Faculty", "Prof Visiting", "Andrew D. White Prof-At-Large", "Vice Provost", "Provost Associate", "Vice Provost Research Admin", "Dean Assoc"};
//               rr = new ConditionalRelationResolver("//person/job/Deptid",202,506,false,false,facade,"//person/job/JobcodeLdesc", OtherFacultyJobcodes, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               // academic staff in
//               String[] AcadStaffSalAdminPlans = {"Cornell Acad Non-Professorial", "Academic - other than CAC"};
//               String[] NonProfessorialFamilies = {"Research/Extension", "Student Services", "Computers & Networks",
//                       "County Extension", "Alumni Affairs Development", "Academic Support",
//                       "Teaching", "Administration",
//                       "Human Resources", "Health", "Service/Facilities",    "Athletics",
//                       "Technical", "Communications", "Fin/Budget/Planning", "Titles - Puerto Rico",
//                       "Auxiliary Services", "Miscellaneous Non CRS"};
//               rr = new ConditionalRelationResolver("//person/job/Deptid",202,507,false,false,facade,"//person/job/SalAdminPlanLdesc", AcadStaffSalAdminPlans, "//person/job/JobFamilyLdesc", NonProfessorialFamilies);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               String[] OtherAcadStaffJobcodes = {"Fellow Visting", "Post Dr Assoc", "Scient Visit", "Scientist Sr", "Scholar Visit",
//                    "Lect Visit Sr", "Lect Visit", "Lect Courtesy Sr", "Instr Visiting", "Scholar Sr", "Instr Courtesy",
//                    "Critic Visiting", "Lect Courtesy", "Fellow Postdoc", "Research Associate,Sr", "Scientist Sr"};
//               rr = new ConditionalRelationResolver("//person/job/Deptid",202,507,false,false,facade,"//person/job/JobcodeLdesc", OtherAcadStaffJobcodes, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               // librarian in
//               String[] LibrarianFamilies = {"Library - Academic"};
//               rr = new ConditionalRelationResolver("//person/job/Deptid",202,512,false,false,facade,"//person/job/JobFamilyLdesc", LibrarianFamilies, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               // head of
//               String[] HeadCodes = {"Dean","Dean Academic","Director","Director Acad","Dept. Chairperson","Dept. Chairperson, Acting","University Librarian", "Dean of Students", "Dean Grad School", "President", "Provost", "V.P. Info Tech", "Vice Provost Assoc", "Dir Athletics", "V.P. Human Resources", "Director, Acting", "Dir Health Svcs", "VP, University Communications", "V.P. Pub Affairs", "Chief Investment Officer", "V.P. Serv/ Facilities", "Executive Vice President", "Treasurer", "Univ Counsel", "V.P. for Planning & Budget", "V.P. Univ Controller", "Sr Vice Provost for Research", "VP, Bus Svcs & Env Safety", "V.P. Student & Acad Svcs"};
//               rr = new ConditionalRelationResolver("//person/job/Deptid",202,396,false,false,facade,"//person/job/JobcodeLdesc", HeadCodes, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               // staff member in
//               String[] NonAcadStaffSalAdminPlans = {"Non-Academic Staff", "Cooperative Extension Exempt", "Cooperative Extension Agent", "Puerto Rico Exempt"};
//               rr = new ConditionalRelationResolver("//person/job/Deptid",202,374,false,false,facade,"//person/job/SalAdminPlanLdesc", NonAcadStaffSalAdminPlans, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               String[] OtherStaffCodes = {"Dir Development", "Gannett Resident", "Sr Resident", "Special Assistant to Provost", "VP/Sr Asst to President", "Assistant Vice President", "Associate Vice President"};
//               rr = new ConditionalRelationResolver("//person/job/Deptid",202,374,false,false,facade,"//person/job/JobcodeLdesc", OtherStaffCodes, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               // do the same thing for activities
//               rr = new ConditionalRelationResolver("//person/job/Deptid",204,506,false,false,facade,"//person/job/JobFamilyLdesc", FacultyFamilies, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               rr = new ConditionalRelationResolver("//person/job/Deptid",204,506,false,false,facade,"//person/job/JobcodeLdesc", OtherFacultyJobcodes, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               rr = new ConditionalRelationResolver("//person/job/Deptid",204,507,false,false,facade,"//person/job/SalAdminPlanLdesc", AcadStaffSalAdminPlans, "//person/job/JobFamilyLdesc", NonProfessorialFamilies);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               rr = new ConditionalRelationResolver("//person/job/Deptid",204,507,false,false,facade,"//person/job/JobcodeLdesc", OtherAcadStaffJobcodes, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               rr = new ConditionalRelationResolver("//person/job/Deptid",204,512,false,false,facade,"//person/job/JobFamilyLdesc", LibrarianFamilies, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               rr = new ConditionalRelationResolver("//person/job/Deptid",204,374,false,false,facade,"//person/job/SalAdminPlanLdesc", NonAcadStaffSalAdminPlans, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               rr = new ConditionalRelationResolver("//person/job/Deptid",204,396,false,false,facade,"//person/job/JobcodeLdesc", HeadCodes, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//               rr = new ConditionalRelationResolver("//person/job/Deptid",204,374,false,false,facade,"//person/job/JobcodeLdesc", OtherStaffCodes, null, null);
//               rr.addStrProcessor(new CleanInput());
//               hands.add(rr);
//           } catch (XPathException e) {
//               // TODO Auto-generated catch block
//               e.printStackTrace();
//           }
//           return hands;
//       }


}