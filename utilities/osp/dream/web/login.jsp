<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session" />
<html>
<head>
<%
int MAX_PASSWORD_LENGTH=20; // variable not used -- hardcoded in isEmptyOrWrongLength
int imageWidth=70;
int nColumns=7;
int securityLevel=-1;
if ( loginHandler.getLoginStatus().equals("authenticated")) { /* test if session is still valid */
	securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
}
String usernameStr=request.getParameter("username");
%>
<title>Log In</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp">
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
	//this does not pass the data to the servlet: window.close();
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
</head>
<body>
<div id="main" >
<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">
<tr><td colspan="3"><jsp:include page="head.jsp" flush="true" ><jsp:param name="current" value="log in" /></jsp:include></td></tr>
<tr><td colspan="3" align="center"><h2>Log in to the Collection</h2></td></tr>
<tr><td width="18%" align="left" valign="top">
	<jsp:include page="left.jsp" flush="true" >
		<jsp:param name="highlight" value="login" />
	</jsp:include>
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
				(status unrecognized: <%=loginHandler.getLoginStatus()%>)
<%			} %>
			</h3>
			</td></tr>
			<tr>
				<td valign="top" align="left" >
				<form name="login" action="login_process.jsp" method="post" onSubmit="return isValidForm(this);">
				<p>Username:</p>
<%					String status= loginHandler.getLoginStatus();
					if ( status.equals("bad_password") || status.equals("first_login_no_password") 
						|| status.equals("first_login_mistyped") || status.equals("first_login_changing_password")
						|| status.equals("changing_password_repeated_old") || status.equals("changing_password") ) { %>
						<p><input tabindex="1" type="username" name="loginName" value='<%=loginHandler.getLoginName()%>' size="10" class="form-item" />
							&nbsp;<i><a href="forgotten_username.jsp">forgot your username?</a></i></p>
<%					} else if (usernameStr != null && !usernameStr.equals("")) { %>
						<p><input tabindex="1" type="username" name="loginName" value='<%=usernameStr%>'size="10" class="form-item" /></p>
<%					} else { %>
						<p><input tabindex="1" type="username" name="loginName" size="10" class="form-item" />
							&nbsp;<i><a tabindex="4" href="forgotten_username.jsp">forgot your username?</a></i></p>
<%					} %>
					<p>Password:</p>
<%					String errorMsg=loginHandler.getErrorMsg("loginPassword");
					if ( errorMsg!=null && !errorMsg.equals("")) { %>
						<p><font color=red><%=loginHandler.getErrorMsg("loginPassword")%></font></p>
<%					} %>
					<p><input tabindex="2" type="password" name="loginPassword" size="10" class="form-item" />
						&nbsp;<i><a tabindex="5" href="forgotten_password.jsp">forgot your password?</a></i></p>
					<input tabindex="3" type="submit" name="loginSubmitMode" value="Log In" class="form-item" />
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
