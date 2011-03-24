import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.security.*;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;

public class Authenticate extends HttpServlet /*implements SingleThreadModel*/ {

	public void doPost( HttpServletRequest request, HttpServletResponse response ) {
		try {
			HttpSession session = request.getSession();
			LoginFormBean f = (LoginFormBean) session.getAttribute( "loginHandler" );
			System.out.println(f.toString());

			contextName  = getInitParameter("contextName");
			databaseName = getInitParameter("databaseName");
			tomcatHome   = getInitParameter("TOMCAT_HOME");

			Connection con = getConnection();
			Statement stmt = con.createStatement();
			String userEnteredPasswordAfterMd5Conversion=f.getLoginPassword(); // won't be null
			if ( userEnteredPasswordAfterMd5Conversion.equals("") ) { // shouldn't get through JS form verification
				f.setErrorMsg( "loginPassword","please enter a password" );
				f.setLoginStatus("bad_password");
				response.sendRedirect("login.jsp");
				dispose( con, stmt );
				return;
			}

			String query1 = "Select id, md5password, oldpassword, roleId, loginCount FROM users WHERE username = '" + f.getLoginName() + "'";
			ResultSet rs = stmt.executeQuery( query1 );

			int userId=0;
			String dbMd5Password=null, oldPassword=null, roleStr=null;
			int loginCount=0;
			int matchCount = 0;

			while (rs.next()) {
				++matchCount;
				userId        = rs.getInt(1);
				dbMd5Password = rs.getString(2);
				oldPassword   = rs.getString(3);
				roleStr       = rs.getString(4);
				loginCount    = rs.getInt(5);
			}
			rs.close();

			if ( matchCount > 0 ) {
				System.out.println( matchCount + " existing user(s) found with username " + f.getLoginName() );
			} else {
				f.setErrorMsg( "loginName","No user found with username " + f.getLoginName() );
				f.setLoginStatus("unknown_username");
				response.sendRedirect("login.jsp");
				dispose( con, stmt );
				return;
			}

			// logic for authentication
			// first check for new users (loginCount==0)
			//	 1)	cold (have username but haven't received initial password)
			//   2) initial password has been set but user mis-typed it
			//   3) correctly typed initial password and oldpassword set to provided password; have to enter a different one
			//   4) entered same password again
			//   5) entered a new private password, and bypass this stage because logincount set to 1
			// then check for users DBA has set to require changing password (md5password is null, oldpassword is not)
			//
			System.out.println("checking password");
			// check password; dbMd5Password is md5password from database
			if ( loginCount == 0 ) { // new user
				if ( dbMd5Password == null ) { // user is known but has not been given initial password
					f.setErrorMsg( "loginPassword", "Please request a username and initial password via the link below" ); // store password in database but force immediate re-entry
					f.setLoginStatus("first_login_no_password");
				} else if (!dbMd5Password.equals( userEnteredPasswordAfterMd5Conversion )) { // mis-typed CCRP-provided initial password
					if ( oldPassword == null ) { // did not make it through match of initially supplied password
						f.setErrorMsg( "loginPassword", "Please try entering provided password again" );
						f.setLoginStatus("first_login_mistyped");
					} else if ( oldPassword.equals( userEnteredPasswordAfterMd5Conversion ) ) {
						f.setErrorMsg( "loginPassword", "Please pick a different password from initially provided one" );
						f.setLoginStatus("changing_password_repeated_old");
					} else { // successfully provided different, private password
						f.setErrorMsg( "loginPassword", "Please re-enter new private password" );
						stmt.executeUpdate( "UPDATE users SET md5password='" + userEnteredPasswordAfterMd5Conversion + "', loginCount=1 WHERE username='" + f.getLoginName() + "'");
						f.setLoginStatus("changing_password");
					}
				} else { // entered a password that matches initial md5password in database; now force them to change it
					// oldpassword could be null or not null depending on number of mistries
					f.setErrorMsg( "loginPassword", "Please now choose a private password" ); // store password in database but force immediate re-entry
					stmt.executeUpdate( "UPDATE users SET oldpassword=md5password WHERE id=" + userId );
					f.setLoginStatus("first_login_changing_password");
				}
				response.sendRedirect("login.jsp");
				dispose( con, stmt );
				return;
			} else if ( dbMd5Password==null ) { // DBA has forced entry of a new password for user with a loginCount > 0
				if ( oldPassword != null && oldPassword.equals( userEnteredPasswordAfterMd5Conversion ) ) {
					f.setErrorMsg( "loginPassword", "Please pick a different password from your old one" );
					f.setLoginStatus("changing_password_repeated_old");
				} else {
					f.setErrorMsg( "loginPassword", "Please re-enter new password" );
					stmt.executeUpdate( "UPDATE users SET md5password='" + userEnteredPasswordAfterMd5Conversion + "' WHERE username='" + f.getLoginName() + "'");
					f.setLoginStatus("changing_password");
				}
				response.sendRedirect("login.jsp");
				dispose( con, stmt );
				return;
			} else if (!dbMd5Password.equals( userEnteredPasswordAfterMd5Conversion )) {
				f.setErrorMsg( "loginPassword", "Incorrect password: try again" );
				f.setLoginStatus("bad_password");
				f.setLoginPassword(""); // don't even reveal how many characters there were
				response.sendRedirect("login.jsp");
				dispose( con, stmt );
				return;
			}

			//set the login bean properties from the database

			System.out.println("authenticated; setting login status in loginformbean");


			f.setLoginStatus( "authenticated" );
			f.setSessionId( session.getId());
			session.setMaxInactiveInterval( 60000 ); // 1 minute
			f.setUserId(userId);
			f.setLoginRole(roleStr);
			f.setLoginPassword("");
			f.setErrorMsg("loginPassword",""); // remove any error messages
			f.setErrorMsg("loginName","");

			String updateQuery=null;
		/*	if (loginCount==1) { // first login
				updateQuery="UPDATE users SET loginCount=1,firstTime=modTime,lastTime=modTime WHERE id=" + userId;
			} else { */
				++loginCount;
				updateQuery="UPDATE users SET loginCount="+loginCount+",lastTime=now() WHERE id=" + userId;
		/*	} */
			try {
				stmt.executeUpdate(updateQuery);
			} catch (SQLException ex) {
				f.setErrorMsg("loginName","Error: could not update login count or time via query " + updateQuery);
				dispose( con, stmt );
				response.sendRedirect("register.jsp");
				return;
			}

			response.sendRedirect("index.jsp");
			dispose( con, stmt );
		} catch (Exception ex) {
			System.out.println( ex.getMessage() );
			ex.printStackTrace();
		}
	}

	private static Connection getConnection()
		throws SQLException, IOException
	{
		Properties dbProps = new Properties();
		String fileName = tomcatHome + "/webapps/" + contextName + "/WEB-INF/classes/properties/" + databaseName + "_jdbc.properties";
		FileInputStream in = new FileInputStream(fileName);
		dbProps.load(in);

		String drivers = dbProps.getProperty("jdbc.drivers");
		if (drivers != null)
			System.setProperty("jdbc.drivers", drivers);
		String url = dbProps.getProperty("jdbc.url");
		String username = dbProps.getProperty("jdbc.username");
		String password = dbProps.getProperty("jdbc.password");

		return
			DriverManager.getConnection( url, username, password );
	}

	private void dispose( Connection myCon, Statement myStmt )
	{  try
		{
			myStmt.close();
			myCon.close();
		}
		catch(SQLException e) {}
	}

private static String contextName;
private static String databaseName;
private static String tomcatHome;

}

