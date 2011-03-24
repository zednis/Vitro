import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;

public class Logout extends HttpServlet /*implements SingleThreadModel*/ {

	public void doPost( HttpServletRequest request, HttpServletResponse response ) {
		try {
			HttpSession session = request.getSession();
			LoginFormBean f = (LoginFormBean) session.getAttribute( "loginHandler" );

			//don't need to touch the users database for now

			//reset the login bean properties

			f.setLoginStatus( "logged out" );
			f.setLoginRole( "1" );
			// f.setLoginName( "" ); leave so users can see who they last logged in as
			f.setLoginPassword( "" );
			f.setErrorMsg( "loginPassword", "" ); // remove any error messages
			f.setErrorMsg( "loginUsername", "" ); // remove any error messages
			f.setEmailAddress( "reset" );
			f.setSessionId( "" );

			response.sendRedirect("index.jsp");
			/*
			String otherServlet = "/index.jsp";
			RequestDispatcher rd = getServletContext().getRequestDispatcher(otherServlet);
			rd.forward( request, response);
			*/
		} catch (Exception ex) {
			System.out.println( ex.getMessage() );
			ex.printStackTrace();
		}
	}
}

