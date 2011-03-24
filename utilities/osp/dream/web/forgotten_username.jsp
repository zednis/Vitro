<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session" />
<html>
<head>
<%
int nColumns=1;
int securityLevel=-1;
if ( loginHandler.getLoginStatus().equals("authenticated")) { /* test if session is still valid */
	securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
}
%>
<title>Username Reminder</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp">
<script language="JavaScript" src="md5.js"></script>
<script language="JavaScript">
<!-- Hide from browsers without JavaScript support

function isValidForm( theForm ) {
	if ( isEmptyOrNotAnEmailAddress(theForm.emailAddress.value)) {
		theForm.emailAddress.focus();
		return false;
	}
	return true;
}

function isEmptyOrNotAnEmailAddress( aStr ) {
	if ( aStr.length == 0 ) {
		alert("Please enter an email address");
		return true;
	}
	if (aStr.indexOf("@")<=0) {
		alert("Please enter a complete email address in the form user@somewhere.net");
	}
	return false;
}

-->
</script>
</head>
<body>
<div id="main" >
<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">
<tr><td colspan="3"><jsp:include page="head.jsp" flush="true" ><jsp:param name="current" value="log in" /></jsp:include></td></tr>
<tr><td colspan="3" align="center"><h2>Bringing Real World Wisdom into Your Classroom</h2></td></tr>
<tr><td width="18%" align="left" valign="top">
	<jsp:include page="left.jsp" flush="true" />
	</td>
	<td width="64%" align="center" valign="top" >
		<table border="0" width="100%" cellspacing="0" cellpadding="0">
		<tr><td colspan="<%=nColumns%>" ><h3 class="celltopcentered">
<%		if (loginHandler.getLoginStatus().equals("authenticated")) { %>
			(already logged in)</h3></td></tr>
			<tr><td><p class="smaller">Use the "admin" menu above to access user account functions.</p></td></tr>
<%		} else {
			if ( loginHandler.getLoginStatus().equals("logged out")) { %>
				(currently logged out)
<%			} else if ( loginHandler.getLoginStatus().equals("bad_password")) { %>				
				(password incorrect)
<%			} else if ( loginHandler.getLoginStatus().equals("first_login_no_password")) { %>
				(1st login; need to request initial password below)		
<%			} else if ( loginHandler.getLoginStatus().equals("first_login_mistyped")) { %>
				(1st login; initial password entered incorrectly)	
<%			} else if ( loginHandler.getLoginStatus().equals("first_login_changing_password")) { %>
				(1st login; changing to new private password)	
<%			} else if ( loginHandler.getLoginStatus().equals("changing_password_repeated_old")) { %>
				(changing to a different password)	
<%			} else if ( loginHandler.getLoginStatus().equals("changing_password")) { %>
				(changing to new password)		
<%			} else if ( loginHandler.getLoginStatus().equals("none")) { %>
				(new session)
<%			} else { %>	
				(<%=loginHandler.getLoginStatus()%>)
<%			} %>
			</h3>
			</td></tr>
			<tr>
				<td valign="top" align="left" >
				<form name="forgotten_username" action="forgotten_username_process.jsp" method="post" onSubmit="return isValidForm(this);">
					<p>Enter your full email address, and we will check for a previously registered user name:</p>
					<p><input type="text" name="emailAddress" size="30" class="form-item" />
					<input type="submit" name="loginSubmitMode" value="Check" class="form-item" />
				</form>
				<p class="smaller">If you do not yet have a username set up for this collection, please <a href="register.jsp">register</a>.<br />
				Registered users can immediately access the full contents of the collection.</p>
				</td>
			</tr>
<%		} %>
		</table>
	</td>
	<td width="18%" align="right" valign="top">
		<jsp:include page="quotes.jsp" flush="true" />
	</td>
</tr>
<jsp:include page="foot.jsp" flush="true" />
</table>
</div>
</body>
</html>
