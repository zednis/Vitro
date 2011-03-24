<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session"/>
<html>
<head>
<%
int imageWidth=70;
int nColumns=7;
%>
<title>OSP Data Warehouse</title>
<meta name="DC.Title" content="CALS Sponsored Research Projects"/>
<meta name="DC.Description" content="active sponsored research"/>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp">
<script type="text/javascript" language="Javascript" >
function setWindowName() {
	window.document.name="homebase";
}
</script>
</head>
<body onload="javascript:setWindowName();">
<div id="main" >
<%
int securityLevel=-1;
if ( loginHandler.getLoginStatus().equals("authenticated")) { /* test if session is still valid */
	securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
} %>


<div id="bodyContainer">
<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">

<tr><td colspan="3" align="center"><h2>OSP Data Warehouse</h2></td></tr>
<tr><td width="18%" align="left" valign="top">
	</td>
	<td width="64%" align="center" valign="top" >
		<table border="0" width="100%" cellspacing="0" cellpadding="0">
<%		if (securityLevel<0) { %>
			<tr><td colspan="<%=nColumns%>" ><h3 class="celltopcentered"><a href="register.jsp">Register</a> for Access to the Data Warehouse</h3></td></tr>
<%		} %>
		<tr><td colspan="<%=nColumns%>" ><h3 class="celltopcentered"><a href="fetch?queryspec=awards">Retrieve projects</a></h3></td></tr>
		</table>
	</td>
	<td width="18%" align="right" valign="top">
	</td>
</tr>
</table>
</div>
</div>
</body>
</html>
