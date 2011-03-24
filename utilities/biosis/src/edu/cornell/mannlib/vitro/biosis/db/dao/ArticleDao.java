package edu.cornell.mannlib.vitro.biosis.db.dao;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sql.DataSource;

import edu.cornell.mannlib.vitro.biosis.beans.Article;
import edu.cornell.mannlib.vitro.dao.db.VitroBaseDao;

public class ArticleDao extends VitroBaseDao {

    public static final int UNSPECIFIED    =0;
    public static final int AUTHOR         =1;
    public static final int MEETING        =2;
    public static final int SOURCE         =3;
    public static final int LINKTOHOLDINGS =4;
    public static final int CITATION       =5;

    private Connection connection = null;
    public ArticleDao(Connection con){
        this.connection = con;
    }

    /**
     * Inserts article and its tokens.
     */
    public void doInsert( Article article) throws SQLException {
        Connection con = this.connection;
        Statement stmt = con.createStatement();
        String insertQuery = makeInsertSql( article );
        stmt.executeUpdate( insertQuery );

        //here we add the tokens for the fields with multipul values
        ResultSet maxRS = stmt.getGeneratedKeys();
        if ( maxRS.next() ) {
            int newRecordId = maxRS.getInt(1);

            if ( article.authorTokenList!=null && article.authorTokenList.size()>0 )
                loadTokens(  article.authorTokenList, AUTHOR, article.authorListOrigin, newRecordId, false, true );
            if ( article.editorTokenList!=null && article.editorTokenList.size()>0 )
                loadTokens(  article.editorTokenList, AUTHOR, article.editorListOrigin, newRecordId, false, true );
            if ( article.addressTokenList!=null && article.addressTokenList.size()>0 )
                loadTokens(  article.addressTokenList, UNSPECIFIED, article.addressListOrigin, newRecordId, false, false );
            if ( article.meetingTokenList!=null && article.meetingTokenList.size()>0 )
                loadTokens(  article.meetingTokenList, MEETING, article.meetingListOrigin, newRecordId, true, false );

            if ( article.sourceTokenList!=null && article.sourceTokenList.size()>0 ) {
                if ( article.linkToHoldingsStr != null )
                    article.sourceTokenList.add( article.linkToHoldingsStr );
                loadTokens(  article.sourceTokenList, SOURCE, article.sourceListOrigin, newRecordId, true, false );
            }

            if ( article.citationText!=null && article.citationText.length() > 0 ){
                ArrayList citationList = new ArrayList(1);
                citationList.add( article.citationText );
                loadTokens(  citationList, CITATION, article.citationOrigin, newRecordId, true, false );
            }
        }

        maxRS.close();
        stmt.close();
        return;
    }

    /**
       Load the multi-value fields from the article into the tokens table.

       @param con live connection to vivo
       @param tokenList the list of tokens to insert.
       @param newTokenTypeId type of token
       @param tokenOriginId
       @param pubId id of the publication, the value of pubs.id.
       @param specifyFirstTypeOnly
       @param includeRank
    */
    private void loadTokens(  ArrayList tokenList, int newTokenTypeId,
                             int tokenOriginId, int pubId,
                             boolean specifyFirstTypeOnly, boolean includeRank )
        throws SQLException {
        Statement stmt = connection.createStatement();
        boolean gaveTokenTypeWarningMessage=false;
        int tokenCount=0;
        int firstTokenId=0;
        Iterator tokenIter = tokenList.iterator();

        while ( tokenIter.hasNext() ) {
            Object obj = tokenIter.next();
            if ( obj instanceof String ) {
                ++tokenCount;
                int tokenId=0, tokenFrequency=0, currentTokenTypeId=0;
                String thisToken = (String) obj;
                // first check if token already exists
                String tokenQuery = "SELECT id, frequency, typeId FROM tokens WHERE token=" + prepareForSql(thisToken,false) ;
                int tokenMatchCount=0;

                ResultSet tokenRS = stmt.executeQuery( tokenQuery );
                if ( tokenRS.next() ) {
                    ++tokenMatchCount;
                    tokenId = tokenRS.getInt(1);
                    tokenFrequency = tokenRS.getInt(2);
                    currentTokenTypeId = tokenRS.getInt(3);
                }
                tokenRS.close();

                if ( tokenMatchCount == 0 ) { // insert new token record
                    String insertStr=null;
                    if ( specifyFirstTypeOnly ) {
                        if ( tokenCount==1) {
                            insertStr = "INSERT INTO tokens(token,frequency,typeId) VALUES (" + prepareForSql(thisToken,false) + ",1," + newTokenTypeId + ")";
                        } else if ( newTokenTypeId==SOURCE && thisToken.indexOf("http://")>=0) {
                            insertStr = "INSERT INTO tokens(token,frequency,typeId) VALUES (" + prepareForSql(thisToken,false) + ",1," + LINKTOHOLDINGS + ")";
                        } else {
                            insertStr = "INSERT INTO tokens(token,frequency,typeId) VALUES (" + prepareForSql(thisToken,false) + ",1," + UNSPECIFIED + ")";
                        }
                    } else {
                        insertStr = "INSERT INTO tokens(token,frequency,typeId) VALUES (" + prepareForSql(thisToken,false) + ",1," + newTokenTypeId + ")";
                    }

                    stmt.executeUpdate( insertStr );
                    ResultSet maxTokenRS = stmt.executeQuery( "SELECT max(id) FROM tokens" );
                    if ( maxTokenRS.next() ) {
                        tokenId = maxTokenRS.getInt(1);
                    }
                    maxTokenRS.close();

                } else if (tokenMatchCount == 1 ) { //update frequency
                    // check first to see if new token type is unspecified or the same as current token type
                    if ( specifyFirstTypeOnly ) {
                        if ( tokenCount == 1 ) {
                            if ( currentTokenTypeId != newTokenTypeId ) {
                                if (currentTokenTypeId==4 && newTokenTypeId==3) {
                                    if ( !gaveTokenTypeWarningMessage ) {
                                        System.out.println("Warning: trying to match token of type " + currentTokenTypeId + " with type " + newTokenTypeId );
                                        gaveTokenTypeWarningMessage=true;
                                    }
                                }
                                System.out.println("Warning: trying to match token of type " + currentTokenTypeId + " with type " + newTokenTypeId );
                            }
                        } else if (currentTokenTypeId != UNSPECIFIED ){
                            System.out.println("Warning: trying to match token " + thisToken + " of type " + currentTokenTypeId + " with type " + newTokenTypeId );
                        }
                    } else if (newTokenTypeId!=currentTokenTypeId && newTokenTypeId != UNSPECIFIED) {
                        System.out.println("Warning: trying to match token " + thisToken + " of type " + currentTokenTypeId + " with type " + newTokenTypeId );
                    }
                    ++tokenFrequency;

                    String updateStr = "UPDATE tokens SET frequency=" + tokenFrequency + " WHERE id=" + tokenId;
                    stmt.executeUpdate( updateStr );

                } else {
                    System.out.println("Error: " + tokenMatchCount + " token records with token " + thisToken );
                }

                if (tokenMatchCount < 2) {
                    String originInsertStr = "INSERT INTO tokens2origins(tokenId,originId) VALUES (" + tokenId + "," + tokenOriginId + ")";
                    stmt.executeUpdate( originInsertStr );

                    String linkInsertStr=null;
                    String messageStr = "Error: SQL exception on inserting new tokens2pubs record for pubs record " + pubId + " and token id " + tokenId + ": ";
                    if ( specifyFirstTypeOnly ) {
                        if ( tokenCount==1 ) {
                            firstTokenId = tokenId;
                            linkInsertStr = "INSERT INTO tokens2pubs(tokenId,pubId,rank,originId) VALUES (" + tokenId + "," + pubId + "," + tokenCount + "," + tokenOriginId + ")";
                        } else { // don't insert links to pub record but rather to first token (meeting or source)
                            linkInsertStr = "INSERT INTO tokens2tokens (domainId,rangeId) VALUES (" + firstTokenId + "," + tokenId + ")";
                            messageStr="Error: SQL exception on inserting new tokens2tokens record for root token record " + firstTokenId + " and new token id " + tokenId + ": ";
                        }
                    } else if ( includeRank ) {
                        linkInsertStr = "INSERT INTO tokens2pubs(tokenId,pubId,rank,originId) VALUES (" + tokenId + "," + pubId + "," + tokenCount + "," + tokenOriginId + ")";
                    } else {
                        linkInsertStr = "INSERT INTO tokens2pubs(tokenId,pubId,originId) VALUES (" + tokenId + "," + pubId + "," + tokenOriginId + ")";
                    }

                    stmt.executeUpdate( linkInsertStr );
                }
            }
        }
        stmt.close();
    }

    /**
       make and return a sql String that would insert this Article object into
       the pubs table.  The String that is created is not executed by this method.
       ex. INSERT INTO pubs (col1, col2, ...) VALUES (val1, val2, ...)
    */
    private String makeInsertSql( Article article){
        String fieldsStr = "title";
        String valuesStr = "'untitled'";
        if (article.titleStr == null || article.titleStr.equals("")) {
            if ( article.bookTitleStr != null && !article.bookTitleStr.equals("")) {
                valuesStr = prepareForSql( article.bookTitleStr,false);
                if ( article.bookPublisherStr != null && !article.bookPublisherStr.equals("")) {
                    fieldsStr += ",bookPublisher";
                    valuesStr += "," + prepareForSql(article.bookPublisherStr,false) ;
                }
            } // leave title as 'untitled'
        } else {
            valuesStr = prepareForSql( article.titleStr ,false);
            if ( article.bookTitleStr != null && !article.bookTitleStr.equals("")) {
                fieldsStr += ",bookTitle";
                valuesStr += "," +prepareForSql( article.bookTitleStr ,false);
                if ( article.bookPublisherStr != null && !article.bookPublisherStr.equals("")) {
                    fieldsStr += ",bookPublisher";
                    valuesStr += "," +prepareForSql( article.bookPublisherStr ,false);
                }
            }
        }
        if (article.authorsStr != null && !article.authorsStr.equals("")) {
            fieldsStr += ",authors";
            valuesStr += "," +prepareForSql( article.authorsStr ,false);
        }
        if ( article.authorAddressStr != null && !article.authorAddressStr.equals("")) {
            fieldsStr += ",authorAddress";
            valuesStr += "," +prepareForSql( article.authorAddressStr ,false);
        }
        if ( article.emailAddressStr != null && !article.emailAddressStr.equals("")) {
            fieldsStr += ",email";
            valuesStr += "," +prepareForSql( article.emailAddressStr ,false);
        } else if ( article.emailStr != null && !article.emailStr.equals("")) {
            fieldsStr += ",email";
            valuesStr += "," +prepareForSql( article.emailStr ,false);
        }
        if ( article.sourceStr != null && !article.sourceStr.equals("")) {
            fieldsStr += ",source";
            valuesStr += "," +prepareForSql( article.sourceStr ,false);
        }
        if ( article.pubYear != null ) {
            fieldsStr += ",pubYear";
            valuesStr += "," + article.pubYear;
        }
        if ( article.languageStr != null && !article.languageStr.equals("")) {
            fieldsStr += ",language";
            valuesStr += "," +prepareForSql( article.languageStr ,false);
        }
        if ( article.abstractStr != null && !article.abstractStr.equals("")) {
            fieldsStr += ",abstract";
            valuesStr += "," +prepareForSql( article.abstractStr ,false);
        }
        if ( article.accessionNumber != null ) {
            fieldsStr += ",accessionNumber";
            valuesStr += "," + article.accessionNumber;
        }
        if ( article.updateCodeStr != null && !article.updateCodeStr.equals("")) {
            fieldsStr += ",updateCode";
            valuesStr += "," +prepareForSql( article.updateCodeStr ,false);
        }
        if ( article.meetingInfoStr != null && !article.meetingInfoStr.equals("")) {
            fieldsStr += ",meetingInfo";
            valuesStr += "," +prepareForSql( article.meetingInfoStr ,false);
        }

        if ( article.linkToHoldingsStr != null && !article.linkToHoldingsStr.equals("")) {
            fieldsStr += ",linkToHoldings";
            valuesStr += "," +prepareForSql( article.linkToHoldingsStr ,false);
        }

        if ( article.fullTextLinks != null && article.fullTextSources != null) {
            int linkcount=0;
            String concatLinks="", concatSources="";
            for (int i=0; i<article.fullTextLinks.length; i++) {
                if (article.fullTextLinks[i] != null && !article.fullTextLinks[i].equals("")) {
                    if (article.fullTextSources != null && !article.fullTextSources[i].equals("")) {
                        if (i==0) {
                            concatLinks=article.fullTextLinks[i];
                            concatSources=article.fullTextSources[i];
                        } else {
                            concatLinks += "," + article.fullTextLinks[i];
                            concatSources += "," + article.fullTextSources[i];
                        }
                        ++linkcount;
                    }
                }
            }
            if (linkcount>0) {
                fieldsStr += ",fullTextLink,fullTextSource";
                valuesStr += "," + prepareForSql( concatLinks ,false) + "," + prepareForSql(concatSources,false);
            }
        }

        String insertQuery = "INSERT INTO pubs (" + fieldsStr + ") VALUES (" + valuesStr + ")";
        return insertQuery;
    }

    /**
       Create a string that updates an article's record on the pubs table
       with the values from this article object.
       The following fields may be updated if they are set in this object:
       updateCode
       accessionNumber
       linkToHoldings
       fullTextLink
       fullTextSource
       @param the pubs.id of the pubs record to update
    */
    private String makeUpdateSql(int pubId, Article article){
        String updateStr = "UPDATE pubs SET updateCode=" + prepareForSql(article.updateCodeStr,false) +
            ", accessionNumber=" + article.accessionNumber;
        if ( article.linkToHoldingsStr != null ) {
            updateStr += ",linkToHoldings=" + prepareForSql( article.linkToHoldingsStr ,false);
        }
        if ( article.fullTextLinks != null && article.fullTextSources != null) {
            int linkcount=0;
            String concatLinks="", concatSources="";
            for (int i=0; i<article.fullTextLinks.length; i++) {
                if (article.fullTextLinks[i] != null && !article.fullTextLinks[i].equals("")) {
                    if (article.fullTextSources != null && !article.fullTextSources[i].equals("")) {
                        if (i==0) {
                            concatLinks=article.fullTextLinks[i];
                            concatSources=article.fullTextSources[i];
                        } else {
                            concatLinks += "," + article.fullTextLinks[i];
                            concatSources += "," + article.fullTextSources[i];
                        }
                        ++linkcount;
                    }
                }
            }
            if (linkcount>0) {
                updateStr += ",fullTextLink=" + prepareForSql(concatLinks,false) + ",fullTextSource=" + prepareForSql(concatSources,false);
            }
        }
        updateStr +=  " where id=" + pubId;
        return updateStr;
    }

    /**
       Creates or updates the row in the pubs table that corosponds to this article.
       This is method that you most likely want to use.

       @param doInserts true - do the inserts, false - just test, no changes to db.
       @param outFile comments will be writen to this file.
                    try something like out= new PrintWriter(new FileWriter(infileName + ".log"))
       @returns the return string will status codes to indicate what happened:
       insert - a row was inserted (or would have been inserted if doInserts is false)
       update - an update was done on a pub that was already there.
                ( or would have been updated if doInserts is false)
       match  - there was a title match,followed by the pub.id of the match,
                may be more than noe
       duplicate - there were duplicate titles in the pubs title field
       single - there was a single pub that matched this title
    */

    public String doArticle2Pub(boolean doInserts, Article article, PrintWriter outFile)
        throws IOException, SQLException{
        Connection con = this.connection;
        if(outFile == null)
            throw new IOException("doArticle2Pub needs a non null outFile");

        StringBuffer return_code = new StringBuffer("");

        int titleMatchCount=0, matchedId=0;
        String matchedUpdateCode=null;
        String matchedAccessionNumber="";

        //check to see if there is already an article with this title on the pubs table
        int titleLength=article.titleStr.length() < 255 ? article.titleStr.length() : 255;
        String titleQuery = "SELECT id,updateCode, accessionNumber FROM pubs WHERE title LIKE " +
        prepareForSql(article.titleStr.substring(0,titleLength),false);
        Statement stmt = con.createStatement();
        ResultSet titleMatchRS = stmt.executeQuery( titleQuery );
        while (titleMatchRS.next()) {
            matchedId         = titleMatchRS.getInt(1);
            matchedUpdateCode = titleMatchRS.getString(2);
            matchedAccessionNumber = titleMatchRS.getString(3);
            titleMatchCount++;
            return_code.append("match,").append(matchedId);
        }

        // we didn't find this article on the db, insert data from record into database
        if ( titleMatchCount==0 ) {
            if (doInserts)
                doInsert(article);
            return_code.append("insert");
        }

        //the article was already loaded, update fields
        if ( titleMatchCount == 1 ) {
            return_code.append("single");
            if ( !matchedUpdateCode.equals( article.updateCodeStr )) {
                outFile.println( "New update " + article.updateCodeStr +
                                 " with accession#: " + article.accessionNumber +
                                 " for " + matchedUpdateCode + " [" + matchedAccessionNumber + "]"+
                                 " title: " + article.titleStr );
                if ( doInserts )
                    stmt.executeUpdate(makeUpdateSql(matchedId, article ));
                return_code.append(",update");
            }
        }

        //there were more than one articles that matched the title, this shouldn't happen
        if ( titleMatchCount>1 ) {
            outFile.println( article.updateCodeStr + ": found " + titleMatchCount + " matches for "+
                             matchedUpdateCode + " title " + article.titleStr  );
            return_code.append("duplicate");
        }

        titleMatchRS.close();
        stmt.close();
        outFile.flush();
        return return_code.toString();
    }

}
