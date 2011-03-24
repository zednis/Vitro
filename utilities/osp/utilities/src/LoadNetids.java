import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

import java.util.*;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

/**
 stick netid from tab delimited file into vivo
*/	
class LoadNetids {
    /* bdc34: This seems to load netids from a tab delimited file. 
     * It is NOT used in the OSP download */
	public static void main (String args[]) {
		try	{
			makeConnections();
			BufferedReader in = null;
			PrintWriter out = null;
			String infileName = "";
			boolean doDBInserts=false;
			if (args.length > 0) {
				infileName = args[0];
   	      		in = new BufferedReader(new FileReader(infileName));
				out= new PrintWriter( new FileWriter( infileName + ".messages.txt"));
				if ( args.length > 1 ) {
					String updateParameter = args[1];
					if ( updateParameter.equalsIgnoreCase("true")) 
						doDBInserts = true;
				}
			} else {
				System.out.println("Please specify a file to read and optionally whether to do actual inserts");
            	System.exit(0);
         	}
			loadTabFile(  in, out, doDBInserts );

			if (out !=null) {
				out.flush();
				out.close();
			}
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


	public static void loadTabFile( BufferedReader in, PrintWriter out, boolean doInserts )
		throws IOException, SQLException{
		int lineCount=0;
		int facultyInsertedCount=0, newDeptRelationCount=0, newExternalIdCount=0;
		String line="";
		while ((line = in.readLine()) != null) {
			if (line.equals(""))  // JCR 20041103 to take care of Windows files with extra line feeds
				continue;
			++lineCount;			
			doLine(line,lineCount);
		}			
		out.flush();
		out.close();
		System.out.println("processed " + lineCount + " lines of input");
	}

	static int insertCount=0;
	static void doLine(String line,int lineNum)throws SQLException{
		String entityid=getTabField(line,1);
		String netid=getTabField(line,2);
		
		if(netid==null||entityid==null){
			System.out.println("skipped line " + lineNum);			
			return;
		}
		
		entityid=cleanInput(entityid);
		netid=cleanInput(netid);		
		if(netid.indexOf("@")<0)
			netid+="@cornell.edu";
		
		String inst="INSERT INTO EXTERNALIDS (EXTERNALIDTYPE,VALUE,ENTITYID) VALUES (101,'";
		inst=inst+netid+"',"+entityid+")";
		System.out.println("the insert: " + inst);
		
		Statement stmt = global_vivoCon.createStatement();
		insertCount += stmt.executeUpdate(inst);
	}

	/**
	   0 is the first tab field from the start of the line to the first tab.
	   @returns null if the field iField is not there, if the line if null.
	*/
	protected static String getTabField(String line, int iField){
		if(line == null || line.length() == 0)
			return null;				
		String fields[]=null;
		try{fields = line.split("\t");}
		catch(PatternSyntaxException ex){System.out.println("getTabField patern is bad"	+ex	);}		
		if(fields == null || iField > fields.length-1  )
			return null;
		return fields[iField];		
	}

	protected static String cleanInput( String input_str ) {
		return escapeApostrophes(input_str.trim().replaceAll("\"",""));
	}
	protected static String stripQuotes( String termStr ) {
		int characterPosition= -1;
		while ((characterPosition=termStr.indexOf(34,characterPosition+1))==0) {
			termStr = termStr.substring(characterPosition+1);
		}
		return termStr;
	}

	protected static String escapeApostrophes( String termStr ) {
		int characterPosition= -1;
		if (termStr==null || termStr.equals("")) {
			return termStr;
		}
		while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
			termStr = termStr.substring(characterPosition+1);
		}
		characterPosition=-1;
		while ( ( characterPosition = termStr.indexOf( 39, characterPosition+1 ) ) >= 0 ) {
			if ( characterPosition == 0 ) // just drop it
				termStr = termStr.substring( characterPosition+1 );
			else if ( termStr.charAt(characterPosition-1) != 92 ) // already escaped
				termStr = termStr.substring(0,characterPosition) + "\\" + termStr.substring(characterPosition);
			++characterPosition;
		}
		return termStr;
	}

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

			String url = props.getProperty("localosp.url");
			String username = props.getProperty("localosp.username");
			String password = props.getProperty("localosp.password");
			System.out.println("local database: " + url);			
			global_localCon = DriverManager.getConnection( url, username, password );
			
			url = props.getProperty("vivo.url");
			username = props.getProperty("vivo.username");
			password = props.getProperty("vivo.password");
			System.out.println("vivo database: " + url);			
			global_vivoCon = DriverManager.getConnection( url, username, password );

			url = props.getProperty("changegroup.url");
			username = props.getProperty("changegroup.username");
			password = props.getProperty("changegroup.password");
			System.out.println("ChangeGroup database: " + url);			
			global_changeGroup = DriverManager.getConnection( url, username, password );

		}
	}	
	
	static Connection global_vivoCon=null, global_localCon=null,global_changeGroup=null;	
}
