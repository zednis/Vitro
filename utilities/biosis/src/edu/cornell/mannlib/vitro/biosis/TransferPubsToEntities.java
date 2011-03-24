package edu.cornell.mannlib.vitro.biosis;
/**
 * @version 1.20 1999-08-16
 * @author Cay Horstmann
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.cornell.mannlib.vitro.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.biosis.beans.ArticleEntity;
import edu.cornell.mannlib.vitro.biosis.db.PubsToEntityRowMapper;
import edu.cornell.mannlib.vitro.dao.db.EntityDaoDb;
import edu.cornell.mannlib.vitro.dao.db.Ents2EntsDaoDb;
import edu.cornell.mannlib.vitro.dao.db.VitroConnection;

/**
 * 
    This will insert rows in the PUBS table as entities into the ENTITIES
    table.  The rows from the PUBS table that will be added are those
    where PUBS.ENTITYID = 0.  URLs to the publications are added to 
    the LINKS table.
    
 * @author bdc34
 *
 */
class TransferPubsToEntities {
    /* bdc34: updated to work with new db schema 2006-08-30 */
    private final static int defaultArticleVClassId = 318; //vclass Id of article in vivo
    private int journalArticleVClassId;

    private final static int defaultArticle2AuthorPropertyId = 3333;
    private int article2AuthorPropertyId;

    private int maxArticleAgeInDays = 0; //unit: days

    private DataSource dataSource;
    private EntityDaoDb entityDao;
    private Ents2EntsDaoDb ents2entsDao;

    private  int debug = 2; //try 0 1 or 2

    private int totalNewPubCount=0, totalRawCount=0,
        textLinksInserted=0, authorLinksInserted=0;

    private String defaultCitation =
        "This citation and abstract may be subject to restrictions on use, " +
        "confer with the copyright holder for details.";

    
    public TransferPubsToEntities(String propertiesFile ) throws IllegalArgumentException{
        try{ 
            setupProperties( propertiesFile, this );            
        }catch (IOException ioex ){
            throw new IllegalArgumentException("Could not get properties from " + 
                    propertiesFile + " " + ioex.getMessage());            
        }

        try {            
            this.setupConnection( propertiesFile );//this sets up the static VitroConnection
        } catch (SQLException ex) {
            System.out.println("Unable to setup DataSource via VitroConnection:");
            if (ex != null) {
                System.out.println ("SQLState: " + ex.getSQLState());
                System.out.println ("Message:  " + ex.getMessage());
                System.out.println ("Vendor:   " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println ("");
            }
            throw new IllegalArgumentException("unable to setup db connection" + ex);
        }        
    }
    
    /**
     Creates an entity for new publications.
     The following steps are followed:
     Takes all the publications on the table pubs where the entityId column =0.

     Once that is done then the pubs row for that publication is updated so that the
     pubs.entityId = entities.id of the new entity.

     Next a row is added to the link table.  The value of the
     pubs.fullTextLink used for this value.  If there is no fullTextLink
     there is no error, the entity will just have no link to the
     publication.

     Next, creates a relations between the authors and the new publication entity.

     Finally, sets the portal flags of the new entity to the flags of the authors:
      flag1set = author1.flag1set BitWiseOr author2.flag1set ... BitWiseOr authorN.flag1set
      flag2set = author1.flag2set BitWiseOr author2.flag2set ... BitWiseOr authorN.flag2set
      flag3set = author1.flag3set BitWiseOr author2.flag3set ... BitWiseOr authorN.flag3set
    */
    public void doTransfer(  ) throws SQLException {
        totalNewPubCount=0; totalRawCount=0; textLinksInserted=0; authorLinksInserted=0;        
        JdbcTemplate template = new JdbcTemplate( dataSource );
        entityDao = new EntityDaoDb();
        entityDao.setDataSource( dataSource );
        ents2entsDao = new Ents2EntsDaoDb();
        ents2entsDao.setDataSource(dataSource);

        ArticleEntity articleEnt = null;

        Iterator articleIter = getUpdateableArticles(template);
        while ( articleIter.hasNext() ) {
            ++totalRawCount;
            articleEnt = (ArticleEntity)articleIter.next();
            int pubId= articleEnt.pubId;

            /* don't do articles earlier than cutoff */
            if( beforeCutoff( articleEnt ) ){
                if(debug==2)
                    System.out.println("pub " + pubId + " rejected because the date " + articleEnt.getTimekey() +
                                       " is " +maxArticleAgeInDays+ " days before today.");
                continue;
            }

            /* don't do articles with no authors */
            if( noAuthorsAssociatedWithPub( pubId, template)){
                if(debug==2)
                    System.out.println("pub " + pubId + " rejected because it is not associated with any authors.");
                continue;
            }
            
            articleEnt.setCitation(getCitation(articleEnt));
            articleEnt.setVClassId( journalArticleVClassId );
            
            // Insert new entity for this pub
            int entityId = insertNewEntity( articleEnt );
            if( entityId < 0 ) return;

             boolean failed = updatePubWithEntityId( articleEnt );
             if( failed ) return;

             // do url
             try {
                 doURLs( articleEnt );
              } catch ( SQLException ex) {
                    System.out.println("error adding URL: " + ex + "\n" + ex.getSQLState());
                    return;
              }

              // do author association
              try {
                  doAuthorAsoc( articleEnt );
              } catch (SQLException ex) {
                  System.out.println("error loading authors: " + ex + "\n" + ex.getSQLState());
                  return;
              }
              printPeriodicMessage();
        }
        System.out.println("Inserted " + totalNewPubCount + " new article entities and " +
                           authorLinksInserted + " links to authors ...");
    }

    /**
     * Returns true if no authors are found for the given pub.
     */
    private boolean noAuthorsAssociatedWithPub(int pubId, JdbcTemplate jdbcTemplate) {        
        String query = "SELECT count(*) FROM " +
                "tokens2pubs t2p join tokens t ON t2p.tokenid = t.id "+
                "WHERE (t.typeId = 1 OR t.typeId = 4) " +
                "AND t2p.pubid = " + pubId + " AND t.entityid != 0";
        int count = jdbcTemplate.queryForInt(query);
        return count < 1;       
    }

    private final Iterator getUpdateableArticles(JdbcTemplate template){
        //Get all the pubs that have no entities, as indicated by pubs.enitityId=0
        String selQuery=
            "SELECT id, updateCode, title, authors, source, abstract, fullTextLink, " +
            "fullTextSource FROM pubs "+
            "WHERE entityId=0 ORDER BY updateCode";

        List articleEnts = template.query(selQuery, new PubsToEntityRowMapper() );
        return articleEnts.iterator();
    }

    /** returns < 0 if failed, new entity id otherwise */
    private final int insertNewEntity( ArticleEntity articleEnt ){
        int entityId = -1;
        try {
            entityId = entityDao.insertNewIndividual(articleEnt);
            if (entityId < 0) {
                System.out.println("could not insert pub "
                        + articleEnt.pubId);
                return -1;
            }
            ++totalNewPubCount;
        } catch (Exception ex) {
            System.out.println("error on inserting articleEntity pubId: "
                    + articleEnt.pubId);
            System.out.println( ex.getMessage() );
            return -1;
        }

        if (debug > 0)
            System.out.println("Transfering publication from "+ articleEnt.getTimekey()
                    + " to entity " + entityId);

        articleEnt.setId(entityId);
        return entityId;
    }

    /** returns true on error */
    private final boolean updatePubWithEntityId(ArticleEntity ent ){
        String pubUpdateStr = "UPDATE pubs SET entityId='" + ent.getId()
            + "' WHERE id='" + ent.pubId + "'";
        try {
            int count = (new JdbcTemplate( dataSource)).update(pubUpdateStr);
            if( count != 1 && debug > 0 )
                System.out.println("updatePubsWithEntityId: updated "+count+
                        " rows for pubid "+ ent.pubId );
        } catch (Exception ex) {
            System.out.println("Error updating pub with new entity id: \n"
                    + ex.getMessage());
            return true;
        }        
        return false;
    }

    public void doURLs( ArticleEntity ent )
        throws SQLException{
        JdbcTemplate template = new JdbcTemplate(dataSource);
        int entityId = ent.getId(),  pubId = ent.pubId;
        String fullTextLink = ent.fullTextLinks, fullTextSource = ent.fullTextSources;

        if (fullTextLink != null && !fullTextLink.equals("")) {
            StringTokenizer linkTokens=new StringTokenizer(fullTextLink,",");
            int linkCount=linkTokens.countTokens();
            StringTokenizer sourceTokens=new StringTokenizer(fullTextSource,",");
            int sourceCount=sourceTokens.countTokens();

            if (linkCount==sourceCount) {
                for (int j=0; j<linkCount; j++) {
                    String theLink=linkTokens.nextToken();
                    String theSource=sourceTokens.nextToken();
                    if (theLink != null && theSource!=null && !theLink.equals("") && !theSource.equals("")) {
                        String linkInsertStr="INSERT INTO links (URL,anchor,entityId,typeId) VALUES ('" +
                            theLink + "','via " + theSource + "','" + entityId + "',2)";
                        try {
                            textLinksInserted +=  template.update( linkInsertStr );
                        } catch (Error ex) {
                            System.out.println("error in inserting new links record for pub " +
                                               pubId + " and new entity " +
                                               entityId + " via [" + linkInsertStr + "] is: " +
                                               ex.getMessage());
                            return;
                        }
                    }
                }
            } else {
                System.out.println("Error: token count mismatch \n +fullTextLink "
                        + fullTextLink + " in pub " +
                        pubId + " has " + linkCount +
                        " tokens when fullTextSource " + fullTextSource +
                        " has " + sourceCount + " tokens.");
            }
        }
    }

    /** *********************************************************************
     * Associate the entity that represents a publication with its authors.

     create author relations using this property:
     id            317
     domainSide    ArticleReferenceIncludesAcademicEmployeeAuthor
     rangeSide     needs entry
     parent prop   PublicationReferenceIncludesGenericAgentAuthor
     domainClass   Article Reference
     rangeClass    Academic Employee
     @returns number of author links inserted
     */
    public void doAuthorAsoc(ArticleEntity ent) throws SQLException{
        int pubId = ent.pubId, pubEntityId = ent.getId();
        Connection con = dataSource.getConnection();
        Statement  innerStmt = con.createStatement();
        ObjectPropertyStatement ents2ents = new ObjectPropertyStatement();

        try{
            BigInteger flag1=new BigInteger("0"),flag2=new BigInteger("0");
            BigInteger flag3=new BigInteger("0");

            String authorQuery="SELECT "+
                "entities.id, "+
                "entities.flag1Set+0 as 'flag1Set', "+
                "entities.flag2Set+0 as 'flag2Set', entities.flag3Set+0 as 'flag3Set' "+
                " FROM "+
                "tokens,tokens2pubs,entities " +
                " WHERE " +
                "tokens2pubs.pubId=" + pubId + " AND " +
                "tokens2pubs.tokenId=tokens.id AND " +
                "tokens.entityId=entities.id AND " +
                "(tokens.typeId=1 or tokens.typeId=4) ";
            ResultSet authorsRS = innerStmt.executeQuery( authorQuery );

            while (authorsRS.next()) {
                int authorEntityId = authorsRS.getInt(1);

                ents2ents.setPropertyId(article2AuthorPropertyId);
                ents2ents.setDomainId(pubEntityId);
                ents2ents.setRangeId(authorEntityId);
                ents2entsDao.insertNewEnts2Ents( ents2ents );
                authorLinksInserted++;

                //accumulate the flags of the authors for later use
                String authorf1 = authorsRS.getString("flag1Set");
                if( !authorsRS.wasNull() )
                    flag1 = flag1.or(new BigInteger(authorf1));

                String authorf2 = authorsRS.getString("flag2Set");
                if( !authorsRS.wasNull() )
                    flag2 = flag2.or(new BigInteger(authorf2));

                String authorf3 = authorsRS.getString("flag3Set");
                if( !authorsRS.wasNull() )
                    flag3 = flag3.or(new BigInteger(authorf3));
            }
            authorsRS.close();

            /**** Set flag fields of publication entity ****/
            if( pubEntityId > 0){
                String flagUpdateQuery="UPDATE entities SET flag1set="+flag1.toString()+
                ",flag2set=" + flag2.toString() + ",flag3set=" + flag3.toString() +
                " WHERE entities.id=" + pubEntityId;
                innerStmt.executeUpdate( flagUpdateQuery );
                System.out.println("flag1: " + flag1.toString() + " flag2:" + flag2.toString() + " flag3:" + flag3.toString());
                
            }

        }catch(Throwable thr){
            System.out.println("error updating authors: " + thr.getLocalizedMessage());
        }finally{
            VitroConnection.close(innerStmt);
            VitroConnection.close(con);
        }
    }

    private void printPeriodicMessage(){
        if (totalRawCount % 50 == 0)
            System.out.println("Processed " + totalRawCount +
                    " publications so far with " + totalNewPubCount +
                    " article insertions, " + textLinksInserted +
                    " full text links, and " + authorLinksInserted +
                    " author links.");
    }

    /** ********************************************************************
       Gets the citation string for a given token. This does not get writen to the log.
       tokentype 5 is citation.
       @returns "" if no citation is found.
    */
    public String getCitation( ArticleEntity art )throws SQLException{
        String cita = null;

        String query = "SELECT tokens.token FROM tokens, tokens2pubs WHERE " +
            "tokens2pubs.tokenId = tokens.id AND tokens.typeid = 5 AND tokens2pubs.pubid = "+art.pubId;

        cita =(String)(new JdbcTemplate(dataSource)).queryForObject(query, String.class );
        if(cita != null)
            return cita;
        else
            return defaultCitation;
    }

    public String getRelationId(String s1, String s2, String s3, Connection con){ return "22";}

    private final boolean beforeCutoff(ArticleEntity article){
        if( article == null) return false;
        if( maxArticleAgeInDays == 0 ) return false;
        DateTime cutoffDate =
            new DateTime( article.getTimekey() ).plusDays( maxArticleAgeInDays );
        return cutoffDate.isBeforeNow();
    }

    private void setupConnection(String propFileName) throws SQLException {
        VitroConnection.establishDataSourceFromProperties( propFileName );
        dataSource = VitroConnection.getDataSource(false);
    }

    /** ***************** MAIN ********************** */
    public static void main (String args[]) {
        if( args == null || args.length != 1 ){
            showHelp();
            System.out.println("You must pass a configuration file.");
            return;
        }

        String errmsg = goodFile( args[0] );
        if( errmsg != null ){
            showHelp();
            System.out.println( errmsg );
            return;
        }

        String propsFile = args[0];
        TransferPubsToEntities transfer = new TransferPubsToEntities(propsFile);

//        try{ 
//            setupProperties( args[0], transfer );            
//        }catch (IOException ioex ){
//            System.out.println("Could not get properties from " + args[0] + " " + ioex.getMessage());
//            return;
//        }

//        try {            
//            setupConnection( args[0] );//this sets up the static VitroConnection
//        } catch (SQLException ex) {
//            System.out.println("Unable to setup DataSource via VitroConnection:");
//            if (ex != null) {
//                System.out.println ("SQLState: " + ex.getSQLState());
//                System.out.println ("Message:  " + ex.getMessage());
//                System.out.println ("Vendor:   " + ex.getErrorCode());
//                ex = ex.getNextException();
//                System.out.println ("");
//            }
//        }

        try{
            transfer.doTransfer(   );
        } catch (SQLException ex) {
            System.out.println ("Unable to Transfer Pubs to Entities:");
            while (ex != null) {
                System.out.println ("SQLState: " + ex.getSQLState());
                System.out.println ("Message:  " + ex.getMessage());
                System.out.println ("Vendor:   " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println ("");
            }
        }
    }

    /* ********************** Related to Main ************************* */

    private static final void setupProperties(String pFileName, TransferPubsToEntities trans)
    throws IOException{
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(pFileName);
        props.load(in);
        String cName ="TransferPubsToEntities.";
        trans.maxArticleAgeInDays = Integer.parseInt(getProp(props, cName + "maxArticleAgeInDays", "100"));
        trans.debug = Integer.parseInt(getProp(props, cName+"debug", "2"));
        trans.journalArticleVClassId =
            Integer.parseInt(getProp(props,cName+"journalArticleVClassId",
                                     defaultArticleVClassId+"" ));
        trans.article2AuthorPropertyId =
            Integer.parseInt(getProp(props,cName+"article2AuthorPropertyId",
                    defaultArticle2AuthorPropertyId+"" ));

        System.out.println("maxArticleAgeInDays:"+trans.maxArticleAgeInDays);
        System.out.println("debug:"+trans.debug);
        System.out.println("journalArticleVClassId:"+trans.journalArticleVClassId);
        System.out.println("author2ArticlePropertyId:"+trans.article2AuthorPropertyId);
    }

    private static String getProp(Properties p, String name, String pDefault){
        String retv = p.getProperty(name);
        if( retv == null )
            retv = pDefault;
        return retv;
    }

    private static final void showHelp(){
        System.out.println("TransferPubsToEntities will move new publications " +
                "into the main system by making new entities for them.");
        System.out.println("java TransferPubsToEntities <ConfigFile> ");
        System.out.println("The ConfigFile should have db and other configuration info.");
    }

    public static final String goodFile(String filename){
        File file = new File(filename);
       if( !file.exists() ) return "File " + filename + " does not exist. " ;
       if( file.isDirectory() ) return  filename + " is not a property file, it is a Directory. " ;
       if( !file.isFile() ) return filename + " is not a normal file. " ;
       if( !file.canRead() ) return "This process cannot read the file " + filename;
       return null;
    }
}
