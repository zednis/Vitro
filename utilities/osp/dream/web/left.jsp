<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session"/>
<%
String highlightStr=(highlightStr=(String)request.getAttribute("highlight"))==null ? ((highlightStr=request.getParameter("highlight"))==null ? "none":highlightStr) : highlightStr;
int securityLevel=-1;
if ( loginHandler.getLoginStatus().equals("authenticated")) { /* test if session is still valid */
	securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
}
%>
<html>
<head>
<title>Footer</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp">
</head>
<body>
<span id="leftsidebar" >
	<table border="0" width="80%" cellspacing="0" cellpadding="3">
	<tr><td class="<%=highlightStr.equalsIgnoreCase("topics") ? "topTopicButtonSelected" : "topTopicButton"%>"><a href="topics.jsp">themes</a></td></tr>
	<tr><td class="<%=highlightStr.equalsIgnoreCase("entrepreneurs") ? "topicButtonSelected" : "topicButton"%>"><a href="entrepreneurs.jsp">entrepreneurs/experts</a></td></tr>
	<tr><td class="<%=highlightStr.equalsIgnoreCase("cases") ? "topicButtonSelected" : "topicButton"%>"><a href="cases.jsp">cases</a></td></tr>
	<tr><td class="<%=highlightStr.equalsIgnoreCase("companies") ? "topicButtonSelected" : "topicButton"%>"><a href="companies.jsp">companies</a></td></tr>
	<tr><td class="spacerButton" >&nbsp;</td></tr>
	<tr><td class="<%=highlightStr.equalsIgnoreCase("search") ? "topTopicButtonSelected" : "topTopicButton"%>"><a href="search.jsp">search tips</a></td></tr>
<%	if (securityLevel<1) { %>
	<tr><td class="spacerButton" >&nbsp;</td></tr>
	<tr><td class="<%=highlightStr.equalsIgnoreCase("login") ? "topTopicButtonSelected" : "topTopicButton"%>"><a href="login.jsp">log in</a></td></tr>
	<tr><td class="<%=highlightStr.equalsIgnoreCase("register") ? "topicButtonSelected" : "topicButton"%>"><a href="register.jsp">register</a></td></tr>
	<tr><td class="spacerButton" >&nbsp;</td></tr>
	<tr><td><p class="smaller"><i><strong>Welcome to the Collection</strong>.  Unregistered users may preview fully-functional <a href="cases.jsp">cases</a> for the
				<a href="entrepreneurs.jsp">entrepreneurs</a> outlined in red on the home page, including information on their
				<a href="companies.jsp">companies</a>, relevant <a href="topics.jsp">themes</a>, and associated video clips.<br />
				For access to the full contents of the site, please <a href="register.jsp">register</a> with a
				few elements of basic information (name, email address and affiliation) to help us improve our site and its content.</i></p></td></tr>
<%	} %>
	</table>
</span>
</body>
</html>