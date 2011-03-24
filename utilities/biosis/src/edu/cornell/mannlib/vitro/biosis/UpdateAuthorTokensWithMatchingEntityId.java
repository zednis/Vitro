package edu.cornell.mannlib.vitro.biosis;
/**
 * @version 1.20 2004-08-23
 * @author Jon Corson-Rikert
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import edu.cornell.mannlib.vitro.biosis.beans.PersonToMatch;
import edu.cornell.mannlib.vitro.dao.db.VitroConnection;

/*** JCR
 *** Run this after importing the pubs from a Biosis download
 *** to create new authors and then match them against
 *** existing publications but before loading publications as Vivo entities (TransferPubsToEntities) */


/*
 * Description by bdc34 of old name matching
 * (may not be accurate)
 *
 * The import was done in steps:
 * 1) load articles and tokens
 * 2) get names for vivo person entities
 * 3) match article author tokens to vivo entities
 *
 * in step 1 the authors were extracted from the
 * records.  The original name value was stored
 * as a token.
 *
 * in step 2 all vivo entities in class group people
 * were put in a list
 * For each person make a data struct:
 *   break entity.name into first name, middle name and last name.
 *   Last name = everything before the first comma
 *   first name = indexof(first comma+1) to space
 *   middle name = end of first name to end of string
 *   entityId
 *
 * step 3, match these datastructs with article tokens:
 * This used a scoring system:
 *   lastname match: +128
 *        first initials when entity has only first initial: +8
 *        first initials when entity has full first name: +4
 *        check for hyphenated first name?
 *   full first name match: +8
 *   if score > 128
 *      entity has only middle initial and that matches: + 32
 *      entity has full middle and that matches initial +16
 *      entity has full middle and that matches full middle + 64
 *      both entity and article lack middle: +3
 *      entity has middle, article lacks middle: +1
 *
 *   Run this test for all vivo entities that are people, keep track
 *   of the max score, if that is greater than 133 make the association,
 *   if it is less than 133 but greater than 128, print a notice.
 *
 * possible automatic matches, score > 133:
 * last name and first initials when both only first initials,
 * last name and full first name match
 * last name, first initial, and entity has middle while article lacks middle
 * last name, first initial, and entity and article both lack middle.
 * last name, first initial, and full, or initial middle name match.
 */
class UpdateAuthorTokensWithMatchingEntityId {
    /* bdc34: updated to work with new db schema 2006-08-30 */
    private static boolean   NOISY=true;


    private static Vector mismatches=null;

    private static final int PEOPLE_CLASSGROUP_ID = 1; // id of classgroup "people"

    public static void doAuthorTokenUpdate( Connection con ) throws IOException, SQLException{
        PrintWriter out= new PrintWriter( new FileWriter("logs/ListOfKnownPeople.output"));

        List knownPeople = createListOfKnownPeople( con, out, NOISY );
        int peopleCount = knownPeople.size();

        writeNamesToFile(knownPeople,out);

        int matchCount=matchTokensToKnownPeople( con, knownPeople );
        System.out.println("matched " + matchCount + " publication authors against " +
                           peopleCount + " people already in Vivo");

        out.close();
    }

    public static void main (String args[]) {
        try {
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

            Connection con = getConnection( args[0] );
            doAuthorTokenUpdate(con);

            con.close();
        } catch (SQLException ex) {
            System.out.println ("SQLException:");
            while (ex != null) {
                System.out.println ("SQLState: " + ex.getSQLState());
                System.out.println ("Message:  " + ex.getMessage());
                System.out.println ("Vendor:   " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println ("");
            }
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace ();
        }
    }

    /**
     * this fills up the static knownPeople array with PersonToMatch objects
     */
    private static List createListOfKnownPeople( Connection con,PrintWriter out,boolean noisy)
        throws SQLException {
        int peopleCount=0;
        Statement stmt = null;
        ResultSet namesRS = null;
        List knownPeople =  new LinkedList();

        try {
            stmt=con.createStatement();

        } catch (SQLException ex) {
            System.out.println("Error on creation of new statement in createKnownPeople is " + ex.getMessage());
            stmt.close();
            return knownPeople;
        }

        String namesQuery="SELECT entities.id, entities.name " +
            "FROM entities join vclass on entities.vclassId = vclass.id " +
            "JOIN classgroups ON vclass.groupId = classgroups.id " +
            "WHERE classgroups.id = "+ PEOPLE_CLASSGROUP_ID;
        try {
            namesRS=stmt.executeQuery(namesQuery);
        } catch (SQLException ex) {
            System.out.println("Error on select query [" + namesQuery +
                               "] in createKnownPeople is " + ex.getMessage());
            stmt.close();
            return knownPeople;
        }
        while (namesRS.next()) {  //this loop parses the name into last, middle and first names
            ++peopleCount;
            int entityId=namesRS.getInt(1);
            String entityName = namesRS.getString(2);
            StringTokenizer fullNameTokens=new StringTokenizer(entityName ,",");
            int outerTokenCount=0;
            String lastnameStr=null, firstnameStr=null, middlenameStr=null;
            while (fullNameTokens.hasMoreTokens()) {
                ++outerTokenCount;
                switch (outerTokenCount) {
                case 1: lastnameStr=cleanUp(fullNameTokens.nextToken()); break;
                case 2: StringTokenizer restOfNameTokens=new StringTokenizer(fullNameTokens.nextToken()); // used to specify delimeter of space
                    int innerTokenCount=0;
                    while (restOfNameTokens.hasMoreTokens()) {
                        ++innerTokenCount;
                        switch (innerTokenCount) {
                        case 1: firstnameStr=cleanUp(restOfNameTokens.nextToken());
                            break;
                        case 2:  middlenameStr  = cleanUp(restOfNameTokens.nextToken());
                            break;
                        default: middlenameStr += cleanUp(restOfNameTokens.nextToken());
                            break;
                        }
                    }
                    break;
                case 3:
                    if (middlenameStr == null || middlenameStr.equals("")) {
                        middlenameStr=cleanUp(fullNameTokens.nextToken());
                    } else {
                        middlenameStr+=cleanUp(fullNameTokens.nextToken());
                    }
                    break; // was suffixStr
                default: System.out.println("additional token in people entity name " + entityName +
                                            " read as " + fullNameTokens.nextToken()); break;
                }
            }
            if (noisy) {
                String outputStr="first:[" + firstnameStr + "]";
                if (middlenameStr != null && !middlenameStr.equals("")) {
                    outputStr += "\t\t\tmiddle:[" + middlenameStr + "]";
                }
                out.println(outputStr);
            }
            PersonToMatch newPerson =
                new PersonToMatch(entityId,entityName,
                                  firstnameStr,middlenameStr,lastnameStr);
            if (newPerson != null) {
                knownPeople.add(newPerson);
            }
        }
        namesRS.close();
        stmt.close();
        return knownPeople;
    }

    /**
       Load up a list of people from the TOKENS table and call matchTokenToPerson() on them.
    */
    private static int matchTokensToKnownPeople( Connection con, List knownPeople )
        throws SQLException {
        clearMismatches();
        int matchedTokenCount=0;
        String tokensQuery = null;
        Statement stmt = null;
        ResultSet tokensRS = null;

        try {
            stmt=con.createStatement();
            tokensQuery="SELECT id,entityId,token FROM tokens " +
                    "WHERE (typeId=1 OR typeId=4) AND entityId = 0";
            tokensRS=stmt.executeQuery(tokensQuery);
        } catch (SQLException ex) {
            System.out.println("Error on select query [" + tokensQuery +
                    "] in matchTokensToKnownPeople is " + ex.getMessage());
            stmt.close();
            return matchedTokenCount;
        }
        while (tokensRS.next()) {
            int tokenId=tokensRS.getInt(1);
            int entityId=tokensRS.getInt(2);
            if (entityId==0) { // 0 means not already successfully matched to a person
                StringTokenizer fullNameTokens=new StringTokenizer(tokensRS.getString(3),",");
                int outerTokenCount=0;
                String tokenLastname=null, tokenFirstname=null, tokenMiddlename=null, tokenRemainder="";
                while (fullNameTokens.hasMoreTokens()) {
                    ++outerTokenCount;
                    switch (outerTokenCount) {
                    case 1: tokenLastname=cleanUp(fullNameTokens.nextToken()); break;
                    case 2: StringTokenizer restOfNameTokens=new StringTokenizer(fullNameTokens.nextToken()," ");
                        int innerTokenCount=0;
                        while (restOfNameTokens.hasMoreTokens()) {
                            ++innerTokenCount;
                            switch (innerTokenCount) {
                            case 1:  tokenFirstname   = cleanUp(restOfNameTokens.nextToken()); break;
                            case 2:  tokenMiddlename  = cleanUp(restOfNameTokens.nextToken()); break;
                            default: tokenMiddlename += cleanUp(restOfNameTokens.nextToken()); break;
                            }
                        }
                        break;
                    case 3: tokenRemainder=cleanUp(fullNameTokens.nextToken()); break;
                    default: System.out.println("additional token in token name " + tokensRS.getString(3) +
                                                " read as " + fullNameTokens.nextToken()); break;
                    }
                }
                // updates token database if a good match
                matchedTokenCount +=
                    matchTokenToPerson(con, tokenId, knownPeople, tokensRS.getString(3),
                            tokenFirstname,tokenMiddlename,tokenLastname);
            }
        }
        tokensRS.close();
        stmt.close();
        return matchedTokenCount;
    }

    /**
       Try to match a token with an entity.  The parameters indicate the token and the entities come from
       the static knownPeople collection. If a match is found then the TOKENS.ENTITYID will be updated
       with the token_id.

       con - the connection to make sql calls to
       token_id - the TOKENS.ID field value
       original_token - the TOKENS.TOKEN field value
       first_name - the First name from the TOKENS.TOKEN field
       middle_name - the Middle name from the TOKENS.TOKEN field
       last_name - the last name from the TOKENS.TOKEN field
    */
    private static int matchTokenToPerson(Connection con, int token_id, List knownPeople,
            String original_token, String first_name,String middle_name, String last_name) {
        if (knownPeople == null || knownPeople.size()==0) {
            return 0;
        }
        //int lastnameMatchCount=0, firstnameMatchCount=0, firstInitialMatchCount=0, middlenameMatchCount=0, middleInitialMatchCount=0;
        int maxScore=0, entityIdWithMaxScore=0, updatedTokenCount=0;
        String entityOriginalName=null;
        Iterator peopleIter=knownPeople.iterator();
        while (peopleIter.hasNext()) {
            PersonToMatch entity = (PersonToMatch) peopleIter.next();
            int score=0;
            if (entity.getLastname().equalsIgnoreCase(last_name)) {
                score += 128;
                //System.out.println("token matches entity last name so score is " + score );
                int firstnameLength=(first_name == null || first_name.equals("")) ? 0 : first_name.length();
                if (firstnameLength>0) {
                    if (firstnameLength==1) {
                        String first_initial=first_name.substring(0,1);
                        if (entity.getFirstnameLength()==1 && first_initial.equals(entity.getFirstInitial())) {
                            score += 8;
                        } else if (entity.getFirstnameLength()>1 && first_initial.equals(entity.getFirstInitial())) {
                            score += 4;
                            if (entity.getMiddlename()==null || entity.getMiddlename().equals("")) { // check for hyphenated first name
                                StringTokenizer firstTokens=new StringTokenizer(entity.getFirstname(),"-");
                                int entityFirstnameTokenCount=firstTokens.countTokens();
                                if ( entityFirstnameTokenCount>1) {
                                    String newMiddlename=null;
                                    for (int i=0; i<entityFirstnameTokenCount; i++) {
                                        switch (i) {
                                            case 0: firstTokens.nextToken(); break;
                                            case 1: newMiddlename=firstTokens.nextToken(); break;
                                            default: newMiddlename += firstTokens.nextToken(); break;
                                        }
                                        entity.setMiddlename(newMiddlename);
                                    }
                                }
                            }
                        }
                    } else if (entity.getFirstnameLength()>1 && first_name.equals(entity.getFirstname())) {
                        score += 8;
                        //System.out.println("token full first name matches full first name of entity so score is now " + score);
                    }
                    if (score > 128) {
                        //System.out.println("going on to check middle name");
                        int middlenameLength = middle_name==null || middle_name.equals("") ? 0 : middle_name.length();
                        if (middlenameLength > 0) {
                            if (middlenameLength==1) {
                                String middle_initial=middle_name.substring(0,1);
                                if (entity.getMiddlenameLength()==1 && middle_initial.equals(entity.getMiddleInitial())) {
                                    score += 32;
                                    //System.out.println("token matches a first-initial-only middle name so score is now " + score);
                                } else if (entity.getMiddlenameLength()>1 && middle_initial.equals(entity.getMiddleInitial())) {
                                    score += 16;
                                }
                            } else if (entity.getMiddlenameLength()>1 && middle_name.equals(entity.getMiddlename())) {
                                score += 64;
                                //System.out.println("token full first name matches full first name of entity so score is now " + score);
                            }
                        } else if (entity.getMiddlenameLength()==0) {
                            score += 3;
                            //System.out.println("neither token nor entity has a middle initial or name so score is now " + score);
                        } else {
                            score += 1;  // token is missing middle initial or middle name
                        }
                        if (score > maxScore ) {
                            maxScore=score;
                            entityIdWithMaxScore=entity.getEntityId();
                            entityOriginalName=entity.getOriginalname();
                        }
                    }
                }
            }
        }
        if (maxScore>=133) {
            try {
                Statement stmt=con.createStatement();
                /* sql */
                String updateQuery="UPDATE tokens SET entityId='" + entityIdWithMaxScore + "',entityMatchScore='" + maxScore + "' WHERE id='" + token_id + "'";
                try {
                    updatedTokenCount += stmt.executeUpdate(updateQuery);
                    //System.out.println("matched " + original_token + " against " + entityOriginalName + " with a score of " + maxScore);
                } catch (SQLException ex) {
                    System.out.println("Error on token update with matching entityId in createKnownPeople is " + ex.getMessage());
                }
                stmt.close();
            } catch (SQLException ex) {
                System.out.println("Error on creation of new inner statement for token update in createKnownPeople is " + ex.getMessage());
            }
        } else if (maxScore>=128) {
            System.out.println("did not match " + original_token + " against " + entityOriginalName + " with a score of " + maxScore);
            ArrayList list = new ArrayList(5);
            list.add(new Integer(token_id)); list.add(original_token); list.add(first_name); list.add(middle_name); list.add(last_name);
            list.add(entityOriginalName);list.add(new Integer(maxScore));
            getMismatches().add( list );
        }
        return updatedTokenCount;
    }

    /**
     *  Clean up a string for our use
     */
    private static String cleanUp(String inputStr) {
        if (inputStr==null || inputStr.equals("")) {
            return null;
        }
        return escapeQuotes(removeCharacters(removeCharacters(inputStr.trim(),46,""),34,""),39);
    }

    private static String removeCharacters( String termStr, int removeChar, String replaceStr ) {
        if (termStr==null || termStr.equals("")) {
            return termStr;
        }
        int characterPosition= -1;
        // strip leading spaces
        while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
            termStr = termStr.substring(characterPosition+1);
        }
        characterPosition=-1;
        while ( ( characterPosition = termStr.indexOf( removeChar, characterPosition+1 ) ) >= 0 ) {
            if ( characterPosition == 0 ) {
                termStr = replaceStr + termStr.substring( characterPosition+1 );
            } else {
                termStr = termStr.substring(0,characterPosition) + replaceStr + termStr.substring(characterPosition+1);
            }
            characterPosition += replaceStr.length() + 1;
        }
        return termStr;
    }

    private static String escapeQuotes( String termStr, int whichChar ) {
        if (termStr==null || termStr.equals("")) {
            return termStr;
        }
        int characterPosition= -1;
        // strip leading spaces
        while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
            termStr = termStr.substring(characterPosition+1);
        }
        characterPosition=-1;
        while ( ( characterPosition = termStr.indexOf( whichChar, characterPosition+1 ) ) >= 0 ) {
            if ( characterPosition == 0 ) // just drop it
                termStr = termStr.substring( characterPosition+1 );
            else if ( termStr.charAt(characterPosition-1) != 92 ) // already escaped
                termStr = termStr.substring(0,characterPosition) + "\\" + termStr.substring(characterPosition);
            ++characterPosition;
        }
        return termStr;
    }


    /**
     * Gets the ids of rows in pubs table associated with the token with the given name.
     * This is used by NameMatcherPanel.java.
     *
     * @returns ids in the following format: "(2333, 4344, 3, 233)"
     */
    public static String getPubIdsForAuthor(String tokenName, Connection con) throws SQLException, IOException {
        String query = "SELECT pubs.id from tokens, tokens2pubs, pubs where tokens.token like '"+tokenName+
            "' and tokens2pubs.tokenid = tokens.id and tokens2pubs.pubid = pubs.id ";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        StringBuffer pubIds = new StringBuffer("(");

        while( rs.next()){
            pubIds.append(rs.getString(1));
            if( !rs.isLast() )
                pubIds.append(", ");
        }
        return pubIds.append(")").toString();
    }

    public static void forceAssociation(String tokenName, String entityName, Connection con)
        throws SQLException, IOException{
        Statement stmt = con.createStatement();
        int entId = -1;
        String idQuery = "select id from entities where name = '" + entityName + "'";
        ResultSet rs = stmt.executeQuery( idQuery );
        if(rs.next())
            entId = rs.getInt(1);
        if( entId > 0 ){
            String force= "UPDATE tokens set entityid = "+ entId +
                " where token = '" + tokenName + "'";
            stmt.executeUpdate(force);
        } else {
            System.out.println("unable to force association between "+
                               tokenName+" and "+entityName);
        }
    }

    static Vector getMismatches(){
        if(mismatches!= null)
            return mismatches;
        else return mismatches=new Vector();
    }

    private static void clearMismatches(){ mismatches = null;   }

    /** returns an array of names from biosis/TOKENS that were not matched */
    static Object[] getMismatchArray(){
        Object names[] = new Object [getMismatches().size()];
        for(int i=0;  i < getMismatches().size() ; i++ )
            names[i]= (String)((AbstractList)getMismatches().get(i)).get(1);
        return names;
    }

    static Object[] searchForMatches(String name, Connection con)
    throws IOException, SQLException{
        Statement stmt = con.createStatement();
        StringTokenizer st = new StringTokenizer(name, ", ");
        boolean ya = false;

        String query = "SELECT e.NAME " +
                "FROM vclass vc JOIN entities e ON e.vclassId = vc.id " +
                "WHERE vc.groupid = " + PEOPLE_CLASSGROUP_ID +
                " AND (";
        while(st.hasMoreElements()){
            String tok = escapeQuotes(st.nextToken(),39);
            if (tok.length() < 2) continue;  //ignore initials for now
            if( ya )  query += " OR "; else ya = true;
            query += " e.name LIKE '%";
            query += tok;
            query += "%'";
        }
        ResultSet rs = stmt.executeQuery (query + ");");
        Vector guesses = new Vector();
        while(rs.next()){
            guesses.add( rs.getString(1) );
        }
        return guesses.toArray();
    }

    private static void writeNamesToFile(List in, PrintWriter out) throws IOException{
        if (in!=null && in.size()>0) {
            Collections.sort(in); // uses PersonToMatch.compareTo()
            Iterator iter=in.iterator();
            while (iter.hasNext()) {
                PersonToMatch eachPerson=(PersonToMatch)iter.next();
                if (eachPerson != null) {
                    out.println(eachPerson.getSortName() + "\t\t" + eachPerson.getOriginalname() + "\t\t" +
                                eachPerson.getMiddlenameLength() + "\t" + eachPerson.getMiddlename());
                }
            }
        }
        out.flush();
        out.close();
    }

    private static Connection  getConnection(String propFileName) throws SQLException {
        VitroConnection.establishDataSourceFromProperties( propFileName );
        return VitroConnection.getConnection();
    }

    /* ********************** Related to Main ************************* */

    private static final void setupProperties(String pFileName, TransferPubsToEntities trans)
    throws IOException{
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(pFileName);
        props.load(in);
        String cName ="TransferPubsToEntities.";
//        trans.maxArticleAgeInDays = Integer.parseInt(getProp(props, cName + "maxArticleAgeInDays", "100"));
//        trans.debug = Integer.parseInt(getProp(props, cName+"debug", "2"));
//        trans.journalArticleVClassId =
//            Integer.parseInt(getProp(props,cName+"journalArticleVClassId",
//                                     defaultArticleVClassId+"" ));
//        trans.article2AuthorPropertyId =
//            Integer.parseInt(getProp(props,cName+"article2AuthorPropertyId",
//                    defaultArticle2AuthorPropertyId+"" ));
//
//        System.out.println("maxArticleAgeInDays:"+trans.maxArticleAgeInDays);
//        System.out.println("debug:"+trans.debug);
//        System.out.println("journalArticleVClassId:"+trans.journalArticleVClassId);
//        System.out.println("author2ArticlePropertyId:"+trans.article2AuthorPropertyId);
    }

    private static String getProp(Properties p, String name, String pDefault){
        String retv = p.getProperty(name);
        if( retv == null )
            retv = pDefault;
        return retv;
    }

    private static final void showHelp(){
        System.out.println("UpdateAuthorTokensWithMatchingEntityId will move match author tokens with " +
                "entities in the main system.");
        System.out.println("java UpdateAuthorTokensWithMatchingEntityId <ConfigFile> ");
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
