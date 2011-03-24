package formbeans;
import java.util.*;

public class LoginFormBean {
	private String sessionId;
	private String loginBrowser;
	private String loginRemoteAddr;
	private int    userId;
	private String loginName;
	private String loginPassword;
	private String duplicatePassword;
	private String loginReminderType;
	private String loginReminder;
	private String loginFirstname;
	private String loginMiddlename;
	private String loginLastname;
	private String loginInstType;
	private String loginInstitution;
	private String loginPersonType;
	private String loginDepartment;
	private String loginStatus;
	private String loginRole;
	private String emailAddress;
	private Hashtable errors;

	public boolean validateLoginForm() {
		boolean allOk=true;

		if ( loginName.equals("")) {
			errors.put( "loginName","Please enter your user name" );
			loginName = "";
			allOk = false;
		}

		if ( loginPassword.equals("")) {
			errors.put( "loginPassword","Please enter your password" );
			loginPassword="";
			allOk=false;
		}

		return allOk;
	}

	/* required parameter-less constructor */
	public LoginFormBean() {
		sessionId         = "";
		loginBrowser      = "";
		loginRemoteAddr   = "";
		userId            = 0;
		loginName         = "";
		loginPassword     = "";
		loginStatus       = "none";
		loginRole         = "1";
		duplicatePassword = "";
		loginReminderType = "";
		loginReminder     = "";
		loginFirstname    = "";
		loginMiddlename   = "";
		loginLastname     = "";
		loginInstType     = "";
		loginInstitution  = "";
		loginPersonType   = "";
		loginDepartment   = "";
		emailAddress      = "";

		errors = new Hashtable();
	}

	public String toString() {
		String output="Login formbean contents:\n";
		output += "user name:          " + getLoginName()         + "\n";
		output += "user id:            " + getUserId()            + "\n";
		output += "encrypted password: " + getLoginPassword()     + "\n";
		output += "duplicate password: " + getDuplicatePassword() + "\n";
		output += "reminder type:      " + getLoginReminderType() + "\n";
		output += "reminder value:     " + getLoginReminder()     + "\n";
		output += "first name:         " + getLoginFirstname()    + "\n";
		output += "middle name:        " + getLoginMiddlename()   + "\n";
		output += "last name:          " + getLoginLastname()     + "\n";
		output += "institution type:   " + getLoginInstType()     + "\n";
		output += "institution:        " + getLoginInstitution()  + "\n";
		output += "person type:        " + getLoginPersonType()   + "\n";
		output += "department:         " + getLoginDepartment()   + "\n";
		output += "email address:      " + getEmailAddress()      + "\n";
		return output;
	}

    /********************** GET METHODS *********************/

    public String getSessionId() {
		return sessionId;
	}

    public String getLoginBrowser() {
		return loginBrowser;
	}

    public String getLoginRemoteAddr() {
		return loginRemoteAddr;
	}

	public int getUserId() {
		return userId;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public String getDuplicatePassword() {
		return duplicatePassword;
	}

	public String getLoginReminderType() {
		return loginReminderType;
	}

	public String getLoginReminder() {
		return loginReminder;
	}

	public String getLoginFirstname() {
		return loginFirstname;
	}

	public String getLoginMiddlename() {
		return loginMiddlename;
	}

	public String getLoginLastname() {
		return loginLastname;
	}

	public String getLoginInstType() {
		return loginInstType;
	}

	public String getLoginInstitution() {
		return loginInstitution;
	}

	public String getLoginPersonType() {
		return loginPersonType;
	}

	public String getLoginDepartment() {
		return loginDepartment;
	}

	public String getLoginStatus() {
		return loginStatus;
	}

	public String getLoginRole() {
		return loginRole;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getErrorMsg( String s ) {
		String errorMsg =(String) errors.get( s.trim() );
		return ( errorMsg == null ) ? "" : errorMsg;
	}

    /********************** SET METHODS *********************/

    public void setSessionId( String string_val ) {
		sessionId = string_val;
	}

    public void setLoginBrowser( String string_val ) {
		loginBrowser = string_val;
	}

    public void setLoginRemoteAddr( String string_val ) {
		loginRemoteAddr = string_val;
	}

	public void setUserId( int int_val ) {
		userId=int_val;
	}

	public void setLoginName( String string_val ) {
		loginName = string_val;
	}

	public void setLoginPassword( String string_val) {
		loginPassword = string_val;
	}

	public void setDuplicatePassword( String string_val ) {
		duplicatePassword = string_val;
	}

	public void setLoginReminderType( String string_val) {
		loginReminderType=string_val;
	}

	public void setLoginReminder( String string_val) {
		loginReminder=string_val;
	}

	public void setLoginFirstname( String string_val) {
		loginFirstname=string_val;
	}

	public void setLoginMiddlename( String string_val) {
		loginMiddlename=string_val;
	}

	public void setLoginLastname( String string_val) {
		loginLastname=string_val;
	}

	public void setLoginInstType( String string_val) {
		loginInstType=string_val;
	}

	public void setLoginInstitution( String string_val) {
		loginInstitution=string_val;
	}

	public void setLoginPersonType( String string_val) {
		loginPersonType=string_val;
	}

	public void setLoginDepartment( String string_val) {
		loginDepartment=string_val;
	}

	public void setLoginStatus( String string_val ) {
		loginStatus = string_val;
	}

	public void setLoginRole( String string_val ) {
		loginRole = string_val;
	}

	public void setEmailAddress( String string_val ) {
		emailAddress = string_val;
	}

	public void setErrorMsg( String key, String msg ) {
		errors.put( key,msg );
	}

}
