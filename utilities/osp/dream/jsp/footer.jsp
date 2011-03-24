<HTML>
<HEAD>
<%@ page import="java.util.*, java.lang.String.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session"/>
</HEAD>
<BODY>
<P/>
<TABLE CELLSPACING="0" WIDTH="100%" border="0">
<tr class="darkbackground" height="25"><td >&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=awards"class="headerlink">Awards (all Details)</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=sponsors"class="headerlink">Sponsors (all details)</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=all.investigator"class="headerlink">Investigators (all details)</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=all.departments"class="headerlink">Departments (all details)</a></td><td>&nbsp;</td>
<!-- 	<td align="center"><a href="index.jsp" class="headerlink">Home</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="admin.jsp" class="headerlink">Admin</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="fetch?queryspec=public_cases" class="headerlink">Cases</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="searchclips.jsp" class="headerlink">Clips</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="fetch?queryspec=public_entrepreneurs" class="headerlink">Entrepreneurs</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="fetch?queryspec=public_companies" class="headerlink">Companies</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="fetch?queryspec=public_themes" class="headerlink">Themes</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="subjheadings.jsp" class="headerlink">Subject Headings</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="formats.jsp" class="headerlink">Formats</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="discs.jsp" class="headerlink">Discs</a></td><td>&nbsp;</td> -->
<!-- 	<td align="center"><a href="help/help.jsp" class="headerlink">Help</a></td><td>&nbsp;</td> -->
<%	if ( loginHandler.getLoginStatus().equals("authenticated")) {	
		String currentSessionId = session.getId();
		String storedSessionId = loginHandler.getSessionId();
		if ( currentSessionId.equals( storedSessionId ) ) {
			String currentRemoteAddrStr = request.getRemoteAddr();
			String storedRemoteAddr = loginHandler.getLoginRemoteAddr();
			if ( currentRemoteAddrStr.equals( storedRemoteAddr ) ) { %>
				<td><a target="_top" href="index.jsp">Log Out</a></td>
<%			} else { %>
				<td><a target="_top" href="index.jsp">Log In</a></td>
<%			}
		} else { %>
			<td><a target="_top" href="index.jsp">Log In</a></td>
<%		}
	} else { %>
		<td><a target="_top" href="index.jsp">Log In</a></td>
<%	} %>
</TR>
</TABLE>
</BODY>
</HTML>

