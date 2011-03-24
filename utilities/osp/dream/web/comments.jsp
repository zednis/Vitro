<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<html>
<head>
<link rel='stylesheet' type='text/css' href='css/home.css.jsp'/>
<title>Comments</title>
<script language="JavaScript">
<!--
function ValidateForm(formName) {
	var x = 0; // counts form elements - used as array index
	var y = 0; // counts required fields - used as array index
	errors = false;
	var errorList;

	if (document.forms[formName].RequiredFields) {
		errorList = 'Please fill out the following required fields:\n';
		// build array of required fields
		reqStr = document.forms[formName].RequiredFields.value;
		requiredFields = reqStr.split(',');
		// build array holding the names of required fields as
		// displayed in error box
		if (document.forms[formName].RequiredFieldsNames) {
			reqNameStr = document.forms[formName].RequiredFieldsNames.value;
		} else {
			reqNameStr = document.forms[formName].RequiredFields.value;
		}
		requiredNames = reqNameStr.split(',');
		// Loop through form elements, checking for required fields
		while ((x < document.forms[formName].elements.length)) {
			if (document.forms[formName].elements[x].name == requiredFields[y]) {
				if (document.forms[formName].elements[x].value == '') {
					errorList += requiredNames[y] + '\n';
					errors = true;
				}
				y++;
			}
			x++;
		}
		if (errors) {
			alert(errorList);
			return false;
		}
	x = 0;
	y = 0;
	}

	// Check for Email formatting
	if (document.forms[formName].EmailFields) {
		errorList = 'Please format your e-mail address as: \"netId@cornell.edu\" or enter another complete email address';
		// build array of required fields
		emailStr = document.forms[formName].EmailFields.value;
		emailFields = emailStr.split(',');
		// build array holding the names of required fields as
		// displayed in error box
		if (document.forms[formName].EmailFieldsNames) {
			emailNameStr = document.forms[formName].EmailFieldsNames.value;
		} else {
			emailNameStr = document.forms[formName].EmailFields.value;
		}
		emailNames = emailNameStr.split(',');
		// Loop through form elements, checking for required fields
		while ((x < document.forms[formName].elements.length)) {
			if (document.forms[formName].elements[x].name == emailFields[y]) {
				if ((document.forms[formName].elements[x].value.indexOf('@') < 1)
					|| (document.forms[formName].elements[x].value.lastIndexOf('.') < document.forms[formName].elements[x].value.indexOf('@')+1)) {
					errors = true;
				}
				y++;
			}
			x++;
		}
		if (errors) {
			alert(errorList);
			return false;
		}
	x = 0;
	y = 0;
	}

	return true;
}
// -->
</script>
</head>
<body>
<div id="main" >
<table width="100%" cellpadding="0" cellspacing="0" border="0" align="center">
<tr><td colspan="3"><jsp:include page="head.jsp" flush="true" ><jsp:param name="current" value="comments" /></jsp:include></td></tr>
<tr>	<td colspan="3" align="center"><h2>Contact Us</h2></td></tr>
<tr><td width="18%" align="left" valign="top">
	<jsp:include page="left.jsp" flush="true" />
	</td>
	<td width="64%" valign="top">
		<p> Please use <b>this form</b> to submit comments or questions.</p>
		<form name = "contact_form" action="sendmail" method="post" onSubmit="return ValidateForm('contact_form');">
		<input type=hidden  name="sendto" value = "streeter-l@postoffice.mannlib.cornell.edu">
		<input type="hidden" name="confirmpage" value="thankyou.jsp">
		<input type="hidden" name="errpage" value="contact_err.jsp">
		<input type="hidden" name="mailfromname" value="Message via the Streeter Contact Form">
		<input type="hidden" name="RequiredFields" value="webuseremail,webusername,comments">
		<input type="hidden" name="RequiredFieldsNames" value="Email address,Name,Comments">
		<input type="hidden" name="EmailFields" value="webuseremail">
		<input type="hidden" name="EmailFieldsNames" value="emailaddress">
		
		<p>My email address (e.g., abc1<b>@cornell.edu</b>) is:<br/>
			<input type="text" name="webuseremail" size="30">
		</p>
		<p>My full name is:<br>
			<input type="text" name="webusername" size="40">
		</p>
		<p><i>Please help us out by providing suggestions for additional content (people, businesses, entrepreneurship themes) that belong on this site.</i></p>
		<h3>Enter your comments, questions, or suggestions in the box below.</h3>
		<p>
		<textarea name="comments" rows="10" cols="50" wrap="physical"></textarea>
		<p>
		<input type="submit" value="Send Mail" class="form-button">
		<input type="reset" value="Clear Form" class="form-button">
		</p>
		<h3>Thank you!</h3>
		</form>
	</td>
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

