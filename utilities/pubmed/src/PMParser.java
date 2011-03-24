import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.cornell.mannlib.vitro.dao.db.VitroConnection;

public class PMParser {

    private boolean test = false;
    private Connection con = null;
    private String props;
       
    
    public PMParser(Connection connection, String props, boolean test ) {
        this.con = connection;
        this.props = props;        
        this.test = test;
    }

    public static void main(String[] args) {
        String conprops = "";
        boolean test = false;
        
        if (args.length < 1) {
            printMessage();            
            return;
        }
        String props = null;
        String uri = null;
        if(args.length > 3){
            if("--test".equals(args[0])){
                test = true;
            }else{
                printMessage();
                return;
            }
            props = args[1];
            conprops = args[2];
            uri = args[3];
        } else {
            props = args[0];
            conprops = args[1];
            uri = args[2];
        }
        System.out.println("pubmed parse properties file: " + props);
        System.out.println("vitro db connection properties file: " + conprops);
        System.out.println("URI: " + uri);
        
        PMParser parser = null;
        try {
            parser = new PMParser(setupConnection(conprops), props, test);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        parser.parseXML( uri );
    }

    public void parseXML( String uri ){
        System.out.println("Parsing XML File: " + uri + "\n\n");
        PMContentHandler contentHandler = null;
        try {
            contentHandler = new PMContentHandler(con,props,test);
        } catch (IOException e1) {
            System.out.println("Error when settingup PMContestHandler()");
            e1.printStackTrace();
            return;
        }
        
        try {
            XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            parser.setContentHandler( contentHandler );
            parser.setErrorHandler(new SAXErrorHandler());
            parser.parse(uri);
        } catch (IOException e) {
            System.out.println("Error reading URI: " + e.getMessage());
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("Error while attempting to parse file:\n" +
                               e.getMessage());
        }
    }

    private static Connection setupConnection(String propFileName) throws SQLException {
         VitroConnection.establishDataSourceFromProperties( propFileName );
         return VitroConnection.getConnection();
    }
    
    private static void printMessage(){
        System.out.println("Usage: java PMParser [--test] propertiesFileName connectionPropFile XMLURI ");
        System.out.println("--test test run only, no writes to the database.");
        System.out.println("the properties file indicates how the xml should be parsed (required)");
        System.out.println("XMLURI is the uri of the xml file to parse (required)");
    }
}
