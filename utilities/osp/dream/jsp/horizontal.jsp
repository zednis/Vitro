<html>
<%@ page import="java.util.*, java.lang.String.*"%>
<head>
<link rel="stylesheet" type="text/css" href="css/global.css.jsp">
<title>Query Results</title>
<%	String headerStr = (String)request.getAttribute("header");
	if ( headerStr == null || (!headerStr.equalsIgnoreCase("noheader")) ) {
%>
<jsp:include page="header.jsp" flush="true" >
<jsp:param name="header" value="<%=headerStr%>" />
</jsp:include>
<%	} %>
</head>
<BODY leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
<jsp:useBean id="results" class="java.util.ArrayList" scope="request" />
<!-- jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session"/> -->

<!-- ENCLOSING TABLE TO PUT 1 row of space at top and 1 along left edge -->
<table border=0 cellpadding=0 cellspacing=0 width = 100%>
<tr>
	<td class="whitebackground" colspan="2"><img src="icons/transparent.gif" width="1" height="1" border="0"></td>
</tr>
<tr>
<td class="whitebackground" width="1" height="300">&nbsp;</td>

<% int columns = 0;
 	boolean havePostQueryData = false;
   
	String editFormStr = (String)request.getAttribute("editform");
	String minEditRoleStr = (String)request.getAttribute("min_edit_role");
	
 	String firstValue = "null", secondValue = "null";
	Integer columnCount = (Integer)request.getAttribute("columncount");
	columns = columnCount.intValue();
	
	String clickSortStr = (String)request.getAttribute("clicksort");
	
	if ( columns > 0 && results.size() > 0) {	 // avoid divide by zero error in next statement
		/* start enclosing table cell that holds all results */
%>
<td valign="top">
<%		String suppressStr = null;
		boolean isPostQHeaderRow = false;

		if ( ( suppressStr = (String)request.getAttribute("suppressquery")) == null ) { // only inserted into request if true
%>
<i><b><%=(results.size() - columns) / columns %></b> rows of results were retrieved in <b><%= columns %></b> columns for query "<%=request.getAttribute("querystring")%>".</i>
<br/>
<%		}
		Iterator iter = results.iterator();
		int resultCount = 0, primaryRowNumber=0, pageRowNumber=0;
		while (iter.hasNext()) {
			String classString;
						
   			String thisResult = (String)iter.next();
			if ( thisResult.equals("+")) {
				havePostQueryData = true;
				classString = "database_postheader";
				isPostQHeaderRow = true;
				thisResult = "&nbsp";
			} else if ( thisResult.indexOf("@@")== 0) {
				classString=thisResult.substring(2);
				thisResult ="&nbsp;"; //leave as follows for diagnostics: thisResult.substring(2);
				isPostQHeaderRow = false;
			} else {
				classString = isPostQHeaderRow ? "database_postheader" : "row";
				if ( thisResult.equals(""))
					thisResult = "&nbsp;";
			}
    			if ( resultCount == 0 ) { // first pass : column names
%>
<b>
<table border=0 cellpadding=2 cellspacing=0 width = 100%>
<%				if ( clickSortStr != null && clickSortStr.equals("true")) {
					if ( (results.size() - columns) / columns > 2 ) {
%>
<tr>
<td class="database_upperleftcorner" colspan="<%=columns%>">
<i>Click on the column header to sort rows by that column.</i>
</td>
</tr>
<%					}
				}
%>

<tr>
	<td class="rownumheader">#</td>
<%				if ( !(thisResult.equals("XX"))) {
%>
	<td class="database_header">
<%				}
    			} else if ( resultCount == columns ) {  // end column names and start numbered list
				++ primaryRowNumber;
				++ pageRowNumber;
				firstValue = thisResult;
%>
	</b>
<tr valign="top" class="row">
	<td class="rownum">1</td>
<%				if ( !(thisResult.equals("XX"))) { %>
	<td class="row">
<%				}
   			} else if ( resultCount % columns == 0 ) {  // end row and start next row with calculated row number
				++ pageRowNumber;
%>
</tr>
<!-- <tr valign="top" class=<%=classString%> > -->
<%				if ( classString.equals("row")) { 
					if ( havePostQueryData ) {
%>
<tr><td>&nbsp;</td></tr>
<%						havePostQueryData = false;
					}
					++ primaryRowNumber;
%>
<tr valign="top" class="row">
	<td class="rownum"><%= primaryRowNumber /*resultCount / columns*/%></td>
<%				} else { // classString does not equal "row"
%>
<tr valign="top" class=<%=classString%> >
<%				}
				if ( !(thisResult.equals("XX"))) {
%>
	<td class=<%=classString%>>
<%				}
			} else { // not the end of a row
				if ( resultCount <= columns ) { // header rows
					if ( !(thisResult.equals("XX"))) {
%>
	<td class="database_header">
<%
					}
				} else if ( resultCount == columns + 1 ) {
					secondValue=thisResult;
					if ( !( thisResult.equals("XX"))) {
%>
	<td class="row">
<%
					}
				} else  { // cells in later rows
					if ( !( thisResult.equals("XX"))) {
						if ( classString.equals("row")) { 
							if ( primaryRowNumber % 2 == 0 ) {
								if ( pageRowNumber % 2 == 0 ) {
%>
	<td class="rowalternate">
<%								} else {
%>
	<td class="row">
<%								}
							} else if ( pageRowNumber % 2 == 0 ) {
%>
	<td class="rowalternate">
<%							} else {
%>
	<td class="row">
<%							}
						} else {
%>
	<td class=<%=classString%> >							
<%						}
					}	
				}
   			}
			if ( !( thisResult.equals("XX"))) {
%>
					<%= thisResult %>
	</td>
<%
			}
			++ resultCount;
   		} 
%>
</table>
</td>
<%	
	} else { /* results not > 0 */
		Iterator errorIter = results.iterator();
		while ( errorIter.hasNext()) {
			String errorResult = (String)errorIter.next();
%>
			<td>Error returned: <%= errorResult%><br/></td>	
<%
		}
	}
%>
</tr>
</table>
<%
if ( editFormStr != null  && minEditRoleStr != null ) {
	String loginStatus =(String)session.getAttribute("loginStatus");
	if ( loginStatus != null &&  loginStatus.equals("authenticated")) {	
		String currentRemoteAddrStr = request.getRemoteAddr();
		String storedRemoteAddr = (String)session.getAttribute("loginRemoteAddr");
		if ( storedRemoteAddr != null && currentRemoteAddrStr.equals( storedRemoteAddr ) ) {
			int minEditRole = Integer.parseInt(  minEditRoleStr );
			String authorizedRoleStr = (String)session.getAttribute("loginRole");
			if ( authorizedRoleStr != null ) {
				int authorizedRole = Integer.parseInt( authorizedRoleStr );
				if ( authorizedRole >= minEditRole ) { %>
					<jsp:include page="<%=editFormStr%>" flush="true">
					<jsp:param name="firstvalue" value="<%=firstValue%>" />
					<jsp:param name="secondvalue" value="<%=secondValue%>" />
					</jsp:include>
<%						} else { %>
					<jsp:include page="footer.jsp" flush="true">
					<jsp:param name="header" value="<%=headerStr%>" />
					</jsp:include>
<%						}
			} else {	%>
				<jsp:include page="footer.jsp" flush="true">
					<jsp:param name="header" value="<%=headerStr%>" />
				</jsp:include>
<%					}
		} else { %>
			<jsp:include page="footer.jsp" flush="true">
				<jsp:param name="header" value="<%=headerStr%>" />
			</jsp:include>
<%				}
	} else { %>
		<jsp:include page="footer.jsp" flush="true">
			<jsp:param name="header" value="<%=headerStr%>" />
		</jsp:include>
<% }
} else { %>
	<jsp:include page="footer.jsp" flush="true">
		<jsp:param name="header" value="<%=headerStr%>" />
	</jsp:include>
<%		
} %>
</body>
</html>					
