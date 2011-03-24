import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

public class TestMySQLDriver
{
    public static void main (String args[]) {
	try {
	    boolean readProps=false;
	    // 			if (args.length == 1) {
	    // 				readProps = args[0].equalsIgnoreCase("true") ? true : false;
	    // 			}

	    //Class.forName("com.mysql.jdbc.Driver").newInstance();
	    Connection con = getConnection( readProps ); // true for from properties file; false for hardwired parameters

	    Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("select count(*) from test");
	    while ( rs.next() ) {
		System.out.println("the entities table has " + rs.getInt(1) + " records.");
	    }
	    rs.close();
	    stmt.close();
	    con.close();
	} catch (SQLException ex) {
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	} catch (IOException ex) {
	    System.out.println("IOException: " + ex.getMessage());
	    /*
	      } catch (ClassNotFoundException ex) {
	      System.out.println("ClassNotFoundException: " + ex.getMessage());
	      } catch (InstantiationException ex) {
	      System.out.println("InstantiationException: " + ex.getMessage());
	      } catch (IllegalAccessException ex) {
	      System.out.println("IllegalAccessException: " + ex.getMessage());
	    */
	}
    }


    public static Connection getConnection( boolean byProps )
	throws SQLException, IOException
    {
	try {
	    //Class.forName("com.mysql.jdbc.Driver").newInstance();
	    String url=null;
	    String username=null;
	    String password=null;

	    if ( byProps ) {
		Properties props = new Properties();
		String fileName = "vivo2_jdbc.properties";
		FileInputStream in = new FileInputStream(fileName);
		props.load(in);

		//String drivers = props.getProperty("jdbc.drivers");
		String drivers=(drivers=props.getProperty("jdbc.drivers"))==null || drivers.equals("") ? "org.gjt.mm.mysql.Driver" : drivers;
		System.setProperty("jdbc.drivers", drivers);
		//url=(url=props.getProperty("jdbc.url"))==null || url.equals("") ? "jdbc:mysql://new-elfrieda.mannlib.cornell.edu/vivo2" : url;
		//url=(url=props.getProperty("jdbc.url"))==null || url.equals("") ? "jdbc:mysql://localhost/test" : url;
		username=(username=props.getProperty("jdbc.username"))==null || username.equals("") ? "root" : username;
		password=(password=props.getProperty("jdbc.password"))==null || password.equals("") ? "R" : password;
		/*
		  if (drivers != null) {
		  System.setProperty("jdbc.drivers", drivers);
		  url = props.getProperty("jdbc.url");
		  username = props.getProperty("jdbc.username");
		  password = props.getProperty("jdbc.password");
		  }
		*/
	    } else { // hardwired connection
		System.setProperty("jdbc.drivers", "org.gjt.mm.mysql.Driver");
		//url = "jdbc:mysql://localhost/lcgsa";
		//username = "lcgsaweb";
		//password = "hfas6503";
		url= "jdbc:mysql://localhost/test" ;
		username = "root";
		password = "RedRed";
		//url = "jdbc:mysql://new-elfrieda.mannlib.cornell.edu/vivo2";
		//username = "vivoweb";
		//password = "ksc3jc55";
	    }

	    return DriverManager.getConnection(url, username, password);
	    /*
	      } catch (ClassNotFoundException ex) {
	      System.out.println("ClassNotFoundException: " + ex.getMessage());
	      } catch (InstantiationException ex) {
	      System.out.println("InstantiationException: " + ex.getMessage());
	      } catch (IllegalAccessException ex) {
	      System.out.println("IllegalAccessException: " + ex.getMessage());
	      }
	    */
	} catch (SQLException ex) {
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	}
	return null;
    }
}



