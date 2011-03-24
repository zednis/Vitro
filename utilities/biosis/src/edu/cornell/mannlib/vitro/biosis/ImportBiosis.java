package edu.cornell.mannlib.vitro.biosis;
/**
 * @version .1 2004-12-22
 * @author Brian Caruso 
 */

//import java.net.*;
//import java.sql.*;
//import java.io.*;
import java.io.File;
import java.sql.SQLException;
import java.util.*;


/** 
 *This will execute BiosisLoad, UpdateAuthorTokensWithMatchingEntityId, TransferPubsToEntities, and UpdatePubsWithAuthors.
 * 
 *
 */

class ImportBiosis {
	public static void main (String args[]) {
        if( args == null || args.length != 3 ){
            showHelp();                
            return;
        }

        String errmsg = goodFile( args[0] );  //check the config file
        if( errmsg != null ){
            showHelp();
            System.out.println( "file " + args[0] + " " +errmsg );
            return;
        }
        
        errmsg = goodFile( args[1] ); //check the datafile
        if( errmsg != null ){
            showHelp();
            System.out.println( "file " + args[1] + " " +errmsg );
            return;
        }
        
        String propertiesFile = args[0];
        String dataFile = args[1];
        
		//nothing exciting here
		System.out.println("---Running BiosisLoad---");		
		BiosisLoad.main(args);
		System.out.println("---Running UpdateAuthorTokensWithMatchingEntityId---");		
		//		UpdateAuthorTokensWithMatchingEntityId.main(args); //this is now part of NameMatcherPanel
		System.out.println("---Presenting Mismatches---");
		NameMatcherPanel.doNameMatchBlocking(args);

		System.out.println("---Running TransferPubsToEntities---");
        TransferPubsToEntities tpte = new TransferPubsToEntities( propertiesFile );
		try {
            tpte.doTransfer();
        } catch (SQLException e) {
            System.out.println("Error in TransferPubsToEntities" + e);
            e.printStackTrace();
        }
        
		//mismatchesPrint(UpdateAuthorTokensWithMatchingEntityId.getMismatches());		

		//System.out.println("---Running UpdatePubsWithAuthors---");		
		//UpdatePubsWithAuthors.main(args); //is this done in TransferPubsToEntities?
		//System.exit(0);
	}
	public static void mismatchesPrint(Collection list){
		Iterator mis = list.iterator();
		while(mis.hasNext()){
			Iterator it = ((Collection) mis.next()).iterator();
			while (it.hasNext()){
				Object item = it.next();
				if(item != null)
					System.out.println(item.toString());
				else
					System.out.println("null");
			}
			
		}
	}
    
      private static final void showHelp(){
            System.out.println("ImportBiosis will run the whole biosis import process");
            System.out.println("java ImportBiosis <ConfigFile> <datafile.dat> true|false");
            System.out.println("The ConfigFile should have db and other configuration info.");
            System.out.println("The datafile.dat should be the data downloaded from biosis.");
            System.out.println("the last argument indicates if the db inserts should be done or not.");
        }
      public static final String goodFile(String filename){
          File file = new File(filename);
         if( !file.exists() ) return "File " + filename + " does not exist. " ;
         if( file.isDirectory() ) return  filename + " is not a property file, it is a Directory. " ;
         if( !file.isFile() ) return filename + " is not a normal file. " ;
         if( !file.canRead() ) return "This process cannot read the file " + filename;
         return null;
      }
}
