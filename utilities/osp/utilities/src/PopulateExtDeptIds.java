import edu.cornell.mannlib.vitro.utils.VitroDbUtils;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
Adds ExternalIds with osp department codes.  This is just
to populate the osp department codes in the ExternalIds table
and does nothing more than insert them if they are not there.

@author Brian Caruso
@version 1.0

*** ChangeLog ***

*/

class PopulateExtDeptIds{

    //URI of ospExternalID property
    String ospExternalIdURI = "http://fake.fake.fake.In.PopulateExtDeptIds.java";

    public static void main(String[] args ){
        try{
            makeConnections();
        }catch(SQLException ex){
            System.out.println("while trying to connect: " );
            ex.printStackTrace();
        }catch(IOException ex){
            System.out.println("while reading properties file: " );
            ex.printStackTrace();
        }

        try{
            populateWithOSPDeptIds(global_vivoCon);
        }catch(SQLException ex){
            System.out.println("while loading: " );
            ex.printStackTrace();
        }
    }

    protected static final String type_name="'OSP department id'";
    private static String deptExtIdType=null;

    public static String getOSPDeptExIdType(Connection con)throws SQLException{
        if(deptExtIdType!=null)
            return deptExtIdType;

        String query="SELECT ID FROM externalidtypes WHERE TYPE ="+type_name;
        ResultSet rs=con.createStatement().executeQuery(query);
        if(rs.next())
            return deptExtIdType = rs.getString("ID");
        else
            return null;
    }

    public String getEntityIdFromOspDeptId(IndividualDao individualDao, String ospDeptId)throws SQLException{
        return individualDao.getIndividualByExternalId(ospExternalIdURI, ospDeptId);
//        String deptQ="SELECT ENTITYID FROM externalids WHERE "+
//            " EXTERNALIDTYPE="+getOSPDeptExIdType(con)+
//            " AND VALUE='"+ospDeptId + "'";
//        ResultSet rs=con.createStatement().executeQuery(deptQ);
//        if(rs.next())
//            return rs.getString("ENTITYID");
//        else
//            return null;
    }

    public static void populateWithOSPDeptIds(Connection con)throws SQLException{
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        String vivoDeptLinkId=null;
        //make sure that there is a department id link type and make one if there is none
        try{
            if((vivoDeptLinkId=getOSPDeptExIdType(con))==null){
                rs = stmt.executeQuery("SELECT MAX(ID)+1 AS ID FROM externalidtypes");
                rs.next();
                stmt.executeUpdate("INSERT INTO externalidtypes (ID, TYPE, GENERIC, CLASSGROUPID) VALUES ("+
                                   rs.getString("ID")+", "+type_name+", '', 4)");
                vivoDeptLinkId=getOSPDeptExIdType(con);
            }
        }catch (SQLException ex){System.out.println("could not create linktype "+type_name+": " + ex);}

        //insert links between VIVO entity id's and OSP DEPARTMENT_T.DEPARTMENT_ID's
        Iterator it = getOSPDeptIdtoVivoDeptIdMap().entrySet().iterator();
        while(it.hasNext()){
            Map.Entry ent = (Map.Entry)it.next();
            String ospId = (String)ent.getKey();
            String vivoName = (String)ent.getValue();
            if(getEntityIdFromOspDeptId(con, ospId) != null)  //check for duplicates
                continue;

            String entityId = null;
            String query = "SELECT ID FROM entities "+
                "WHERE NAME LIKE "+ VitroDbUtils.quoteChar(vivoName);//for more general matches put % in HashMap values
            rs = stmt.executeQuery(query);
            if(rs.next())
                entityId = rs.getString("ID");
            String insert = "INSERT INTO externalids (VALUE, ENTITYID, EXTERNALIDTYPE)"+
                "VALUES ('"+ospId+"', "+entityId+", "+vivoDeptLinkId+")";
            System.out.println("the Insert: " + insert);

            stmt.executeUpdate(insert);
        }

    }

    /**
     * we need to map the OSP departments to the VIVO departments.
     * So should we have a set of aliases in VIVO? or just a map here?
     * For now we will have just a map here.
     */
    private static HashMap OSPDeptIdtoVivoDeptIdMap = null;
    public static HashMap getOSPDeptIdtoVivoDeptIdMap(){
        if(OSPDeptIdtoVivoDeptIdMap != null) {return OSPDeptIdtoVivoDeptIdMap;}
        HashMap map = OSPDeptIdtoVivoDeptIdMap = new HashMap(64);

        /* note that the VIVO_ent_name doesn't need to be the exact name and it will be used in
           a SELECT ... LIKE '$VIVO_ent_name' statement so you could include some % */

        /*
          Quick key to where these strings are from:
          map.put(OSPWH_dept_id, VIVO_entity_name);  // OSP_department_namegeneva
        */

        //map.put("121", "unknown"); //     "CALS RESEARCH OFFICE"
        map.put("126", "Cornell International Institute for Food, Agriculture, and Development (CIIFAD)");//    "CORNELL INTERNTNL INST FOR FOOD, AGRICULTURE & DEV"
        map.put("127", "International Programs (IP%CALS)"); //  "INTERNATIONAL PROGRAMS"
        map.put("129", "Applied Economics and Management (AEM)"); //    "APPLIED ECONOMICS AND MANAGEMENT"
        map.put("130", "Biological and Environmental Engineering (BEE)"); //    "BIOLOGICAL AND ENVIRONMENTAL ENGINEERING"
        map.put("131", "Earth and Atmospheric Sciences (EAS)"); //  "EARTH AND ATMOSPHERIC SCIENCES (S)"
        map.put("132", "Crop and Soil Science (CSS)"); //   "CROP AND SOIL SCIENCES"
        map.put("133", "Animal Sciences (AN SC)"); //   "ANIMAL SCIENCE"
        map.put("134", "Communication (COMM)"); //  "COMMUNICATION"
        //      map.put("137", "Education (EDUC)"); //  "EDUCATION"
        map.put("139", "Entomology (ENTOM)"); //    "ENTOMOLOGY (S)"
        map.put("142", "Food Science (FOOD)"); //   "FOOD SCIENCE (S)"
        map.put("143", "Horticulture (HORT)"); //   "HORTICULTURE"
        map.put("144", "Landscape Architecture (LA)");  // "LANDSCAPE ARCHITECTURE (S)"
        map.put("145", "Natural Resources (NTRES)"); // "NATURAL RESOURCES"
        map.put("146", "Plant Breeding and Genetics (PB&G)"); //    "PLANT BREEDING"
        map.put("147", "Biological Statistics and Computational Biology (BSCB)"); //    "BIOMETRICS"
        map.put("148", "Plant Pathology (PL PA)"); //   "PLANT PATHOLOGY (S)"
        map.put("149", "Development Sociology (D SOC%"); // "DEVELOPMENT SOCIOLOGY"
        map.put("151", "Molecular Biology and Genetics (MBG)"); //  "MOLECULAR BIO AND GENETICS (S)"
        map.put("152", "Cornell Laboratory of Ornithology (CLO)"); //   "ORNITHOLOGY"
        map.put("154", "International Agriculture and Rural Development"); //   "COMMUNITY AND RURAL DEVELOPMENT INST"
        map.put("155", "Center for the Environment (CFE)"); //  "CENTER FOR THE ENVIRONMENT (S)"
        map.put("156", "Institute for Biotechnology and Life Science Technologies"); // "INST FOR BIOTECHNOLOGY & LIFE SCIENCE TECH  (S)"
        map.put("160", "Shoals Marine Laboratory (SML)"); //    "SHOALS MARINE LAB"
        map.put("161", "Ecology and Evolutionary Biology (EEB)"); //    "ECOLOGY AND EVOLUTIONARY BIOLOGY (S)"
        map.put("162", "Plant Biology (BIOPL)"); // "PLANT BIOLOGY (S)"
        map.put("166", "Microbiology (BIOMI)"); //  "MICROBIOLOGY"
        map.put("167", "Neurobiology and Behavior (BIO NB)"); //    "NEUROBIOLOGY & BEHAVIOR (S)"
        map.put("169", "Division of Nutritional Sciences (DNS)"); //    "NUTRITIONAL SCIENCES AG"
        map.put("216", "Cornell University Agricultural Experiment Station%"); //   "GENEVA ADMINISTRATION"
        map.put("218", "Entomology at Geneva"); //  "ENTOMOLOGY-GENEVA"
        map.put("220", "Food Science & Technology at Geneva"); //   "FOOD SCIENCE-GENEVA"
        map.put("221", "Plant Pathology at Geneva"); // "PLANT PATHOLOGY-GENEVA"
        map.put("224", "Horticultural Sciences at Geneva"); //  "HORTICULTURAL SCIENCES-GENEVA"
        map.put("302", "Mann Library"); //  "MANN LIBRARY AG"
        map.put("562", "Biological Statistics and Computational Biology (BSCB)"); //    "BIOLOGICAL STATISTICS & COMPUTATIONAL BIOLOGY"
        map.put("79", "Mann Library");
        map.put("76", "Mann Library");
        map.put("52", "Computer Science (COM%");
        //map.put("110", "");//inst social and economic research, not in vivo
        //map.put("112", "Nanobiotech Center"); //not in vivo.
        map.put("141", "Horticulture (HORT)");
        map.put("174", "Human Development (HD)");
        map.put("175", "Policy Analysis and Management (PAM)");
        map.put("177", "Policy Analysis and Management (PAM)");
        map.put("178", "Policy Analysis and Management (PAM)");
        map.put("227", "Boyce Thompson Institute for Plant Research (BTI)");

//        All of these osp departments get mapped to the vivo entity "Cornell Universiy Veterinary College"
//        184 VET ADMINISTRATION
//        202 VET MEDICAL TEACHING HOSPITAL
//        304 FLOWER-SPRECHER VETERINARY LIBRARY
//        362 VETERINARY FINANCIAL SERVICES
//        762 VET FACILITIES AND SERVICES
        map.put("184", "Cornell University Veterinary College"); 
        map.put("202", "Cornell University Veterinary College");
        map.put("304", "Cornell University Veterinary College");
        map.put("362", "Cornell University Veterinary College");
        map.put("762", "Cornell University Veterinary College");

        return map;
    }
    static Connection global_vivoCon=null;
    /**
     * Create the connections to the db's using the values from the properties file.
     * The connections that get set are global_vivoCon and global_localCon.
     */
    public static void makeConnections()
        throws SQLException, IOException {
        Properties props = new Properties();
        String fileName = "vivo2_jdbc.properties";
        FileInputStream in = new FileInputStream(fileName);
        props.load(in);
        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null) {
            System.setProperty("jdbc.drivers", drivers);

            String url = props.getProperty("vivo.url");
            String username = props.getProperty("vivo.username");
            String password = props.getProperty("vivo.password");
            System.out.println("vivo database: " + url);
            global_vivoCon = DriverManager.getConnection( url, username, password );

        }
    }
   public static final String escapeForSql(String strIn){
        if( strIn != null ){
        	String res = strIn.replaceAll("'", "''");
        	//backslash is escaped for java string, then for regexp, wow.
            return res.replaceAll("\\\\", "\\\\\\\\"); //<-- repalces a backslash with two bkslashes
        } else
            return null;
    }
}
