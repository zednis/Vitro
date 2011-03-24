<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session"/>
<%
int MAX_PASSWORD_LENGTH=20; // variable not used -- hardcoded in isEmptyOrWrongLength
%>
<script language="JavaScript" src="md5.js"></script>
<script language="JavaScript">
<!-- Hide from browsers without JavaScript support

function isValidForm( theForm ) {
	if ( isEmpty( theForm.loginName.value)) {
		theForm.loginName.focus();
		return false;
	}
	if ( isEmptyOrWrongLength( theForm.loginPassword.value)) {
		theForm.loginPassword.focus();
		return false;
	}
	
	//alert("theForm.loginPassword.value=" + theForm.loginPassword.value );
	theForm.loginPassword.value = calcMD5( theForm.loginPassword.value );
	//alert("theForm.loginPassword.value=" + theForm.loginPassword.value );
	return true;
}

function isEmpty( aStr ) {
	if ( aStr.length == 0 ) {
		alert("Please enter both username and password to log in");
		return true;
	}
	return false;
}

function isEmptyOrWrongLength( aStr ) {
	if ( aStr.length == 0 ) {
		alert("Please enter both username and password to log in");
		return true;
	} else if ( aStr.length < 6 || aStr.length > 20 ) {
		alert("Please enter a password between 6 and 20 characters long");
		return true;
	}
	return false;
}

-->
</script>
<html>
<head>
<title>Streeter Project Prototype</title>
<meta name="DC.Title" content="ICE"/>
<meta name="DC.Description" content="entrepreneurship"/>
<link rel="stylesheet" type="text/css" href="css/global.css.jsp">
</head>
<body bgcolor="#F5F5E1" leftmargin="5" topmargin="10" marginheight="5" marginwidth="5">
<table width="800" border="0" cellspacing="0" cellpadding="0" align="center">
<tr><td valign="top">
	<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td><img src="icons/transparent.gif" width="1" height="25" border="0"></td>
		<td colspan="3" class="darkbackground">
			<table cellspacing="0" cellpadding="6" border="0">
			<tr><td>&nbsp;</td>
				<td align="center"><a href="index.jsp" class="headerlink">Home</a></td><td>&nbsp;</td>
				<td align="center"><a href="admin.jsp" class="headerlinkOn">Admin</a></td><td>&nbsp;</td>
				<td align="center"><a href="fetch?queryspec=public_cases" class="headerlink">Cases</a></td><td>&nbsp;</td>
				<td align="center"><a href="searchclips.jsp" class="headerlink">Clips</a></td><td>&nbsp;</td>
				<td align="center"><a href="fetch?queryspec=public_entrepreneurs" class="headerlink">Entrepreneurs</a></td><td>&nbsp;</td>
				<td align="center"><a href="fetch?queryspec=public_companies" class="headerlink">Companies</a></td><td>&nbsp;</td>
				<td align="center"><a href="fetch?queryspec=public_themes" class="headerlink">Themes</a></td><td>&nbsp;</td>
				<td align="center"><a href="subjheadings.jsp" class="headerlink">Subject Headings</a></td><td>&nbsp;</td>
				<td align="center"><a href="formats.jsp" class="headerlink">Formats</a></td><td>&nbsp;</td>
				<td align="center"><a href="discs.jsp" class="headerlink">Discs</a></td><td>&nbsp;</td>
				<td align="center"><a href="help/help.jsp" class="headerlink">Help</a></td><td>&nbsp;</td>
			</tr>
			</table>
		</td>
	</tr>
	<tr><td colspan="4" height="1"><img src="icons/transparent.gif" width="100%" height="1" border="0"></td></tr>
	<tr><td width="1"><img src="icons/transparent.gif" width="1" height="30" border="0"></td>
		<td height="30" width="16%" background="icons/videoworkflow.gif">
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr><td><img src="icons/transparent.gif" width="1" height="10"></td></tr>
			<tr><td align="center" valign="bottom"><font color="FFFFFF"><h2><font color="FFFFFF">&nbsp;</font></h2></font></td>
			</tr>
			</table>
		</td> 
		<td width="1"><img src="icons/transparent.gif" width="1" height="30" border="0"></td>
		<td width="100%" valign="top" background="icons/videoworkflow.gif">
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr><td colspan="2"><img src="icons/transparent.gif" width="1" height="10"></td></tr>
			<tr><td><img src="icons/transparent.gif" height="1" width="7" border="0"></td>
				<td><font color="FFFFFF"><h1><font color="FFFFFF">Digital Video Clips on Business and Entrepreneurship</font></h1></font></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr><td colspan="4"><img src="icons/transparent.gif" width="100%" height="1" alt="" border="0"></td></tr>
	<tr>
		<td bgcolor="#FFFFFF" width="1"><img src="icons/transparent.gif" width="1" height="100%" border="0"></td>
		<td valign="top">
			<!-- ########## start Legend block ########## -->
			<table border="0" width="100%" cellspacing="0" cellpadding="0">
			<tr><td colspan="3" bgcolor="E0FFFF"><img src="icons/transparent.gif" width="100%" height="10" border="0"></td></tr>
			<tr><!--------- begin legend -------- -->
				<td class="lightbackground" width="10" >&nbsp;</td>
				<td class="lightbackground" valign="top" align="left" height="100%">							
<%
if ( loginHandler.getLoginStatus().equals("authenticated")) { 
	/* test if session is still valid */
 	String currentSessionId = session.getId();
	String storedSessionId = loginHandler.getSessionId();
	if ( currentSessionId.equals( storedSessionId ) ) {
		String currentRemoteAddrStr = request.getRemoteAddr();
		String storedRemoteAddr = loginHandler.getLoginRemoteAddr();
		int securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
		if ( currentRemoteAddrStr.equals( storedRemoteAddr ) ) {  
%>
				<p><strong>Logged In</strong><br/>
				User: <i><jsp:getProperty name="loginHandler" property="loginName"/></i></p>
				<form name="logout" action="login_process.jsp" method="post">
					<input type="submit" name="loginSubmitMode" value="Log Out" class="form-item" style="font-size: 10px;" />
				</form>
<%		} else { %>
				<strong>Program Login</strong><br>
				<i>(IP address has changed)</i><br>
<%			loginHandler.setLoginStatus("logged out");
	 	} 
	} else { 
		loginHandler.setLoginStatus("logged out");				
%>
				<strong>Program Login</strong><br/>
				<i>(session has expired)</i><br/>
					<form name="login" action="login_process.jsp" method="post" onSubmit="return isValidForm(this) ">
					Username:<br>
					<input type="username" name="loginName" size="10" class="form-item" /><br><br>
					Password:<br>
					<input type="password" name="loginPassword" size="10" class="form-item" />
					<br><br>
					<input type="submit" name="loginSubmitMode" value="Log In" class="form-item" style="font-size: 10px;" />
				</form>
<% 	
	}
} else { /* not thrown out by coming from different IP address or expired session; check login status returned by authenticate.java */ %>
				<strong>Program Login</strong><br/>
<%	if ( loginHandler.getLoginStatus().equals("logged out")) { %>
				<i>(currently logged out)</i>
<%	} else if ( loginHandler.getLoginStatus().equals("bad_password")) { %>				
				<i>(password incorrect)</i><br/>
<%	} else if ( loginHandler.getLoginStatus().equals("first_login_no_password")) { %>
				<i>(1st login; need to request initial password below)</i>		
<%	} else if ( loginHandler.getLoginStatus().equals("first_login_mistyped")) { %>
				<i>(1st login; initial password entered incorrectly)</i>		
<%	} else if ( loginHandler.getLoginStatus().equals("first_login_changing_password")) { %>
				<i>(1st login; changing to new private password)</i>		
<%	} else if ( loginHandler.getLoginStatus().equals("changing_password_repeated_old")) { %>
				<i>(changing to a different password)</i>		
<%	} else if ( loginHandler.getLoginStatus().equals("changing_password")) { %>
				<i>(changing to new password)</i>		
<%	} else if ( loginHandler.getLoginStatus().equals("none")) { %>
				<i>(new session)</i><br/>
<%	} else { %>	
				<i>(status unrecognized: <%=loginHandler.getLoginStatus()%>)</i><br/>
<%	} %>
				<p/>
				<form name="login" action="login_process.jsp" method="post" onSubmit="return isValidForm(this) ">
						Username:<br/>
<% String status= loginHandler.getLoginStatus();
	if ( status.equals("bad_password") || status.equals("first_login_no_password") 
		|| status.equals("first_login_mistyped") || status.equals("first_login_changing_password")
		|| status.equals("changing_password_repeated_old") || status.equals("changing_password") ) { %>
						<input type="username" name="loginName" value='<%=loginHandler.getLoginName()%>' size="10" class="form-item" /><br/><br/>
<% } else { %>
						<input type="username" name="loginName" size="10" class="form-item" /><br/><br/>
<% } %>
						Password:<br/>
						<font color=red><%=loginHandler.getErrorMsg("loginPassword")%></font><br/>
						<input type="password" name="loginPassword" size="10" class="form-item" /><br/><br/>
						<input type="submit" name="loginSubmitMode" value="Log In" class="form-item" style="font-size: 10px;" />
				</form>
<%		
} %>
				</td>
				<td class="lightbackground"><img src="icons/transparent.gif" width="10" height="1" alt="" border="0"></td>
				<!-- ----- end legend ----- -->
			</tr>
			<tr><td colspan="3" bgcolor="E0FFFF"><img src="icons/transparent.gif" width="100%" height="30" border="0"></td></tr>
			</table>
			<!-- ########## end Legend block ########## -->
		</td>
		<td colspan="2" width="100%" valign="top">
			<table border="0" width="100%" cellspacing="0" cellpadding="0">
			<tr><td colspan="3"><img src="icons/transparent.gif" width="100%" height="12" border="0"></td></tr>
			<tr>
				<td width="15">&nbsp;</td>
				<td>
					<h2>Welcome to the staff interface for the ICE project</h2>
				</td>
				<td width="10">&nbsp;</td>
			</tr>
			<tr><!--------- begin legend -------- -->
				<td width="15" >&nbsp;</td>
				<td class="lightbackground" valign="top" align="left">							
					<h3>Editing:</h3>
					<ul><li><a href="fetch?queryspec=public_quotes">quotes</a></li>
						<li><a href="fetch?queryspec=public_interviewers">interviewers</a></li>
					</ul>
<%					if ( loginHandler.getLoginStatus().equals("authenticated")) {	
						String currentSessionId = session.getId();
						String storedSessionId = loginHandler.getSessionId();
						if ( currentSessionId.equals( storedSessionId ) ) {
							String currentRemoteAddrStr = request.getRemoteAddr();
							String storedRemoteAddr = loginHandler.getLoginRemoteAddr();
							int securityLevel = Integer.parseInt(loginHandler.getLoginRole());
							if ( currentRemoteAddrStr.equals( storedRemoteAddr ) ) {%>
<%								if ( securityLevel > 3 ) { %>
									<h3>Uploads:</h3>	
									<ul><li><a href="uploadppts.jsp">upload powerpoint files</a></li>		
										<li><a href="uploadfiles.jsp">upload headshots</a></li>
										<li><a href="batchloadclipnames.jsp">upload clip names</a></li>
										<li><a href="fetch?queryspec=private_clipSandboxes">clip sandbox</a></li>		
										<li><a href="batchloadclips.jsp">upload completed clip metadata</a></li>
									</ul>
									<h3>Data Processing:</h3>
									<ul><li><a href="setclipsizes.jsp">review status of clips on server</a></li>
										<li><a href="datainventory.jsp">inventory clips on server by case directory</a></li>				
										<li><a href="metafiles.jsp">update windows media video metafiles</a></li>				
										<li><a href="quicktimes.jsp">update quicktime and mpeg 1 metafiles</a></li>
									</ul>
									<h3>User Reporting:</h3>
									<ul><li><a href="fetch?queryspec=private_searchFailures">failed searches</a></li>			
										<li><a href="fetch?queryspec=private_clipdownloads">clip downloads</a></li>
									</ul>			
<%										if ( securityLevel > 4 ) { %>				
										<h3>Reserved Functionality:</h3>
										<ul><li><a href="fetch?queryspec=private_titlesMatched">clip titles starting with names</a></li>		
											<li><a href="fetch?queryspec=private_titlesUnmatched">clip titles not starting with names</a></li>							
											<!--li><a href="cliptitles.jsp">re-process clip titles</a></li-->
											<li><a href="addkeyword.jsp">add keyword</a></li>
											<li><a href="schema.gif">database schema</a></li>
											<li><a href="fetch?queryspec=public_clips_without_cases">clips without cases</a></li>															
											<li><a href="fetch?queryspec=public_clips_without_keywords">clips without keywords</a></li>														
											<li><a href="fetch?queryspec=public_clips_without_formats">clips without formats</a></li>
											<li><a href="fetch?queryspec=public_clips_without_people">clips without people</a></li>
											<li><a href="fetch?queryspec=public_headings">all subject headings</a></li>
											<li><a href="addkeyword.jsp">add keyword</a></li>
										</ul>
<%									}
								}
							}
						}
					} %>			
					<!--li><a href="fetch?queryspec=public_discs">discs</a></li-->
				</td>
				<td width="10">&nbsp;</td>
				</tr>
			</table>
		</td>				
	</tr>
	</table>
	<!--end main map title, map, and legend table -->
	</td>
</tr>
<!-- begin footer -->
<tr><td bgcolor="#444466">
	<table border="0" cellspacing="0" cellpadding="0" height="25">
	<tr><td bgcolor="#FFFFFF" width="1"><img src="icons/transparent.gif" width="1" height="38" border="0"></td>
		<td width="10"></td>
		<td><p class="footerlink">
			<a href="search_sim/quick_search.html" class="footerlink">Search</a> | 
			<a href="contact_us" class="footerlink">Contact Us</a>
			<br>
		</td>
	</tr>
	</table>
	</td>
</tr>
<tr><td bgcolor="#FFFFFF"><img src="icons/transparent.gif" width="1" height="1" alt="" border="0"></td></tr>
<tr><td style="padding: 4"><p class="footertext">&copy; <a target="_new" href="http://www.library.cornell.edu">Cornell University Library</a>, <a target="_new" href="http://www.cornell.edu">Cornell University</a>.</p></td></tr>
<!-- end footer -->
</table>
<!-- #### all page-wide content fits above here #### -->
</form>
</body>
</html>
