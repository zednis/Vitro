import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;


/**
What this does:
1) gets all investigator netids and names from local copy of osp
for each of these:
2) checks to see which ones are in vivo's externalids table, do nothing for these
3) checks if there is someone in vivo who is a close match and put in change queue if not sure
4) if it looks like there is no one in vivo, add this person using ldap info

@author $Author: Brian Caruso bdc34.cornell.edu$
@version  $Rev: 0.1$
$Date: 2005-02-09 17:36:20 -0500 (Wed, 09 Feb 2005) $

*** Changes ***
2005-02-08 TypeName is used for moniker if there isn't anything else.
*/

class OspInvestigatorCheck {
    private static void checkAllInvestigators(Connection vivoCon, Connection ospLocalCon)throws SQLException, LDAPException{
        System.out.println("Checking all Investigators on local osp db to make sure they exist in the vivo db. ");
        Statement ospStmt=ospLocalCon.createStatement(),
            vivoStmt=vivoCon.createStatement();

        String query="SELECT INVPROJ_INVESTIGATOR_NETID, INVPROJ_FIRST_NAME, INVPROJ_MIDDLE_NAME, "+
            "INVPROJ_LAST_NAME, INVPROJ_DEPT_ID FROM INVESTIGATORS_T";

        ResultSet rs = ospStmt.executeQuery(query);
        while(rs.next()){
            checkCount++;
            String netid = rs.getString("INVPROJ_INVESTIGATOR_NETID");
            String entityId=null;

            //get VIVO entity id linked to this netID
            String linkQuery = "SELECT ENTITYID FROM externalids WHERE EXTERNALIDTYPE="+LocalOsp2Vivo.vivoExtIdNetIdType+
                " AND VALUE LIKE '"+netid+"%'"; //the vivo netId have '@cornell.edu' on them, osp netid's don't
            ResultSet entRs = vivoStmt.executeQuery(linkQuery);
            if(entRs.next()) { //we have a externalId in vivo, so things are good.
                entityId = entRs.getString("ENTITYID");
                good++;
                continue;
            }

            int added =0;
            String first=rs.getString("INVPROJ_FIRST_NAME"),middle=rs.getString("INVPROJ_MIDDLE_NAME"),last=rs.getString("INVPROJ_LAST_NAME");
            entityId=matchInvestigator2Entity(first,middle,last,vivoCon);
            if(entityId!=null)
                System.out.println("found: '"+entityId+ "', it will being associated with "+netid);

            if(entityId != null && entityId.length() != 0 && !"null".equals(entityId)){
                added = addExternalId(entityId, netid, vivoCon);
                externalidAddCount += added;
                if(added == 1)
                    good++;
                else
                    bad += added;
                continue;
            }

            //no external id and it doesn't seem to be in vivo, add to vivo or do changeGroup?
            if(createInvestigator(first,middle,last,netid,rs.getString("INVPROJ_DEPT_ID"), vivoCon)){
                good++;
                entityAddCount++;
            }else
                bad++;
        }
    }

    /**
       adds a row to the vivo externalids table that associates the entity and the given netid
       @returns number of rows inserted.
    */
    public static int addExternalId(String entityId, String netid, Connection global_vivoCon)throws SQLException{
        String ins="INSERT INTO externalids (EXTERNALIDTYPE,VALUE,ENTITYID) VALUES ("+
            LocalOsp2Vivo.vivoExtIdNetIdType+","+VitroDbUtils.quoteChar(netid)+","+entityId+")";
        int count=0;
        if(doIt)
            count = global_vivoCon.createStatement().executeUpdate(ins);
        return count;
    }

    private static String getLdapAttribute(LDAPEntry entry, String[] attributeNames){
        String value=null;
        for(int iName=0;iName<attributeNames.length; iName++){
            LDAPAttribute lAtt= entry.getAttribute(attributeNames[iName]);
            if(lAtt!=null && (value=lAtt.getStringValue())!=null){
                return value;
            }
        }
        return null;
    }

    /* if input is null, "null" or zero length return "null"
       otherwise, quote it and return it
    */
    public static String clean(String input){
        if(input==null || input.length()<1 || "null".equals(input))
            return "null";
        else
            return VitroDbUtils.quoteChar(input);
    }


    public static String makeName(String ldapFullName, String first, String middle, String last){
        int i=ldapFullName.toUpperCase().indexOf(last.toUpperCase());
        String newLast=ldapFullName.substring(i).trim();
        String newGiven=ldapFullName.substring(0,i).trim();
        return newLast+", "+newGiven;
    }

    public String getEntityIdFromOspDeptId(IndividualDao individualDao, String ospDeptId)throws SQLException{
        return individualDao.getIndividualByExternalId(LocalOsp2Vivo.OSP_DEPARTMENT_EXTERNAL_ID_URI, ospDeptId);
    }

    /**
     * create investigator entity in VIVO using data from LDAP
     * It will have:
     * a name made from first, middle and last.
     * a link with a netid
     * a citation indicating that it came from ospwh and LDAP
     * maybe a relation with a department
     @returns true if success, otherwise false
     */
    static boolean createInvestigator(String first,String middle, String last,String netid,String ospDeptId, Connection vivoCon)
        throws SQLException, LDAPException{
        Statement stmt=vivoCon.createStatement();

        //get dept for this person, used for flags and for the relation between dept and person
        String deptEntity=getEntityIdFromOspDeptId(individualDao,ospDeptId);
        String deptQ="SELECT FLAG1SET+0 AS 'F1', FLAG2SET+0 AS 'F2', FLAG3SET+0 AS 'F3' FROM entities WHERE ID="+deptEntity;
        ResultSet rs=stmt.executeQuery(deptQ);
        String flag1set=null,flag2set=null,flag3set=null;
        if(!rs.next()){
            System.out.println("unable to load department for user: "+netid+" osp dept code: " + ospDeptId);
            return false;
        }
        flag1set=rs.getString("F1");flag2set=rs.getString("F2");flag3set=rs.getString("F3");

        //get info from ldap
        String filter="(uid="+netid+")";
        /* BROKEN */
        LDAPSearchResults ldapRes = LdapFrame.searchLdap(filter);
        if(!ldapRes.hasMoreElements()){
            return false;
        }
        LDAPEntry entry=(LDAPEntry)ldapRes.nextElement();
        String[] nameA={"cn","fullname"},monikerA={"cornelleduwrkngtitle1","cornelleduwrkngtitle2"};
        String[] typeidA={"edupersonprimaryaffiliation","cornelledutype"},urlA={"labeledUri"},anchorA={"description"};

        String name=getLdapAttribute(entry,nameA);
        name=makeName(name,first,middle,last);
        String moniker=getLdapAttribute(entry,monikerA);
        String etypeStr=getLdapAttribute(entry,typeidA);
        String url=getLdapAttribute(entry,urlA);
        String anchor=getLdapAttribute(entry,anchorA);
        String citation="Added using data from directory.cornell.edu.";

        int etypeId = 0;
        if (etypeStr.equalsIgnoreCase("Extension Associate")) {
            etypeId=94; // Cornell academic staff
        } else if (etypeStr.equalsIgnoreCase("Senior Extension Associate")) {
            etypeId=94; // Cornell academic staff
        } else if (etypeStr.equalsIgnoreCase("Senior Research Associate")) {
            etypeId=94; // Cornell academic staff
        } else if (etypeStr.equalsIgnoreCase("Lecturer")) {
            etypeId=94; // Cornell academic staff
        } else if (etypeStr.equalsIgnoreCase("Senior Lecturer")) {
            etypeId=94; // Cornell academic staff
        } else if (etypeStr.equalsIgnoreCase("Cornell Faculty Member")) {
            etypeId=31; // Cornell faculty member
        } else if (etypeStr.equalsIgnoreCase("Visiting Associate Professor")) {
            etypeId=31; // Cornell faculty member
        } else if (etypeStr.equalsIgnoreCase("Cornell non-academic staff")) {
            etypeId=33; // Cornell non-academic staff
        } else if (etypeStr.equalsIgnoreCase("academic")) {
            etypeId=31; // ldap academic type
        } else if (etypeStr.equalsIgnoreCase("staff")) {
            etypeId=33; // Cornell non-academic staff
        } else if (etypeStr.equalsIgnoreCase("exception - w/sponsor")) {
            etypeId=33; // Cornell non-academic staff
        } else if (etypeStr.equalsIgnoreCase("retiree")) {
            etypeId=33; // Cornell non-academic staff
        } else if (etypeStr.equalsIgnoreCase("cu-connect -directory")) {
            etypeId=33; // Cornell non-academic staff
        } else if (etypeStr.equalsIgnoreCase("temporary")) {
            etypeId=33; // Cornell non-academic staff
        } else if (etypeStr.equalsIgnoreCase("affiliate - CUMC")) {
            etypeId=33; // Cornell non-academic staff
        } else if (etypeStr.equalsIgnoreCase("affiliate")) {
            etypeId=33; // Cornell non-academic staff
        } else if (etypeStr.equalsIgnoreCase("student")) {
            System.out.println("ignoring student "+netid);
            return false;
        } else {
            System.out.println("Error: unexpected type name "+etypeStr+" for user "+netid );
            return false;
        }

        int classId=0;
        switch (etypeId) {
        case 31: classId=232; break; //Academic Employee
        case 33: classId=241; break; //Non-Academic Employee
        case 94: classId=232; break; //Academic Employee
        default: System.out.println("Error: unexpected type id "+etypeId+" for user "+netid);
            return false;
        }

        if(moniker==null || moniker.length() == 0 || "null".equals(moniker) ){ //use etype name as moniker
            String query="SELECT typename FROM etypes where classId ="+classId;
            rs=stmt.executeQuery(query);
            if(rs.next())
                moniker=rs.getString("typename");
        }

        /* add entity */
        String ins="INSERT INTO entities (NAME,CLASSID,MONIKER,TYPEID,URL,ANCHOR, "+
            "CITATION,FLAG1SET,FLAG2SET,FLAG3SET) VALUES (";
        ins+=clean(name)+",";
        ins+= classId +",";
        ins+=clean(moniker)+",";
        ins+= etypeId +",";
        ins+=clean(url) +",";
        ins+=clean(anchor)+",";
        ins+=clean(citation)+",";
        ins+= flag1set +",";
        ins+= flag2set +",";
        ins+= flag3set +")";
        if(doIt) {
            stmt.executeUpdate(ins);
            rs=stmt.getGeneratedKeys();
            rs.next();
            String personEntityId=rs.getString(1);
            /* associate entity and dept */
            String propertyId = "506"; //AcademicSupportUnitHasAffiliatedAcademicEmployee
            /*
             * BROKEN
            if(VitroDbUtils.relateEntity2Entity(deptEntity, personEntityId,propertyId, vivoCon)==null)
                System.out.println("Could not relate dept:"+deptEntity+" person:"+ personEntityId+" using property: " + propertyId);
             */
            /* add external id for entity */
            addExternalId(personEntityId, netid, vivoCon);
            return true;
        } else {
            return true;
        }
    }

    /**
     * @return entity id of vivo.entities that matches, null if no match could be found
     */
    static String matchInvestigator2Entity(String first, String middle, String last, Connection vivoCon)throws SQLException{
        if( first==null || last == null )
            throw new SQLException("last name or first name passed matchInvestigators2Entity was null");
        String st=null;
        if(first.indexOf('.') >= 0  ){ //first name is an initial
            first=first.replace('.','%');
        }else{
            first+="%";
        }

        if( middle!=null){
            if(middle.indexOf(".") >= 0 ){ //middle name is an initial
                middle=middle.replace('.','%');
            }else{
                middle+="%";
            }
            st="SELECT ID, NAME FROM entities WHERE NAME LIKE '"+last+", "+first+" "+middle+"'";
        }else{
            st="SELECT ID, NAME FROM entities WHERE NAME LIKE '"+last+", "+first+"'";
        }
        ResultSet rs = vivoCon.createStatement().executeQuery(st);

        if( rs == null )
            return null;
        rs.last();
        int count = rs.getRow();
        if( count == 0 ){
            //no entity found
            return null;
        }
        if( count > 1 ){
            System.out.println("The name '"+last+", "+first+" "+middle+"' matche multiple entities' names in vivo");
            rs.beforeFirst();
            while(rs.next()){
                System.out.println(" entityId: " + rs.getString("ID") + " name: '" + rs.getString("NAME") + "'");
            }
            return null;
        } else {
            rs.beforeFirst();
            rs.next();
            return rs.getString("ID");
        }
    }


    public static void makeConnections(String fileName)
        throws SQLException, IOException {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(fileName);
        props.load(in);
        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null) {
            System.setProperty("jdbc.drivers", drivers);

            String url = props.getProperty("OSPDWDownload.local.url");
            String username = props.getProperty("OSPDWDownload.local.user");
            String password = props.getProperty("OSPDWDownload.local.passwd");
            System.out.println("local osp database: " + url);
            global_localCon = DriverManager.getConnection( url, username, password );

            url = props.getProperty("vivo.url");
            username = props.getProperty("vivo.username");
            password = props.getProperty("vivo.password");
            System.out.println("vivo database: " + url);
            global_vivoCon = DriverManager.getConnection( url, username, password );
        }
    }
    private static Connection global_vivoCon=null, global_localCon=null;
    private static int checkCount=0,externalidAddCount=0,entityAddCount=0,good=0,bad=0;
    private static String deptTypeId=null;

    private static boolean doIt=false;

    /**
     * Populate department osp ids, then check all investigators in local osp to
     * make sure they exist in vivo.  If they don't exist in vivo, try to make them
     * from ldap.
     *
     * @param args
     */
    public static void main(String[] args){
        if( args.length != 2){
            System.out.println("Attempts to load osp deparment ids and then osp investigators into vivo");
            System.out.println("OspInvestigatorCheck connection.properties true|false");
        }
        if(args != null && args.length >1 && "true".equals(args[1]))
            doIt = true;
        else{
            System.out.println("Doing test run, nothing will be changed in the database.  Run with the argument true to execute changes");
            doIt=false;
        }
        try{ makeConnections(args[0]); }
        catch (SQLException ex ){
            System.out.println("SQLException: " + ex);
        }catch (IOException ex){
            System.out.println("IOException: " + ex);
        }

//        try{
//            PopulateExtDeptIds.populateWithOSPDeptIds(global_vivoCon);
//            deptTypeId=PopulateExtDeptIds.getOSPDeptExIdType(global_vivoCon);
//        }catch(SQLException ex){
//            System.out.println("error trying to populate link table with dept ids:\n"+ex);
//        }

        try{
            checkAllInvestigators(global_vivoCon, global_localCon);
        }catch(SQLException ex){
            System.out.println("error while checking investigators:\n"+ex);
            ex.printStackTrace();
        }catch(LDAPException ex){
            System.out.println("error while searching LDAP:\n");
            ex.printStackTrace();
        }
        System.out.println("checked:" + checkCount+" added id:"+externalidAddCount+" added entity:" + entityAddCount);
        System.out.println("good: " + good+ " bad:" + bad);
        if(!doIt)
            System.out.println("This was just a test, nothing was changed in the database.  Run with the argument 'true' to execute changes");
    }
}
