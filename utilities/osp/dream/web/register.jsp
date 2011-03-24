<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session" />
<html>
<head>
<%
int MAX_PASSWORD_LENGTH=20; // variable not used -- hardcoded in isEmptyOrWrongLength
int nColumns=1;
int securityLevel=-1;
if ( loginHandler.getLoginStatus().equals("authenticated")) { /* test if session is still valid */
	securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
}
String usernameStr=request.getParameter("username");
String emailStr=request.getParameter("email");
%>
<title>Register</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp">
<script language="JavaScript" src="md5.js"></script>
<script language="JavaScript">
<!-- Hide from browsers without JavaScript support

function isValidForm( theForm ) {
	if (isEmpty(theForm.loginName.value,'both username and password to register')) {
		theForm.loginName.focus();
		return false;
	}
	if (isEmptyOrWrongLength(theForm.loginPassword.value,'both username and password to register')) {
		theForm.loginPassword.focus();
		return false;
	}
	if (isEmptyOrDoesNotMatch(theForm.loginPassword.value,theForm.duplicatePassword.value,'the same password twice to confirm it')){
		theForm.duplicatePassword.focus();
		return false;
	}
	if (isEmptyOrNotEmailAddress(theForm.emailAddress.value,'a valid email address to register')) {
		theForm.loginName.focus();
		return false;
	}
	if (isEmpty(theForm.loginReminder.value,'a hint so we can help you recover your password if forgotten')) {
		theForm.loginName.focus();
		return false;
	}
	if (isEmpty(theForm.loginFirstname.value,'your first name')) {
		theForm.loginName.focus();
		return false;
	}
	if (isEmpty(theForm.loginLastname.value,'your last name')) {
		theForm.loginName.focus();
		return false;
	}
	if (isEmpty(theForm.loginDepartment.value,'your primary department or unit within your institution')) {
		theForm.loginName.focus();
		return false;
	}
	
	//alert("theForm.loginPassword.value=" + theForm.loginPassword.value );
	theForm.loginPassword.value = calcMD5( theForm.loginPassword.value );
	theForm.duplicatePassword.value = calcMD5( theForm.duplicatePassword.value );
	return true;
}

function isEmpty( aStr, message ) {
	if ( aStr.length == 0 ) {
		alert("Please enter " + message );
		return true;
	}
	return false;
}

function isEmptyOrWrongLength( aStr, message ) {
	if ( aStr.length == 0 ) {
		alert("Please enter " + message);
		return true;
	} else if ( aStr.length < 6 || aStr.length > 20 ) {
		alert("Please enter a password between 6 and 20 characters long");
		return true;
	}
	return false;
}

function isEmptyOrDoesNotMatch( aStr, bStr, message ) {
	if ( bStr.length == 0 || aStr != bStr ) { // already checked aStr
		alert("Please enter " + message);
		return true;
	}
	return false;
}

function isEmptyOrNotEmailAddress( aStr, message ) {
	if ( aStr.length == 0 || aStr.indexOf("@")<=0 ) {
		alert("Please enter " + message);
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
<tr><td colspan="3"><jsp:include page="head.jsp" flush="true" ><jsp:param name="current" value="register" /></jsp:include></td></tr>
<tr><td colspan="3" align="center"><h2>Register for Full Access to the Collection</h2></td></tr>
<tr><td width="18%" align="left" valign="top">
	<jsp:include page="left.jsp" flush="true" >
		<jsp:param name="highlight" value="register" />
	</jsp:include>
	</td>
	<td width="64%" align="center" valign="top" >
		<table border="0" width="100%" cellspacing="0" cellpadding="0">
		<tr><td colspan="<%=nColumns%>" ><h3 class="celltopcentered">
<%		if (loginHandler.getLoginStatus().equals("authenticated")) { %>
			(already registered and logged in)</h3></td></tr>
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
				<p class="smaller">The information requested here serves two purposes -- first, to allow you to uniquely
				identify yourself	so that when you return to the site you can see what clips you may already have downloaded.
				Second, we	need to build a profile of our user base to help us target further development.</p>
				<form name="login" action="registration_process.jsp" method="post" onSubmit="return isValidForm(this);">
					<table id="register" border="0" cellspacing="2" cellpadding="2" width="100%">
					<tr><td width="33%">
<%						String userMsg=loginHandler.getErrorMsg("loginName");
						if ( userMsg!=null && !userMsg.equals("")) { %>
							<font color="red"><%=userMsg%></font><br />
<%						} %>
						Select a Username:<br />(3-10 characters)<br />
<%						String status= loginHandler.getLoginStatus();
						if ( status.equals("bad_password") || status.equals("first_login_no_password") 
							|| status.equals("first_login_mistyped") || status.equals("first_login_changing_password")
							|| status.equals("changing_password_repeated_old") || status.equals("changing_password") ) { %>
							<input type="username" name="loginName" value='<%=loginHandler.getLoginName()%>' size="10" class="form-item" /></p>
<%						} else if (usernameStr != null && usernameStr.equals("unregistered")) { 
							if (emailStr!=null && !emailStr.equals("")) {%>
								<font color="red">We did not find any registered user with the email address <%=emailStr%></font><br />
<%							} else { %>
								<font color="red">We did not find any registered user matching your input</font><br />
<%							}%>
							<input type="username" name="loginName" value=''size="10" class="form-item" />
<%						} else { %>
							<input type="username" name="loginName" size="10" class="form-item" /></p>
<%						} %>
						</td>
						<td width="34%">
						Password: <br />(6-12 characters)<br />
<%						String errorMsg=loginHandler.getErrorMsg("loginPassword");
						if ( errorMsg!=null && !errorMsg.equals("")) { %>
							<font color="red"><%=loginHandler.getErrorMsg("loginPassword")%></font><br />
<%						} %>
						<input type="password" name="loginPassword" size="10" class="form-item" />
						</td>
						<td width="33%">
						Re-enter<br />password:<br />
						<input type="password" name="duplicatePassword" size="10" class="form-item" />
						</td>
					</tr>
					<tr><td colspan="3" >
						<i>Passwords are encrypted, but please do not re-use a password associated with private personal information</i>
					</td></tr>
					<tr><td colspan="2">
						For security purposes, enter one of the choices below:</br />
						<select name="loginReminderType">
							<option value="mothers_maiden_name">mother's maiden name</option>
							<option value="pet_name">pet's name</option>
							<option value="elementary_school">elementary school</option>
							<option value="first_car">first car</option>
						</select>&nbsp;
						<input type="text" name="loginReminder" size="30" class="form-item" maxlength="40"/></td>
					</tr>
					<tr><td>First name<br /><input type="text" name="loginFirstname" value="" size="15" maxlength="80" /></td>
						<td>Middle initial<br /><input type="text" name="loginMiddlename" value="" size="2" maxlength="80"/></td>
						<td>Last name<br /><input type="text" name="loginLastname" value="" size="20" maxlength="120"/></td>
					</tr>
					<tr><td>Email address:<br />
<%						if (emailStr==null || emailStr.equals("")) {%>
							<input type="text" name="emailAddress" size="30" class="form-item" maxlength="80"/>
<%						} else { %>
							<input type="text" name="emailAddress" size="30" value='<%=emailStr%>' class="form-item" maxlength="80"/>
<%						} %>
						</td>
						<td colspan="2" valign="baseline"><br /><i>We will not share your email address with any third party -- we ask for it only to be able to
							send you a new password if you forget.</i>
						</td>
					</tr>
					<tr>
						<td colspan="2">Primary institutional affiliation:<br />
						<select name="loginInstType">
							<option value="university">university</option>
							<option value="four_year_college">four-year college</option>
							<option value="community_college">community college</option>
							<option value="high_school">high school</option>
							<option value="business">business</option>
							<option value="government">government</option>
							<option value="other">other</option>
						</select>
						<input type="text" name="loginInstitution" size="30" class="form-item" maxlength="80" />
						</td>
						<td>Departmental affiliation:<br />
						<input type="text" name="loginDepartment" size="30" class="form-item" maxlength="60"/>
						</td>
					</tr>
					<tr>
						<td>I will be using the collection as a:<br />
						<select name="loginPersonType">
							<option value="professor">professor/teacher/instructor</option>
							<option value="developer">instruction preparer/media specialist</option>
							<option value="student">student</option>
							<option value="observer">entrepreneur</option>
							<option value="librarian">librarian</option>
							<option value="other">other</option>
						</select></td>
					</tr>
					
					<tr><td><input type="submit" name="loginSubmitMode" value="Register" class="form-item" /></td>
						<td colspan="2"><i>Please print out this page before you submit your registration so you have a record of your entries.</i></td>
					</tr>
					</table>
				</form>
				<p class="smaller">Registered users can immediately access the full contents of the collection.</p>
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
