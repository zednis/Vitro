import java.net.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import com.novell.ldap.*;

	/**
	   
	   from RFC2254:
	   String Search Filter Definition

	   The string representation of an LDAP search filter is defined by the
	   following grammar, following the ABNF notation defined in [5].  The
	   filter format uses a prefix notation.

	   filter     = "(" filtercomp ")"
	   filtercomp = and / or / not / item
	   and        = "&" filterlist
	   or         = "|" filterlist
	   not        = "!" filter
	   filterlist = 1*filter
	   item       = simple / present / substring / extensible
	   simple     = attr filtertype value
	   filtertype = equal / approx / greater / less
	   equal      = "="
	   approx     = "~="
	   greater    = ">="
	   less       = "<="
	   extensible = attr [":dn"] [":" matchingrule] ":=" value
	   / [":dn"] ":" matchingrule ":=" value
	   present    = attr "=*"
	   substring  = attr "=" [initial] any [final]
	   initial    = value
	   any        = "*" *(value "*")
	   final      = value
	   attr       = AttributeDescription from Section 4.1.5 of [1]
	   matchingrule = MatchingRuleId from Section 4.1.9 of [1]
	   value      = AttributeValue from Section 4.1.6 of [1]

	   The attr, matchingrule, and value constructs are as described in the
	   corresponding section of [1] given above.

	   If a value should contain any of the following characters

	   Character       ASCII value
	   ---------------------------
	   *               0x2a
	   (               0x28
	   )               0x29
	   \               0x5c
	   NUL             0x00

	   the character must be encoded as the backslash '\' character (ASCII
	   0x5c) followed by the two hexadecimal digits representing the ASCII
	   value of the encoded character. The case of the two hexadecimal
	   digits is not significant.

	   This simple escaping mechanism eliminates filter-parsing ambiguities
	   and allows any filter that can be represented in LDAP to be
	   represented as a NUL-terminated string. Other characters besides the
	   ones listed above may be escaped using this mechanism, for example,
	   non-printing characters.

	   For example, the filter checking whether the "cn" attribute contained
	   a value with the character "*" anywhere in it would be represented as
	   "(cn=*\2a*)".

	   Note that although both the substring and present productions in the
	   grammar above can produce the "attr=*" construct, this construct is
	   used only to denote a presence filter.

	   5. Examples

	   This section gives a few examples of search filters written using
	   this notation.

	   (cn=Babs Jensen)
	   (!(cn=Tim Howes))
	   (&(objectClass=Person)(|(sn=Jensen)(cn=Babs J*)))
	   (o=univ*of*mich*)

	   example of a ldap searchfilter for directory.cornell.edu:
	   (&(sn=*Caruso)(|(&(givenname=*brian*)(cornelledumiddlename=d*))(givenname=brian)))

	   These are all of the attribute names that directory.cornell.edu knows about:
	   cornelleduunivtitle1			
	   homePhone			 
	   homePostalAddress			
	   displayName			
	   cn			
	   cornelleduregtemp2			
	   cornelleduregtemp1			
	   cornelledudeptname2			
	   cornelledumiddlename			
	   givenName			
	   sn			
	   cornelledudeptname1			
	   cornelledunetid			
	   edupersonprimaryaffiliation			
	   cornelledutype			
	   cornelleduunivtitle2			
	   edupersonnickname			
	   labeledUri			
	   description			
	   cornelledulocaladdress			
	   cornelledulocalphone			
	   facsimileTelephoneNumber			
	   cornelleduwrkngtitle2			
	   cornelleduwrkngtitle1			
	   pager			
	   mobile			
	   cornelleducampusphone			
	   cornelleducampusaddress			
	   cornelledudefaultpo			
	   mail			
	   mailRoutingAddress			
	   objectClass			
	   o			
	   c			
	   uid			
	   eduPersonOrgDN			
	   eduPersonPrincipalName			

	   
	*/
	
class CheckFacultyInLdap {
	static final String WHICH_APP="vivo3real";
	static final int DEPT_HRCODE_EXTERNAL_ID_TYPE=202;
	static final boolean NOISY=true;
	static ArrayList keywordList=null, collabList=null;
	static TreeMap deptTreeMap=null;

	static int flagUpdateCount=0;

	public static void main (String args[]) {
		try	{
			
			//Connection con = getConnection();
			Connection con = null;

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
			loadNewFaculty( con, in, out, doDBInserts );

         	if (con!=null) 
				con.close();
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
      	} catch (LDAPException ex){
			System.out.println("LDAP exception: " + ex);
		}
	}


	public static void loadNewFaculty( Connection con, BufferedReader in, PrintWriter out, boolean doInserts )
		throws IOException, SQLException, LDAPException{
		int lineCount=0;
		int facultyInsertedCount=0, newDeptRelationCount=0, newExternalIdCount=0;
		String line="";
		while ((line = in.readLine()) != null) {
			if (line.equals(""))  // JCR 20041103 to take care of Windows files with extra line feeds
				continue;
			++lineCount;
			int tabPosition = line.indexOf('\t');
			int lastTabPosition=0;
			if ( tabPosition < 0 ) { //name, column1
				out.println("<p>line " + lineCount + " cuts off before 1st tab position");
				out.flush();
				return;
			}
			String nameStr=cleanInput(line.substring(0,tabPosition));

			String entitiesIdStr = null;
			entitiesIdStr = cleanInput(line.substring(lastTabPosition + 1));
			
			out.println("entities.id: "+entitiesIdStr+"name: "+nameStr );
			String filter=makeLdapSearchFilter(nameStr);
			if(filter==null)
				System.out.print("line number: " + lineCount + "\n");

			LDAPSearchResults res = searchLdap(filter);
			
			System.out.println(ldapResult2String(res,nameStr,filter));
		}			
		out.flush();
		out.close();
		System.out.println("processed " + lineCount + " lines of input");
	}


	private static String makeLdapSearchFilter(String fullname){
		int comma = fullname.indexOf(','),space1=-1,space2=-1;
		if(comma<0){
			System.out.print("name lacks a comma: " + fullname);
			return null;
		}
		StringBuffer filter=new StringBuffer("(&(!(type=student*))"); //no students from ldap
		String cn=null, strictGivenNames=null, looseGivenNames=null;
		String first=null,middle=null,last=null; 
		last=fullname.substring(0,comma).trim();
		space1=fullname.indexOf(' ',comma+1);
		space2=fullname.indexOf(' ',space1+1);
		if(space2 < 0){ //nothing after first name
			first=fullname.substring(space1).trim();
		}else{ //there is a middle name there
			first=fullname.substring(space1,space2).trim();
			middle=fullname.substring(space2+1).trim();
		}
		
		if(first!=null && first.indexOf('(')>0)
			first=first.replace('(',' ');
		if(first!=null && first.indexOf(')')>0)
			first=first.replace(')',' ');
		
		if(middle!=null && middle.indexOf('(')>0)
			middle=middle.replace('(',' ');		
		if(middle!=null && middle.indexOf(')')>0)
			middle=middle.replace(')',' ');
		
		if(first!=null) //check for initials
			if(first.indexOf('.')>0)
				first=first.replace('.','*');
			else
				first=first+"*";

		if(middle!=null) //check for initials
			if( middle.indexOf('.')>0)
				middle=middle.replace('.','*');
			else
				middle=middle+"*";
		
		cn="(cn="; //put together common name query
		if(first!=null){
			if(middle!=null)
				cn=cn+first+middle;
			else
				cn=cn+first;
		}
		cn=cn+last+")";
		filter.append(cn);		
			
		filter.append(")");		
		return filter.toString();
	}

		/**
	   @param searchFilter - eduora uses the searchFilter:(|(cn=*^0*)(uid=*^0*)(edupersonnickname=*^0*)) where 
	   ^0 gets replaced by the search str.
	   @param attributes - the attributes that you want from ldap, one att name per an array item. ex:
	   String[] attr = new String[] {"cornelledunetid","cn","cornelledutype","mail","mailRoutingAddress"};
	*/
	public static LDAPSearchResults searchLdap(String searchFilter)throws LDAPException{

		int ldapPort = LDAPConnection.DEFAULT_PORT;
		int searchScope = LDAPConnection.SCOPE_SUB;
		int ldapVersion  = LDAPConnection.LDAP_V3;        
		String ldapHost = "directory.cornell.edu";
		String loginDN  = ""; //no login id needed
		String password = "";// no password needed	  

		String searchBase = "o=Cornell University, c=US";
		String attributes[]={LDAPConnection.ALL_USER_ATTRS,"cn"};
		
		LDAPConnection lc = new LDAPConnection();
		LDAPSearchResults thisResult = null;
		try {
			lc.connect( ldapHost, ldapPort );

			LDAPConstraints constraints = new LDAPConstraints(0,true,null,0);
			lc.bind( ldapVersion, loginDN, password, constraints );

			thisResult = lc.search(  searchBase, searchScope, searchFilter, attributes, false);
		} catch( LDAPException e ) {
			System.out.println( "error: " + e );
			String serverError = null;
			if( (serverError = e.getLDAPErrorMessage()) != null) 
				System.out.println("Server: " + serverError);
			return null;
		}
		return thisResult;
	}

	/**
	   tab delimited output string fomrat:	   
	   name	netId	deptHRcode	type	moniker	keywords	URL	anchor
	*/
	private static String ldapResult2String(LDAPSearchResults res, String orgName,String ldapFilter){
		/*the strings are ldap attribute names for tab field i*/
		String map[][]= {
			//	{"cn","displayName"}, //we'll use the original vivo name
			{"mail","uid"},       
			{"cornelledudeptname1","cornelledudeptname2"},
			{"cornelledutype","edupersonprimaryaffiliation"},
			{"cornelleduwrkngtitle1","cornelleduwrkngtitle2"},
			{},
			{"labeledUri"},
			{"description"},
			{"cornelledudeptname2"}};
		StringBuffer output=new StringBuffer("");
		output.append(orgName).append("\t"); //just stick the original name on the front.
		while(res.hasMoreElements()){
			LDAPEntry entry=(LDAPEntry)res.nextElement();			
			//for tab field i look in map[i] for ldap attribute names, output first non-null value
			for(int iField=0;iField<map.length;iField++){
				
				for(int iName=0;iName< map[iField].length; iName++){
					LDAPAttribute lAtt=entry.getAttribute(map[iField][iName]);

					if(lAtt!=null){
						String value=lAtt.getStringValue();
						if(value!=null && value.length()>0 ){
							output.append(value);
							break;
						}
					}
				}
				output.append("\t");
			}
			output.append(ldapFilter);
			if(res.hasMoreElements()){
				output.append("\n").append(orgName).append("\t");
			}
		}
		return output.toString();
	}

	private static String cleanInput( String input_str ) {
		return escapeApostrophes(input_str.trim().replaceAll("\"",""));
	}
	private static String stripQuotes( String termStr ) {
		int characterPosition= -1;
		while ((characterPosition=termStr.indexOf(34,characterPosition+1))==0) {
			termStr = termStr.substring(characterPosition+1);
		}
		return termStr;
	}

	private static String escapeApostrophes( String termStr ) {
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

	public static Connection getConnection() throws SQLException, IOException
    {
		Properties props = new Properties();
		String fileName = "../../"+WHICH_APP+"_jdbc.properties";
		FileInputStream in = new FileInputStream(fileName);
		props.load(in);

		String drivers = props.getProperty("jdbc.drivers");
		if (drivers != null) {
			System.setProperty("jdbc.drivers", drivers);
		}
		String url = props.getProperty("jdbc.url");
		String username = props.getProperty("jdbc.username");
		String password = props.getProperty("jdbc.password");

		return DriverManager.getConnection( url, username, password );
	}
}
