import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;
/**
 * adapted and extended by Jonathan Corson-Rikert
 * LATEST CHANGES
 * 20021203 for CUGIR removed references to directory "assets" -- icons will be in "icons" rather than assets/icons
 *** 9/18/2002: changed generation of links to use ../servlet instead of /databaseName/servlet so that
 ***			both rikert.mannlib.cornell.edu/newCCRP and mcknight.ccrp.cornell.edu would work

 * from CoreJava vol 2, chapter 4, QueryDB.java
 * @version 1.20 1999-08-16
 * @author Cay Horstmann
 */
import java.net.*;
import java.sql.*;
// import formbeans.LoginFormBean; not needed unless want to use individual user names for access to tables
// import formbeans.CommonSearchFormBean;

public class QueryServlet extends HttpServlet
{
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
		throws ServletException, IOException
	{
		String doProcessMessage="no message";
		try {
			doProcessMessage = doProcess( request, response );
		} catch( IOException q ) {
			System.out.println( "doProcess fails with message: " + doProcessMessage );
			System.out.println( "IOException: " + q.getMessage());
			//q.printStackTrace();
      	} catch (ServletException ex) {
			System.out.println( "doProcess fails with message: " + doProcessMessage );
			System.out.println("ServletException: " + ex.getMessage());
         	//ex.printStackTrace ();
      	} catch (Exception e) {
			System.out.println( "doProcess fails with message: " + doProcessMessage );
			System.out.println("unknown Exception: " + e.getMessage());
         	//e.printStackTrace ();
		}
	}

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
		throws ServletException, IOException
	{
		String doProcessMessage="no message";
		try {
			doProcessMessage = doProcess( request, response );
		} catch( IOException q ) {
			System.out.println( "doProcess fails with message: " + doProcessMessage );
			System.out.println( "IOException: " + q.getMessage());
			//q.printStackTrace();
      	} catch (ServletException ex) {
			System.out.println( "doProcess fails with message: " + doProcessMessage );
			System.out.println("ServletException: " + ex.getMessage());
         	//ex.printStackTrace ();
      	} catch (Exception e) {
			System.out.println( "doProcess fails with message: " + doProcessMessage );
			System.out.println("unknown Exception: " + e.getMessage());
         	//e.printStackTrace ();
		}
	}

	public String doProcess( HttpServletRequest request,
			HttpServletResponse response)
		throws ServletException, IOException
	{
		Properties queryProps = new Properties();
		String mainQuerySpecStr;
		int globalPostGenLimit = -1;
		ArrayList compositedResultsList = new ArrayList();
		RequestDispatcher rd;
		String layoutStr = null;
		Integer columnsRetrieved = new Integer(0); // number of columns needed for layout of results in output jsp

		String returnMessage= "(no return message) ";
		lastPostQueryStr=null;

		// the database for the current context is specified in the web.xml file

		contextName  = getInitParameter("contextName");
		databaseName = getInitParameter("databaseName");

		// as is this parameter, which is necessary for patching together property file path names
		tomcatHome = getInitParameter("TOMCAT_HOME"); // has no final /

		/* the sequence feature allows stringing together a series of servlets,
		** it's not actively used now, but is useful to keep dormant for purposes
		**  of issuing a followup summary query, for example
		*/
		int querySequence = 0;
		String seqStr, ultimateStr;

		seqStr = request.getParameter("sequence");
		if ( seqStr == null ) {
			seqStr = "";
		} else {
			querySequence = Integer.parseInt( seqStr );
		}
		// end sequence feature initialization

		// retrieve the name of the query properties file
		try {
			mainQuerySpecStr = request.getParameter("queryspec");
		} catch( Exception q ) {
			mainQuerySpecStr = "defaultquery";
			returnMessage = "no queryspec parameter found";
		}

		String postQueryLimitStr;

		/* tempPostGenerationLimit=-1; reset on each pass */

		if ( (postQueryLimitStr = request.getParameter("postGenLimit")) != null  && !postQueryLimitStr.equals("")) {
			globalPostGenLimit = Integer.parseInt( postQueryLimitStr );
		} else {
			globalPostGenLimit = -1; // flag as not set so read property files values
			returnMessage += "; no postGenLimit parameter found";
		}

		String headerStr = "";  // added = "" 8/26/02 since including on output
		if ( ( headerStr=request.getParameter("header")) != null && !headerStr.equals("") ) {
			request.setAttribute( "header", headerStr );
		}

		try {
			columnsRetrieved = processQuery( request, queryProps, mainQuerySpecStr, seqStr, "", 0, 0, globalPostGenLimit, headerStr, compositedResultsList);
		} catch ( Exception c ) {
			System.out.println( "processQuery fails with exception: " + c.getMessage());
			returnMessage += "; processQuery fails with exception: " + c.getMessage();
			c.printStackTrace();
		}
		String queryActive="";

		String onwardStr = (layoutStr = queryProps.getProperty("LAYOUT")) == null
						  ? "/horizontal.jsp"
						  : "/" + layoutStr; // can be a servlet or a jsp

		if ( columnsRetrieved.intValue() > 0 && compositedResultsList.size() > 0 ) {
			// seqStr normally ""; see above note about dormant query sequencing feature
			request.setAttribute( "columncount" + seqStr, columnsRetrieved );
			request.setAttribute( "priorqueryspec", mainQuerySpecStr );
			request.setAttribute( "priorlinkwhere", request.getParameter("linkwhere") );
			/*
			if ( tempPostGenerationLimit >= 0 ) {
				String tempStr = null;
				int parameterPostGenLimit = (tempStr = request.getParameter("postGenLimit")) == null ? -1 : Integer.parseInt( request.getParameter("postGenLimit"));
				if ( parameterPostGenLimit < 0 ) {
					tempStr = "" + tempPostGenerationLimit;
					request.setAttribute( "priorpostgenlimit", tempStr );
				} else {
					request.setAttribute( "priorpostgenlimit", request.getParameter("postGenLimit") );
				}
			} else {
				request.setAttribute( "priorpostgenlimit", request.getParameter("postGenLimit") );
			}
			*/
			request.setAttribute( "priorpostgenlimit", request.getParameter("postGenLimit") );

			// seqStr adds runtime request parameter specifying sequence number to append results name
			// seqStr not actively used but keep dormant for implementing followup summary queries
			request.setAttribute("results" + seqStr, compositedResultsList);

			rd = getServletContext().getRequestDispatcher(onwardStr);
		/* not using CommonSearchForm in CUGIR
		} else if ( (queryActive = request.getParameter("queryActive")) != null && queryActive.equals("true") ) {
			// for multith:
			//addToFailures( request );
			String searchFormHandlerStr = (String) request.getAttribute("searchFormHandlerStr");
			CommonSearchFormBean formBean = (CommonSearchFormBean) request.getAttribute( searchFormHandlerStr );
			// multith: formBean.setError( "queryText",
			//				   "Sorry, no matches were found for the term '"
			//				   + request.getParameter("queryText") + "'; modify your search term(s) or try a different search mode" );
			Iterator errorIter = compositedResultsList.iterator();
			String errorResult="";
			while ( errorIter.hasNext()) {
				errorResult = errorResult + (String)errorIter.next();
			}
			if ( errorResult.equals("") ) {
				formBean.setError( "searchFailure",
							   	   "Sorry, no matches were found for this query; please modify your search parameters" );
			} else {
				formBean.setError( "searchFailure", errorResult );
			}
			formBean.setSearchStatus("failed");
			String retryStr = formBean.getRetryJSP();
			if ( retryStr != null && !(retryStr.equals(""))) {
				System.out.println("about to send failed search retry from doProcess to " + retryStr );
				rd = getServletContext().getRequestDispatcher(retryStr);
			} else {
				rd = getServletContext().getRequestDispatcher("/common_search_retry.jsp");
			}
		*/
		} else {
			request.setAttribute( "columncount" + seqStr, columnsRetrieved );
		    compositedResultsList.add("Sorry, no results were found");
			request.setAttribute("results" + seqStr, compositedResultsList);

			rd = getServletContext().getRequestDispatcher(onwardStr);
		}
		try {
			rd.forward( request, response );
		} catch (ServletException ex) {
			returnMessage += "; rd.forward to " + onwardStr + " fails;";
			System.out.println("rd.forward to " + onwardStr + " fails;");
			System.out.println("ServletException: " + ex.getMessage());
         	ex.printStackTrace ();
		}
		return returnMessage;
	}

	private static int countSpacers( String fieldStr ) {
		int count = 0, spacePosition= -1;

		while ( ( spacePosition = fieldStr.indexOf( 32, spacePosition+1 ) ) >= 0 ) {
			++count;
			// System.out.println( "Space " + count + " found at position " + spacePosition);
		}
		System.out.println(count + " spaces found in string '" + fieldStr + "', so " + (count + 1) + " fields" );
		return count;
	}

	private static int[] loadFragmentSeparators ( String fieldStr, int count ) {
		int[] spaceArray = new int[count];
		int spacePosition = -1;

		for ( int i = 0; i < count; i++ ) {
			spacePosition = fieldStr.indexOf( 32, spacePosition + 1 );
			spaceArray[i] = spacePosition;
		}
		/*
		for ( int i=0; i < count; i++ )
			System.out.println("comma "+(i+1) + " = "+commaArray[i]);
		*/
		return spaceArray;
	}


	private void doPostQuery( String querySpecStr, String postWhereStr, int maxCols, int postQGeneration, int globalPostGenLimit, String headerStr, List parentResultsList )
	{
		Integer colsRetrieved;
		HttpServletRequest nullRequest = null;

		Properties postQueryProps = new Properties();

		// System.out.println("maxCols starts postQuery as " + maxCols );

		colsRetrieved = processQuery( nullRequest, postQueryProps, querySpecStr, "", postWhereStr, maxCols, postQGeneration, globalPostGenLimit, headerStr, parentResultsList );
	}


	private Integer processQuery( HttpServletRequest request, Properties propsFile, String querySpecStr, String seqSuffixStr, String whereStr,
								  int maxCols, int postQGeneration, int globalPostGenerationLimit, String headerStr, List outputResultsList )
	{

		Integer columnsRetrieved = new Integer(0);

		boolean requestMode = ( request == null ) ? false : true;

		/* this is an as-yet unsuccessful attempt to determine whether column headers need to be repeated on output
		** from the ResultSet into the results List passed to the jsp for display
		*/
		boolean suppressHeaders = requestMode ? false : ( querySpecStr.equals( lastPostQueryStr )) ? false : false; // was true:false

		try {
			String queryString = "SELECT "; // stub of query string to be assembled
			String paramStr = null;

			// the following variable relate to values read in from the request and/or the query properties file
			// Explanatory text copied from a sample query properties file, people.properties

			/* whether to build links and notify the user that clicking on column headers will sort by that column
			** CLICKSORT=false
			*/
			boolean clickSort = false;

			/* for fieldlist, * may be used unless linkfield is active, in which case need field names explicitly
			** if fields are listed, separate by commas and no spaces
			** use "as" specifers with alternative names for count(*) or sum() statements to allow column heads to sort
			** "as" specifiers must be enclosed in (single) quotes if contain spaces
			** FIELDLIST=people.lastname,title,role,focus
			*/
			String fieldList = null;
			int fieldCount = 1;

			/* specification for drilldown action for any active linkfields
			** 1 means that the current supplemental "where" parameter (from another column) will be retained and supplemented
			** by the linkfield's value in the new request
			** 0 means that only this linkfield's value will be passed on as a where parameter to the new request
			** -1 means that no limiting where parameter will be passed on with the new request
			** leave blank for field positions that are not linkfields
			** LINKDRILLDOWN=1,,,
			*/
			int[] linkDrillToggles;

			/* linkfield should be blank unless display of the specified field is to incorporate a link to another properties file
			** LINKFIELDS=people.lastname,,,
			*/
			String linkFieldsProp = null;
			String[] linkFieldValues;
			int linkCount = 0;

			/* linkfield offset -- to create a link in one column that uses the value in another
			** LINKOFFSETS=,,,,-4
			*/
			int[] linkOffsetCounts;

			/* linkprops specifies the properties file to be specified by each LINKFIELD
			** LINKPROPS=person,,,
			*/
			int linkSpecCount = 0;
			String linkSpecsProp = null;
			String[] linkSpecValues;

			/* table or tables for this query
			** TABLES=people
			*/
			String tableName = null;

			/* list of subqueries, separated by commas
			** POSTQUERIES=
			*/
			String postQueriesProp = null;
			String[] postQueryValues;
			int postQueryCount = 0;

			/* name for 1st column above when used in a where clause for a postquery
			** POSTQUERYFIELD=people.lastname
			*/
			String postQueryFieldsProp = null;
			String[] postQueryFields = null;
			int postQueryFieldCount = 0;

			/* limit to the depth of recursive postquerying
			** POSTQUERYGENERATIONLIMIT=2
			*/
			String postQueryLimitString="";
			int postQueryGenerationLimit=2; // default if not set globally

			/* css style for results when displayed via a postquery
			** ROWSTYLE=people
			*/
			String rowStyleStr = "";

			/* baseline where clause (usually to connect two or more tables); may be supplemented in request
			** leave blank after equal sign if no baseline whereclause required
			** note that the whereclause can contain spaces and equal signs or other operators, as well as quotes
			** WHERECLAUSE=
			*/
			boolean whereStarted = false;
			String whereClause = null;
			String whereSuppPropStr = null;
			String whereSuppStr = null;

			// now start by initializing arrays for properties that involve lists to parse
			linkOffsetCounts = new int[ 0 ];
			linkDrillToggles = new int[ 0 ];
			linkFieldValues = new String[ 0 ];
			linkSpecValues = new String[ 0 ];
			postQueryValues = new String[ 0 ];
			postQueryFields = new String[ 0 ];

			// assemble filename for this file, using parameters read via web-inf file
			String propsFileName =  tomcatHome + "/webapps/" + contextName
								 + "/WEB-INF/classes/properties/" + querySpecStr + ".properties";
			FileInputStream in = new FileInputStream( propsFileName );
			propsFile.load( in );

			if ( ( tableName = propsFile.getProperty("TABLES") ) == null || tableName.equals("") )
				tableName = "people";  // by specifying a table will at least will get an error message

			if ( ( fieldList = propsFile.getProperty("FIELDLIST") ) == null || fieldList.equals("") ) {
				queryString = queryString + "*" + " FROM " + tableName;
				if ( noisy ) System.out.println("no readable fieldlist in " + querySpecStr + " properties file so linking disabled" );
			} else {
				queryString = queryString + fieldList + " FROM " + tableName;
				// parse fieldList for fieldCount and make sure it agrees with count of LINKFIELDS
				fieldCount = countCommas( fieldList ) + 1;
				if ( fieldCount > 0 ) { // fill with dummy values so not array bounds problems in submitquery
					linkOffsetCounts = new int[ fieldCount ];
					linkDrillToggles = new int[ fieldCount ];
					linkFieldValues = new String[ fieldCount ];
					linkSpecValues = new String[ fieldCount ];
					for ( int i=0; i < fieldCount; i++ ) {
						linkOffsetCounts[i] = 0;
						linkDrillToggles[i] = 0;
						linkFieldValues[i] = "";
						linkSpecValues[i]="";
					}
				}
				if ( ( linkFieldsProp = propsFile.getProperty("LINKFIELDS") ) != null && !( linkFieldsProp.equals("") ) ) {
					// original parsing: linkfieldPos = positionInFieldList( linkField, fieldList );
					linkCount = countCommas( linkFieldsProp ) + 1;
					if ( linkCount == fieldCount ) {
						if ( ( linkSpecsProp = propsFile.getProperty("LINKPROPS") ) != null && !( linkSpecsProp.equals("") ) ) {
							linkSpecCount = countCommas( linkSpecsProp ) + 1;
							if ( linkSpecCount == linkCount ) {
								String linkOffsetProp, linkDrillProp;

								// now go ahead and parse the active fields and their forwarding query property names
								int[] linkCommaPositions = loadCommaPositions( linkFieldsProp, linkCount-1 );
								for ( int i = 0; i < linkCount; i++ ) {
									linkFieldValues[i] = linkFieldsProp.substring( ( i == 0 ? 0 : linkCommaPositions[i-1]+1),
																			i == linkCount-1 ? linkFieldsProp.length() : linkCommaPositions[i]);
									// System.out.println("linkFieldValue "+ i + " = " + linkFieldValues[i]);
								}
								int[] specCommaPositions = loadCommaPositions( linkSpecsProp, linkSpecCount-1 );
								for ( int i = 0; i < linkSpecCount; i++ ) {
									linkSpecValues[i] = linkSpecsProp.substring( ( i == 0 ? 0 : specCommaPositions[i-1]+1) ,
																			i == linkSpecCount-1 ? linkSpecsProp.length() : specCommaPositions[i]);
									// System.out.println("linkSpecValue "+ i +" = " + linkSpecValues[i]);
								}
								if ( ( linkOffsetProp = propsFile.getProperty("LINKOFFSETS") ) != null && !( linkOffsetProp.equals("") ) ) {
									int offsetCount = countCommas( linkOffsetProp ) + 1;
									int[] offsetCommaPositions = loadCommaPositions( linkOffsetProp, offsetCount-1 );
									String[] linkOffsetValues = new String[ fieldCount ];
									for ( int i = 0; i < offsetCount; i++ ) {
										linkOffsetValues[i] = linkOffsetProp.substring( ( i == 0 ? 0 : offsetCommaPositions[i-1]+1),
																			i == offsetCount-1 ? linkOffsetProp.length() : offsetCommaPositions[i]);
										linkOffsetCounts[i] = linkOffsetValues[i].equals("") ? 0 : Integer.parseInt( linkOffsetValues[i] );
										// System.out.println("link offset value "+ i + "=" + linkOffsetToggles[i]);
									}
								} else if (fieldCount>1) {
									System.out.println("no readable LINKOFFSETS list in properties file " + querySpecStr );
								}
								if ( ( linkDrillProp = propsFile.getProperty("LINKDRILLDOWN") ) != null && !( linkDrillProp.equals("") ) ) {
									int drillCount = countCommas( linkDrillProp ) + 1;
									int[] drillCommaPositions = loadCommaPositions( linkDrillProp, drillCount-1 );
									String[] linkDrillValues = new String[ fieldCount ];
									for ( int i = 0; i < drillCount; i++ ) {
										linkDrillValues[i] = linkDrillProp.substring( ( i == 0 ? 0 : drillCommaPositions[i-1]+1),
																			i == drillCount-1 ? linkDrillProp.length() : drillCommaPositions[i]);
										linkDrillToggles[i] = linkDrillValues[i].equals("") ? 0 : Integer.parseInt( linkDrillValues[i] );
										// System.out.println("link drilldown toggle "+ i + "=" + linkDrillToggles[i]);
									}
								} else
									System.out.println("no readable LINKDRILLDOWN in properties file " + querySpecStr );
							} else {
								System.out.println("linkSpecCount " + linkSpecCount + " does not equal linkCount " + linkCount + " in " + querySpecStr + "properties file." );
							}
						} else {
							System.out.println("no readable LINKPROPS in properties file " + querySpecStr );
						}
					} else { // linkcount != fieldcount
						System.out.println("fieldCount " + fieldCount + " does not equal linkcount " + linkCount + " in " + querySpecStr + " properties file." );
					}
				} else if (fieldCount>1) {
					System.out.println("no readable LINKFIELDS in properties file " + querySpecStr );
				}
			}

			// add any LEFT JOIN clause
			String leftJoinClause = null;
			if ( ( leftJoinClause = propsFile.getProperty("LEFTJOIN") ) != null && !( leftJoinClause.equals("") ) ) {
				queryString = queryString + " LEFT JOIN " + leftJoinClause;
			}

			// add any where clause drawn from properties file -- critical for multi-table queries
			if ( ( whereClause = propsFile.getProperty("WHERECLAUSE") ) != null && !( whereClause.equals("") ) ) {
				whereStarted = true;
				queryString = queryString + " WHERE " + whereClause;
			}

			if ( requestMode ) {
				// add the usually dynamically created supplemental where clause if this properties file permits it
				if ( ( whereSuppPropStr = propsFile.getProperty("WHERESUPP") ) != null && whereSuppPropStr.equals("true") ) {
					// check request to see if additional selection desired
					if ( ( whereSuppStr = request.getParameter("linkwhere") ) != null && !( whereSuppStr.equals("")) ) {
						if ( whereStarted )
							queryString = queryString + " AND " + whereSuppStr;
						else
							queryString = queryString + " WHERE " + whereSuppStr;
					} else
						if ( noisy ) System.out.println("no linkwhere parameter in request");
				}
			} else {
				// add the supplemental where clause passed in as a parameter from the prior parent query
				if ( whereStarted )
					queryString = queryString + " AND " + whereStr;
				else
					queryString = queryString + " WHERE " + whereStr;

				whereSuppStr = whereStr; // so that linkprops > 0 has effect on links crafted in a postQuery
				// add diagnostics to trace postquerying up or down a hierarchy
				if ( noisy ) System.out.println( querySpecStr + " gen#" + postQGeneration + ": " + whereStr );
			}

			// add any GROUP BY clause
			if ( requestMode) {
				// add from either request or props file
				if ( request.getParameter("groupby") != null )  { // take request parameter as priority
					queryString = queryString + " GROUP BY " + request.getParameter("groupby");
					// now add baseline sort preference from props file as secondary sort
					if ( ( paramStr = propsFile.getProperty("GROUPBY") ) != null && !( paramStr.equals("")) ) // default group by specification
						queryString = queryString + "," + paramStr;
				} else if ( ( paramStr = propsFile.getProperty("GROUPBY")) != null && !( paramStr.equals("")) ) // default group by specification
					queryString = queryString + " GROUP BY " + paramStr;
			} else {
				// add from props file only
				if ( ( paramStr = propsFile.getProperty("GROUPBY") ) != null && !( paramStr.equals("")) ) // default group by specification
					queryString = queryString + " GROUP BY " + paramStr;
			}

			// add any sort parameter
			if ( requestMode ) {
				// add from either request or props file
				paramStr=request.getParameter("sortfield");
				if ( paramStr != null && !paramStr.equals(""))  { // take request parameter as priority
					queryString = queryString + " ORDER BY " + request.getParameter("sortfield");
					// now add baseline sort preference from props file as secondary sort
					if ((paramStr = propsFile.getProperty("SORTFIELD"))!=null && !paramStr.equals("")) { // default sort specification
						queryString = queryString + "," + paramStr;
					}
				} else if ((paramStr = propsFile.getProperty("SORTFIELD"))!=null && !paramStr.equals("")) { // default sort specification
					queryString = queryString + " ORDER BY " + paramStr;
				}
			} else {
				// add any sort parameter from props file
				if ((paramStr = propsFile.getProperty("SORTFIELD"))!=null && !paramStr.equals("")) {  // default sort specification
					queryString = queryString + " ORDER BY " + paramStr;
				}
			}

			if ( requestMode ) { // title, query display, and editing directive apply to main query output only; ignore for postqueries
				// check request to see if editing form to append to outgoing request
				if ( ( paramStr = propsFile.getProperty("EDITFORM") ) != null && !paramStr.equals(""))
					request.setAttribute("editform", paramStr );
				else
					request.setAttribute("editform", "footer.jsp" );

				if ( ( paramStr = propsFile.getProperty("MIN_EDIT_ROLE") ) != null && !paramStr.equals(""))
					request.setAttribute("min_edit_role", paramStr );
				else
					request.setAttribute("min_edit_role", "1" );

				if ( (paramStr = propsFile.getProperty("PAGETITLE")) != null && !( paramStr.equals("")) )
					request.setAttribute("pagetitle", paramStr );
				else
					request.setAttribute("pagetitle", "Query Results" );

				// check request to see if text of query should be suppressed in output -- not working yet
				if ( ( paramStr = propsFile.getProperty("SUPPRESSQUERY") ) != null && paramStr.equals("true"))
					request.setAttribute("suppressquery", "true" );
			}

			// read in stylesheet value for rows returned from this query
			rowStyleStr = propsFile.getProperty("ROWSTYLE");
			if ( rowStyleStr == null )
				rowStyleStr = "row";

			String clickSortStr = propsFile.getProperty("CLICKSORT");
			if ( clickSortStr != null && clickSortStr.equals("true") )
				clickSort = true;
			if ( requestMode ) // top level
				request.setAttribute( "clicksort", clickSort ? "true" : "false");

			// read in postQuery specifications
			if ( ( postQueriesProp = propsFile.getProperty("POSTQUERIES") ) != null && !( postQueriesProp.equals("") ) ) {
				postQueryCount = countCommas( postQueriesProp ) + 1; // need not be the same as the number of fields
				if ( postQueryCount > 0 ) {
					if ( noisy ) System.out.println( postQueryCount + " postQuery strings found." );
					postQueryValues = new String[ postQueryCount ];
					int[] postQueryCommaPositions = loadCommaPositions( postQueriesProp, postQueryCount-1 );
					for ( int i = 0; i < postQueryCount; i++ ) {
						postQueryValues[i] = postQueriesProp.substring(
											( i == 0 ? 0 : postQueryCommaPositions[i-1]+1 ),
											i == postQueryCount-1 ? postQueriesProp.length() : postQueryCommaPositions[i] );
						if ( noisy ) System.out.println( "postQuery properties specification is " + postQueryValues[i] + "." );
					}
				} else
					System.out.println("Error: POSTQUERIES property present but count is 0 or less" );
				postQueryFieldsProp = propsFile.getProperty("POSTQUERYFIELD");
				postQueryFieldCount = countCommas( postQueryFieldsProp ) + 1; // needs to be 1 or the same as the number of fields
				if ( postQueryFieldCount > 0 ) {
					postQueryFields = new String[ postQueryCount ]; // need as many as postqueries regardless of count read
					int[] postQueryFieldCommaPositions = loadCommaPositions( postQueryFieldsProp, postQueryFieldCount-1 );
					if ( postQueryFieldCount == postQueryCount ) {
						for ( int i = 0; i < postQueryFieldCount; i++ ) {
							postQueryFields[i] = postQueryFieldsProp.substring(
													   ( i==0 ? 0 : postQueryFieldCommaPositions[i-1]+1 ),
														 i == postQueryFieldCount-1 ?
															postQueryFieldsProp.length() : postQueryFieldCommaPositions[i] );
							// if (noisy) System.out.println( "postQueryField specification " + i + " is " + postQueryFields[i] + "." );
						}
					} else {
						if ( postQueryFieldCount != 1 )
							System.out.println("Error: postQueryFieldCount does not match number of postQueries specified in " + querySpecStr );
						// now initialize array of strings to match postqueryspecifications all as the first value
						for ( int i = 0; i < postQueryCount; i++ ) {
							postQueryFields[i] = postQueryFieldsProp.substring( 0,
														 postQueryFieldCount == 1 ?
														   postQueryFieldsProp.length() : postQueryFieldCommaPositions[0] );
							// if ( noisy) System.out.println( "postQueryField " + i + " specification is " + postQueryFields[i] + "." );
						}
					}
				} else {
					if ( noisy ) System.out.println("POSTQUERY property not read or read as blank in " + querySpecStr);
				}
				if ( globalPostGenerationLimit < 0 ) { // not set yet, so read property file
					postQueryLimitString = propsFile.getProperty("POSTQUERYGENERATIONLIMIT");
					if ( postQueryLimitString == null || postQueryLimitString.equals("") ) {
						if ( noisy) System.out.println("warning: no POSTQUERYGENERATIONLIMIT property, value set to 2 in " + querySpecStr);
						//already the default: postQueryGenerationLimit = 2;
					} else {
						postQueryGenerationLimit = Integer.parseInt( postQueryLimitString );
						// System.out.println( "postQgeneration read as " + postQueryGenerationLimit + " from propsfile " + querySpecStr );
						if ( postQueryGenerationLimit < 1 )
							postQueryGenerationLimit = 0; // was 1;
						/*
						} else {
							tempPostGenerationLimit = postQueryGenerationLimit;
						*/
					}
				} else {
					postQueryGenerationLimit = globalPostGenerationLimit;
				}
				//System.out.println( "postQgeneration ends up as " + postQueryGenerationLimit + " for propsfile " + querySpecStr );
			}

			/*
			if ( requestMode )
				System.out.println("main query is " + queryString);
			else
				System.out.println("postquery is " + queryString);
			*/

			// now go ahead and attempt the connection, followed by the actual query
			try {
				Connection con = getConnection();
				try {
					Statement stmt = con.createStatement();
					try {
						if ( requestMode )
							request.setAttribute( "querystring" + seqSuffixStr, queryString ); // for optional display on output

						columnsRetrieved = 	submitQuery( linkOffsetCounts, linkDrillToggles, linkFieldValues, linkSpecValues,
										 	maxCols, ( requestMode == true ) ? 0 : 1, postQGeneration, postQueryCount,
										 	postQueryGenerationLimit, globalPostGenerationLimit, headerStr, postQueryValues, postQueryFields,
										 	whereSuppStr, querySpecStr, suppressHeaders, queryString,
										 	stmt, con, rowStyleStr, outputResultsList, clickSort );

						dispose( con, stmt );

					} catch( Exception h ) {
						outputResultsList.add("Error " + h + " from submitQuery." );
					}
				} catch( Exception g ) {
					outputResultsList.add("Error " + g + " from createStatement." );
				}
			} catch( Exception e ) {
				outputResultsList.add("Error " + e + " from connection" );
			}
		} catch ( Exception f ) {
			outputResultsList.add("Error " + f + " from initial preparation phase" );
		}

		/* an as-yet unsucessful way to avoid duplicate column headers on recursive queries
		 * Note that postqueries that return no data do set lastPostQueryStr
		if ( noisy ) System.out.println("Last query: " + lastPostQueryStr + ", current postquery: " + querySpecStr );
		*/
		if ( !requestMode ) lastPostQueryStr = querySpecStr;

		return columnsRetrieved;
	}


	private Integer submitQuery( int[] linkOffsets, int[] linkDrillToggles, String[] linkFieldStrs, String[] linkQueryStrs,
								int maxColumns, int indentColumns, int postQGeneration, int postQCount,
								int postQGenerationLimit, int globalPostQGenLimit, String headerStr,
								String[] postQueryStrs, String[] postQFieldStrings,
								String whereStr, String specStr, boolean suppressDupHeaders, String myQuery,
								Statement stmt, Connection con, String styleStr, List resultsList, boolean clickSort ) throws SQLException
	{
		int columnCount = 0, outputTableColumns = 0;

		try {
			ResultSet rs = stmt.executeQuery( myQuery );
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				columnCount = rsmd.getColumnCount(); // is number of columns requested even if no results found
				if ( noisy ) System.out.println("columncount of " + columnCount + " when linkFieldStr size " + linkFieldStrs.length );
				if ( columnCount > linkFieldStrs.length ) { // FIELDLIST= * or not specified in properties file
					linkFieldStrs = new String[ columnCount ];
					for ( int i=0; i < columnCount; i++ ) {
						linkFieldStrs[i] = "";
					}
				}
				// a postquery can have only the number of the primary query's result columns less 1 (or the cumulative indent value)
				outputTableColumns = maxColumns > 0 ? maxColumns : columnCount;
				// System.out.println( "getColumnCount returns " + columnCount + " columns of data in " + specStr + "; tableColumns set to " + outputTableColumns );

				try {
					int resultSetRowsRead = 0;
					/* boolean resetOutputTableColumns = false; */

					boolean doColumnHeaders = suppressDupHeaders ? false : postQGeneration > 1 ? false : true; //( specStr.equals( lastPostQueryStr ) ? false : true );

					String[] prevResult = new String[columnCount];
					for ( int p = 0; p < columnCount; p++ )
						prevResult[p] = null;

					/* NOTE current bug is that this ResultSet seems to have spurious duplicate entries when the generation of a postQuery
					** goes beyond 1 (i.e., with recursion).  The queries issued are legitimate and the results retrieved are appropriate,
					** but each ResultSet gets a repeated number of extra, duplicate values at the point the march up or down the hierarchy reaches
					** termination and starts backing out -- in weird patterns
					*/
					while ( rs.next() ) { // iterate only once through results set until JDBC 2.0
						boolean duplicateRow = false, executePostQueries = false;

						++resultSetRowsRead;
						// if ( noisy ) System.out.println("Stepping through row " + resultSetRowsRead + " of results");

						if ( doColumnHeaders ) { // moved into while loop so no column headers not written if no results
							for ( int i = 0; i < postQGeneration; i++ ) { // pad up to start of postquery results, currently 1 table cell
								resultsList.add( "+" );
							}
							int postQHeaderColumns = postQGeneration > 0 ? postQGeneration - 1 : 0; //CHANGED 1/28 since saving output column in horizontal.jsp
							for ( int i = 0; i < columnCount; i++ ) { // want to repeat same query but with different sort specification
								// just pass on whereStr that came with this request, without altering
								// java.net.URLEncoder.encode( whereValueStr ) below causes Java nullpointerexception
								if ( i + postQHeaderColumns < outputTableColumns ) { // clip results if necessary
									if ( linkFieldStrs[i].equals("XX")) {
										resultsList.add("XX");
										/*
										if ( postQGeneration > 0 )
											++outputTableColumns; // room for one more column to match primary query
										else
											--outputTableColumns; // room for one less on all postqueries
										resetOutputTableColumns = true;
										*/
									} else if ( postQGeneration > 0 || clickSort == false ) { // if >= 0 does same as clickSort false
										resultsList.add( rsmd.getColumnLabel( i+1 ) ); // results start at 1, not 0
									} else {
										resultsList.add( "<a href=\"fetch?queryspec="
											+ specStr
											+ "&linkwhere=" + ( whereStr == null ? "" : java.net.URLEncoder.encode( whereStr,"UTF-8" ) )
											+ "&postGenLimit=" + globalPostQGenLimit
										    + "&header=" + headerStr
											+ "&sortfield='"
											+ java.net.URLEncoder.encode(rsmd.getColumnName( i+1 ),"UTF-8") // JCR 20030117 getColumnLabel causus probs //rsmd.getTableName( i ) + "." + rsmd.getColumnName( i )
											+ "'\">"
											+ rsmd.getColumnName( i+1 ) // was getColumnLabel; results start at 1, not 0
											+ "</a>");
									}
								}
							}
							if ( postQGeneration > 0 ) {
								for ( int i = columnCount; i + postQHeaderColumns < outputTableColumns; i++ ) // pad table to enclosing column count if appropriate
									resultsList.add( "&nbsp;" );
							} else {
								resultsList.add( "XX" );
							}
							// System.out.println( "column/row headers with links added." );
							doColumnHeaders = false;
						}


						for ( int i = 0; i < postQGeneration; i++ ) { // pad beginning of row
							String plusStr = "@@" + styleStr; // styleStr + "gen" + postQGeneration + "row" + resultSetRowsRead + " *";
							/* if desired, this provides a visual cue of postquery generation
							for ( int j = 0; j < postQGeneration; j++ ) {
								plusStr = plusStr + "*";
							}
							*/
							resultsList.add( plusStr );
						}
						int postQBodyColumns = postQGeneration > 0 ? postQGeneration - 1 : 0;
						for ( int i = 0; i < columnCount; i++ ) { // step through each column of the current row based on columnCount
							if ( i + postQBodyColumns < outputTableColumns ) { // clip results if necessary if this is a postquery with more columns than the original
								try {
									// if ( noisy ) System.out.println("calling rs.getString() for value " + (i + 1));
									String vStr = rs.getString(i+1); // results start at 1, not 0
									if ( i == 0 ) {
										//duplicateRow = vStr.equals( prevResult[i] ) ? true : false; // can be set true only on 1st column
										executePostQueries = vStr.equals( prevResult[i] ) ? false : true;
									} else if ( !(vStr.equals( prevResult[i] )))
										duplicateRow = false; // turn off as soon as this row becomes different from last
									// duplicateRow = vStr.equals( prevResult[i] ) ? true : false; // change to allow links to multiple tables

									if ( !(linkFieldStrs[i].equals("")) ) { // assemble an href that will launch a new query
										// System.out.println("linkFieldStr value for column " + i + " = " + linkFieldStrs[i] + " while linktoggle is " + linkDrillToggles[i] );
										// formulating a new request specification from properties file input
										// if the linkdrilldown toggle is -1, pass on no linkwhere parameter so all values retrieved
										// if the linkdrilldown toggle is 0, drop inherited where spec and pass on linkwhere = current field value
										// if the linkdrilldown toggle is 1, pass on both inherited and current field value as linkwhere spec for drilldown
										if ( linkDrillToggles[i] > 1 ) { // read external properties file and return string
											if ( vStr.length() > 0 ) { // don't construct empty links
												String compositeStr = "";
												Properties hyperPropsFile = new Properties();

												String hyperFileName =  tomcatHome + "/webapps/" + contextName
																	 + "/WEB-INF/classes/properties/" + linkQueryStrs[i] + ".properties";

												FileInputStream in = new FileInputStream( hyperFileName );
												hyperPropsFile.load( in );

												for ( int h = 1; h <= linkDrillToggles[i]; h++ ) {
													String hrefStr = null;

													if ( ( hrefStr = hyperPropsFile.getProperty("HREF" + h ) ) != null ) {
														if ( h == linkDrillToggles[i] ) { // for ArcIMS, implement as below
															String offsetStr = null;
															String closeStr = null;
															String targetStr = null;
															String iconStr = null;
															int offset = 0;

															if ( ( offsetStr = hyperPropsFile.getProperty("OFFSET" + h ) ) != null ) {
																offset = offsetStr.equals("") ? 0 : Integer.parseInt( offsetStr );
															} else { // jcr 20021122
																offset = linkOffsets[i];
															}
															if ( rs.getString( i + 1 + offset ).length() > 0 ) {
																if ( ( closeStr = hyperPropsFile.getProperty("CLOSE_HREF" + h)) != null && !( closeStr.equals("")) ) {
																	compositeStr = compositeStr + " <a href=\"" + hrefStr + rs.getString(i + 1 + offset) + closeStr + "\"";
																} else {
																	compositeStr = compositeStr + " <a href=\"" + hrefStr + rs.getString(i + 1 + offset) + "\"";
																}
																if ( ( targetStr = hyperPropsFile.getProperty("TARGET" + h)) != null && !( targetStr.equals("")) ) {
																	compositeStr = compositeStr + " target=\"" + targetStr + "\">";
																} else {
																	compositeStr = compositeStr + ">";
																}
																if ( ( iconStr = hyperPropsFile.getProperty("ICON" + h ) ) != null && !( iconStr.equals("")) ) {
																	compositeStr = compositeStr
																				   + "<img border=0 src=\""
																				   + iconStr
																				   + "\"/></a>";
																}
															}
															/* last link uses field value and puts quotes around it for URL
															// the final link also uses the field value instead of an icon as the visible link anchor
															compositeStr = compositeStr
																		   + " " + "<a href=\""
																		   + hrefStr
																		   + java.net.URLEncoder.encode( "='" + vStr + "'","UTF-8" )
																		   + "\">"
																		   + vStr
																		   + "</a>";
															*/
														} else { // insert href string, then value without quotes, then icon
															String offsetStr = null;
															String closeStr = null;
															String targetStr = null;
															String iconStr = null;
															int offset = 0;

															if ( ( offsetStr = hyperPropsFile.getProperty("OFFSET" + h ) ) != null ) {
																offset = offsetStr.equals("") ? 0 : Integer.parseInt( offsetStr );
															} else { // jcr 20021122
																offset = linkOffsets[i];
															}
															if ( rs.getString( i + 1 + offset ).length() > 0 ) {
																if ( ( closeStr = hyperPropsFile.getProperty("CLOSE_HREF" + h)) != null && !( closeStr.equals("")) ) {
																	//System.out.println("Closing string is " + closeStr );
																	compositeStr = compositeStr + " <a href=\"" + hrefStr + rs.getString(i + 1 + offset) + closeStr + "\"";
																} else {
																	compositeStr = compositeStr + " <a href=\"" + hrefStr + rs.getString(i + 1 + offset) + "\"";
																}
																if ( ( targetStr = hyperPropsFile.getProperty("TARGET" + h)) != null && !( targetStr.equals("")) ) {
																	compositeStr = compositeStr + " target=\"" + targetStr + "\">";
																} else {
																	compositeStr = compositeStr + ">";
																}
																if ( ( iconStr = hyperPropsFile.getProperty("ICON" + h ) ) != null && !( iconStr.equals("")) ) {
																	compositeStr = compositeStr
																				   + "<img border=0 src=\""
																				   + iconStr
																				   + "\"/></a>";
																}
															}
														}
													} else if ( h == 1 )
														compositeStr = vStr;
												}
												// System.out.println(compositeStr);
												resultsList.add( compositeStr );
											} else {
												resultsList.add("&nbsp;"); // was "(no data)"
											}

										} else {
											if ( linkFieldStrs[i].equals("XX")) {
												resultsList.add("XX");
												/*
												if ( !resetOutputTableColumns ) {
													if ( postQGeneration > 0 )
														++outputTableColumns; // room for one more to match primary query
													else
														--outputTableColumns; // room for one less on all postqueries
													resetOutputTableColumns = true;
												}
												*/
											} else { /* JCR latest changes for clickable inline icon links */
												if (( vStr.indexOf(".gif") > 0 || vStr.indexOf(".jpg") > 0 ) && ( vStr.indexOf("icon") >= 0 || vStr.indexOf("thumb") >= 0 )) { //an inline icon
													resultsList.add( duplicateRow ? "(dup)" : "<a href=\""
														  + "fetch?queryspec="
														  + linkQueryStrs[i]
														  + "&postGenLimit=" + globalPostQGenLimit
														  + "&header=" + headerStr
														  + "&linkwhere=" + java.net.URLEncoder.encode(
															 (linkDrillToggles[i] == -1)
															   ? ""
															   : (linkDrillToggles[i] == 0 )
																  ? linkFieldStrs[i] + "='" + retrofitApostrophes(rs.getString( i + 1 + linkOffsets[i])) + "'"
																  : ( whereStr == null || whereStr.equals("") )
																	? linkFieldStrs[i] + "='" + retrofitApostrophes(rs.getString( i + 1 + linkOffsets[i])) + "'"
																	: whereStr + " AND " + linkFieldStrs[i] + "='" + retrofitApostrophes(rs.getString( i + 1 + linkOffsets[i]))  + "'","UTF-8")
														  + "\">"
														  + "<img alt=" + vStr + " border='0' src=\"icons/"
														  + vStr
														  + "\" />"
														  + "</a>");
												} else {
													resultsList.add( duplicateRow ? "(dup)" : "<a href=\""
														  + "fetch?queryspec="
														  + linkQueryStrs[i]
														  + "&postGenLimit=" + globalPostQGenLimit
														  + "&header=" + headerStr
														  + "&linkwhere=" + java.net.URLEncoder.encode(
															 (linkDrillToggles[i] == -1)
															   ? ""
															   : (linkDrillToggles[i] == 0 )
																  ? linkFieldStrs[i] + "='" + retrofitApostrophes(rs.getString( i + 1 + linkOffsets[i])) + "'"
																  : ( whereStr == null || whereStr.equals("") )
																	? linkFieldStrs[i] + "='" + retrofitApostrophes(rs.getString( i + 1 + linkOffsets[i])) + "'"
																	: whereStr + " AND " + linkFieldStrs[i] + "='" + retrofitApostrophes(rs.getString( i + 1 + linkOffsets[i]))  + "'","UTF-8")
														  + "\">"
														  + vStr
														  + "</a>");
												}
											}
										}
									} else { // plain text value that is checked for display as a mailto:, URL, inline icon, or link to a gif
										resultsList.add(
											duplicateRow
											  ? "(dup)"
											  : vStr.indexOf('@') > 0 // an email address
												 ? 	"<a href=\"mailto:"
													+ vStr
													+ "\">"
													+ vStr
													+ "</a>"
												 : vStr.indexOf("http://") >= 0 || vStr.indexOf("https://") >= 0 // a link
													? 	"<a target='_new' href=\""
														+ vStr
														+ "\">"
														+ rsmd.getColumnLabel( i+1 )
														+ "</a>"
													: vStr.indexOf(".gif") > 0 || vStr.indexOf(".jpg") > 0 // an image
														?   vStr.indexOf("icon") >= 0 || vStr.indexOf("thumb") >= 0
															? "<img src=\"icons/"
																+ vStr
																+ "\" />"
															: "<a href=\"icons/"
																+ vStr
																+ "\">"
																+ vStr
																+ "</a>"
														: duplicateRow ? "(dup)" : vStr );
									}
									prevResult[i] = rs.getString(i+1); // original, not permuted value, even if the same as what already in prev array
								} catch( Exception e ) { // nullpointerexception from null value in database; otherwise is real error
									if ( noisy ) System.out.println( "Exception e on rs.getString() is " + e );
									resultsList.add( "" ); //rs.getString(i+1) );
								}
							}
						}
						if ( postQGeneration > 0 ) {
							for ( int i = columnCount; i + postQBodyColumns < outputTableColumns; i++ ) // pad table out to maxColumns
								resultsList.add( "&nbsp;" ); // dashes help confirm postqueries are behaving
						} else {
							resultsList.add("XX");
						}
						if ( executePostQueries ) { // execute one or more queries and intersperse results in the outgoing results List
							String postQueryWhereStr;

							for ( int q = 0; q < postQCount; q++ ) {
								if ( postQFieldStrings[q] == null || postQFieldStrings[q].equals("") )
									postQueryWhereStr = rsmd.getTableName(1) + "." + rsmd.getColumnLabel(1) + "='" + retrofitApostrophes(rs.getString(1)) + "'";
								else
									postQueryWhereStr = postQFieldStrings[q] + "='" + retrofitApostrophes(rs.getString(1)) + "'"; // table.fieldname
								if ( ( postQGeneration + 1 ) <= postQGenerationLimit ) {
									doPostQuery( postQueryStrs[q], postQueryWhereStr, outputTableColumns, postQGeneration + 1, globalPostQGenLimit, headerStr, resultsList );
									// lastpostQueryStr = postQueryStrs[q]; move to doPostQuery
								}
							}
						}
					}
					rs.close(); // try moving earlier to avoid corruption
				} catch( Exception c ) {
					System.out.println( "Exception C on while rs.next() is " + c );
					resultsList.add("Error on while rs.next() is " + c );
				}
			} catch( Exception b ) {
				System.out.println( "Exception B on getMetadata is " + b );
				resultsList.add("Error on getMetadata is " + b );
			}
			//rs.close();
		} catch( Exception a ) {
			if ( noisy ) System.out.println( "Exception A on executing $$" + myQuery + "$$ in " + specStr + " is " + a );
			resultsList.add("Error on executing query $$" + myQuery + "$$ in " + specStr + " is " + a );
		}
		return new Integer( columnCount == 0 ? columnCount : columnCount + 1 ); // reflect adding additional column for postqueries
	}

	private static int countCommas( String fieldStr ) {
		int count = 0, commaPosition= -1;

		while ( ( commaPosition = fieldStr.indexOf( 44, commaPosition+1 ) ) >= 0 ) {
			++count;
			// System.out.println( "Comma " + count + " found at position " + commaPosition);
		}
		// System.out.println(count + " commas found in string '" + fieldStr + "', so " + (count + 1) + " fields" );
		count -= countConcatCommas( "concat(", fieldStr );
		count -= countConcatCommas( "concat_ws(", fieldStr );
		/* JCR latest changes */
		count -= countConcatCommas( "left(", fieldStr );
		count -= countConcatCommas( "right(", fieldStr );
		count -= countConcatCommas( "round(", fieldStr );
		return count;
	}

	private static int countConcatCommas( String testStr, String fieldStr ) {
		int concatPosition = -1, commaCount = 0;
//FIELDLIST=datafiles.id,datafiles.mapsheetId,mapsheets.name as 'mapsheet name',formatId,format_abbrev as format,coverageId,coverages.name as 'data theme',datafiles.seriesId,series.unit as 'data series',filename,concat_ws("/",directory,filename) as 'link to file'
//FIELDLIST=id,concat_ws(", ",address1,address2) as 'address',concat_ws(", ",city,state) as 'city and state',country,zip as 'postal code'
//FIELDLIST=id,concat_ws(", ",lastname,concat_ws(" ",firstname,middle)) as 'name',salut as salutation,email,concat_ws("/",month(modTime),dayofmonth(modTime),year(modTime)) as 'last updated'

		while ( (concatPosition = fieldStr.indexOf(testStr, concatPosition + 1)) >= 0 ) {
			concatPosition += testStr.length() + 1;
			int openParenPosition = fieldStr.indexOf('(',concatPosition);
			int closeParenPosition = fieldStr.indexOf(')',concatPosition);
			while ( openParenPosition > concatPosition && closeParenPosition > openParenPosition) {
				openParenPosition = fieldStr.indexOf('(',openParenPosition + 1 );
				closeParenPosition = fieldStr.indexOf(')',closeParenPosition + 1 );
			}
			if ( closeParenPosition > concatPosition ) {
				String concatStr = fieldStr.substring(concatPosition,closeParenPosition);
				int commaPosition = -1;
				while ( ( commaPosition = concatStr.indexOf( 44, commaPosition+1 ) ) >= 0 ) {
					++commaCount;
				}
				concatPosition = closeParenPosition;
			}
		}
		return commaCount;
	}

	private static int[] loadCommaPositions ( String fieldStr, int count ) {
		int[] commaArray = new int[count];
		int commaPosition = -1;

		for ( int i = 0; i < count; i++ ) {
			commaPosition = fieldStr.indexOf( 44, commaPosition + 1 );
			commaArray[i] = commaPosition;
		}
		/*
		for ( int i=0; i < count; i++ )
			System.out.println("comma "+(i+1) + " = "+commaArray[i]);
		*/
		return commaArray;
	}

	private static String retrofitApostrophes( String termStr ) {
		int characterPosition= -1;

		while ( ( characterPosition = termStr.indexOf( 39, characterPosition+1 ) ) >= 0 ) {
			if ( characterPosition == 0 ) // just drop it
				termStr = termStr.substring( characterPosition+1 );
			else if ( termStr.charAt(characterPosition-1) != 92 ) // already escaped
				termStr = termStr.substring(0,characterPosition) + "\\" + termStr.substring(characterPosition);
			++characterPosition;
		}
		return termStr;
	}


	/* getConnection() reads from a database properties file
	** This will have to change if username and password want to be retrieved by user input
	** in order to control access to editing tables
	*/

	private static Connection getConnection()
		throws SQLException, IOException
	{
		String username="", password="";
		String  url = "", drivers="";

		try {
			Properties dbProps = new Properties();
			String fileName = tomcatHome + "/webapps/" + contextName + "/WEB-INF/classes/properties/" + databaseName + "_jdbc.properties";
			FileInputStream in = new FileInputStream(fileName);
			
			dbProps.load(in);
			
			drivers = dbProps.getProperty("jdbc.drivers");
			if (drivers != null)
			System.setProperty("jdbc.drivers", drivers);
			url = dbProps.getProperty("jdbc.url");
			username = dbProps.getProperty("jdbc.username");
			password = dbProps.getProperty("jdbc.password");

		} catch(Exception ex){
			System.out.println("Error trying to open property file " + ex.getMessage());
		}


//System.out.println("attempting connection in QueryText servlet using url: " + url + ", username: " + username + ", password: " + password );
		Connection myConnection = null;
		try {
			myConnection = DriverManager.getConnection( url,username,password );
		} catch (SQLException ex) {
			System.out.println ("SQLException:");
         	while (ex != null) {
				System.out.println ("SQLState: " + ex.getSQLState());
            	System.out.println ("Message:  " + ex.getMessage());
            	System.out.println ("Vendor:   " + ex.getErrorCode());
            	ex.printStackTrace();
            	ex = ex.getNextException();
            	System.out.println ("");
          	}
      	} catch (Exception ex) {
			System.out.println("Exception: " + ex);
         	ex.printStackTrace ();
      	}

		return myConnection;
		//	DriverManager.getConnection( url, username, password );
		/* hardwired connection
		System.setProperty("jdbc.drivers", "org.gjt.mm.mysql.Driver");
		String url = "jdbc:mysql://localhost/CCRP";
		String username = "test";
		String password = "data";
		return
			DriverManager.getConnection( url, username, password );
			/*"jdbc:mysql://localhost/corejava?user=test&password=data");*/

	}

	private void dispose( Connection myCon , Statement myStmt )
	{
		try
		{
			myStmt.close();
			myCon.close();
		}
		catch(SQLException e) {}
	}

	private void addToFailures( HttpServletRequest request ) {
	List unsuccessfulResultsList = new ArrayList();
		int searchModeId=1, searchTermCount=1;
		String searchTermStr = request.getParameter( "queryText" );
		String trimmedQueryStr = retrofitApostrophes( searchTermStr.trim() );

		String queryMode = request.getParameter( "searchSubmitMode" );
		if ( queryMode.equals( "Starts With" )) {
			searchModeId=2;
		} else if ( queryMode.equals( "Match Any Term" )) {
			searchModeId=3;
			searchTermCount = countSpacers( trimmedQueryStr ) + 1;
		} else if ( queryMode.equals( "Match All Terms" )) {
			searchModeId=4;
		}

		String[] searchTermValues = new String[ searchTermCount ];
		if ( searchTermCount == 1 ) { // one fragment only or treat whole as one or find all terms
			searchTermValues[0] = trimmedQueryStr;
		} else {
			int[] spacerPositions = loadFragmentSeparators( trimmedQueryStr, searchTermCount-1 );
			for ( int i = 0; i < searchTermCount; i++ ) {
				searchTermValues[i] = trimmedQueryStr.substring(
										   ( i==0 ? 0 : spacerPositions[i-1]+1 ),
											 i == searchTermCount-1 ?
												trimmedQueryStr.length() : spacerPositions[i] );
				System.out.println( "fragment " + i + " is " + searchTermValues[i] + "." );
			}
		}

		int thesaurusMix;
		String propertyFileName = request.getParameter( "propertyFileName" );
		if ( propertyFileName.equals("descriptors")) {
			thesaurusMix=5; // FAO,GO,MeSH,NAL
		} else if ( propertyFileName.equals( "nalterm" ) ) {
			thesaurusMix=4; // NAL
		} else if ( propertyFileName.equals( "agvterm" ) ) {
			thesaurusMix=1; // FAO
		} else if ( propertyFileName.equals( "meshdescriptor" ) ) {
			thesaurusMix=3; // MeSH
		} else if ( propertyFileName.equals( "go_term" ) ) {
			thesaurusMix=2; // GO
		} else
			thesaurusMix=0;

		// now go ahead and attempt the connection, followed by the actual query
		try {
			Connection con = getConnection();
			try {
				Statement stmt = con.createStatement();
				try {
					// first create the failed search record and retrieve new id
					int searchFailureMatchCount=0, searchFailureMode, searchFailureCount=0, searchRecordId=0;
					String firstrequestTimeStr="";

					String searchFailureQueryStr = "SELECT searchId,searchmode,searchcount,thesaurusmix FROM searchfailures WHERE fullrequest='"
													+ trimmedQueryStr + "'";
					ResultSet searchFailureRS = stmt.executeQuery( searchFailureQueryStr );
					while ( searchFailureRS.next() ) {
						searchRecordId = Integer.parseInt( searchFailureRS.getString(1));
						searchFailureMode = Integer.parseInt( searchFailureRS.getString(2));
						searchFailureCount = Integer.parseInt( searchFailureRS.getString(3));
						int searchFailureThesMix = Integer.parseInt( searchFailureRS.getString(4));
						if ( searchFailureMode == searchModeId ) {
							if ( searchFailureThesMix == thesaurusMix ) {
								++searchFailureCount;
								++searchFailureMatchCount;
								break;
							}
						}
					}
					if ( searchFailureMatchCount > 0 ) { // update existing record
						String updateSearchFailureStr = "UPDATE searchfailures SET searchcount="
														+ searchFailureCount + " WHERE searchId=" + searchRecordId;
						stmt.executeUpdate( updateSearchFailureStr );
					} else {
						String searchRecordStr = "INSERT INTO searchfailures (termcount,searchmode,fullrequest,thesaurusmix,searchcount) VALUES ("
												 + searchTermCount + ","
												 + searchModeId + ",'"
												 + trimmedQueryStr + "',"
												 + thesaurusMix + ",1)";
						stmt.executeUpdate( searchRecordStr );
						String maxQuery = "SELECT MAX( searchId ) FROM searchfailures";
						ResultSet maxSearchRecordRS = stmt.executeQuery( maxQuery );
						while ( maxSearchRecordRS.next()) {
							searchRecordId = Integer.parseInt( maxSearchRecordRS.getString(1) );
						}
						maxSearchRecordRS.close();
						String firstRequestStr = "UPDATE searchfailures SET firstrequestTime = modTime WHERE searchId="
												 + searchRecordId;
						stmt.executeUpdate( firstRequestStr );
					}

					for ( int i = 0; i < searchTermCount; i++ ) { // look for pre-existing searchterm record
						int matchCount=0, searchTermId=0, previousHitCount=0;

						String searchTermQuery = "SELECT id, count, thesaurusmix FROM searchterms WHERE searchterm='"
												 + searchTermValues[i] + "'";
						ResultSet rs = stmt.executeQuery( searchTermQuery );
						while ( rs.next() ) {
							searchTermId = Integer.parseInt( rs.getString(1));
							previousHitCount = Integer.parseInt( rs.getString(2));
							int searchTermThesMix = Integer.parseInt( rs.getString(3));
							if ( searchTermThesMix == thesaurusMix ) {
								++matchCount;
								break;
							}
						}
						rs.close();
						if ( matchCount == 0 ) { // create new searchterm record,then link it to searchfailures
							String insertStr = "INSERT INTO searchterms (searchterm,count,thesaurusmix) VALUES ('"
												+ searchTermValues[i] + "',1," + thesaurusMix + ")";
							stmt.executeUpdate( insertStr );
							String maxTermQuery = "SELECT MAX( id ) FROM searchterms";
							ResultSet maxTermRS = stmt.executeQuery( maxTermQuery );
							while ( maxTermRS.next() ) {
								searchTermId = Integer.parseInt( maxTermRS.getString(1) );
							}
							maxTermRS.close();
							String firstTermRequestStr = "UPDATE searchterms SET firstrequestTime = modTime WHERE id="
													 + searchTermId;
							stmt.executeUpdate( firstTermRequestStr );
							String searchInstanceStr = "INSERT INTO searchInstances (searchId,searchTermId) VALUES ("
														+ searchRecordId + ","
														+ searchTermId + ")";
							stmt.executeUpdate( searchInstanceStr );
						} else {
							if ( matchCount > 1 ) {
								System.out.println("Error: " + matchCount + " searchterms matched by " + searchTermValues[i]);
							}
							++previousHitCount;
							// replace firstrequestTime with its own value so not automatically updated as timestamp field
							String updateStr = "UPDATE searchterms SET count=" + previousHitCount
												+ " WHERE id=" + searchTermId;
							stmt.executeUpdate( updateStr );
						}
					}
				} catch( Exception h ) {
					System.out.println("Error " + h + " from saving unsuccessful search terms." );
				}
				dispose( con, stmt );
			} catch( Exception g ) {
				System.out.println("Error " + g + " from createStatement()." );
			}
		} catch( Exception e ) {
			System.out.println("Error " + e + " from getConnection()" );
		}
	}

/* warning: all these variables apply across multiple threads of active servlet */
private static String lastPostQueryStr=null;
private static String contextName;
private static String databaseName;
private static String tomcatHome;
private static boolean noisy = false;
/* private static int tempPostGenerationLimit=-1; */
}
