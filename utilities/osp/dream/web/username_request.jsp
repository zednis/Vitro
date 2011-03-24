<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<html>
<head>
<link rel='stylesheet' type='text/css' href='../css/home.css.jsp'/>
<title>Username Request</title>
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
		errorList = 'Please format your e-mail address as: \"abc3@cornell.edu\" or enter another complete email address';
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
<table width="800" cellpadding="1" cellspacing="1" border="0" align="center">
<tr><td colspan="7">
	<jsp:include page="../jsp/header.jsp" flush="true" />
	<!--jsp:param name="pagetitle" value="Find Genomics Related Courses" /-->
	<!--/jsp:include-->
	</td>
</tr>
<tr><td colspan="7" height="1"><img src="../images/transparent.gif" width="100%" height="1" border="0"></td></tr>
<tr><td align="center" colspan="7"><h2>Vivo Username Request</h2></tr>
<!--img height="125" src="../images/feedback.jpg" alt="picture of fan"/><br/-->
<tr><td>
		<p>Thank you for your interest in becoming a registered user on the Vivo website.</p>
		<p>Please submit your request for a username in the form below.</p>
		<form name = "contact_form" action="../servlet/sendmail" method="post" onSubmit="return ValidateForm('contact_form');">
		<input type=hidden  name="sendto" value = "vivo-l@postoffice.mannlib.cornell.edu">
		<input type="hidden" name="confirmpage" value="../contact/thankyou.jsp">
		<input type="hidden" name="errpage" value="../contact/contact_err.jsp">
		<input type="hidden" name="mailfromname" value="Request for a Vivo User Name">
		<!-- for javascript form verification -->
		<input type="hidden" name="RequiredFields" value="webuseremail,webusername,institution">
		<input type="hidden" name="RequiredFieldsNames" value="email address,full name,institutional affiliations">
		<input type="hidden" name="EmailFields" value="webuseremail">
		<input type="hidden" name="EmailFieldsNames" value="email address">
		
		<p>My email address (e.g., abc3<b>@cornell.edu</b>) is:<br/>
			<input type="text" name="webuseremail" size="30">
		</p>
		<p>My full name is:<br>
			<input type="text" name="webusername" size="40">
		</p>
		<p class="body"><strong>Departmental, Field, and Project Affiliation(s):</strong></p>
		<textarea rows="5" cols="50" name="institution" class="form-item"></textarea>
		<p/>
		<p><i>In this pre-release version, many records are missing or incomplete.<br>
		Please help us out by providing suggestions for additional content (people, departments, courses, genomics services, etc.) that belong in Vivo.</i></p>
		<h3>Enter your comments, questions, or suggestions in the box below.</h3>
		<p>
		<textarea name="comments" rows="10" cols="50" wrap="physical"></textarea>
		<p>
		<input type="submit" value="Send Request" class="yellowbutton">
		<input type="reset" value="Clear Form" class="plainbutton">
		</p>
		<p>You will be notified of your initial password by email when your Vivo user account has been set up.</p>
		<p>The first time you log in you will be asked to enter the initial password, and then to change it to a private password that only you will know.
			Passwords must be between 6 and 12 characters long, and are encrypted before being sent to the server.</p>
		<h3>Thank you!</h3>
		</form>
	</td>
</tr>
<!--FOOTER-->
<tr><td colspan="7">
	<jsp:include page="../jsp/footer.jsp" flush="true">
	<jsp:param name="header" value="none"/>
	</jsp:include>
	</td>
</tr>
</table>
</body>
</html>

