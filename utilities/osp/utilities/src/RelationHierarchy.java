import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection; 
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.net.URL;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;

//import edu.cornell.mannlib.ChangeQueue.*;

/**
 Tool to look at tree strut of an ents relation
 @author Brian Caruso bdc34.cornell.edu
 @verison 0.1

 **** Change Log ****
2005-31-01 created.
*/

public class RelationHierarchy extends JPanel
	implements TreeSelectionListener {

    private JEditorPane htmlPane;
    private JTree tree;
    private URL helpURL;
    private static boolean DEBUG = false;

    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";
    
    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;

    public RelationHierarchy() {
        super(new GridLayout(1,0));

        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode(" entites");
        createNodes(top);

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); //XXX: ignored in some releases
                                           //of Swing. bug 4101306
        //workaround for bug 4101306:
        //treeView.setPreferredSize(new Dimension(100, 100)); 

        splitPane.setPreferredSize(new Dimension(500, 300));

        //Add the split pane to this panel.
        add(splitPane);
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();

        if (node == null) return;

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            BookInfo book = (BookInfo)nodeInfo;
            displayURL(book.bookURL);
            if (DEBUG) {
                System.out.print(book.bookURL + ":  \n    ");
            }
        } else {
            displayURL(helpURL); 
        }
        if (DEBUG) {
            System.out.println(nodeInfo.toString());
        }
    }

    private class BookInfo {
        public String bookName;
        public URL bookURL;

        public BookInfo(String book, String filename) {
            bookName = book;
            bookURL = RelationHierarchy.class.getResource(filename);
            if (bookURL == null) {
                System.err.println("Couldn't find file: "
                                   + filename);
            }
        }

        public String toString() {
            return bookName;
        }
    }

    private void initHelp() {
        String s = "RelationHierarchyHelp.html";
        helpURL = RelationHierarchy.class.getResource(s);
        if (helpURL == null) {
            System.err.println("Couldn't open help file: " + s);
        } else if (DEBUG) {
            System.out.println("Help URL is " + helpURL);
        }

        displayURL(helpURL);
    }

    private void displayURL(URL url) {
        try {
            if (url != null) {
                htmlPane.setPage(url);
            } else { //null url
		htmlPane.setText("File Not Found");
                if (DEBUG) {
                    System.out.println("Attempted to display a null URL.");
                }
            }
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }

	private class Relation{
		public String name;
		public int id;
		public Relation(String name_in, int id_in){
			this.name = name_in;
			this.id = id_in;
		}
		public String toString(){
			return name + " " + id;
		}
	}

    private class Entity {
        public String eName;
        public int eId;
        public Entity(String name, int id) {
            eName = name;
			eId = id;
        }
        public String toString() {
            return eName +" " + eId;
        }
    }


	private void createEntityNode(int entId, DefaultMutableTreeNode top,Connection con, int level)throws SQLException{
		DefaultMutableTreeNode relation = null;
        DefaultMutableTreeNode entity = null;
		String entQuery="SELECT entities.name FROM ENTITIES WHERE ID="+entId + " order by name";		
		
		Statement stmt=con.createStatement();
		ResultSet rs = stmt.executeQuery(entQuery);
		if(rs.next()){
			String eName = rs.getString("NAME");
			entity=new DefaultMutableTreeNode(new Entity(eName,entId));
			top.add(entity);

			String relationQuery = "SELECT PROPERTIES.DOMAINPUBLIC, ETYPES2RELATIONS.ID AS 'ID' FROM "+
				"PROPERTIES,ETYPES2RELATIONS,ENTITIES WHERE ETYPES2RELATIONS.PROPERTYID=PROPERTIES.ID AND " +
				"ETYPES2RELATIONS.DOMAINTYPEID=ENTITIES.TYPEID AND ENTITIES.ID="+entId;
			ResultSet relRs = con.createStatement().executeQuery(relationQuery);
			while(relRs.next()){
				relation=new DefaultMutableTreeNode(new Relation(relRs.getString("DOMAINPUBLIC"),
																 relRs.getInt("ID")));
				entity.add(relation);		   				
				
				if(level > 0){
					String subQ="SELECT NAME, ENTITIES.ID FROM ENTITIES, ENTS2ENTS "+
						"WHERE entities.id=ents2ents.rangeid and DOMAINID="+entId+" AND ETYPES2RELATIONSID="
						+relRs.getString("ID");
					System.out.println(subQ);
					ResultSet subEnts=stmt.executeQuery(subQ);
					while(subEnts.next()){
						createEntityNode(subEnts.getInt("ID"), relation, con, level-1);				
					}
					subEnts.close();
				}
			}
			String rangeQuery="SELECT PROPERTIES.RANGEPUBLIC, ETYPES2RELATIONS.ID AS 'ID' FROM "+
				"PROPERTIES,ETYPES2RELATIONS,ENTITIES WHERE ETYPES2RELATIONS.PROPERTYID=PROPERTIES.ID AND " +
				"ETYPES2RELATIONS.RANGETYPEID=ENTITIES.TYPEID AND ENTITIES.ID="+entId;
			System.out.println(rangeQuery);
			
			ResultSet rangeRs = null;
			rangeRs = con.createStatement().executeQuery(rangeQuery);
			while(rangeRs.next()){
				relation=new DefaultMutableTreeNode(rangeRs.getString("RANGEPUBLIC")+" "+rangeRs.getString("ID"));
				entity.add(relation);
			
				if(level > 0){
					String subQ="SELECT NAME, ENTITIES.ID FROM ENTITIES, ENTS2ENTS "+
						"WHERE entities.id=ents2ents.domainid and Rangeid="+entId+
						" AND ETYPES2RELATIONSID="+rangeRs.getString("ID");
					System.out.println(subQ);					
					ResultSet subEnts=stmt.executeQuery(subQ);
					while(subEnts.next()){
						createEntityNode(subEnts.getInt("ID"), relation, con, level-1);				
					}
					subEnts.close();
				}
			}
			relRs.close();
		}
		rs.close(); 
		rs.close();
		stmt.close();
	}


	private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;
// 		try{
// 			String query="SELECT ID FROM ENTITIES WHERE TYPEID=94 order by name";
// 			DefaultMutableTreeNode node=new DefaultMutableTreeNode("etype 94");
// 			top.add(node);
// 			ResultSet rs=global_vivoCon.createStatement().executeQuery(query);
// 			while(rs.next()){
// 				createEntityNode(rs.getInt("ID"), node, global_vivoCon, 0);
// 			}
// 		}catch (SQLException ex){ 
// 			System.out.println("Could not create entity nodes: " + ex);
// 			ex.printStackTrace();
// 		}       
// 		try{
// 			String query="SELECT ID FROM ENTITIES WHERE TYPEID=90 order by name";
// 			DefaultMutableTreeNode node=new DefaultMutableTreeNode("etype 90");
// 			top.add(node);
// 			ResultSet rs=global_vivoCon.createStatement().executeQuery(query);
// 			while(rs.next()){
// 				createEntityNode(rs.getInt("ID"), node, global_vivoCon, 0);
// 			}
// 		}catch (SQLException ex){ 
// 			System.out.println("Could not create entity nodes: " + ex);
// 			ex.printStackTrace();
// 		}       
// 		try{
// 			String query="SELECT ID FROM ENTITIES WHERE TYPEID=31 order by name";
// 			DefaultMutableTreeNode node=new DefaultMutableTreeNode("etype 31");
// 			top.add(node);
// 			ResultSet rs=global_vivoCon.createStatement().executeQuery(query);
// 			while(rs.next()){
// 				createEntityNode(rs.getInt("ID"), node, global_vivoCon, 0);
// 			}
// 		}catch (SQLException ex){ 
// 			System.out.println("Could not create entity nodes: " + ex);
// 			ex.printStackTrace();
// 		}       
		try{
			String query="SELECT ID FROM ENTITIES WHERE TYPEID=32 order by name";
			DefaultMutableTreeNode node=new DefaultMutableTreeNode("etype 32");
			top.add(node);
			ResultSet rs=global_vivoCon.createStatement().executeQuery(query);
			while(rs.next()){
				createEntityNode(rs.getInt("ID"), node, global_vivoCon, 2);
			}
		}catch (SQLException ex){ 
			System.out.println("Could not create entity nodes: " + ex);
			ex.printStackTrace();
		}       

    }

	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("RelationHierarchy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        RelationHierarchy newContentPane = new RelationHierarchy();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
		try{ 
			makeConnections();
		}catch (Exception e){
			System.out.println("Could not make connections " + e );
			e.printStackTrace();
			return;
		}
		
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	

	/** 
	 * Create the connections to the db's using the values from the properties file.
	 * The connections that get set are global_vivoCon and global_localCon.
	 */
	public static void makeConnections()
		throws SQLException, IOException {
		Properties props = new Properties();
		String fileName = "vivo2_jdbc.properties";
		FileInputStream in = new FileInputStream(fileName);
		props.load(in);
		String drivers = props.getProperty("jdbc.drivers");
		if (drivers != null) {
			System.setProperty("jdbc.drivers", drivers);

			String url = props.getProperty("localosp.url");
			String username = props.getProperty("localosp.username");
			String password = props.getProperty("localosp.password");
			System.out.println("local database: " + url);			
			global_localCon = DriverManager.getConnection( url, username, password );
			
			url = props.getProperty("vivo.url");
			username = props.getProperty("vivo.username");
			password = props.getProperty("vivo.password");
			System.out.println("vivo database: " + url);			
			global_vivoCon = DriverManager.getConnection( url, username, password );

			url = props.getProperty("changegroup.url");
			username = props.getProperty("changegroup.username");
			password = props.getProperty("changegroup.password");
			System.out.println("ChangeGroup database: " + url);			
			global_changeGroup = DriverManager.getConnection( url, username, password );

		}
	}	

	/**
	 *   escape the nasty chars
	 */
	private static String esc(String in){
		return escapeQuotes(escapeQuotes(in,34),39);
	}

	/** escape and quote this string */
	private static String quote(String in){
		return "'" + esc(in) + "'";
	}

	private static String escapeQuotes( String termStr, int whichChar ) {
		if (termStr==null || termStr.equals("")) 
			return termStr;
		int characterPosition= -1;
		// strip leading spaces
		while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
			termStr = termStr.substring(characterPosition+1);
		}
		characterPosition=-1;
		while ( ( characterPosition = termStr.indexOf( whichChar, characterPosition+1 ) ) >= 0 ) {
			if ( characterPosition == 0 ) // just drop it
				termStr = termStr.substring( characterPosition+1 );
			else if ( termStr.charAt(characterPosition-1) != 92 ) // already escaped
				termStr = termStr.substring(0,characterPosition) + "\\" + termStr.substring(characterPosition);
			++characterPosition;
		}
		return termStr;
	}


	/**
	 *  Use this to store current sql query.  On an errror this will be displayed.
	 */ 
	private static String setLast(String sql){ return lastSql=sql; }
	private static String lastSql = null;
	

	/* globals */
	static Connection global_vivoCon=null, global_localCon=null,global_changeGroup=null;	
	
	/**
	 *  Connect to db's and attempt to import into VIVO all awards, dept associations, 
	 *  sponsor associations and PrimaryInvistigator associations.
	 */
// 	public static void main(String[] args){ 
// 		try{ makeConnections(); } 
// 		catch (SQLException ex ){
// 			System.out.println("SQLException: " + ex);
// 		}catch (IOException ex){
// 			System.out.println("IOException: " + ex);
// 		}		

// // 		try{
// // 			//populateLinkTableWithOSPDeptIds(global_vivoCon);
// // 			//populateWithOSPDeptIds(global_vivoCon);
// // 		} catch(SQLException ex){
// // 			System.out.println("Error trying to popluate link table with DeptIds from OSP:\n"+ex+ "\n" + lastSql);
// // 			return;
// // 		}

// // 		try{
// // 			//populateLinkTableWithOspSponsorIds(global_vivoCon);
// // 		}catch(SQLException ex){
// // 			System.out.println("error trying to populate link table with Sponsor ids from osp:\n"+ex+"\n"+lastSql);
// // 			return;
// // 		}

// //  		ImportLocalOsp2Vivo(global_vivoCon, global_localCon);		
// 	}
}
