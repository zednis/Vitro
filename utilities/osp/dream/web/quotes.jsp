<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session"/>
<%
int imageWidth=70;
int securityLevel=-1;
if ( loginHandler.getLoginStatus().equals("authenticated")) { /* test if session is still valid */
	securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
}
%>
<html>
<head>
<title>Quotes</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp">
</head>
<body>
	<span id="rightsidebar" >
	<table border="0" width="90%" cellspacing="0" cellpadding="0">
	<database:query id="quote" scope="page">
<%	if (securityLevel<0) { %>
		SELECT quote,personId,clipId FROM quotes,people WHERE quotes.personId=people.id AND people.thumbnail IS NOT NULL AND quotes.securityId<0 ORDER BY rand() LIMIT 1
<%	} else { %>
		SELECT quote,personId,clipId FROM quotes,people WHERE quotes.personId=people.id AND people.thumbnail IS NOT NULL ORDER BY rand() LIMIT 1
<%	} %>
	</database:query>
	<database:rows query="quote">
<%		int col=0;
		String quoteStr=null;
		int personId=0;
		int clipId=0; %>
		<database:columns query="quote" id="theValue" >
<%			switch (col) {
				case 0: quoteStr=theValue; %>
						<tr><td class="topmost"><h5>&quot;<%=quoteStr%>&quot;</h5></td></tr>
						<tr><td align="center"><hr /></td></tr>
<%						break; 
				case 1: personId=Integer.parseInt(theValue); %>
						<database:query id="thumbnail" scope="page">
							SELECT thumbnail, concat(firstname," ",lastname) FROM people WHERE id=<%=personId%>
						</database:query>
						<database:rows query="thumbnail">
							<database:select_columns query="thumbnail" id="selectId" selectValue="selectName" >
								<tr><td align="center">
									<a href="entrepreneur.jsp?id=<%=personId%>" >
										<img width="<%=imageWidth%>px" src="images/thumbs/<%=selectId%>" alt="<%=selectName%>" />
									</a><br />
									<a href="entrepreneur.jsp?id=<%=personId%>" ><%=selectName%></a>
								</td></tr>
							</database:select_columns>
						</database:rows>
						<database:release query="thumbnail" />
<%						break;
				case 2: clipId=Integer.parseInt(theValue); %>
						<tr><td align="center"><hr /><p><a href="clip.jsp?id=<%=clipId%>" >Featured Clip</a></p></td></tr>
<%				break;
			}
			++col;%>
			</database:columns>
		</database:rows>
		<database:release query="quote" />
	</table>
	</span>
</body>
</html>