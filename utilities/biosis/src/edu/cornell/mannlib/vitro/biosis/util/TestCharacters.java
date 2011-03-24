package edu.cornell.mannlib.vitro.biosis.util;
/**
 * @version 1.20 2004-08-23
 * @author Jon Corson-Rikert
 */

import java.net.*;
import java.sql.*;
import java.io.*;
import java.util.*;

/*** JCR
 *** Run this after importing the pubs from a Biosis download
 *** to create new authors and then match them against
 *** existing publications but before loading publications as Vivo entities (TransferPubsToEntities) */

class TestCharacters {
	private static final boolean   DO_UPDATES=false;
	private static final boolean   NOISY=true;

	public static void main (String args[]) {
		try	{
			Connection con = getConnection();

			PrintWriter out= new PrintWriter( new FileWriter("TestCharacters.output"));
			Statement stmt=con.createStatement();
			String selectQuery="SELECT name FROM entities WHERE name like '%Barry%'";
			try {
				ResultSet rs=stmt.executeQuery(selectQuery);
				while (rs.next()) {
					String nameStr=rs.getString(1);
					out.println(nameStr);
					System.out.println(nameStr);
					for (int i=0; i<nameStr.length(); i++) {
						char theChar=nameStr.charAt(i);
						Character theCharacter=new Character(theChar);
						out.println(Character.getNumericValue(theChar) + " type: " + Character.getType(theChar) + " hashcode: " + theCharacter.hashCode());
						System.out.println(Character.getNumericValue(theChar) + " type: " + Character.getType(theChar) + " hashcode: " + theCharacter.hashCode());
						if (Character.isWhitespace(theChar)) {
							out.println("we have a white space with value " + Character.digit(theChar,0) + " that is is unicode?: " + Character.isDefined(theChar));
							System.out.println("we have a white space with value " + Character.digit(theChar,0) + " that is is unicode?: " + Character.isDefined(theChar));
							if (Character.isSpaceChar(theChar)) {
								out.println("This is also a unicode space character");
								System.out.println("This is also a unicode space character");
							}
						}
					}
					out.println("\n");
					System.out.println("\n");
				}
				rs.close();
				stmt.close();
				out.flush();
				out.close();
			} catch (SQLException ex) {
				out.println("Error: SQLException with message: " + ex.getMessage());
			}
		} catch (SQLException ex) {
			System.out.println ("SQLException:");
         	while (ex != null) {
				System.out.println ("SQLState: " + ex.getSQLState());
            	System.out.println ("Message:  " + ex.getMessage());
            	System.out.println ("Vendor:   " + ex.getErrorCode());
            	ex = ex.getNextException();
            	System.out.println ("");
          	}
      	} catch (IOException ex) {
			System.out.println("Exception: " + ex);
         	ex.printStackTrace ();
      	}
	}

	public static Connection getConnection()
      	throws SQLException, IOException
    {
		Properties props = new Properties();
		String fileName = "vivo2_jdbc.properties";
		FileInputStream in = new FileInputStream(fileName);
		props.load(in);

		String drivers = props.getProperty("jdbc.drivers");
		if (drivers != null)
			System.setProperty("jdbc.drivers", drivers);
			String url = props.getProperty("jdbc.url");
			String username = props.getProperty("jdbc.username");
			String password = props.getProperty("jdbc.password");

			return DriverManager.getConnection( url, username, password );
	}
}



