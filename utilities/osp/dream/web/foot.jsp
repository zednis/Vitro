<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session"/>
<html>
<head>
<title>Footer</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp">
<%
String colStr=(colStr=(String)request.getAttribute("cols"))==null ? ((colStr=request.getParameter("cols"))==null ? "3":colStr) : colStr;
%>
</head>
<body>
<tr id="footer" ><td width="18%" >&nbsp;</td>
<%	if (colStr.equals("2")) { %>
	<td colspan="2" width="82%" >
<%	} else { %>
	<td width="64%" >
<%	} %>
		<p>
		<hr class="fat" />
		&copy;<a target="_new" href="http://www.aem.cornell.edu/profiles/streeter.htm">Applied Economics &amp; Management</a>,
		<a target="_new" href="http://www.cornell.edu">Cornell University</a>.
		</p>
	</td>
<%	if (colStr.equals("3")) { %>
	<td width="18%" >&nbsp;</td>
<%	} %>
</tr>
</body>
</html>