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
<tr>	<td colspan="3" align="center"><h2>Thank You</h2></td></tr>
<tr><td width="18%" align="left" valign="top">
	<jsp:include page="left.jsp" flush="true" />
	</td>
	<td width="64%" valign="top">
		<img src="icons/mail.gif" alt="picture of mailbox"/><br/>
		<p>Thank you for contacting us.</p>
		<p>We will respond to your inquiry as soon as possible.</p>
		<p>Click to return to the <a href="index.jsp">home page</a>.</p>
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
