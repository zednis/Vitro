<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<%@ taglib uri="WEB-INF/tlds/database.tld" prefix="database"%>
<%@ page import="java.util.*" %>
<html>
<head>
<title>Thank you</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp"/>
</head>
<body>
<%
int imageWidth=100;
%>
<div id="main" >
<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">
<tr><td colspan="3">
	<jsp:include page="head.jsp" flush="true" />
	</td>
</tr>
<tr>	<td colspan="3" align="center"><h2>We're Sorry</h2></td></tr>
<tr><td width="18%" align="left" valign="top">
	<jsp:include page="left.jsp" flush="true" />
	</td>
	<td width="64%" valign="top">
		<img src="icons/bomb.gif" alt="picture of bomb" height="200" /><br/>
<%		 String errorString=request.getParameter("ERR");
		if ( errorString != null && !errorString.equals("")) { %>
			<p>The following error occurred in processing your request:<br/>
			<b><%=errorString%></b>
			</p>
<% 		} else { %>
			<p>An unidentified error occurred in processing your request.</p>
<%		} %>
		<p>Please email the <a href="mailto:jc55@cornell.edu">webmaster</a> directly
		and include the error listed above in your message.</p>
		<p>Return to the <a href="index.jsp">home page</a>.</p>
	</td>
	<td width="18%" align="right" valign="top">
		<span id="rightsidebar" >
		</span>
	</td>
</tr>
<jsp:include page="foot.jsp" flush="true" />
</table>
</div>
</body>
</html>
