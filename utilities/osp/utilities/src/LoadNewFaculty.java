/** 
 * @version 0.9 2004-12-21
 * @author Jon Corson-Rikert
 */

import java.net.*;
import java.sql.*;
import java.io.*;
import java.util.*;

class LoadNewFaculty {
    /* bdc34: This seems to load faculty from a tab delimited file. 
     * It is NOT used in the OSP download */
    
	static final String WHICH_APP="vivo3real";
	static final int DEPT_HRCODE_EXTERNAL_ID_TYPE=202;
	static final boolean NOISY=true;
	static ArrayList keywordList=null, collabList=null;
	static TreeMap deptTreeMap=null;

/*	static final int REMOVE_VALUE   =0;
	static final int ONLY_THIS_VALUE=1;
	static final int ADD_VALUE      =2; */

	static int flagUpdateCount=0;

	public static void main (String args[]) {
		try	{
			Connection con = getConnection();

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
					if ( updateParameter.equalsIgnoreCase("true")) {
						doDBInserts = true;
					}
				}
			} else {
				System.out.println("Please specify a file to read and optionally whether to do actual inserts");
            	System.exit(0);
         	}
			loadNewFaculty( con, in, out, doDBInserts );

         	if (con!=null) {
				con.close();
			}
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


	public static void loadNewFaculty( Connection con, BufferedReader in, PrintWriter out, boolean doInserts )
		throws IOException, SQLException
	{
		Statement stmt = con.createStatement();
		int lineCount=0;
		int facultyInsertedCount=0, newDeptRelationCount=0, newExternalIdCount=0;

		String line = in.readLine(); // ignore first line -- column headings
		while ((line = in.readLine()) != null) {
			if (!line.equals("")) { // JCR 20041103 to take care of Windows files with extra line feeds
				++lineCount;
				int tabPosition = line.indexOf('\t');
				int lastTabPosition=0;
				if ( tabPosition < 0 ) { //name, column1
					out.println("<p>line " + lineCount + " cuts off before 1st tab position");
					out.flush();
					stmt.close();
					return;
				}
				String nameStr=cleanInput(line.substring(0,tabPosition));

				lastTabPosition = tabPosition;
				tabPosition = line.indexOf('\t',lastTabPosition + 1);
				String netIdStr = null;
				if ( tabPosition > lastTabPosition ) { //netid column2
					netIdStr = cleanInput(line.substring(lastTabPosition + 1,tabPosition))+"@cornell.edu";
				} else {
					out.println("<p>line " + lineCount + " cuts off before 2nd tab position (netId)" + "</p>");
					out.flush();
					stmt.close();
					return;
				}

				lastTabPosition = tabPosition;
				tabPosition = line.indexOf('\t',lastTabPosition + 1);
				String deptHRCode = null;
				if ( tabPosition > lastTabPosition ) { //deptHRcode column3
					deptHRCode = cleanInput(line.substring(lastTabPosition + 1,tabPosition));
				} else {
					out.println("<p>line " + lineCount + " cuts off before 3rd tab position (dept HR code)" + "</p>");
					out.flush();
					stmt.close();
					return;
				}

				lastTabPosition = tabPosition;
				tabPosition = line.indexOf('\t',lastTabPosition + 1);
				String etypeStr=null;
				int etypeId=0;
				if ( tabPosition > lastTabPosition ) { //type column4
					etypeStr = cleanInput(line.substring(lastTabPosition + 1,tabPosition));
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
					} else {
						System.out.println("Error: unexpected type name "+etypeStr+" found on input line "+lineCount);
						out.println("Error: unexpected type name "+etypeStr+" found on input line "+lineCount);
					}
				} else {
					out.println("<p>line " + lineCount + " cuts off before 4th tab position (type)" + "</p>");
					out.flush();
					stmt.close();
					return;
				}

				int classId=0;
				switch (etypeId) {
					case 31: classId=232; break; //Academic Employee
					case 33: classId=241; break; //Non-Academic Employee
					case 94: classId=232; break; //Academic Employee
					default: System.out.println("Error: unexpected type id "+etypeId+" found on input line "+lineCount);
							 out.println("Error: unexpected type id "+etypeId+" found on input line "+lineCount);
							 break;
				}


				lastTabPosition = tabPosition;
				tabPosition = line.indexOf('\t',lastTabPosition + 1);
				String monikerStr = null;
				String keywordStr = null;
				String URLStr = null;
				String anchorStr=null;
				if ( tabPosition > lastTabPosition ) { //moniker column5
					monikerStr = cleanInput(line.substring(lastTabPosition + 1,tabPosition));

					lastTabPosition = tabPosition;
					tabPosition = line.indexOf('\t',lastTabPosition + 1);
					if ( tabPosition > lastTabPosition ) { //keyword column 6
						keywordStr = cleanInput(line.substring(lastTabPosition + 1,tabPosition));

						lastTabPosition = tabPosition;
						tabPosition = line.indexOf('\t',lastTabPosition + 1);
						if ( tabPosition > lastTabPosition ) { //url column 7
							URLStr = cleanInput(line.substring(lastTabPosition + 1,tabPosition));

							lastTabPosition = tabPosition; //anchor column 8 
							anchorStr = cleanInput(line.substring(lastTabPosition));
						}
					}
				}

				out.println("netId: "+netIdStr+"\n+name: "+nameStr+"\ntypeId: "+etypeId+"\nmoniker: "+monikerStr+"\ndept: "+deptHRCode+"\nkeywords: "+keywordStr+"\nURL: "+URLStr+"\nanchor: "+anchorStr);
				String fieldsStr="INSERT INTO entities (name,classId,typeId,moniker";
				String valuesStr=" VALUES ('"+nameStr+"',"+classId+","+etypeId+",";
				valuesStr+=monikerStr==null || monikerStr.equals("") ? "'"+etypeStr+"'" : "'"+monikerStr+"'";
				if (keywordStr != null && !keywordStr.equals("")) {
					fieldsStr+=",blurb";
					valuesStr+=",'"+keywordStr+"'";
				}
				if (URLStr != null && !URLStr.equals("")) {
					if (anchorStr != null && !anchorStr.equals("")) {
						fieldsStr+=",URL,anchor";
						valuesStr+=",'"+URLStr+"','"+anchorStr+"'";
					} else {
						System.out.println("Error: URLStr but no anchor on line "+lineCount);
						out.println("Error: URLStr but no anchor on line "+lineCount);
						return;
					}
				}
				fieldsStr+=")";
				valuesStr+=")";
				int newEntityId=-1;
				if (doInserts) {
					try {
						facultyInsertedCount+=stmt.executeUpdate(fieldsStr+valuesStr);
						String maxQuery="SELECT max(id) FROM entities";
						try {
							ResultSet maxRS=stmt.executeQuery(maxQuery);
							if (maxRS.next()) {
								newEntityId=maxRS.getInt(1);
							}
						} catch (SQLException ex) {
							System.out.println("Error on new entity insert via "+fieldsStr+valuesStr+": "+ex.getMessage());
							out.println("Error on new entity insert via "+fieldsStr+valuesStr+": "+ex.getMessage());
							return;
						}
					} catch (SQLException ex) {
						System.out.println("Error on new entity insert via "+fieldsStr+valuesStr+": "+ex.getMessage());
						out.println("Error on new entity insert via "+fieldsStr+valuesStr+": "+ex.getMessage());
						return;
					}

					String netIdInsertStr="INSERT INTO externalids (value,entityId,externalIdType) values ('"+netIdStr+"',"+newEntityId+",101)";
					try {
						newExternalIdCount += stmt.executeUpdate(netIdInsertStr);
					} catch (SQLException ex) {
						System.out.println("Error on new netId insert via "+netIdInsertStr+": "+ex.getMessage());
						out.println("Error on new netId insert via "+netIdInsertStr+": "+ex.getMessage());
						return;
					}
				} else {
					++facultyInsertedCount;
					++newExternalIdCount;
				}

				int deptId=-1,etypes2RelationsId=-1,campusFlagVal=-1;
				String departmentEntityQuery="SELECT entities.id,entities.typeId,entities.flag3Set+0 FROM externalids,entities WHERE externalids.entityId=entities.id AND externalids.externalIdType="+DEPT_HRCODE_EXTERNAL_ID_TYPE+" AND externalids.value like '" + deptHRCode + "'";
				try {
					ResultSet departmentRS=stmt.executeQuery(departmentEntityQuery);
					int deptByCodeCount=0;
					int deptTypeId=0;
					while (departmentRS.next()) {
						++deptByCodeCount;
						deptId=departmentRS.getInt(1);
						deptTypeId=departmentRS.getInt(2);
						String campusFlagStr=departmentRS.getString(3);
						if (campusFlagStr != null && !campusFlagStr.equals("")) {
							campusFlagVal=departmentRS.getInt(3);
						}
					}
					departmentRS.close();
					switch (deptByCodeCount) {
						case 0: System.out.println("no match found by deptHRCode for: " + deptHRCode);
								return;
						case 1: switch (deptTypeId) {
									case  61: //academic department class 229 Academic Unit
											  switch (etypeId) {
												  case 31: etypes2RelationsId=48; break; //faculty
												  case 33: etypes2RelationsId=313; break; //non-academic
												  case 94: etypes2RelationsId=117; break; //academic staff
												  default: System.out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   return;
											 }
											 break;
									case  68: //research unit or center class 225 R&D Unit
											  switch (etypeId) {
												  case 31: etypes2RelationsId=120; break; //faculty
												  case 33: etypes2RelationsId=190; break; //non-academic
												  case 94: etypes2RelationsId=443; break; //academic staff
												  default: System.out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   return;
											 }
											 break;
									case  83: //academic program office class 229 Academic Unit
											  switch (etypeId) {
												  case 31: etypes2RelationsId=121; break; //faculty
												  case 33: etypes2RelationsId=575; break; //non-academic
												  case 94: etypes2RelationsId=418; break; //academic staff
												  default: System.out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   return;
											 }
											 break;
									case  84: //administrative unit class 230 Academic Support Unit
											  switch (etypeId) {
												  case 31: etypes2RelationsId=567; break; //faculty
												  case 33: etypes2RelationsId=118; break; //non-academic
												  case 94: etypes2RelationsId=566; break; //academic staff
												  default: System.out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   return;
											 }
											 break;
									case  93: //academic division class 229 Academic Unit
											  switch (etypeId) {
												  case 31: etypes2RelationsId=49; break; //faculty
												  case 33: etypes2RelationsId=574; break; //non-academic
												  case 94: etypes2RelationsId=314; break; //academic staff
												  default: System.out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   return;
											 }
											 break;
									case 123: //extension research unit class 225 R&D Unit
											  switch (etypeId) {
												  case 31: etypes2RelationsId=403; break; //faculty
												  case 33: etypes2RelationsId=367; break; //non-academic
												  case 94: etypes2RelationsId=577; break; //academic staff
												  default: System.out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   out.println("Error: unexpected etype id type "+etypeId+" to link "+nameStr+" to department "+deptHRCode+" on line "+lineCount);
											  			   return;
											 }
											 break;
									default:  System.out.println("Error: unexpected department id type "+deptTypeId+" for department "+deptHRCode+" on line "+lineCount);
											  out.println("Error: unexpected department id type "+deptTypeId+" for department "+deptHRCode+" on line "+lineCount);
											  return;
								}
								break;
							default:  System.out.println("Error: found "+deptByCodeCount+" matches for department "+deptHRCode+" on line "+lineCount);
									  out.println("Error: found "+deptByCodeCount+" matches for department "+deptHRCode+" on line "+lineCount);
									  return;
					}
					String insertEnts2EntsStr="INSERT INTO ents2ents (domainId,rangeId,etypes2RelationsId) VALUES ("+deptId+","+newEntityId+","+etypes2RelationsId+")";
					if (doInserts) {
						try {
							newDeptRelationCount += stmt.executeUpdate(insertEnts2EntsStr);
						} catch (SQLException ex) {
							System.out.println ("SQLException from inserting new department relationship via " + insertEnts2EntsStr + ": " + ex.getMessage());
							out.println ("SQLException from inserting new department relationship via " + insertEnts2EntsStr + ": " + ex.getMessage());
							return;
						}
					} else {
						out.println("would be updating entity " + newEntityId + " with relationship: " + insertEnts2EntsStr);
						++newDeptRelationCount;
					}

					if (doInserts) {
						String flagMessage=setEntityFlags(con,newEntityId,2,4,campusFlagVal,doInserts);
						if (flagMessage != null) {
							System.out.println(flagMessage);
							out.println(flagMessage);
							out.flush();
							return;
						}
					}
				} catch (SQLException ex) {
					System.out.println ("SQLException from finding existing entity via " + departmentEntityQuery + ": " + ex.getMessage());
					return;
				}
			}
		}

		stmt.close();
		System.out.println("created "+newExternalIdCount+" new netIds, inserted "+facultyInsertedCount+" new faculty with "+newDeptRelationCount+" dept relationships");
		out.println("created "+newExternalIdCount+" new netIds, inserted "+facultyInsertedCount+" new faculty with "+newDeptRelationCount+" dept relationships");
		/*
		if (keywordList != null && keywordList.size()>0) {
			if (keywordList.size() > 1 ) {
				out.println("KEYWORDS ("+keywordList.size()+"):");
				Collections.sort( keywordList, new Comparator() {
					public int compare( Object obj1, Object obj2 ) {
						String first  = (String) obj1;
						String second = (String) obj2;
						return first.toLowerCase().compareTo(second.toLowerCase());
					}
				});
			}
			int columnCount=5;
			int columnIndex=0;
			Iterator keywdIter = keywordList.iterator();
			while ( keywdIter.hasNext() ) {
				++columnIndex;
				String keywd = (String)keywdIter.next();
				out.print("|"+keywd);
				if (columnIndex % columnCount==0) {
					out.println("|");
				}
			}
			out.println();
		}
		if (collabList != null && collabList.size()>0) {
			if (collabList.size() > 1 ) {
				out.println("COLLABORATIVE RESEARCH AREAS ("+collabList.size()+"):");
				Collections.sort( collabList, new Comparator() {
					public int compare( Object obj1, Object obj2 ) {
						String first  = (String) obj1;
						String second = (String) obj2;
						return first.toLowerCase().compareTo(second.toLowerCase());
					}
				});
			}
			int columnCount=5;
			int columnIndex=0;
			Iterator collabIter = collabList.iterator();
			while ( collabIter.hasNext() ) {
				++columnIndex;
				String collab = (String)collabIter.next();
				out.print("|"+collab);
				if (columnIndex % columnCount==0) {
					out.println("|");
				}
			}
			out.println();
		}

		if (deptTreeMap != null && deptTreeMap.size()>0) {
			out.println("DEPARTMENTS ("+deptTreeMap.size()+"):");
			Set keySet=deptTreeMap.keySet();
			Iterator keySetIter=keySet.iterator();
			while (keySetIter.hasNext()) {
				String deptKey = (String)keySetIter.next();
				out.println("|"+deptKey+"|"+deptTreeMap.get(deptKey)+"|");
			}
			out.println();
		}
		*/
		out.flush();
		out.close();
		System.out.println("processed " + lineCount + " lines of input");
	}


	private static String setEntityFlags(Connection con,int entity_id,int flag1_val,int flag2_val,int flag3_val,boolean doUpdates)
		throws SQLException
	{
		if (flag1_val<0) {
			return ("Error: cannot set entity flag 1 to a value less than zero: " +flag1_val);
		}
		if (flag2_val<0) {
			return ("Error: cannot set entity flag 2 to a value less than zero: " +flag2_val);
		}
		if (flag3_val<0) {
			return ("Error: cannot set entity flag 3 to a value less than zero: " +flag3_val);
		}
		Statement stmt=con.createStatement();
		String existingValuesQuery="SELECT flag1Set+0,flag2Set+0,flag3Set+0 FROM entities WHERE id="+entity_id;
		try {
			ResultSet existingRS=stmt.executeQuery(existingValuesQuery);
			if (existingRS.next()) {
				String existingFlag1Str=existingRS.getString(1);
				int existingFlag1=-1;
				if (existingFlag1Str!=null && !existingFlag1Str.equals("")) {
					existingFlag1=existingRS.getInt(1);
				}
				String existingFlag2Str=existingRS.getString(2);
				int existingFlag2=-1;
				if (existingFlag2Str!=null && !existingFlag2Str.equals("")) {
					existingFlag2=existingRS.getInt(2);
				}
				String existingFlag3Str=existingRS.getString(3);
				int existingFlag3=-1;
				if (existingFlag3Str!=null && !existingFlag3Str.equals("")) {
					existingFlag3=existingRS.getInt(3);
				}
				String updateQuery="UPDATE entities SET ";
				updateQuery+=(existingFlag1<0)?"flag1Set="+flag1_val  : "flag1Set=flag1Set|"+flag1_val;
				updateQuery+=(existingFlag2<0)?",flag2Set="+flag2_val : ",flag2Set=flag2Set|"+flag2_val;
				updateQuery+=(existingFlag3<0)?",flag3Set="+flag3_val : ",flag3Set=flag3Set|"+flag3_val;
				updateQuery+=" WHERE id="+entity_id;
				if (doUpdates) {
					try {
						flagUpdateCount = stmt.executeUpdate(updateQuery);
					} catch (SQLException ex) {
						return ("error on trying to set flags via "+updateQuery+": "+ ex.getMessage());
					}
				} else {
					System.out.println("existing flag1: "+existingFlag1+", flag2: "+existingFlag2+", flag3: "+existingFlag3);
				}
			} else {
				return("Error: could not find entity " + entity_id + " to update flags");
			}
			existingRS.close();
		} catch (SQLException ex) {
			return("error trying to retrieve existing flag values via " + existingValuesQuery + ": " + ex.getMessage());
		}
		return null;
	}



/*	private static int updateEntityKeywords(Connection con,int entity_id,String netId_str,String keyword_str)
		throws SQLException, IOException
	{
		int count=0;
		if (keyword_str != null && !keyword_str.equals("")) {
			Statement stmt=con.createStatement();
			String updateQuery="UPDATE entities SET blurb='" + keyword_str + "' WHERE id=" + entity_id;
			try {
				count += stmt.executeUpdate(updateQuery);
			} catch (SQLException ex) {
				System.out.println ("SQLException from updating faculty " + netId_str + " via :" + updateQuery + ": " + ex.getMessage());
				stmt.close();
				return 0;
			}
			stmt.close();
		}
		return count;
	}

	private static String fixCapitalization(String input_str)
	{
		String outputStr=null;
		StringTokenizer fTokens = new StringTokenizer(input_str," ");
		int fragmentCount=fTokens.countTokens();
		for (int f=0; f<fragmentCount; f++) {
			String fragment=fTokens.nextToken().trim();
			int firstChar=(int)fragment.charAt(0);
			if (firstChar==40 && fragment.length()==3) { //open parenthesis for (I) or (O) terms -- throw out term
				fragment=null;
			} else if (firstChar>64 && firstChar<91) {
				//System.out.println("found uppercase start to " + fragment);
				if (!fragment.equalsIgnoreCase("Christmas")) {
					int secondChar=(int)fragment.charAt(1);
					if (secondChar<91) { // catches C. elegans
						;
					} else {
						fragment=fragment.toLowerCase();
					}
				}
			}
			if (f==0 || outputStr==null) {
				outputStr=fragment;
			} else if (fragment != null) {
				outputStr += " " + fragment;
			}
		}
		return outputStr;
	} */

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

	public static Connection getConnection()
      	throws SQLException, IOException
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



