import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Driver;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

/*  JdbcToy
    This is a utility that allows you to test to see if you can get a connection using a JDBC connect string.
    One quirk is the Syste.setProperty("jdbc.drivers","bla.bla.Driver");  I'm not sure what that is about.

    Usage:
    JdbcToy  table  jdbcconnectstring [username password]

    Examples:
    JdbcToy  test.test jdbc:oracle:thin:@test.dataserver.somewhere.edu:1521:testdb dbuser apasswd
    JdbcToy test.test jdbc:mysql://localhost/test?user=dbuser&password=apasswd

    Brian Caruso 2004-12-06
*/

public class JdbcToy {
    public static void main (String args[]) {
	try {		    
	    Connection conn = null;
	    //System.setProperty("jdbc.drivers", "org.gjt.mm.mysql.Driver"); //what is this about?
	    //System.setProperty("jdbc.drivers", "oracle.jdbc.OracleDriver");
	    DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
	    DriverManager.registerDriver(new com.mysql.jdbc.Driver());
	    
	    Enumeration drivers = DriverManager.getDrivers();
	    System.out.println("list of jdbc drivers:");
	    while ( drivers.hasMoreElements() ) {
		System.out.println("driver " + ((Driver)drivers.nextElement()));
	    }
	    System.out.println("End of driver list.");

	    for (int i = 0; i < args.length ; i++) System.out.println("args[" + i + "]:" + args[i] );

	    if (!(args.length == 4 || args.length == 2 )) {
		System.out.println("Error: connect string required.");
		System.out.println("JdbcToy  table  jdbcconnectstring [username password]");
		System.out.println("example: JdbcToy  test.test \"jdbc:oracle:thin:@test.dataserver.somewhere.edu:1521:testdb\" dbuser apasswd");
		System.out.println("example: JdbcToy test.test \"jdbc:mysql://localhost/test?user=dbuser&password=apasswd\"");
		System.exit(1);
	    }

	    if (args.length==2)  conn = DriverManager.getConnection(args[1]);
	    if (args.length ==4) conn = DriverManager.getConnection(args[1],args[2],args[3]);
	    		      
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("select count(*) from " + args[0] );
	    while ( rs.next() ) {
		System.out.println("the " + args[0] + "  table has " + rs.getInt(1) + " records.");
	    }
	    rs.close();
	    stmt.close();
	    conn.close();
	} catch (SQLException ex) {
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	}
    }

    // Utility function to read a line from standard input
    static String readEntry(String prompt)   {
	try      {
	    StringBuffer buffer = new StringBuffer();
	    System.out.print(prompt);
	    System.out.flush();
	    int c = System.in.read();
	    while (c != '\n' && c != -1)         {
		buffer.append((char)c);
		c = System.in.read();
	    }
	    return buffer.toString().trim();
	}
	catch(IOException e)	  {
	    return "";
	}
    }
}




