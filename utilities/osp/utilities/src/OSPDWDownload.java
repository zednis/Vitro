import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;


/**
   This is a class that attempts to access the OSP data warehouse and then download
   that data to a mysql database.  This step is an attempt to handle some of the
   logic about which grants and sponsors to import before the LocalOsp2Vivo step.
   If this two step process of moving the data from OSPWH to a local mysql database,
   and then into vivo becomes too slow then this initial step could be eliminated.

   as of 2006-10-31 the person to conntact about the ospwh db is Phil Robinson
   <pr52@cornell.edu>, 255-0098.

   **Change Log**
   2005-10-26 bdc34 changed to include grants based on investigator college/dept
   2005-10-24 bdc34 added check for netid and full_name
   2005-04-06 bdc34 removed hardwired junk, refactored
   2005-02-09 bdc34 fixed bug where PI list had some CO-PIs in it.
   2005-02-05 bdc34 fixed bug where PI's were not being added as members of projects.
   2004-12-06 started

*/
class OSPDWDownload {
    public static final String dateFormat = "yyyy-MM-dd";

    private static void dataWarehouseDownload(Connection osp, Connection local, Connection vivoDb)
        throws SQLException {
        //check to see if ospwh has some data
        if( !ospwhHasData(osp)){
            System.out.println("OSPWH is missing some data, aborting download");
            return;
        }

        System.out.print("Cleaning out old rows...");
        deleteLocalRows(local);
        System.out.print("cleaned.\n");

        Properties errors = new Properties(); //for storing errors, won't get written to file.
        long start = System.currentTimeMillis();
        Statement stmt=osp.createStatement();
        ResultSet projectsRS = stmt.executeQuery(setLastSql(getAwardQuery()));
        int count = 0;int picount = 0;int copicount =0;int memcount = 0;
        int sponsorCount = 0; int deptCount = 0;

        while ( projectsRS.next() ) {
            count++; System.out.print(count%64==0?count+"\n":"." );
            //copy award_purposal row into local mysql db.
            String award_prop_project_id = projectsRS.getString("award_prop_project_id");
            String award_proposal_id = projectsRS.getString("award_proposal_id");
            String award_prop_department_id = projectsRS.getString("award_prop_department_id");
            String award_prop_sponsor_id = projectsRS.getString("award_prop_sponsor_id");

            //create row in local award_proposal table
            Statement localstmt = local.createStatement();
            String insert = makeAwardInsert(projectsRS);
            localstmt.executeUpdate(setLastSql(insert));

            //create row in local sponsor table
            if( doSponsorInsert(osp, local, award_prop_sponsor_id) ){
                sponsorCount++;
            }

            //create row in local dept table
            if(!isDuplicateDept(local, award_prop_department_id)&&
               (null != (insert = setLastSql(makeDeptInsert(osp, award_prop_department_id))))){
                localstmt.executeUpdate(setLastSql(insert));
                deptCount+=localstmt.getUpdateCount();
            }

            //copy any invistigators from the INVESTIGATOR_PROJECT table
            String query = piQuery + "WHERE INVPROJ_PROJECT_ID = '" + award_prop_project_id +"'";
            Statement piStmt = osp.createStatement();
            ResultSet piRs = piStmt.executeQuery(setLastSql(query));
            while (piRs.next()) {
                if( !hasNeededKeysInvistigator( piRs, errors )){
                    continue;
                }
                if(!isDuplicateInvistigator(local,piRs.getString("INVPROJ_INVESTIGATOR_ID"))){
                    insert=makeInvistegatorInsert(piRs.getString("INVPROJ_INVESTIGATOR_ID"),
                                                  piRs.getString("INVPROJ_FULL_NAME"),
                                                  piRs.getString("INVPROJ_FIRST_NAME"),
                                                  piRs.getString("INVPROJ_MIDDLE_NAME"),
                                                  piRs.getString("INVPROJ_LAST_NAME"),
                                                  piRs.getString("INVPROJ_INVESTIGATOR_NETID"),
                                                  piRs.getString("INVPROJ_DEPT_ID"),
                                                  piRs.getString("INVPROJ_UPDATE_DATE"),
                                                  piRs.getString("INVPROJ_UPDATE_NETID"),
                                                  piRs.getString("INVPROJ_EMAIL_ADDRESS"));
                    localstmt.executeUpdate(setLastSql(insert)); picount++;
                }
                if(!isDuplicateMember(local,piRs.getString("INVPROJ_INVESTIGATOR_ID"),
                                      piRs.getString("INVPROJ_PROJECT_ID"))){
                    insert = makeMemberInsert(piRs.getString("INVPROJ_INVESTIGATOR_ID"),
                                              piRs.getString("INVPROJ_PROJECT_ID"),
                                              piRs.getString("INVPROJ_AWARD_PROPOSAL_ID"),
                                              award_prop_department_id,
                                              piRs.getString("INVPROJ_INVESTIGATOR_ROLE"));
                    localstmt.executeUpdate(setLastSql(insert)); memcount++;
                }
            }
            close(piRs);close(piStmt);

            //copy any investigators from the CO_PI table
            query = coPiQuery + " WHERE CO_PI_PROJECT_ID = '" + award_prop_project_id + "'" ;
            Statement coPiStmt = osp.createStatement();
            ResultSet coPiRs = coPiStmt.executeQuery(setLastSql(query));
            while (coPiRs.next()) {
                if( !hasNeededKeysCoInvistigator( coPiRs,errors )){
                    String id = coPiRs.getString("CO_PI_INVESTIGATOR_ID");
                    errors.setProperty( id + "-COPI-ERROR",
                                       "OSPDW table INVISTIGATOR_PROJECT with id of " + id
                                        +" lacks a NETID or a FULL_NAME,\n\tthis is an error in the"
                                        +" OSPDW data, ignoring row and proceding" );
                    continue;
                }
                if(!isDuplicateInvistigator(local,coPiRs.getString("CO_PI_INVESTIGATOR_ID"))){
                    insert=makeInvistegatorInsert(coPiRs.getString("CO_PI_INVESTIGATOR_ID"),
                                                  coPiRs.getString("CO_PI_FULL_NAME"),
                                                  coPiRs.getString("CO_PI_FIRST_NAME"),
                                                  coPiRs.getString("CO_PI_MIDDLE_NAME"),
                                                  coPiRs.getString("CO_PI_LAST_NAME"),
                                                  coPiRs.getString("CO_PI_INVESTIGATOR_NETID"),
                                                  coPiRs.getString("CO_PI_DEPT_ID"),
                                                  coPiRs.getString("CO_PI_UPDATE_DATE"),
                                                  coPiRs.getString("CO_PI_UPDATE_NETID"),
                                                  coPiRs.getString("CO_PI_EMAIL_ADDRESS"));
                    localstmt.executeUpdate(setLastSql(insert)); copicount++;
                }
                if(!isDuplicateMember(local,coPiRs.getString("CO_PI_INVESTIGATOR_ID"),
                                      coPiRs.getString("CO_PI_PROJECT_ID"))){
                    insert = makeMemberInsert(coPiRs.getString("CO_PI_INVESTIGATOR_ID"),
                                              coPiRs.getString("CO_PI_PROJECT_ID"),
                                              coPiRs.getString("CO_PI_AWARD_PROPOSAL_ID"),
                                              award_prop_department_id,
                                              coPiRs.getString("CO_PI_INVESTIGATOR_ROLE"));
                    localstmt.executeUpdate(setLastSql(insert)); memcount++;
                }
            }
            close(coPiStmt);close(coPiRs);
        }
        int indirectSponsorCount = insertMissingSponsors(osp, local);
        indirectSponsorCount += insertFlowThroughSponsors(osp, local);
        sponsorCount = sponsorCount + indirectSponsorCount;

        close(projectsRS);
        close(stmt);
        System.out.println("\n\n-- Summery of records moved from OSPDW to temporary workspace --");
        System.out.println("\nInserted: " + count + "  project records");
        System.out.println("          " + picount + "  PI records");
        System.out.println("          " + copicount + " CO_PI records "
               +"(usually there are zero co_pi adds because they get added as pi first)");
        System.out.println("          " + sponsorCount+ " sponsors" );
        System.out.println("          " + indirectSponsorCount+ " as indirect sponsors");
        System.out.println("          " + deptCount+ " departments" );
        System.out.println("total inv:" +(picount + copicount) );
        System.out.println("members  :" + memcount );
        long elapsed = (System.currentTimeMillis()-start) ;
        System.out.println("time: "+ elapsed +"msec mean: "+ elapsed/(count*1.0) + "msec per award");
        listErrors( errors );
    }

    /**
       Load all of the sponsors that are referenced in the SPONSOR_LEVEL_X fields
       which are not currently in the localdb.SPONSOR_T from the remote db into
       that table.
       @return - number of sponsors inserted
    */
    private static int insertMissingSponsors(Connection osp, Connection local)
    throws SQLException{
        int sponsorCount = 0, placeholders = 0;
        String insert = null, keyValue = null, sponsor_id = null, abbreviation = null, query = null;
        //find the sponsors that we are missing
        Statement localstmt = local.createStatement();
        Statement ospStmt = osp.createStatement();
        ResultSet rs = localstmt.executeQuery(setLastSql(otherSponsorQuery));
        ResultSet ospRs = null;
        while( rs.next()){
            abbreviation = rs.getString("ABBREVIATION");
            //we have the abbreviation we need the sponsor_id from ospwh
            ospRs=ospStmt.executeQuery("SELECT SPONSOR_ID FROM OSPWH.SPONSOR WHERE SPONSOR_ABBREVIATION = '"
                                         + abbreviation + "'");

            if(ospRs.next()&&(null != (sponsor_id = ospRs.getString("SPONSOR_ID")))){
                //create row in local sponsor table using OSPWH sponsor
                if( doSponsorInsert(osp, local, sponsor_id) ){
                    sponsorCount++;
                }
            }else{
                //no OSPWH sponsor, make a placeholder sponsor
                System.out.println("inserting placeholder sponsor: " +abbreviation);
                placeholders++;
                query = setLastSql("SELECT MAX(SPONSOR_ID)+"+placeholders+
                                   " as SPONSOR_ID FROM OSPWH.SPONSOR");
                ResultSet idRs = ospStmt.executeQuery(query);
                if(idRs.next()&&(null != (sponsor_id = idRs.getString("SPONSOR_ID")))){
                    insert = setLastSql("INSERT INTO "+localdb+"SPONSOR_T " +
                                        "(SPONSOR_ID, SPONSOR_NAME, SPONSOR_ABBREVIATION) "+
                                        "VALUES (" + sponsor_id + ", " +
                                        VitroDbUtils.quoteChar(abbreviation) + ", " +
                                        VitroDbUtils.quoteChar(abbreviation) + ")");
                    localstmt.executeUpdate(insert);
                    sponsorCount = sponsorCount + localstmt.getUpdateCount();
                } else {
                    System.out.println("could not make placeholder sponsor: " + abbreviation);
                }
            }
        }

        close(rs);
        close(ospRs);
        close(ospStmt);
        close(localstmt);
        return sponsorCount;
    }

    private static int insertFlowThroughSponsors(Connection osp, Connection local)
    throws SQLException{
        int sponsorCount = 0;
        Statement localstmt = local.createStatement();
        ResultSet rs = localstmt.executeQuery(setLastSql(flowThroughSponsors));
        try{
            while( rs.next()){
                String sponsor_id = rs.getString("ID");
                if( doSponsorInsert(osp, local, sponsor_id) ){
                    sponsorCount++;
                }
            }
        }catch(Exception ex){
            System.out.println("Error loading sponsors:" + ex);
        }
        close(rs);
        close(localstmt);
        return sponsorCount;
    }

    /* Get all the sponsor ids for flow through awards */
    private static String flowThroughSponsors =
        "SELECT DISTINCT A.FLOW_THROUGH_SPONSOR_ID AS ID FROM AWARD_PROPOSAL A "+
        "WHERE FEDERAL_FLOW_THROUGH = 'Y' AND FLOW_THROUGH_SPONSOR_ID IS NOT NULL";

    /* get all sponsor_abbreviations where there isn't a sponsor_t row
       these are the sponsor_level_1 and sponsor_level_2 sponsors
       that are not directly referenced by AWARD_PROPOSAL.AWARD_PROP_SPONSOR_ID    */
    private static String otherSponsorQuery =
              "(SELECT DISTINCT S1.SPONSOR_LEVEL_1 AS ABBREVIATION FROM SPONSOR_T S1 LEFT JOIN " +
                "SPONSOR_T S2 ON S1.SPONSOR_LEVEL_1 = S2.SPONSOR_ABBREVIATION WHERE " +
                "S2.SPONSOR_ABBREVIATION IS NULL AND S1.SPONSOR_LEVEL_1 IS NOT NULL) " +
                "UNION " +
                "(SELECT DISTINCT S1.SPONSOR_LEVEL_2 AS ABBREVIATION FROM SPONSOR_T S1 LEFT JOIN " +
                "SPONSOR_T S2 ON S1.SPONSOR_LEVEL_2 = S2.SPONSOR_ABBREVIATION WHERE " +
                "S2.SPONSOR_ABBREVIATION IS NULL AND S1.SPONSOR_LEVEL_2 IS NOT NULL) ";


    public static void main (String args[]) {
        if( args.length < 1 || args.length > 2){
            System.out.println("Usage: java OSPDWDownload propertiesFileName auto");
            System.out.println("if auto is true, run without prompting the user");
            return;
        }
        System.out.println("Doing OSPDWDownload($Rev: $), using properties file " + args[0]);
        Connection osp=null, local=null, vivoDb = null;
        String orgUrl=null, orgUser=null,orgPasswd=null,copyUrl=null,copyUser=null,copyPasswd=null,
               vivoUrl=null, vivoUser=null, vivoPasswd=null;
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream(args[0]);
            props.load(in);
            in.close();

            String driver1 = props.getProperty("OSPDWDownload.ospwh.JDBCdriver");
            String driver2 = props.getProperty("OSPDWDownload.local.JDBCdriver");
            try{
                Driver d1 = (Driver)Class.forName( driver1 ).newInstance();
                DriverManager.registerDriver(d1);
            } catch (Exception ex){
                System.out.println("could not load driver named " + driver1);
                return;
            }
            try{
                Driver d2 = (Driver)Class.forName( driver2 ).newInstance();
                DriverManager.registerDriver(d2);
            }catch (Exception ex){
                System.out.println("could not load driver " + driver2);
                return;
            }

            //constatnts for access to the original database
            orgUrl = props.getProperty("OSPDWDownload.ospwh.url");
            orgUser = props.getProperty("OSPDWDownload.ospwh.user");
            orgPasswd = props.getProperty("OSPDWDownload.ospwh.passwd");
            //constatnts for access to the original database
            copyUrl = props.getProperty("OSPDWDownload.local.url");
            copyUser = props.getProperty("OSPDWDownload.local.user");
            copyPasswd = props.getProperty("OSPDWDownload.local.passwd");

            vivoUrl =  props.getProperty("vivo.url");
            vivoUser =  props.getProperty("vivo.username");
            vivoPasswd =  props.getProperty("vivo.password");

            System.out.println("Using remote:\t" + orgUser + "@" + orgUrl + "\n" +
                               "       local:\t" + copyUser + "@" + copyUrl +'\n' +
                               "       vivo: \t" + vivoUser + "@" + vivoUrl);

        } catch (IOException ex) {
            System.out.println("error trying to read properties file " + args[0]);
            ex.printStackTrace(System.out);
        }

//        if( args.length > 1 && "true".compareToIgnoreCase(args[1])!=0){
//            readEntry("press return to continue or ctl-c to quit.");
//        }

        try{
            boolean conSetup = false;
            try{
                osp = DriverManager.getConnection(orgUrl, orgUser, orgPasswd);
                conSetup = true;
            } catch (Exception ex){
                System.out.println("*** Unable To connecto to OSPDW database, "
                                   + orgUser + "@" + orgUrl
                                   +" check password, user and url ***");
                conSetup = false;
            }

            try{
                local = DriverManager.getConnection(copyUrl,copyUser, copyPasswd);
                conSetup = conSetup && true;
            } catch (Exception ex){
                System.out.println("*** Unable To connecto to database "
                                   + copyUser + "@" + copyUrl
                                   +" check password, user and url ***");
                conSetup = false;
            }

            if( ! conSetup ){
                System.out.println("Unable to connect, aborting");
                return;
            }

            try{
                //don't know if this prevents us from writing or only optimizes
                osp.setReadOnly(true);
                dataWarehouseDownload(osp, local, vivoDb );//this is where everything happens
            } catch (SQLException ex) {
                System.out.println ("SQLException while trying to load data from OSPWH:");
                while (ex != null) {
                    ex.printStackTrace(System.out);
                    if (lastSql != null) { System.out.println(lastSql); }
                    System.out.println ("SQLState: " + ex.getSQLState());
                    System.out.println ("Message:  " + ex.getMessage());
                    System.out.println ("Vendor:   " + ex.getErrorCode());
                    ex = ex.getNextException();
                    System.out.println ("");
                }
            }
        }catch (Exception ex){
            System.out.println("unhandled exception: " + ex.toString());
            ex.printStackTrace(System.out);
        } finally {
            close(osp); close(local);
        }
    }



    /**    Use this to store current sql query.  On an errror this will be displayed.   */
    private static String setLastSql(String sql){ return lastSql=sql; }
    private static String lastSql = null;

    private static void deleteLocalRows ( Connection local )
        throws SQLException{
        Statement stmt=local.createStatement();
        stmt.executeUpdate("delete from " + localdb +"INVESTIGATORS_T ");
        stmt.executeUpdate("delete from " + localdb +"PROJECT_MEMBERS_T ");
        stmt.executeUpdate("delete from " + localdb +"AWARD_PROPOSAL ");
        stmt.executeUpdate("delete from " + localdb +"DEPARTMENT_T");
        stmt.executeUpdate("delete from " + localdb +"SPONSOR_T ");
        stmt.close();
    }

    private static String makeAwardInsert(ResultSet rs)
        throws SQLException{
        return
            "INSERT INTO " + localdb + "AWARD_PROPOSAL ("+
            "AWARD_PROP_PROJECT_ID, "+
            "AWARD_PROPOSAL_ID, "+
            "AWARD_OR_PROPOSAL, "+
            "AWARD_PROP_TITLE, "+
            "AWARD_PROP_FULL_TITLE, "+
            "AWARD_PROP_STATUS, "+
            "AWARD_PROP_STATUS_CODE, "+
            "AWARD_PROP_COLLEGE, "+
            "AWARD_PROP_DEPARTMENT_ID, "+
            "AWARD_PROP_SPONSOR_ID, "+
            "SPONSOR_PROJECT_ID, "+
            "AWARD_PROP_TOTAL, "+
            "AWARD_PROP_BUDGET_TOTAL, "+
            "AWARD_DESCRIPTION, "+
            "PROPOSAL_PURPOSE, "+
            "PROPOSAL_PURPOSE_DESCRIPTION, "+
            "FEDERAL_FLOW_THROUGH, "+
            "FLOW_THROUGH_SPONS_ALL_LEVELS, "+
            "FLOW_THROUGH_SPONSOR_ID, "+
            "FLOW_THROUGH_SPONSOR_NAME, "+
            "AWARD_PROP_START_DATE, " +
            "AWARD_PROP_END_DATE, " +
            "AWARD_PROP_BUDGET_START_DATE, " +
            "AWARD_PROP_BUDGET_END_DATE, " +
            "AWARD_PROP_UPDATE_DATE, " +
            "BEST_GUESS_START_DATE, " +
            "BEST_GUESS_END_DATE " +
            ")VALUES(" +
            VitroDbUtils.quoteChar(rs, "AWARD_PROP_PROJECT_ID")+", "+
            VitroDbUtils.quoteChar(rs, "AWARD_PROPOSAL_ID")+", "+
            VitroDbUtils.quoteChar(rs, "AWARD_OR_PROPOSAL")+", "+
            VitroDbUtils.quoteChar(rs, "AWARD_PROP_TITLE")+", "+
            VitroDbUtils.quoteChar(rs, "AWARD_PROP_FULL_TITLE")+", "+
            VitroDbUtils.quoteChar(rs, "AWARD_PROP_STATUS")+", "+
            VitroDbUtils.quoteChar(rs, "AWARD_PROP_STATUS_CODE")+", "+
            VitroDbUtils.quoteChar(rs, "AWARD_PROP_COLLEGE")+", "+
            VitroDbUtils.quoteNumeric(rs, "AWARD_PROP_DEPARTMENT_ID")+", "+
            VitroDbUtils.quoteNumeric(rs, "AWARD_PROP_SPONSOR_ID")+", "+
            VitroDbUtils.quoteChar(rs, "SPONSOR_PROJECT_ID")+", "+
            VitroDbUtils.quoteNumeric(rs, "AWARD_PROP_TOTAL")+", "+
            VitroDbUtils.quoteNumeric(rs, "AWARD_PROP_BUDGET_TOTAL")+", "+
            VitroDbUtils.quoteChar(rs, "AWARD_DESCRIPTION")+", "+
            VitroDbUtils.quoteChar(rs, "PROPOSAL_PURPOSE")+", "+
            VitroDbUtils.quoteChar(rs, "PROPOSAL_PURPOSE_DESCRIPTION")+", "+
            VitroDbUtils.quoteChar(rs, "FEDERAL_FLOW_THROUGH")+", "+
            VitroDbUtils.quoteChar(rs, "FLOW_THROUGH_SPONS_ALL_LEVELS")+", "+
            VitroDbUtils.quoteNumeric(rs, "FLOW_THROUGH_SPONSOR_ID")+", "+
            VitroDbUtils.quoteChar(rs, "FLOW_THROUGH_SPONSOR_NAME")+ ", "+
            VitroDbUtils.quoteDate(rs, "AWARD_PROP_START_DATE" )+ ", "+
            VitroDbUtils.quoteDate(rs, "AWARD_PROP_END_DATE" )+ ", "+
            VitroDbUtils.quoteDate(rs, "AWARD_PROP_BUDGET_START_DATE" )+ ", "+
            VitroDbUtils.quoteDate(rs, "AWARD_PROP_BUDGET_END_DATE" )+ ", "+
            VitroDbUtils.quoteDate(rs, "AWARD_PROP_UPDATE_DATE" )+ ", "+
            VitroDbUtils.quoteDate( "2334-01-01" )+ ", "+
            VitroDbUtils.quoteDate( "2334-01-01" ) + ")";
    }

    private static String makeMemberInsert(String investigatorId,
                                           String projectId,
                                           String awardProposal,
                                           String departmentId,
                                           String role){
        return "INSERT INTO "+localdb+"PROJECT_MEMBERS_T (" +
            "INVESTIGATOR_ID_C, " +
            "PROJECT_ID_C, " +
            "AWARD_PROPOSAL_C, " +
            "DEPARTMENT_ID_C, " +
            "ROLE_C " +
            ") VALUES (" +
            VitroDbUtils.quoteNumeric(investigatorId) + ", " +
            VitroDbUtils.quoteChar(projectId) +", "+
            VitroDbUtils.quoteChar(awardProposal) +", "+
            VitroDbUtils.quoteNumeric(departmentId) +", "+
            VitroDbUtils.quoteChar(role) + ")";
    }

    private static boolean isDuplicateDept(Connection localcon, String id)
        throws SQLException {
        String query = "SELECT DEPARTMENT_ID FROM "+localdb+"DEPARTMENT_T "+
            "WHERE DEPARTMENT_ID = " + id ;
        return VitroDbUtils.hasRows(localcon, query);
    }
    private static boolean isDuplicateSponsor(Connection localcon, Connection ospCon, String id)
        throws SQLException {

        String query = "SELECT SPONSOR_ABBREVIATION FROM OSPWH.SPONSOR WHERE SPONSOR_ID =" + id;
        Statement stmt = ospCon.createStatement();
        ResultSet rs = stmt.executeQuery(setLastSql(query));
        if( !rs.next() ) return false;
        String abbrev = rs.getString("SPONSOR_ABBREVIATION");

        close(stmt);close(rs);
        query = "SELECT SPONSOR_ID FROM "+localdb+"SPONSOR_T "+
            "WHERE SPONSOR_ID = " + id +" OR SPONSOR_ABBREVIATION = "+VitroDbUtils.quoteChar(abbrev);
        return VitroDbUtils.hasRows(localcon, query);
    }
    private static boolean isDuplicateInvistigator(Connection localcon, String id)
        throws SQLException {
        String query = "SELECT INVPROJ_INVESTIGATOR_ID FROM "+localdb+"INVESTIGATORS_T "+
            "WHERE INVPROJ_INVESTIGATOR_ID = " + id;
        return VitroDbUtils.hasRows(localcon, query);
    }
    private static boolean isDuplicateAward(Connection localcon, String id)
        throws SQLException{
        String query="SELECT AWARD_PROP_PROJECT_ID FROM AWARD_PROPOSAL WHERE"+
            "AWARD_PROP_PROJECT_ID ="+id;
        return VitroDbUtils.hasRows(localcon, query);
    }

    private static boolean isDuplicateMember(Connection localcon, String INVESTIGATOR_ID_C,
                                             String PROJECT_ID_C) throws SQLException {
        String query = "SELECT INVESTIGATOR_ID_C FROM PROJECT_MEMBERS_T WHERE "+
            "INVESTIGATOR_ID_C = " + INVESTIGATOR_ID_C + " AND " +
            "PROJECT_ID_C = " + VitroDbUtils.quoteChar(PROJECT_ID_C);
        return VitroDbUtils.hasRows(localcon, query);
    }

    /**
        Does this row have the keys needed?
        Expects that rs is set to row in question and do not call rs.next().
     */
    protected static boolean hasNeededKeysInvistigator(ResultSet rs, Properties errors )
        throws SQLException{
        boolean ok = true;
        if( isEmpty(rs, "INVPROJ_FULL_NAME") ){
            ok = false;
            String id = rs.getString("INVPROJ_INVESTIGATOR_ID");
            String proj = rs.getString("INVPROJ_PROJECT_ID");
            errors.setProperty( id + "-PI-ERROR",
                                "OSPDW row of table INVISTIGATOR_PROJECT with id of " + id
                                +" lacks a FULL_NAME\n\tThis is an error in the"
                                +" OSPDW data for project id " + proj
                                +" Ignoring row and proceding" );

        }
        if(isEmpty(rs, "INVPROJ_INVESTIGATOR_NETID") ){
            ok = false;
            String id = rs.getString("INVPROJ_INVESTIGATOR_ID");
            String proj = rs.getString("INVPROJ_PROJECT_ID");
            errors.setProperty( id + "-PI-ERROR",
                                "OSPDW row of table INVISTIGATOR_PROJECT with id of " + id
                                +" lacks a NETID\n\tThis is an error in the"
                                +" OSPDW data for project id " + proj
                                +" Ignoring row and proceding" );
        }
        return ok;
    }

    private static boolean isEmpty(ResultSet rs, String key) throws SQLException{
        boolean empty = true;
        if( rs == null || key == null ) return empty;
        String value = rs.getString(key);
        if(value == null ||
           value.length() == 0 ||
           "null".equalsIgnoreCase(value) ) return empty;
        return !empty;
    }

    /** Checks to see if all of the tables in ospwh have data. */
    private static boolean ospwhHasData( Connection con) throws SQLException{
        boolean hasData = true;
        if( isTableEmpty( con, "OSPWH.AWARD_PROPOSAL") ){
            hasData =  false;
            System.out.println("OSPWH.AWARD_PROPOSAL has no rows " );
        }
        if( isTableEmpty( con, "OSPWH.CO_PI")){
            hasData =  false;
            System.out.println("CO_PI has no rows " );
        }
        if( isTableEmpty( con, "OSPWH.INVESTIGATOR_PROJECT") ){
            hasData =  false;
            System.out.println("OSPWH.INVESTIGATOR_PROJECT has no rows " );
        }
        if( isTableEmpty( con, "OSPWH.OSP_DEPARTMENT") ){
            hasData =  false;
        System.out.println("OSPWH.OSP_DEPARTMENT has no rows " );
        }
        if( isTableEmpty( con, "OSPWH.SPONSOR") ){
            hasData =  false;
            System.out.println("OSPWH.SPONSOR has no rows " );
        }
        return hasData;
    }

    protected static boolean isTableEmpty(Connection con, String tablename) throws SQLException{
        String query = "SELECT COUNT(*) as rowcount FROM "+tablename;
        String key = "rowcount";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        boolean isEmpty = false;
        try{
            if( rs.next() ){
                int count = rs.getInt(key);
                isEmpty = ( count == 0 );
            }else{
                isEmpty = true;
            }
        }catch(SQLException ex){ /* nothing */  }
        rs.close();stmt.close();
        return isEmpty;
    }

    protected static boolean hasNeededKeysCoInvistigator(ResultSet rs, Properties errors )
        throws SQLException{
        boolean ok = true;
        if( isEmpty(rs,"CO_PI_FULL_NAME") ){
            ok = false;
            String id = rs.getString("CO_PI_INVESTIGATOR_ID");
            String proj = rs.getString("CO_PI_PROJECT_ID");
            errors.setProperty( id + "-CO-PI-ERROR",
                                "OSPDW row of table CO-PI with id of " + id
                                +" lacks a FULL_NAME\n\tThis is an error in the"
                                +" OSPDW data for project " + proj
                                + " Ignoring row and proceding" );

        }
        if( isEmpty(rs,"CO_PI_INVESTIGATOR_NETID") ){
            ok = false;
            String id = rs.getString("CO_PI_INVESTIGATOR_ID");
            String proj = rs.getString("CO_PI_PROJECT_ID");
            errors.setProperty( id + "-CO-PI-ERROR",
                                "OSPDW row of table CO-PI with id of " + id
                                +" lacks a NETID\n\tThis is an error in the"
                                +" OSPDW data for project " + proj
                                +" Ignoring row and proceding" );
        }
        return ok;
    }

    public static void listErrors(Properties errs){
        if( errs != null && errs.size() > 0 ) {
            System.out.println("*** Errors from loading OSPDW to local temp db ***");
            Enumeration names = errs.propertyNames();
            while( names.hasMoreElements() ){
                String key = (String)names.nextElement();
                System.out.println(key + ": " + errs.getProperty(key) + "\n");
            }
            System.out.println("*** End of Errors from loading OSPDW data ***");
        }
    }

    /**
       make a sql statement that inserts the sponsor where the search_field = search_value
    @param ospCon - connection to ospwh
    @param search_value - use row keyed by this value in the search_field.
    @param search_field - field to look for the search_value in.
    @return true = success, false = duplicate or sponsor not found in ospCon
    */
    private static boolean doSponsorInsert(Connection ospCon, Connection local, String sponsor_id)
        throws SQLException{

        if( isDuplicateSponsor(local, ospCon, sponsor_id ) )
            return false;

        String query = "SELECT " +
            "SPONSOR_ID, " +
            "SPONSOR_NAME, " +
            "SPONSOR_ALL_LEVELS, " +
            "SPONSOR_LEVEL_1, " +
            "SPONSOR_LEVEL_2, " +
            "SPONSOR_LEVEL_3, " +
            "SPONSOR_ABBREVIATION, " +
            "SPONSOR_ORIGIN, " +
            "SPONSOR_FUND_SOURCE, " +
            "SPONSOR_EMAIL_ADDRESS, " +
            "SPONSOR_WEB_SITE, " +
            "SPONSOR_STREET_1, " +
            "SPONSOR_STREET_2, " +
            "SPONSOR_CITY, " +
            "SPONSOR_STATE, " +
            "SPONSOR_COUNTRY, " +
            "SPONSOR_ZIP_CODE, " +
            "SPONSOR_LEVEL, " +
            "SPONSOR_PARENT_ID " +
            "FROM OSPWH.SPONSOR " +
            "WHERE SPONSOR_ID= " + sponsor_id;
        Statement stmt = ospCon.createStatement();
        ResultSet rs = stmt.executeQuery(setLastSql(query));
        if (!rs.next()) return false;
        query = "INSERT INTO " + localdb +"SPONSOR_T ("+
            "SPONSOR_ID, " +
            "SPONSOR_NAME, " +
            "SPONSOR_ALL_LEVELS, " +
            "SPONSOR_LEVEL_1, " +
            "SPONSOR_LEVEL_2, " +
            "SPONSOR_LEVEL_3, " +
            "SPONSOR_ABBREVIATION, " +
            "SPONSOR_ORIGIN, " +
            "SPONSOR_FUND_SOURCE, " +
            "SPONSOR_EMAIL_ADDRESS, " +
            "SPONSOR_WEB_SITE, " +
            "SPONSOR_STREET_1, " +
            "SPONSOR_STREET_2, " +
            "SPONSOR_CITY, " +
            "SPONSOR_STATE, " +
            "SPONSOR_COUNTRY, " +
            "SPONSOR_ZIP_CODE, " +
            "SPONSOR_LEVEL, " +
            "SPONSOR_PARENT_ID " +
            ")VALUES(" +
            VitroDbUtils.quoteNumeric(rs, "SPONSOR_ID") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_NAME") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_ALL_LEVELS") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_LEVEL_1") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_LEVEL_2") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_LEVEL_3") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_ABBREVIATION") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_ORIGIN") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_FUND_SOURCE") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_EMAIL_ADDRESS") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_WEB_SITE") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_STREET_1") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_STREET_2") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_CITY") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_STATE") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_COUNTRY") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_ZIP_CODE") + ", " +
            VitroDbUtils.quoteChar(rs, "SPONSOR_LEVEL") + ", " +
            VitroDbUtils.quoteNumeric(rs, "SPONSOR_PARENT_ID")+")";

        Statement localstmt = local.createStatement();
        localstmt.executeUpdate(setLastSql(query));

        close(rs);close(localstmt);close(stmt);
        return true;
    }

    private static String makeDeptInsert(Connection ospCon, String department_id)
        throws SQLException{
        Statement stmt = ospCon.createStatement();
        String query = "SELECT " +
            "DEPARTMENT_NAME, " +
            "DEPARTMENT_ABBREVIATION, " +
            "DEPARTMENT_COLLEGE_NAME, " +
            "DEPARTMENT_COLLEGE_CODE " +
            "FROM OSPWH.OSP_DEPARTMENT " +
            "WHERE DEPARTMENT_ID = " + department_id;
        ResultSet rs = stmt.executeQuery(setLastSql(query));
        if (rs.next()==false) return null;
        return "INSERT INTO "+localdb+"DEPARTMENT_T ("+
            "DEPARTMENT_ID, " +
            "DEPARTMENT_NAME, " +
            "DEPARTMENT_ABBREVIATION, " +
            "DEPARTMENT_COLLEGE_NAME, " +
            "DEPARTMENT_COLLEGE_CODE " +
            ") VALUES (" +
            VitroDbUtils.quoteNumeric(department_id) + ", "+
            VitroDbUtils.quoteChar(rs, "DEPARTMENT_NAME") + ", "+
            VitroDbUtils.quoteChar(rs, "DEPARTMENT_ABBREVIATION") + ", "+
            VitroDbUtils.quoteChar(rs, "DEPARTMENT_COLLEGE_NAME") + ", "+
            VitroDbUtils.quoteChar(rs, "DEPARTMENT_COLLEGE_CODE") + ")";
    }

    private static String makeInvistegatorInsert(String INVESTIGATOR_ID,

                                                 String FULL_NAME,
                                                 String FIRST_NAME,
                                                 String MIDDLE_NAME,
                                                 String LAST_NAME,
                                                 String INVESTIGATOR_NETID,
                                                 String DEPT_ID,
                                                 String UPDATE_DATE,
                                                 String UPDATE_NETID,
                                                 String EMAIL_ADDRESS){
        return "INSERT INTO " + localdb + "INVESTIGATORS_T(" +
            " INVPROJ_INVESTIGATOR_ID, INVPROJ_FULL_NAME, INVPROJ_FIRST_NAME, INVPROJ_MIDDLE_NAME, INVPROJ_LAST_NAME, INVPROJ_INVESTIGATOR_NETID, INVPROJ_DEPT_ID,  INVPROJ_UPDATE_DATE, INVPROJ_UPDATE_NETID, INVPROJ_EMAIL_ADDRESS" +
            ") values( " +
            VitroDbUtils.quoteNumeric(INVESTIGATOR_ID) + ", " +
            VitroDbUtils.quoteChar(FULL_NAME) + ", " +
            VitroDbUtils.quoteChar(FIRST_NAME) +", " +
            VitroDbUtils.quoteChar(MIDDLE_NAME) +", " +
            VitroDbUtils.quoteChar(LAST_NAME) +", " +
            VitroDbUtils.quoteChar(INVESTIGATOR_NETID) +", " +
            VitroDbUtils.quoteNumeric(DEPT_ID) + ", " +
            VitroDbUtils.quoteChar(UPDATE_DATE) +", " +
            VitroDbUtils.quoteChar(UPDATE_NETID) +", " +
            VitroDbUtils.quoteChar(EMAIL_ADDRESS) +
            ")";
    }

    private static String localdb = "osplocal.";
    private static String getAwardQuery() {  return awardQuery;  }

    //departments to get PIs and COPIs from
    private static String gDepartments = null;
    //colleges to get PIs and COPIs from
    private static String gColleges = null;

//     private static String piSubquery =
//         "SELECT DISTINCT(inv.invproj_project_id) FROM OSPWH.INVESTIGATOR_PROJECT inv, "
//         +"OSPWH.OSP_DEPARTMENT dept "
//         +"WHERE inv.INVPROJ_DEPT_ID = dept.DEPARTMENT_ID "
//         +"AND ( dept.DEPARTMENT_NAME IN ( <<<DEPT>>> )) ";

 //    private static String copiSubquery =
//         "SELECT DISTINCT(co.co_pi_project_id) "
//         +"FROM OSPWH.CO_PI co, "
//         +"OSPWH.OSP_DEPARTMENT dept "
//         +"WHERE co.CO_PI_DEPT_ID = dept.DEPARTMENT_ID "
//         +"AND ( dept.DEPARTMENT_NAME IN ( <<<DEPT>>> ))";

    private static String awardQuery =
        "SELECT "+
        "AWARD_PROP_PROJECT_ID, "+
        "AWARD_PROPOSAL_ID, "+
        "AWARD_OR_PROPOSAL, "+
        "AWARD_PROP_TITLE, "+
        "AWARD_PROP_FULL_TITLE, "+
        "AWARD_PROP_STATUS, "+
        "AWARD_PROP_STATUS_CODE, "+
        "AWARD_PROP_COLLEGE, "+
        "AWARD_PROP_DEPARTMENT_ID, "+
        "AWARD_PROP_SPONSOR_ID, "+
        "SPONSOR_PROJECT_ID, "+
        "AWARD_PROP_TOTAL, "+
        "AWARD_PROP_BUDGET_TOTAL, "+
        "AWARD_DESCRIPTION, "+
        "PROPOSAL_PURPOSE, "+
        "PROPOSAL_PURPOSE_DESCRIPTION, "+
        "FEDERAL_FLOW_THROUGH, "+
        "FLOW_THROUGH_SPONS_ALL_LEVELS, "+
        "FLOW_THROUGH_SPONSOR_ID, "+
        "FLOW_THROUGH_SPONSOR_NAME, "+
        "TO_CHAR(AWARD_PROP_START_DATE,'"+dateFormat+"') AS AWARD_PROP_START_DATE," +
        "TO_CHAR(AWARD_PROP_END_DATE, '"+dateFormat+"') AS AWARD_PROP_END_DATE," +
        "TO_CHAR(AWARD_PROP_BUDGET_START_DATE, '"+dateFormat+"') AS AWARD_PROP_BUDGET_START_DATE," +
        "TO_CHAR(AWARD_PROP_BUDGET_END_DATE, '"+dateFormat+"')AS AWARD_PROP_BUDGET_END_DATE," +
        "TO_CHAR(AWARD_PROP_UPDATE_DATE, '"+dateFormat+"') AS AWARD_PROP_UPDATE_DATE" +
        " FROM OSPWH.AWARD_PROPOSAL "+
        "WHERE "+
        "AWARD_DESCRIPTION NOT IN ('MTA', 'NDA', 'RADS') " +
        "AND (AWARD_PROP_STATUS_CODE='ASAP' OR AWARD_PROP_STATUS_CODE='APA') ";
    //this use to have a subquery to limit to a set of departments but that is gone.

    private static String piQuery =
        "SELECT " +
        "INVPROJ_AWARD_PROPOSAL_ID, INVPROJ_FULL_NAME, INVPROJ_FIRST_NAME, "+
        "INVPROJ_INVESTIGATOR_ROLE, INVPROJ_PROJECT_ID, INVPROJ_INVESTIGATOR_ID, " +
        "INVPROJ_LAST_NAME, INVPROJ_INVESTIGATOR_NETID, INVPROJ_EMAIL_ADDRESS, " +
        "INVPROJ_DEPT_NAME, INVPROJ_DEPT_ABBREV, INVPROJ_DEPT_ADW_CODE, INVPROJ_DEPT_ID, "+
        "INVPROJ_COLLEGE, INVPROJ_CORNELL_ID, INVPROJ_MIDDLE_NAME, " +
        "INVPROJ_UPDATE_DATE, INVPROJ_UPDATE_NETID, INVPROJ_INVESTIGATOR_ROLE "+
        "FROM OSPWH.INVESTIGATOR_PROJECT ";

    private static String coPiQuery =
        "SELECT " +
        "CO_PI_PROJECT_ID, " +
        "CO_PI_AWARD_PROPOSAL_ID, " +
        "CO_PI_FULL_NAME, " +
        "CO_PI_FIRST_NAME, " +
        "CO_PI_MIDDLE_NAME, " +
        "CO_PI_LAST_NAME, " +
        "CO_PI_INVESTIGATOR_NETID, " +
        "CO_PI_INVESTIGATOR_ROLE, " +
        "CO_PI_DEPT_NAME, " +
        "CO_PI_DEPT_ABBREV, " +
        "CO_PI_DEPT_ADW_CODE, " +
        "CO_PI_DEPT_ID, " +
        "CO_PI_COLLEGE, " +
        "CO_PI_INVESTIGATOR_ID, " +
        "CO_PI_UPDATE_DATE, " +
        "CO_PI_UPDATE_NETID, " +
        "CO_PI_EMAIL_ADDRESS, " +
        "CO_PI_CORNELL_ID " +
        "FROM OSPWH.CO_PI";

    /* these use to be in VitroBaseDb */
        /************ static closing methods *****************/
    public static void close(Statement stmt){
        try{ if(stmt != null ) stmt.close(); }
        catch(Exception ex){}
    }
    public static void close(Connection con){
        try{ if(con != null ) con.close(); }
        catch(Exception ex){}
    }
    public static void close(ResultSet rs){
        try{ if(rs != null ) rs.close(); }
        catch(Exception ex){}
    }

    public static final String quoteDate(Date in){
        if( in == null ) return "NULL";
        Format formatter= new SimpleDateFormat(dateFormat);
        String s = formatter.format(in);
        return VitroDbUtils.quoteDate(s);
    }

}
