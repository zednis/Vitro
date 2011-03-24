package edu.cornell.mannlib.vitro.biosis.util;
/**
 * @version 1.20 1999-08-16
 * @author Cay Horstmann
 */

import java.net.*;
import java.sql.*;
import java.io.*;
import java.util.*;

class BitwiseTests {

	public static void main (String args[]) {
		if (args.length > 0) {
			try {
				int n = Integer.parseInt(args[0]);
				System.out.println("n + (1<<1) = " + String.valueOf(n & (1<<1)));
				System.out.println("n + (1<<2) = " + String.valueOf(n & (1<<2)));
				System.out.println("n + (1<<3) = " + String.valueOf(n & (1<<3)));
				System.out.println("n + (1<<4) = " + String.valueOf(n & (1<<4)));
				System.out.println("n + (1<<5) = " + String.valueOf(n & (1<<5)));
				System.out.println("n + (1<<6) = " + String.valueOf(n & (1<<6)));
				System.out.println("n + (1<<7) = " + String.valueOf(n & (1<<7)));
				System.out.println("n + (1<<8) = " + String.valueOf(n & (1<<8)));
			} catch (Exception ex) {
				System.out.println("Could not decode your argument " + args[0] + " as an integer value for the tests");
				System.exit(0);
			}
		} else {
			System.out.println("Please specify an integer value for the tests");
			System.exit(0);
		}
	}
}
