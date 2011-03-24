import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

public class TestOracleDriver
{
    public static void main (String args[]) {
        try {
            String readProps = null;
            if (args.length == 1)
                readProps = args[0];

            try {
                Class.forName("oracle.jdbc.OracleDriver").newInstance();
            } catch (ClassNotFoundException ex) {
                System.out.println("could not find class: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("could not instantiate class: " + ex.getMessage());
            }
            Connection con = getConnection( readProps ); // true for from properties file; false for hardwired parameters

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select count(*) from OSPWH.AWARD_PROPOSAL");
            while ( rs.next() ) {
                System.out.println("the OSPWH.AWARD_PROPOSAL table has " + rs.getInt(1) + " records.");
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


    public static Connection getConnection( String propsFile )
            throws SQLException, IOException
    {
        boolean byProps = propsFile != null;

        try {

            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url=null;
            String username=null;
            String password=null;

            if ( byProps ) {
                Properties props = new Properties();
                String fileName = propsFile;
                FileInputStream in = new FileInputStream(fileName);
                props.load(in);

                //String drivers = props.getProperty("jdbc.drivers");

                String drivers=
                    (drivers=props.getProperty("OSPDWDownload.ospwh.jdbc.drivers")) == null || drivers.equals("")  ?
                      "oracle.jdbc.OracleDriver" : drivers;

                System.setProperty("jdbc.drivers", drivers);
                url=(url=props.getProperty("OSPDWDownload.ospwh.url"))==null || url.equals("") ? "jdbc:oracle:thin:@cudmtest.dataserver.cornell.edu:1521:cudmtest" : url;
                username=(username=props.getProperty("OSPDWDownload.ospwh.username"))==null || username.equals("") ? "vivo" : username;
                password=(password=props.getProperty("OSPDWDownload.ospwh.password"))==null || password.equals("") ? "s2s1m2st" : password;
                /*
                if (drivers != null) {
                    System.setProperty("jdbc.drivers", drivers);
                    url = props.getProperty("jdbc.url");
                    username = props.getProperty("jdbc.username");
                    password = props.getProperty("jdbc.password");
                }
                */
            } else { // hardwired connection
                System.setProperty("jdbc.drivers", "oracle.jdbc.OracleDriver");
                url = "jdbc:oracle:thin:@cudmtest.dataserver.cornell.edu:1521:cudmtest";
                username = "vivo";
                password = "s2s1m2st";
                System.out.println("using hardwired connection." );
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



