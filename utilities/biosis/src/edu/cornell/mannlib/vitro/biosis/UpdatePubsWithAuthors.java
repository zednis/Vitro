package edu.cornell.mannlib.vitro.biosis;
/**
 * @version 1.20 2004-08-23
 * @author Jon Corson-Rikert
 */
//import java.net.*;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.cornell.mannlib.vitro.dao.db.VitroConnection;


/**
 * Associates publication entities with authors  
 * 
 */
/* JCR
 *** Run this after importing the pubs from a Biosis download
 *** and AFTER running UpdateAuthorTokensWithMatchedEntityIds
 *** and AFTER running TransferPubsToEntities */
class UpdatePubsWithAuthors {    
    private static boolean   DO_UPDATES=true;

    public static void main (String args[]) {
        Connection con = null;
        if( args == null || args.length != 1){
            showHelp();
            return;
        }
        String filecheck = goodFile(args[0]);
        if( filecheck != null ){
            System.out.println("bad config file: " + filecheck );
            return;            
        }
        
        try {            
            con = getConnection(args[0]);
            updatePubsWithAuthors( con );                     
        } catch (SQLException ex) {
            System.out.println ("SQLException:");
            while (ex != null) {
                System.out.println ("SQLState: " + ex.getSQLState());
                System.out.println ("Message:  " + ex.getMessage());
                System.out.println ("Vendor:   " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println ("");
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace ();
        }
        
        try{ con.close(); } catch( SQLException ex ){}
    }

    private static void updatePubsWithAuthors( Connection con )
        throws SQLException {
        int totalRawCount=0, authorLinksInserted=0;
        Statement stmt=con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, entityId FROM pubs WHERE entityId>0 ORDER BY entityId" );
        while ( rs.next() ) {
            ++totalRawCount;
            int pubId=rs.getInt(1);
            int pubEntityId=rs.getInt(2);
            if (pubEntityId>0) {
                Statement innerStmt=con.createStatement();
                // then create author links
                String authorQuery="SELECT tokens.id,tokens2pubs.rank,tokens.entityId,entities.typeId FROM tokens,tokens2pubs,entities "
                                  +"WHERE tokens2pubs.pubId=" + pubId + " AND tokens2pubs.tokenId=tokens.id AND tokens.entityId=entities.id "
                                  +"AND (tokens.typeId=1 or tokens.typeId=4) ORDER BY tokens2pubs.rank";
                try {
                    ResultSet authorsRS=innerStmt.executeQuery(authorQuery);
                    while (authorsRS.next()) {
                        int tokenId=authorsRS.getInt(1);
                        int rank=authorsRS.getInt(2);
                        int authorEntityId=authorsRS.getInt(3);
                        int authorTypeId=authorsRS.getInt(4);
                        authorLinksInserted = authorLinksInserted +
                            associatePubWithAuthor(con, pubId, pubEntityId,
                                                   authorEntityId, authorTypeId, rank);
                    }
                    authorsRS.close();
                } catch (SQLException ex) {
                    System.out.println("error on querying pub author tokens via [" +
                                       authorQuery + "] is: " + ex.getMessage());
                    return;
                }
            }
            if ( totalRawCount%500==0) {
                System.out.println("processed " + totalRawCount + " publications so far with " +
                                   authorLinksInserted + " new author links ...");
            }
        }
        rs.close();
        stmt.close();
        System.out.println("Inserted " + authorLinksInserted + " new links to authors ...");
    }

    static int associatePubWithAuthor(Connection con, int pubId, int pubEntityId, int authorEntityId, int authorTypeId, int rank )
    throws SQLException{
        // leave off qualifier for now
//      String qualifier=null;
//      switch (rank) {
//      case 1: qualifier = "1st author"; break;
//      case 2: qualifier = "2nd author"; break;
//      case 3: qualifier = "3rd author"; break;
//      default: qualifier= rank + "th author"; break;
//      }
        int inserted = 0;

        int etypes2RelationsId = 0;
        switch (authorTypeId) {
        case 31: etypes2RelationsId=360; break;
        case 32: etypes2RelationsId=362; break;
        case 90: etypes2RelationsId=361; break;
        case 94: etypes2RelationsId=363; break;
        default: System.out.println("Error: unexpected entity type id " +
                                    authorTypeId + " for author " + authorEntityId + " and article entity " +
                                    pubEntityId + " (pub " + pubId + ")");
        }

        if ( etypes2RelationsId<=0) return 0;

        Statement authorStmt=con.createStatement();
        String existingQuery="SELECT ents2ents.id FROM ents2ents WHERE domainId="+pubEntityId+
            " AND rangeId="+authorEntityId+" AND etypes2RelationsId="+etypes2RelationsId;
        try {
            ResultSet existingLinkRS=authorStmt.executeQuery(existingQuery);
            if (existingLinkRS.next()) {
                int existingLinkId=existingLinkRS.getInt(1);
            } else if (DO_UPDATES) {
                String insertLinkQuery=
                    "INSERT INTO ents2ents(domainId,rangeId,etypes2RelationsId) VALUES ('"
                    + pubEntityId + "','" + authorEntityId + "','" + etypes2RelationsId + "')";
                try {                    
                    inserted = authorStmt.executeUpdate(insertLinkQuery);
                } catch (SQLException ex) {
                    System.out.println("error on inserting article-author ents2ents via [" +
                                       insertLinkQuery + "] is: " + ex.getMessage());
                    return 0;
                }
            } else {
                System.out.println("Would be inserting new link for author " +
                                   authorEntityId + " to pub " + pubEntityId);
            }
            existingLinkRS.close();
        } catch (SQLException ex) {
            System.out.println("error on checking for existing article-author ents2ents via [" +
                               existingQuery + "] is: " + ex.getMessage());
            return 0;
        }
        authorStmt.close();
        return inserted;
    }


    private static Connection  getConnection(String propFileName) throws SQLException {
        VitroConnection.establishDataSourceFromProperties( propFileName );
        return VitroConnection.getConnection();
    }
    
    private static final void showHelp(){
        System.out.println("UpdatePubsWithAuthors will set author entity ids for pubs after " +
                "authors have been associated with entities" );
        System.out.println("java UpdatePubsWithAuthors<ConfigFile> ");
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
