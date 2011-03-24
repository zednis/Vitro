import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import org.apache.commons.dbcp.BasicDataSource;
import org.joda.time.DateTime;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 Tools to load grant award data from a local db into a VIVO database.

 The bulk of the work is done in importLocalOsp2Vivo.  This iterates
 on all awards in osplocal.award_proposal.  For each of these awards
 it adds the award, associates the award to the sponsor, associates
 the award to the department, adds the sponsors and adds the
 investigators of the award.

 If the department that the award is associated with is not found in
 vivo.externalids a warning message is printed to stdout.

 @author $Author: Brian Caruso bdc34.cornell.edu$
 @verison $Rev: 69 $
 $Date: 2005-02-09 18:56:23 -0500 (Wed, 09 Feb 2005) $

 **** Change Log ****
2006-09-06 removed logging, updating for db schema changes, removed changeGroup code
2005-10-24 added better error messages
2005-04-06 refactoring, moved string and db methods to StringUtils and VitroDbUtils
2005-02-10 adding ability to drop all investigator relations and re-do them.
2005-02-08 added some comments,cleaned out unused code
2005-02-07 changed citation for awards, added NIH to getvivoEntityIdtoOspSponsor(),moved dept stuff to
  PopulateExtDeptIds.java, changed the case of the table names. OSP Award ids are now tracked on the externalIds
  table.
2005-01-27 bdc34 changed populateWithOSPDeptIds() and populateLinkTableWithOSPDeptIds() to use the
  EXTERNALIDS and EXTERALIDTYPES tables.  The matching of investigators and entities still uses the
  NetId's from the LINKS table.
  Added code to handle sponsors, sponsor2sponsor relations and sponsor2project relations.<p>
2005-01-28 bdc34 Fixed code to handle sponsor2sponsor relations to remove duplicates. Added relateEntity2Enity().
2005-02-01 bdc34 Added support to LocalOsp2Vivo.java for portal flags:
  Awards get vivo + four CALS for portal, location of parent dept and college of CALS; this is set in doDept()
  Sponsors get vivo + four CALS for portal, location of non-cornell and college of null; this is set in addSponsors()
*/

class LocalOsp2Vivo {
    private HashMap missingNetIds;
    private HashSet missingDepartments;
    private HashSet <Integer> encounteredSponsors; //osp sponsor ids

    private ObjectPropertyStatementDao ents2entsDao = null ;
    private IndividualDao individualDao = null;
    private DataPropertyStatementDao dataPropertyStatementDao = null;

    private final String now = (new DateTime()).toString();
    //Connection vivoCon=null;
    Connection localCon=null;
    DataSource localOspDs = null;
    JdbcTemplate ospTemplate = null;
    private  String vivoSponsorLinkType="";


    public LocalOsp2Vivo(String configFileName ) throws SQLException, IOException{
        makeConnections(configFileName );
        //vivoSponsorLinkType = populateLinkTableWithOspSponsorIds();
        //PopulateExtDeptIds.populateWithOSPDeptIds( vivoCon );
    }

    /**
     * Create the connections to the db's using the values from the properties file.
     * The connections that get set are global_vivoCon and global_localCon.
     */
    private final void makeConnections(String fileName)
        throws SQLException, IOException {
        /* checked for db schema changes, ok */
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(fileName);
        props.load(in);
        in.close();
        String drivers = props.getProperty("jdbc.drivers");

        if (drivers != null) {
            System.setProperty("jdbc.drivers", drivers);

            //setup osp local connection
            String url = props.getProperty("localosp.url");
            String username = props.getProperty("localosp.username");
            String password = props.getProperty("localosp.password");
            System.out.println("local database: " + url);
            localCon = DriverManager.getConnection( url, username, password );

            BasicDataSource bds = new BasicDataSource();
            bds.setDriverClassName( drivers );
            bds.setUrl(url);
            bds.setUsername(username);
            bds.setPassword(password);
            localOspDs = bds;

            ospTemplate = new JdbcTemplate( localOspDs );

            //setup vitro connection
            VitroConnection.establishDataSourceFromProperties(fileName);
            vivoCon = VitroConnection.getConnection();

            System.out.println("Using remote:\t" + vivoCon.getMetaData().getURL() + '\n' +
                               "       local:\t" + localCon.getMetaData().getURL());

            //set up the dao objects
            ents2entsDao = new Ents2EntsDaoDb();
            ents2entsDao.setDataSource(VitroConnection.getDataSource(false));
            individualDao = new EntityDaoDb();
            individualDao.setDataSource(VitroConnection.getDataSource(false));
        }else{
            System.out.println("There are no jdbc.drivers specified in the properties file " + fileName);
        }
    }

    /**
     * Import all awards from the local OSP site at localCon into the
     * VIVO system at vivoCon.  If an osp award's department is not
     * found in the vivo.externalid table then the award is skipped.
     */
    void importLocalOsp2Vivo( ){
        missingDepartments = new HashSet();
        missingNetIds = new HashMap();
        encounteredSponsors = new HashSet<Integer>();

        /* checked for db schema changes */
        String awardQuery = "SELECT * FROM AWARD_PROPOSAL;";
        int awardCount = 0, invCount=0;
        Statement localStmt = null;
        ResultSet awardsRs = null;
        try{
            try{
                localStmt = localCon.createStatement();
            }catch (SQLException ex){
                System.out.println("error making local statement: " + ex);
            }
            try{
                awardsRs = localStmt.executeQuery(setLast(awardQuery));
            } catch (SQLException ex){
                System.out.println("error getting local awards: " + ex +"\n" + lastSql);
            }

            try{
                while(awardsRs.next()){
                    String ospProjId = awardsRs.getString("AWARD_PROP_PROJECT_ID");
                    String ospDeptId = awardsRs.getString("AWARD_PROP_DEPARTMENT_ID");
                    boolean deptExists = departmentExists( ospDeptId);

                    String vivoId = doAward( awardsRs );
                    if(vivoId != null) {
                        int newid = addAwardExternalId(vivoId, ospProjId);
                        awardCount++;

                        if( deptExists )
                            doDept(vivoId, ospDeptId);
                        else
                            badDeptMessage(awardsRs);

                        doSponsors(ospProjId, vivoId,
                                   awardsRs.getString("AWARD_PROP_SPONSOR_ID"),
                                   awardsRs.getString("FEDERAL_FLOW_THROUGH"),
                                   awardsRs.getString("FLOW_THROUGH_SPONSOR_ID"));

                        invCount = invCount +
                        doInvestigators(ospProjId, vivoId, localCon);

                        setFlagsForAward( vivoId );
                    }
                }
                setFlagsForSponsors( encounteredSponsors );
            }catch(SQLException ex){
                System.out.println("error in getting award row: " + ex
                                   +"\n" + lastSql);
            }
        }catch(Throwable ex){
            System.out.println("error in importLocalOsp2Vivo: " + ex);
        }

        System.out.println("Awards added: " + awardCount);
        System.out.println("Investigators added: " + invCount);
        printMissingDept();
        printMissingNetIds();
    }

    /**
     *  Take the values from a row off of localosp.award_proposal and sticks them into
     *  a vivo entity.  If the title of the award is longer than 255 chars then it is
     *  cut short with limitField().
     *
     *  @return Successful insert: VIVO ENTITIES.id of the newly added entity row,
     *          Duplicate or could not insert: null.
     */
    String doAward(ResultSet awardsRs ) throws SQLException{
        String ret = null;
        Individual newEnt = new IndividualImpl();

        if(! isDuplicateAward( awardsRs.getInt("AWARD_PROP_PROJECT_ID"))){
            String fullTitle= awardsRs.getString("AWARD_PROP_FULL_TITLE");
            if( fullTitle == null ){
                System.out.println("Award " + awardsRs.getInt("AWARD_PROP_PROJECT_ID") + " had no title" );
                return null;
            }

            String cutTitle = limitField(fullTitle, 225);
            newEnt.setName( cutTitle );
            boolean cut = ( fullTitle.length() != cutTitle.length() );
            if( cut )
                newEnt.setDescription( fullTitle );

            String ospId = awardsRs.getString("AWARD_PROP_PROJECT_ID");
            newEnt.setVClassURI( vivoGrantAwardVClass );
            newEnt.setMoniker( vivoGrantMoniker );
            newEnt.setCitation( getAwardCitation(ospId));

            ret = individualDao.insertNewIndividual( newEnt );
            System.out.println("Added osp grant " + ospId + " as Vitro entityURI " + ret);

            if(cut){
                System.out.println("Title field for award with vivo entity id " + ret
                                   + " too long, shortened.");
                System.out.println("\tuncut:" + fullTitle + "\n" +
                                   "\t  cut:" + cutTitle );
            }
        }
        return ret;
    }

    /**
     * Attempts to break the string on a space.
     * @param in string to limit
     * @param maxLength  a hard limit to how many chars the string can be.
     * @return
     */
    private final String limitField(String in, int maxLength) {
        if( in == null ) return null;
        if( in.length() <= maxLength ) return in;

        int tenPercent = (int)Math.round( maxLength * .01 );
        int positionOfColon = in.lastIndexOf(':',maxLength);
        if( positionOfColon != -1 && positionOfColon > (3*tenPercent) )
            return in.substring(0,positionOfColon);

        int positionOfSpace = in.lastIndexOf(' ', maxLength);
        if( positionOfSpace != -1 && positionOfSpace > tenPercent ){
            return in.substring(0,positionOfSpace);
        }
        return in.substring(0, maxLength);
    }

    /**
     * Associates the entity, vivoId, to the department with the OSP
     * depId.  Also this sets the location portal flag of vivId to the
     * same as the department.
     *
     * @param vivoId entities.id of an award.
     * @param ospDeptId osp department id of a department to associate with the award.
     * @return true if successfully found a the department, false otherwise
     */
    void doDept(String awardUri, String ospDeptId){
        /* checked for db schema changes, OK */

        //get row from link table that indicates which entity is associated with a given OSPdeptId
        try{

            String vivoDeptEntityId = getEntityIdFromOspDeptId( ospDeptId);
            if(vivoDeptEntityId != null){
                if( relateEntity2Entity(vivoDeptEntityId,awardUri,dept2AwardPropId, ents2entsDao)==-1)
                    System.out.println("Unable to relate the Award with a department because there is no relation between entity "+
                                       vivoDeptEntityId+" and "+awardUri+" with property "+dept2AwardPropId);
//
//                Statement stmt = vivoCon.createStatement();
//                try{
//                    ResultSet deptRs=stmt.executeQuery("SELECT FLAG3SET+0 AS 'FLAG3SET' FROM entities WHERE ID="+vivoDeptEntityId);
//                    if(deptRs.next()){
//                        String flag = deptRs.getString("FLAG3SET");
//                        if(!deptRs.wasNull()){
//                            stmt.executeUpdate("UPDATE entities SET flag3set="+flag+" WHERE id="+vivoId);
//                        }
//                    } else
//                        System.out.println("Unable to set location flag on entity " + vivoId + " because department " +
//                                           vivoDeptEntityId + " lacked location flag");
//                    close(deptRs);
//                }catch (SQLException ex){
//                    System.out.println("Unable to set location flag on entity " + vivoId +ex);
//                }
            } else {
                //Here we could look at the investigators and associate the award with the PI's dept.
                //System.out.println("Could not find department with OSP dept id of " + ospDeptId);
                missingDepartments.add(ospDeptId);
            }
        } catch (SQLException ex){
            System.out.println("Error while trying to associate a department with an award:\n" +
                               ex + "\n" + lastSql);
        }
    }

    /**
     * Associate a vivo project entity and a sponsor.  If that sponsor has no
     * entity in vivo, add a sponsor entity to Vivo.  If the subsponsors
     * have no entities add them too.
     *
     * There are some problems as there is no way to easily match
     * the sponsor to an entity.  We will start with a set of hardcoded rows
     * to add to the links table to associate the existing sponsor entities
     * and then use the ChangeGroup class to queue up changes that can be
     * spruced up with urls and descriptions.
     *
     * @param projectId - project id value from AWARD_PROPOSAL.AWARD_PROP_PROJECT_ID,
     *  must be able to be parsed to a int.
     * @param awardVivoUri - Id from Vivo entities.ID, must be able to parse to int.
     */
    void doSponsors(String projectId,
                    String awardVivoUri,
                    String ospSponsorId,
                    String federalFlowThrough,
                    String ospFlowThroughId )
        throws SQLException{
        if( projectId == null || awardVivoUri == null || ospSponsorId == null )
            return;
        int sponsorId = Integer.parseInt( ospSponsorId );

        boolean flowThrough = ospFlowThroughId != null && ospFlowThroughId.trim().length() > 0
            && ospFlowThroughId.matches("[0-9]+?");

        int flowThroughId = -1;
        if( flowThrough ){
            try{ flowThroughId = Integer.parseInt(ospFlowThroughId);}
            catch( Throwable t){ flowThrough = false; }
        }

        if( flowThrough && flowThroughId != -1 ){
            String newVivoUri = addSponsor( flowThroughId );
            relateSponsor2Project( newVivoUri , awardVivoUri );
            encounteredSponsors.add( flowThroughId );

            newVivoUri = addSponsor( sponsorId );
            relateSubContract2Project( newVivoUri, awardVivoUri);
            encounteredSponsors.add( sponsorId );

        } else {
            String newVivoId = addSponsor( sponsorId );
            relateSponsor2Project( newVivoId, awardVivoUri );
            encounteredSponsors.add( sponsorId );
        }
    }

    void addFunder(int projectId, int vivoId, int ospSponsorId, Connection ospCon, Connection vivoCon){

    }

    /**
     * Adds a sponsor entity to vivo built from an osp sponsor_T row.
     * @param sponsorId - OSP sponsor Id.
     * @return vivo.entities.id for the new vivo entity, null if no entity created.
     * or the vivo entity id of the entity that is already in the db.
     */
    private String addSponsor( int sponsorId )throws SQLException{
        String existingId = sponsorHasEntity(sponsorId);
        if( existingId != null ){
            return existingId;
        }

        String query="SELECT " +
            "SPONSOR_ID, SPONSOR_NAME, SPONSOR_ABBREVIATION, SPONSOR_FUND_SOURCE, SPONSOR_WEB_SITE, "+
            "SPONSOR_LEVEL_1, SPONSOR_LEVEL_2, SPONSOR_LEVEL_3 "+
            "FROM  SPONSOR_T WHERE SPONSOR_ID = "+ sponsorId;
        Map<String,String> colMap = null;
        try{
            colMap = (Map<String,String>)ospTemplate.queryForMap( setLast(query) );
        }catch(IncorrectResultSizeDataAccessException e){
            System.out.println("could not find sponsor in local osp data: " + sponsorId );
            return null; //couldn't find sponsor
        }
        if (colMap == null ) return null;

        //make a new entity for the sponsor
        Individual ent = new IndividualImpl();
        ent.setName( colMap.get("SPONSOR_NAME") );
        ent.setMoniker(  vivoSponsorMoniker );
        ent.setVClassURI( isGovernment( colMap.get("SPONSOR_FUND_SOURCE") ) ? vivoGovVClass : vivoNonGovVClass  );
        ent.setCitation( "Initially added with OSPWH data, sponsor_id: " + sponsorId );

        String url=colMap.get("SPONSOR_WEB_SITE");
        if( url != null && url.trim().length() > 0 && url.indexOf("http") > 0){
            ent.setUrl( url.trim() );
            ent.setAnchor( colMap.get("SPONSOR_ABBREVIATION")+" web site" );
        }

        //flags for sponsors?

        String vivoEntityUri = individualDao.insertNewIndividual( ent );

        addSponsorLink(""+vivoEntityUri, ""+sponsorId);
        addSponsorHierarchy( sponsorId, vivoEntityUri,
                             colMap.get("SPONSOR_LEVEL_1"),
                             colMap.get("SPONSOR_LEVEL_2"),
                             colMap.get("SPONSOR_LEVEL_3"));
        return vivoEntityUri;
    }

    /**
     * Adds all of the subsponsors of a sponsor.
     * @param level1 - value from SPONSOR_LEVEL_1, set to null for no level X sponsor
     */
    private void addSponsorHierarchy(int ospSponsorId, String vivoUri,
                                     String level1, String level2, String  level3  )
        throws SQLException{
        String subsponsorQuery="SELECT SPONSOR_ID FROM SPONSOR_T WHERE SPONSOR_ABBREVIATION=";
        StringBuffer query= new StringBuffer();
        int level = -1;

        if(level3==null){
            if(level2==null){
                //has null in level_2 & level_3 - this is a top level sponsor - nothing to do.
                return;
            }else{
                // null in only level_3, this is 2nd level sponsor, add sponsor with the 1st Level abbreviation
                query.append( subsponsorQuery ).append( quote(level1) );
                level = 2;
            }
        }else{ //no nulls - this is a 3rd level sponsor - add sponsor with the 2nd level abbreviation
            query.append(subsponsorQuery).append(quote(level2));
            level = 3;
        }

        int subOspId = -1;
        try{
            subOspId = ospTemplate.queryForInt(setLast(query.toString()) );
        } catch(IncorrectResultSizeDataAccessException ex){}
        if( subOspId > 0){
            String levelXVivoId = addSponsor(subOspId);
            relateSponsor2Sponsor(levelXVivoId, vivoUri + "");
        }else{
            System.out.println( "Level " + level +" sponsor "+ ospSponsorId  +" has level " +
                                (level-1) + " sponsor but it was not found. This is an anomoly in the OSPWH Data. " +
                                "Import preceding normally.");
        }
    }

    private void relateSubContract2Project( String vivoSubcontId, String vivoProjectId )
        throws SQLException{
        relateEntity2Entity( vivoSubcontId, vivoProjectId, subcontract2ProjectPropId,ents2entsDao);
    }

    /** Creates a relation in vivo between a supersponsor and a subsponsor.
     * @param superSponId - vivo entity id of the super sponsor.
     * @param subSponId - vivo entity id of the sub sponsor.
     * @param vivoCon - live vivo connection.
     */
    private void relateSponsor2Sponsor(String superSponId, String subSponId )
        throws SQLException{
        relateEntity2Entity(superSponId,subSponId,sponsor2sponsorPropId,ents2entsDao);
    }

    /** Creates a relation in vivo between a sponsor of a project and that project.
     *  @param vivoSponsorId -vivo entity id fo the sponsor.
     *  @param vivoProjectId -vivo entity id of the project.
     *  @pram vivoCon - a live vivo connection.
     */
    private void relateSponsor2Project(String vivoSponsorId, String vivoProjectId )
        throws SQLException{
        relateEntity2Entity(vivoSponsorId,vivoProjectId,sponsor2ProjectPropId,ents2entsDao);
    }


    /**
     * @returns true if the string describes a government orginization.
     */
    private final boolean isGovernment(String source){
        /* checked for db schema changes, OK */
        boolean gov = true;
        if("CORPORATION".equals(source)) gov=false;
        if("FEDERAL GOVERNMENT".equals(source)) gov=true;
        if("OTHER STATES/LOCAL".equals(source)) gov=true;
        if("FOUNDATION".equals(source)) gov=false;
        if("NON-PROFIT ORG.".equals(source)) gov=false;
        if("UNIVERSITY".equals(source)) gov=false;
        if("NEW YORK STATE".equals(source)) gov=true;
        return gov;
    }

    /** Checks to see if the sponsor has already been added.
        @returns null if no entity is found, the vivo entity id otherwise
    */
    private final String sponsorHasEntity(int  ospSponsorId ) throws SQLException{
//        /* checked for db schema changes, OK */
//        String query="SELECT entityId FROM externalids WHERE VALUE='"+ospSponsorId+"' AND EXTERNALIDTYPE="+vivoSponsorLinkType;
//        String rtv = null;
//        try{
//            rtv = (String)VitroConnection.getJdbcTemplate().queryForObject(query,String.class);
//        }catch(IncorrectResultSizeDataAccessException ex){
//            return null;
//        }
//        return rtv;
        Individual ind =
                individualDao.getIndividualByExternalId(OSP_SPONSOR_EXTERNAL_ID_PROP_URI, Integer.toString(ospSponsorId));
        if( ind != null )
            return ind.getURI();
        else
            return null;
    }

    private final String ospSponsorId2Entity(int ospSponsorId ) throws SQLException{
        Individual ind =
                individualDao.getIndividualByExternalId(OSP_SPONSOR_EXTERNAL_ID_PROP_URI, Integer.toString(ospSponsorId));
        if( ind != null )
            return ind.getURI();
        else
            return null;
    }

    /**
     * Create associations between the vivo entity vivoId and the
     * investigators of the OSPWH project with the id ospProjectId.
     * OSPWH investigators are mapped to VIVO entites by matching the
     * OSPWH.INVESTIGATORS_T.NETID to the VIVO.Links.url, which should
     * be a net id.
     *
     * @param ospProjectId OSPWH award project id such as
     * OSPWH.AWARD_PROPOSAL.AWARD_PROP_PROJECT_ID
     * @param vivoId VIVO entity id to associate the investigators with.
     * @param ospCon connection to osp db
     * @param vivoCon connetion to vivo db
     */
    @SuppressWarnings("unchecked")
    private final int doInvestigators(String ospProjectId, String vivoProjId,
                                      Connection ospCon)
        throws SQLException {
        //get all investigators for this osp award
        String query = "SELECT INVPROJ_INVESTIGATOR_NETID, ROLE_C, " +
            "INVPROJ_FIRST_NAME AS FIRST, INVPROJ_MIDDLE_NAME AS MIDDLE , INVPROJ_LAST_NAME AS LAST " +
            "FROM INVESTIGATORS_T IT, PROJECT_MEMBERS_T PM WHERE " +
            "PM.PROJECT_ID_C = " + ospProjectId + " AND PM.INVESTIGATOR_ID_C = IT.INVPROJ_INVESTIGATOR_ID";
        Statement ospStmt = ospCon.createStatement();
        ResultSet rs = ospStmt.executeQuery(setLast(query));
        int count=0;

        while(rs.next()){
            String  netid = rs.getString("INVPROJ_INVESTIGATOR_NETID"), entityId=null;
            String role = rs.getString("ROLE_C");

            //get VIVO entity id linked to this netID
//            String linkQuery = "SELECT ENTITYID FROM externalids WHERE EXTERNALIDTYPE="+vivoExtIdNetIdType+
//                " AND VALUE LIKE '"+netid+"%'"; //the netId have '@cornell.edu' on them?
            Individual ent= individualDao.getIndividualByExternalId(vivoExtIdNetIdType, netid);

            //ResultSet entRs = vivoStmt.executeQuery(setLast(linkQuery));
            if(ent != null) {
                entityId = ent.getURI();
            }else{
                //matchInvestigator2Entity(rs.getString("FIRST"),rs.getString("MIDDLE"),rs.getString("LAST"),vivoCon);
                String key=rs.getString("LAST")+"\t\t"+rs.getString("FIRST")+"\t"+rs.getString("MIDDLE")+"\t\t"+netid;
                if(missingNetIds.containsKey(key)){ //keep track of investigators we can't find
                    ArrayList projectList=(ArrayList)missingNetIds.get(key);
                    missingNetIds.remove(key);
                    projectList.add( vivoProjId+"" );
                    missingNetIds.put(key, projectList);
                }else{
                    ArrayList projectList=new ArrayList(2);
                    projectList.add(vivoProjId+"");
                    missingNetIds.put(key,projectList);
                }
                continue;
            }
            if(isDuplicateInvestigator(entityId, vivoProjId, role )){
                continue;
            }
            relateEntity2Entity(entityId,vivoProjId,("PI".equals(role)?piInvPropId:coInvPropId),ents2entsDao);
            count++;
        }
        return count;
    }

    private final  int relateEntity2Entity(String domainUri, String rangeUri, String propertyUri, ObjectPropertyStatementDao dao){
        if( domainUri == null || rangeUri == null || propertyUri == null || dao == null ) return -1;
        ObjectPropertyStatement ent2ent = new ObjectPropertyStatement();
        ent2ent.setSubjectURI(domainUri);
        ent2ent.setObjectURI(rangeUri);
        ent2ent.setPropertyURI(propertyUri);
        return dao.insertNewObjectPropertyStatement(ent2ent);
    }

    /**
     * Sets the flags of the award to be the OR'ed value of all
     * pis copis and depts
     */
    private final void  setFlagsForAward( String vivoAwardUri ) throws SQLException{
        String [] propIds = {piInvPropId,
                             coInvPropId,
                             dept2AwardPropId};
        individualDao.setFlagsForIndividualFromRelatedIndividuals(vivoAwardUri,propIds,false,true);
    }

    private final void setFlagsForSponsors( HashSet <Integer> encounteredSponsors  )
    throws SQLException{
        Iterator <Integer>it = encounteredSponsors.iterator();
        while(it.hasNext()){
            Integer val =  it.next();
            String vivoIdForSponsor = null;
            try{
                vivoIdForSponsor = ospSponsorId2Entity( val.intValue() );
            }
            catch(Exception  ex){ }
            String [] propIds = {sponsor2ProjectPropId,
                                 subcontract2ProjectPropId };
            individualDao.setFlagsForIndividualFromRelatedIndividuals( vivoIdForSponsor, propIds, true,true);
        }
    }

    /**
     *  Check to see if any VIVO.entities record has a citation that is the same as the one provided.
     *  @param projId value from osplocal.award_prop_project_id
     */
    private final  boolean isDuplicateAward(int projId) throws SQLException{
        return getAwardEntityId(projId)!=null;
    }

    /**
     * @return true if there is a row on the vivo.externalids table
     * with entityid, false otherwise.
     */
//     private final  boolean isDuplicateDeptId(String entityid, Connection vivoCon) throws SQLException{
//         /* checked for db schema changes, OK  */
//         String query="SELECT ID FROM externalids WHERE ENTITYID=" + entityid + " AND " +
//             "EXTERNALIDTYPE=" + vivoDeptLinkId ;
//         return VitroDbUtils.hasRows(vivoCon,  query );
//     }

    /**
     * check in VIVO for an association between a given investigator and award/project.
     * @param investigatorURI VIVO.entities.id of investigator
     * @param projectURI VIVO.entities.id of award
     * @param role must be "PI" for primary investigator or "CO" for co investigator
     * @return true if there is an association between the sponsor and award with the given role.
     */
    private final  boolean isDuplicateInvestigator(String investigatorURI, String projectURI, String role)
        throws SQLException{
        String propertyId = "PI".equals(role) ? piInvPropId : coInvPropId;
        Individual ind = individualDao.getIndividualByURI(investigatorURI)    ;
        ents2entsDao.fillExistingObjectPropertyStatements(ind);
        List<ObjectPropertyStatement> stmts = ind.getObjectPropertyStatements();
        for(ObjectPropertyStatement stmt : stmts){
            if( propertyId.equals(stmt.getPropertyURI()) && projectURI.equals(stmt.getObjectURI()))
              return true;
        }
        return false;
    }

    /**
     * check to see if this osp department exists in vivo
     */
    private final  boolean departmentExists(String ospDeptId )throws SQLException{
        /* checked for db schema changes, OK */
        return null != getEntityIdFromOspDeptId( ospDeptId);
    }


    private String getEntityIdFromOspDeptId( String ospDeptId)throws SQLException{
        return individualDao.getIndividualByExternalId(OSP_DEPARTMENT_EXTERNAL_ID_URI, ospDeptId);
    }

    public final void close(){
        close(localCon);
    }

    /** escape and quote this string */
    private final String quote(String in){ return VitroDbUtils.quoteChar(in) ; }

    /** prints error message when no department is found in vitro for a grant */
    private final  void badDeptMessage(ResultSet rs) throws SQLException{
        /* checked for db schema changes, OK */
        if(rs != null )
            System.out.println("osp award "
                               + rs.getString("AWARD_PROP_PROJECT_ID")
                               + ": no Vitro dept has externalId of "
                               + rs.getString("AWARD_PROP_DEPARTMENT_ID")
                               + ", adding anyway.");
    }

    private final  String badDeptDesc(ResultSet awardRs) throws SQLException{
        /* checked for db schema changes , OK */
        Statement stmt = localCon.createStatement();

        String deptId = awardRs.getString("AWARD_PROP_DEPARTMENT_ID");
        ResultSet deptRs = stmt.executeQuery("SELECT * FROM DEPARTMENT_T WHERE DEPARTMENT_ID = "+deptId );
        String dept = null, college = null;
        if( deptRs.next() ){
            dept = deptRs.getString("DEPARTMENT_NAME");
            college = deptRs.getString("DEPARTMENT_COLLEGE_NAME");
        }
        if(dept == null || "null".equalsIgnoreCase(dept)) dept = "unknown";
        if(college == null || "null".equalsIgnoreCase(college)) college = "unknown";

        return "This award is administered by a department that is not in this system: "
            + dept + " in college " + college + ".";
    }

    private final void printMissingDept(){
        Iterator it = missingDepartments.iterator();
        while(it.hasNext())
            System.out.println("Missing Dept osp id: " + (String)it.next());
    }

    private final void printMissingNetIds(){
        Iterator it = missingNetIds.keySet().iterator();
        if(missingNetIds.size() > 0 ){
            System.out.println("Invistigators from OSP who's netids are not found in vitro:");
            System.out.println("last\t\tfirst\tmiddle\t\tnetid\tentityId of grant");
        }
        while(it.hasNext()){
            String netid= (String)it.next();
            ArrayList projects=(ArrayList)missingNetIds.get(netid);
            System.out.print(netid+"\t");
            Iterator pIt=projects.iterator();
            while(pIt.hasNext()){
                System.out.print(" "+(String)pIt.next());
            }
            System.out.print("\n");
        }
    }

    /** return string to use for entity citation. this uses the static String now */
    private final String getAwardCitation(String projectId){
        return VitroDbUtils.escapeForSql("From Cornell OSP data warehouse (project id:"+projectId+")"+" on "+ now);
    }

    /**
     *  Use this to store current sql query.  On an errror this will be displayed.
     */
    private final String setLast(String sql){ return lastSql=sql; }
    public String lastSql = null;

    /**
       Adds the list of sponsors names we already have to the
       ExternalIds table.  This permits sponsors which are already
       entities in vivo to be associated with osp sponsor id's.  The
       mapping of these vivo entities to osp sponsors is from
       getvivoEntityIdToOspSponsor().
    */
    private final  String populateLinkTableWithOspSponsorIds()throws SQLException{
        /* checked for db schema changes , OK */
        String type_name ="'OSP sponsor id'";
        Statement stmt = vivoCon.createStatement();
        ResultSet rs = null;
        //make sure that there is a department id link type and make one if there is none
        rs = stmt.executeQuery(setLast("SELECT ID FROM externalidtypes WHERE TYPE ="+type_name));
        if(!rs.next()){
            rs = stmt.executeQuery(setLast("SELECT MAX(ID)+1 AS ID FROM externalidtypes"));
            rs.next();
            stmt.executeUpdate("INSERT INTO externalidtypes (ID, TYPE, GENERIC, CLASSGROUPID) VALUES ("+
                               rs.getString("ID")+", "+type_name+", "+type_name+", 4)");
            rs = stmt.executeQuery(setLast("SELECT ID FROM externalidtypes WHERE TYPE="+type_name));
            rs.next();
        }
        vivoSponsorLinkType = rs.getString("ID");

        Iterator it = getvivoEntityIdtoOspSponsor().entrySet().iterator();
        while(it.hasNext()){
            Map.Entry ent = (Map.Entry)it.next();
            String vivoEntityId = (String)ent.getKey();
            String ospSponsorId = (String)ent.getValue();
            addSponsorLink(vivoEntityId, ospSponsorId);
        }
        close(rs);
        close(stmt);
        return vivoSponsorLinkType;
    }

    /**
     *  @return true if added a link, false otherwise
     */
    private final  boolean addSponsorLink(String vivoEntityId, String ospSponsorId )throws SQLException{
        Individual sponsor = individualDao.getIndividualByExternalId(OSP_SPONSOR_EXTERNAL_ID_PROP_URI,ospSponsorId);
        if( sponsor != null )
            return false;

        DataPropertyStatement dps = new DataPropertyStatement();
        dps.setIndividualURI(vivoEntityId);
        dps.setDatapropURI(OSP_SPONSOR_EXTERNAL_ID_PROP_URI);
        dps.setData(ospSponsorId);
        dataPropertyStatementDao.insertNewDataPropertyStatement(dps);
        return true;
    }

    private String awardExternalIdType = null;
    private final  String getAwardExternalIdType(Connection vivoCon)throws SQLException{
        /* checked for db schema changes , OK */
        String osptype="'OSP award id'";
        Statement stmt = vivoCon.createStatement();
        ResultSet rs=stmt.executeQuery("SELECT id FROM externalidtypes WHERE type like "+osptype);
        if(rs.next()){
            return awardExternalIdType = rs.getString("id");
        } else {
            rs=stmt.executeQuery("SELECT MAX(ID)+1 AS ID FROM externalidtypes");
            awardExternalIdType = rs.getString("ID");
            stmt.executeUpdate("INSERT INTO externalidtypes (id,type,generic,classGroupId)VALUES"+
                               "("+awardExternalIdType+","+osptype+",'integer code set',2)");
            return awardExternalIdType;
        }
    }

    /** gets the vivo entity id for a given osp award id.
        @returns the vivo entity id, null if there is none.
    */
    private final  String getAwardEntityId(int ospAwardId)throws SQLException{
        Individual ind = individualDao.getIndividualByExternalId(OSP_AWARD_EXTERNAL_ID_PROP_URI,Integer.toString(ospAwardId));
        if( ind != null )
            return ind.getURI();
        else
            return null;
    }

    private final  int addAwardExternalId(String vivoEntityId, String ospAwardId)throws SQLException{
        /* checked for db schema changes , OK */
        String typeId = OSP_AWARD_EXTERNAL_ID_PROP_URI;
        DataPropertyStatement dps = new DataPropertyStatement();
        dps.setIndividualURI(vivoEntityId);
        dps.setData(ospAwardId);
        dps.setDatapropURI(typeId);
        dataPropertyStatementDao.insertNewDataPropertyStatement(dps);
    }

    /** vivo.entities.id mapped to OSP.sponsor_t.sponsor_id */
    private HashMap vivoEntityIdtoOspSponsor=null;
    @SuppressWarnings("unchecked")
    private final  HashMap getvivoEntityIdtoOspSponsor(){
        /* checked for db schema changes , OK */
        if(vivoEntityIdtoOspSponsor!=null) {return vivoEntityIdtoOspSponsor;}
        HashMap map=new HashMap();
        //sponsors not found here were not in vivo at the time this code was writen, Jan 2005
        //vivo.entity.id    osp.id  vivo.entity.name
        //map.put("336",    );      //European Molecular Biology Laboratory (EMBL)
        //map.put("351",  );        //National Institutes of Health (NIH)
        //map.put("357",  );        //The I.M.A.G.E. Consortium
        //map.put("488",  );        //Alliance for Nanomedical Technologies
        map.put("http://vivo.library.cornell.edu/ns/0.1#individual607",  "2244");        //National Science Foundation (NSF)
        //map.put("890",  );        //Bridging the Rift Foundation
        map.put("http://vivo.library.cornell.edu/ns/0.1#individual5758", "151" );        //International Livestock Research Institute (ILRI)
        //map.put("5802",  );       //Max Planck Institute for Chemical Ecology (Germany)
        map.put("http://vivo.library.cornell.edu/ns/0.1#individual5811",  "1716");       //Institute of Ecosystem Studies (IES)
        //map.put("6092",  );       //Memorial Sloan-Kettering Cancer Center
        //map.put("6153",  );       //The Burke Medical Research Institute
        //map.put("6305",  );       //Natural Resource, Agriculture, and Engineering Service (NRAES)
        //map.put("6532",  );       //International Maize and Wheat Improvement Center (CIMMYT)
        map.put("http://vivo.library.cornell.edu/ns/0.1#individual6549",  "2301");       //New York State Department of Agriculture and Markets
        map.put("http://vivo.library.cornell.edu/ns/0.1#individual6550",  "479");        //United States Department of Defense
        map.put("http://vivo.library.cornell.edu/ns/0.1#individual6603",  "3554");       //National Oceanic and Atmospheric Administration (NOAA)
        //map.put("7095",  );       //Institute for Civil Infrastructure Systems (ICIS)
        //map.put("7178",  );       //Pinhead Institute
        //map.put("7336",  );       //Agricultural Biotechnology Support Project II (ABSPII)
        //map.put("7834",  );       //Global Seminar
        //map.put("7878",  );       //Air Force Office of Scientific Research (AFOSR)
        //map.put("7879",  );       //The Army Research Office
        map.put("http://vivo.library.cornell.edu/ns/0.1#individual7880",  "1284");       //Office of Naval Research
        //map.put("607",  "1284");      //NSF
        //map.put("6550", );           //US DOD
        map.put("http://vivo.library.cornell.edu/ns/0.1#individual351", "476");   // National Institutes of Health (NIH)
        vivoEntityIdtoOspSponsor=map;
        return vivoEntityIdtoOspSponsor;
    }

    /* some constants */
    /*set-flags for projects */
    private final static String PROJECT_PORTALFLAG="534"; //value for entities.setflag1
    private final static String PROJECT_COLLEGEFLAG="4"; //value for entities.setflag2
    private final static String PROJECT_LOCATIONFLAG="2"; //value for entities.setflag3
    /*set-flags for sponsors */
    private final static String SPONSOR_PORTALFLAG="534"; //value for entities.setflag1
    private final static String SPONSOR_COLLEGEFLAG="don't set the college flag to anything"; //value for entities.setflag2
    private final static String SPONSOR_LOCATIONFLAG="16";//value for entities.setflag3

    /* external id table types */
    static final String vivoExtIdNetIdType = "101";
    //  static final String vivoLinkNetIdType = "101";//this is the vivo.linktypes.id for a netid
    static final String OSP_AWARD_EXTERNAL_ID_PROP_URI = "http://Fake.fake.LocalOsp2Vivo.java/osp_award_externalId_URI";
    static final String OSP_SPONSOR_EXTERNAL_ID_PROP_URI = "http://Fake.fake.LocalOsp2Vivo.java/osp_sponsor_external_id_URI";
    static final String OSP_DEPARTMENT_EXTERNAL_ID_URI = "http://Fkake.fake.LocalOsp2Vivo.java/osp_dept_exgteranl_id_URI";

    /* property types, these properties must be defined in vivo */
    private final static String dept2AwardPropId = "http://vivo.library.cornell.edu/ns/0.1#OrganizedEndeavorAdministersFinancialAward"; //property id for dept to award relation.
    private static final String coInvPropId = "http://vivo.library.cornell.edu/ns/0.1#PersonCoInvestigatorOfFinancialAward";
    private static final String piInvPropId = "http://vivo.library.cornell.edu/ns/0.1#PersonPrimaryInvestigatorOfFinancialAward";
    private static final String sponsor2sponsorPropId = "http://vivo.library.cornell.edu/ns/0.1#OrganizedEndeavorProvidesFundingToOrganizedEndeavor";//a property that will relate sponsors to sub sponsors
    private static final String sponsor2ProjectPropId = "http://vivo.library.cornell.edu/ns/0.1#OrganizedEndeavorFundsAward";//will relate sponsors to a project/award
    private static final String subcontract2ProjectPropId = "info:/fake/nsuri/for/Thomson/wosOrganizedEndeavorSubcontractsAward";

    /* vclasses */
    private static final String vivoGovVClass="http://vivo.library.cornell.edu/ns/0.1#GovernmentAgency"; //vclass to use for Gov sponsors
    private static final String vivoNonGovVClass="http://vivo.library.cornell.edu/ns/0.1#IndependentResearchInstitute"; //vclass to use for non Gov sponsors
    static final String vivoPersonVClass = "http://vivo.cornell.edu/ns/mannadditions/0.1#CornellFaculty";//this is vivo.vclass.id for a cornell faculty member
    static final String  vivoGrantAwardVClass = "http://vivo.library.cornell.edu/ns/0.1#ResearchGrant";//this is the vivo.vclass.id for a grant award

    private static final String vivoSponsorMoniker = "Research Funding Agency";
    private static final String vivoGrantMoniker = "Research Grant";

    /**
     *  Connect to db's and attempt to import into VIVO all awards, dept associations,
     *  sponsor associations and PrimaryInvestigator associations.
     */
    public static void main(String[] args){
        if(  args.length != 1 ){
            System.out.println("Usage: java LocalOsp2Vivo jdbcPropertiesFile");
            System.out.println("This uitility does NOT do test runs with the argument 'true'");
            return;
        }
        LocalOsp2Vivo localOsp2Vivo = null;
        try{
            localOsp2Vivo = new LocalOsp2Vivo( args[0]);
        }catch (SQLException ex ){
            System.out.println("While trying to establish connectioins, SQLException: " + ex);
            return;
        }catch (IOException ex){
            System.out.println("While trying to establish connectioins, IOException: " + ex);
            return;
        }

        if( args.length > 1 && "true".compareToIgnoreCase(args[1]) != 0 )
            VitroDbUtils.readEntry("press return to continue or ctl-c to quit.");

        System.out.println("Importing awards.");
        localOsp2Vivo.importLocalOsp2Vivo();
    }

    private void close(Object obj){
        try{
        if( obj instanceof ResultSet)
            ((ResultSet)obj).close();
        if( obj instanceof Statement)
            ((Statement)obj).close();
            if( obj instanceof Connection)
            ((Connection)obj).close();
        }catch(Exception ex){
            
        }

    }
}
