package edu.cornell.mannlib.vitro.biosis;
/**
 * @version 0.9 2003-06-24
 * @author Jon Corson-Rikert
 */

// NOTE: from sample file I am assuming the person and the case have already been entered
// NOTE: from sample file I gather that clip names will be the datafile name of the clip, including
//       a file extension that will indicate the clip format
// QUESTION: how will we load clip size with the format?
// QUESTION: how will we differentiate MPEG1 and MPEG4 files?
// QUESTION: will Quicktime clips have the extensions I am assuming -- QT2, QT3, QT4

//import java.net.*;
//import java.sql.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import edu.cornell.mannlib.vitro.biosis.beans.Article;
import edu.cornell.mannlib.vitro.biosis.db.dao.ArticleDao;
import edu.cornell.mannlib.vitro.dao.db.VitroConnection;

class BiosisLoad {

    //origins
    private static int BIOSIS_AUTHORS       =2;
    private static int BIOSIS_AUTHOR_ADDRESS=3;
    private static int BIOSIS_SOURCE        =4;
    private static int BIOSIS_BOOK_TITLE    =5;
    private static int BIOSIS_BOOK_PUBLISHER=6;
    private static int BIOSIS_MEETING_INFO  =7;
    private static int BIOSIS_EDITORS       =8;

    private static Hashtable ignoredFieldCounts = null;

    public static void main (String args[]) {
        try {
            if( args == null || args.length != 3 ){
                showHelp();                
                return;
            }

            String errmsg = goodFile( args[0] );  //check the config file
            if( errmsg != null ){
                showHelp();
                System.out.println( "file " + args[0] + " " +errmsg );
                return;
            }
            
            errmsg = goodFile( args[1] ); //check the datafile
            if( errmsg != null ){
                showHelp();
                System.out.println( "file " + args[1] + " " +errmsg );
                return;
            }
                        
            Connection con = getConnection( args[0] );

            BufferedReader in = null;
            PrintWriter out = null;
            String infileName = "";
            boolean doDBInserts = false;
            
            infileName = args[1];
            in = new BufferedReader(new FileReader(infileName));
            out= new PrintWriter( new FileWriter( infileName + ".output"));            
            doDBInserts = args[2].equalsIgnoreCase("true");                           

            loadRecords( con, in, out, doDBInserts );            
            con.close();

        } catch (SQLException ex) {
            System.out.println ("SQLException:");
            sysPrintSqlException(ex);            
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace ();            
        }
    }

    public static void loadRecords( Connection con, BufferedReader in, PrintWriter outFile, boolean doInserts )
        throws IOException, SQLException {
        Statement stmt = con.createStatement();

        Article article = new Article();
        ArticleDao articleDao = new ArticleDao( con );
        setupArticle(article);

        int    lineCount   = 0;
        int    singleTitleMatchTotal=0,duplicateTitleMatchCount=0,wouldBeInsertCount=0;
        int    insertCount = 0;
        String line = in.readLine();
        boolean recordInProgress=false; // initial condition only
        ignoredFieldCounts = new Hashtable();

        while ((line = in.readLine()) != null /*&& insertCount < 8 && lineCount < 400*/ ) {
            ++lineCount;
            if ( line != null && !line.equals("")) {
                if (line.indexOf("Record") == 0 ) { // demarcates start of new record
                    if ( recordInProgress ) {   //it just finished reading in a record
                        String ret_code = articleDao.doArticle2Pub(  doInserts, article, outFile );
                        if( ret_code != null && ret_code.startsWith("insert"))
                            wouldBeInsertCount++;
                        else if (ret_code != null && ret_code.startsWith("match"))
                            singleTitleMatchTotal++;
                        else if (ret_code != null && ret_code.startsWith("duplicate"))
                            duplicateTitleMatchCount++;                        
                        
                        article.clearAllFields();
                        setupArticle(article);
                    }
                    recordInProgress=true;
                } else {
                    /* here we parsed a single line from the biosis download file */
                    recordInProgress=true;
                    article = parseLine(line, article);
                }
            }
            if ( lineCount % 1000 == 0 ) {
                System.out.println("processed " + lineCount + " lines and " +
                                   (doInserts?"":"would have ")+ "inserted "+wouldBeInsertCount +
                                   " records so far, with "+singleTitleMatchTotal+
                                   " matched titles and "+duplicateTitleMatchCount+" existing duplicate titles found." );
            }
        }

        outFile.println("total titles matching existing records: " + singleTitleMatchTotal+"; "+
                        duplicateTitleMatchCount+" existing duplicates found");

        stmt.close();
        outFile.flush();
        outFile.close();

        System.out.println("processed " + lineCount + " lines, inserted " + wouldBeInsertCount + " records");
        if(!ignoredFieldCounts.isEmpty()){
            System.out.println("ignored the following codes:");
            Enumeration keys=ignoredFieldCounts.keys();
            while(keys.hasMoreElements()){
                String key = (String)keys.nextElement();
                System.out.println( key + " " + ignoredFieldCounts.get(key));
            }
        }
    }

    public static void sysPrintSqlException(SQLException ex){
        while (ex != null) {
            System.out.println ("SQLState: " + ex.getSQLState());
            System.out.println ("Message:  " + ex.getMessage());
            ex = ex.getNextException();
            System.out.println ("");
        }
    }

    private static String prepareForInsert( String inputStr, int mode ) {
        switch ( mode ) {
            case 1: inputStr = escapeQuotes( inputStr, 34 );
                    inputStr = escapeQuotes( inputStr, 39 );
                    break;
            case 2: break;
            case 3: //inputStr = removeCharacters( inputStr, '"', "" );   // double quotes added by Excel at start and end of text block
                    inputStr = removeCharacters( inputStr, '�', "..." );  // 133 0x85 ellipsis ...
                    inputStr = removeCharacters( inputStr, '�', "'" );  // 145 0x91 back tic &#145;"
                    inputStr = removeCharacters( inputStr, '�', "'" );  // 146 0x92 forward tic
                    inputStr = removeCharacters( inputStr, '�', "\"" ); // 147 0x93 back double tic
                    inputStr = removeCharacters( inputStr, '�', "\"" ); // 148 0x94 forward double tic
                    inputStr = removeCharacters( inputStr, '�', "-" );  // 150 0x96 short dash
                    inputStr = removeCharacters( inputStr, '�', "--" ); // 151 0x97 long dash
                    inputStr = escapeQuotes( inputStr, 34 );
                    inputStr = escapeQuotes( inputStr, 39 );
                    break;
        }
        return inputStr;
    }


    private static Article setupArticle(Article article){        
        article.authorListOrigin = BIOSIS_AUTHORS;
        article.editorListOrigin = BIOSIS_EDITORS;
        article.addressListOrigin= BIOSIS_AUTHOR_ADDRESS;
        article.meetingListOrigin= BIOSIS_MEETING_INFO;
        article.sourceListOrigin = BIOSIS_SOURCE;
        article.citationText = citationText;
        return article;
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

   /*
    * In the input String replace all the " by &quot
    */
    private static String insertQuot(String str)    {
        String temp = str;
        String finalStr = "";
        int pos;
        while((pos = temp.indexOf('\"')) != -1 ) {
            finalStr = finalStr + temp.substring(0,pos);
            finalStr = finalStr + "&quot";
            temp = temp.substring(pos+1);
        }
        finalStr = finalStr + temp;
        return finalStr;
    }

   /*
    * In the input String replace all the " by \"
    */
    private static String insertSlash(String str)   {
        String temp = str;
        String finalStr = "";
        int pos;
        while((pos = temp.indexOf('\"')) != -1 ) {
            finalStr = finalStr + temp.substring(0,pos);
            finalStr = finalStr + "\\\"";
            temp = temp.substring(pos+1);
        }
        finalStr = finalStr + temp;
        return finalStr;
    }

    private String replaceQuot(String str) {
        String temp = str;
        String finalStr = "";
        int pos;
        while((pos = temp.indexOf("&quot")) != -1 ) {
            finalStr = finalStr + temp.substring( 0,pos );
            finalStr = finalStr + "\"";

            temp = temp.substring(pos+5);
        }
        finalStr = finalStr + temp;
        return finalStr;
    }

    private static String stripLeadingSpaces( String termStr ) {
        int characterPosition= -1;
        while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
            termStr = termStr.substring(characterPosition+1);
        }
        return termStr;
    }

    private static String stripLastPeriod( String termStr ) {
        int characterPosition=-1;
        while ( ( characterPosition = termStr.indexOf( 46, characterPosition+1 ) ) >= 0 ) {
            if (characterPosition >= (termStr.length()-1)) {
                termStr = termStr.substring(0,termStr.length()-1);
            }
        }
        return termStr;
    }

    private static Connection  getConnection(String propFileName) throws SQLException {
            VitroConnection.establishDataSourceFromProperties( propFileName );
            return VitroConnection.getConnection();
    }

    /**
       Takes a single line from a biosis article record and updates the article object
       to reflect the data in that line.

       @param line a single line from a biosis article record
       @param article article to update
       @returns the article updated with info from the line
    */

    public static Article parseLine(String line, Article article){
        String codeStr = line.substring(0,5);

        if ( codeStr.equals("AB:  ")) {
            article.abstractStr = prepareForInsert(line.substring(5),3);

        } else if (codeStr.equals("AD:  ")) {
            article.authorAddressStr = prepareForInsert(line.substring(5),1);
            // first split string to separate address from email, if any
            StringTokenizer t = new StringTokenizer( article.authorAddressStr,";");
            int sectionCount = t.countTokens();
            if ( sectionCount > 0 ) {
                for (int i=0; i<sectionCount; i++ ) {
                    String sectionStr = t.nextToken();
                    switch (i) {
                    case 0: //department address in variable number of chunks
                        int cutPosition = -1;
                        if ((cutPosition = sectionStr.indexOf("{a}")) >= 0 ) {
                            sectionStr = stripLeadingSpaces(sectionStr.substring(cutPosition + 4));
                        }
                        article.authorAddressStr = sectionStr; // without email address
                        StringTokenizer t2 = new StringTokenizer( sectionStr, ",");
                        int chunkCount = t2.countTokens();
                        if ( article.addressTokenList == null ) {
                            article.addressTokenList = new ArrayList();
                        }
                        for (int j=0; j<chunkCount; j++) {
                            article.addressTokenList.add( t2.nextToken().trim());
                            /*
                              String chunkStr = stripLeadingSpaces(t2.nextToken());
                              // if (j==0) {
                              int cutPosition = -1;
                              if ((cutPosition = chunkStr.indexOf("{a}")) >= 0 ) {
                              chunkStr = stripLeadingSpaces(chunkStr.substring(cutPosition + 4));
                              }
                              //}
                              article.addressTokenList.add( chunkStr );
                            */
                        }
                        break;
                    case 1: //email
                        StringTokenizer t3 = new StringTokenizer( sectionStr,"," );
                        int pieceCount = t3.countTokens();
                        if ( pieceCount > 0 ) { // read only first part; second is "USA"
                            String pieceStr = t3.nextToken().trim();
                            int emailStartPos = pieceStr.indexOf("E-Mail: ");
                            if ( emailStartPos >=0 ) {
                                article.emailStr = pieceStr.substring(emailStartPos + 8);
                            } else {
                                article.emailStr = pieceStr;
                            }
                        }
                        break;
                    }
                }
            }

        } else if (codeStr.equals("AN:  ")) {
            article.accessionNumber = line.substring(5);
        } else if (codeStr.equals("AU:  ")) {
            article.authorsStr = prepareForInsert(line.substring(5),1);
            StringTokenizer t = new StringTokenizer( article.authorsStr,";");
            int authorsCount = t.countTokens();
            for (int i=0; i<authorsCount; i++ ) {
                if ( article.authorTokenList == null ) {
                    article.authorTokenList = new ArrayList();
                }
                String rawAuthor = t.nextToken();
                int cutPosition = -1;
                if ((cutPosition = rawAuthor.indexOf("{a}")) > 0 ) {
                    rawAuthor = rawAuthor.substring(0,cutPosition);
                } else if ((cutPosition = rawAuthor.indexOf("[Author")) > 0 ) {
                    rawAuthor = rawAuthor.substring(0,cutPosition);
                }
                String author = null;
                StringTokenizer n = new StringTokenizer( rawAuthor,"-" );
                int nameCount = n.countTokens();
                for ( int j=0; j<nameCount; j++ ) {
                    switch (j) {
                    case 0: author = n.nextToken().replace(',',' ').trim();break; // last name
                    case 1: author += ", " + n.nextToken().trim(); break; // first name
                    default: author += " " + n.nextToken().trim();
                    }
                }
                if ( author != null ) {
                    article.authorTokenList.add(author);
                    if ( i==0 ) {
                        article.authorsStr = author;
                    } else {
                        article.authorsStr += "; " + author;
                    }
                }
            }

        } else if (codeStr.equals("AUB: ")) {
            article.editorsStr = prepareForInsert(line.substring(6),1);
            int colonPos = article.editorsStr.indexOf(": Ed"); // separator after last editor and before Ed or Eds
            if ( colonPos >= 0 ) {
                article.editorsStr = article.editorsStr.substring(0,colonPos);
            }
            StringTokenizer t = new StringTokenizer( article.editorsStr,";");
            int editorsCount = t.countTokens();
            for (int i=0; i<editorsCount; i++ ) {
                if ( article.editorTokenList == null ) {
                    article.editorTokenList  = new ArrayList();
                }
                String rawAuthor = t.nextToken();
                int cutPosition = -1;
                if ((cutPosition = rawAuthor.indexOf("{a}")) > 0 ) {
                    rawAuthor = rawAuthor.substring(0,cutPosition);
                }
                String editor = null;
                StringTokenizer n = new StringTokenizer( rawAuthor,"-" );
                int nameCount = n.countTokens();
                for ( int j=0; j<nameCount; j++ ) {
                    switch (j) {
                    case 0: editor = n.nextToken().trim(); break; // last name
                    case 1: editor += ", " + n.nextToken().trim(); break; // first name
                    default: editor += " " + n.nextToken().trim();
                    }
                }
                if ( editor != null ) {
                    article.editorTokenList.add(editor);
                    if ( i==0 ) {
                        article.editorsStr = editor;
                    } else {
                        article.editorsStr += "; " + editor;
                    }
                }
            }

        } else if (codeStr.equals("BK:  ")) {
            article.bookTitleStr = prepareForInsert(line.substring(5),1);
        } else if (codeStr.equals("EMA: ")) {
            article.emailAddressStr = prepareForInsert(line.substring(5).trim(),1);
        } else if (codeStr.equals("FTXT:")) {
            StringTokenizer fullTextTokens=new StringTokenizer(prepareForInsert(line.substring(7),1),",");
            int fTokenCount=fullTextTokens.countTokens();
            article.fullTextSources = new String[fTokenCount];
            article.fullTextLinks = new String[fTokenCount];
            for (int i=0; i<fTokenCount;i++) {
                String fToken=fullTextTokens.nextToken().trim();
                int urlPos = fToken.indexOf("http");
                article.fullTextSources[i] = fToken.substring(0,urlPos).trim();
                article.fullTextLinks[i] = fToken.substring(urlPos).trim();
            }
        } else if (codeStr.equals("LA:  ")) {
            article.languageStr = prepareForInsert(line.substring(5),1);

        } else if (codeStr.equals("MT:  ")) {
            article.meetingInfoStr = prepareForInsert(line.substring(5),1);
            StringTokenizer t = new StringTokenizer( article.meetingInfoStr,",");
            int chunkCount = t.countTokens();
            if ( chunkCount > 0 ) {
                if ( article.meetingTokenList == null ) {
                    article.meetingTokenList = new ArrayList();
                }
                for (int i=0; i<chunkCount; i++ ) {
                    String chunkStr = t.nextToken().trim();
                    article.meetingTokenList.add(chunkStr);
                }
            }
        } else if (codeStr.equals("PB:  ")) {
            article.bookPublisherStr = prepareForInsert(line.substring(5),1);
        } else if (codeStr.equals("PY:  ")) {
            Integer.parseInt(line.substring(5,9)); //just for error check
            article.pubYear = line.substring(5,9);

        } else if (codeStr.equals("SO:  ")) {
            article.sourceStr = prepareForInsert(line.substring(5),1);
            StringTokenizer t = new StringTokenizer( article.sourceStr,".");
            int chunkCount = t.countTokens();
            if ( chunkCount > 0 ) {
                if ( article.sourceTokenList == null ) {
                    article.sourceTokenList = new ArrayList();
                }
                for (int i=0; i<chunkCount; i++ ) {
                    String chunkStr=null;
                    chunkStr = t.nextToken().trim();
                    switch (i) {
                    case 0: StringTokenizer n = new StringTokenizer( chunkStr,"-" );
                        int nameCount = n.countTokens();
                        for ( int j=0; j<nameCount; j++ ) {
                            switch (j) {
                            case 0: chunkStr = n.nextToken().trim(); break;
                            default: chunkStr += " " + n.nextToken(); break;
                            }
                        }
                        if ( chunkStr != null ) {
                            article.sourceTokenList.add(chunkStr); // load only the journal name into tokens
                            article.sourceStr = chunkStr;
                        }
                        break;
                    default: StringTokenizer t2 = new StringTokenizer( chunkStr,";");
                        int pieceCount = t2.countTokens();
                        for (int k=0; k<pieceCount; k++) {
                            String pieceStr = t2.nextToken().trim();
                            //outFile.println("pieceStr: " + pieceStr);
                            switch(k) {
                            case 0: // [print] August 2003 2003
                                String lastFragmentStr=null; // for repeating 2003's
                                StringTokenizer t3 = new StringTokenizer( pieceStr," ");
                                int fragmentCount = t3.countTokens();
                                for (int f=0; f<fragmentCount; f++) {
                                    String fragmentStr = t3.nextToken();
                                    //outFile.println("fragment " + f + ": " + fragmentStr);
                                    boolean addThisFragment=false;
                                    if (fragmentStr.indexOf("[print]")<0 &&
                                        fragmentStr.indexOf("[cd-rom]")<0 &&
                                        fragmentStr.indexOf("[e-file]")<0 ) {
                                        if ( lastFragmentStr==null ) {
                                            addThisFragment = true;
                                        } else if (!fragmentStr.equals(lastFragmentStr)) {
                                            addThisFragment = true;
                                        }
                                    }
                                    if (addThisFragment) {
                                        article.sourceStr += ", " + fragmentStr;
                                        lastFragmentStr = fragmentStr;
                                    }
                                }
                                break;
                            default: if ( pieceStr.indexOf(".") >= (pieceStr.length()-1)) {
                                pieceStr = pieceStr.substring(0,pieceStr.length()-1);
                            }
                                article.sourceStr += "; " + pieceStr;
                                break;
                            }
                        }
                        if ( article.sourceStr.indexOf("2003; 2003;") >= 0 ) {
                            //outFile.println("");
                            //outFile.println("article.sourceStr: " + article.sourceStr + " at line " + lineCount );
                        }
                        break;
                    }
                }
            }

        } else if (codeStr.equals("TI:  ")) {
            article.titleStr = stripLastPeriod( prepareForInsert(line.substring(5),1) );
            //outFile.println(article.titleStr);
        } else if (codeStr.equals("UD:  ")) {
            article.updateCodeStr = line.substring(5,9) + "-" + line.substring(9,11) + "-" + line.substring(11,13);
        } else if (codeStr.equals("WEBLH")) {
            if (line.length() > 25) {
                article.linkToHoldingsStr = line.substring(25);
            }

        } else {
            Integer fieldCount = (Integer)ignoredFieldCounts.get(codeStr);
            if(fieldCount == null)
                ignoredFieldCounts.put(codeStr,new Integer(1));
            else
                ignoredFieldCounts.put(codeStr,new Integer(fieldCount.intValue() + 1));
        }
        return article;
    }

    private static String citationText =
        "This citation and abstract are made available courtesy of BIOSIS, " +
        "Biological Abstracts, "+
        "Inc., Two Commerce Square, 2001 Market Street, Suite 700, "+
        "Philadelphia, PA 19103-7095. BIOSIS "+
        "is a registered trademark of Biological Abstracts, Inc.";

    private static final void showHelp(){
        System.out.println("BiosisLoad will move data from a biosis download file into the" +
                " pubs and tokens tables.");
        System.out.println("java BiosisLoad <ConfigFile> <datafile.dat> true|false");
        System.out.println("The ConfigFile should have db and other configuration info.");
        System.out.println("The datafile.dat should be the data downloaded from biosis.");
        System.out.println("the last argument indicates if the db inserts should be done or not.");
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




