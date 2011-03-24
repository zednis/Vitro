package edu.cornell.mannlib.vitro.biosis;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import edu.cornell.mannlib.vitro.biosis.util.BrowserControl;
import edu.cornell.mannlib.vitro.dao.db.VitroConnection;

/**
 *  
    This will match authors with vivo entities and present you with a 
    window of article authors from biosis citations that were not matched.
    If you double click on the name of an author in the left window it will bring
    up a list of guesses.  The guesses are names in vivo that might match.
    The guesses are very loose.  If you double click on a guess it will
    bring up a browser with that person in vivo so you can edit the name
    to add an initial or such.  
*/
public class NameMatcherPanel extends JFrame implements ActionListener{
    private Connection con;
    
    /* bdc34: updated to work with new db schema 2006-08-30
       Opening the article or person in a browser is disabled since the
       new Vivo doesn't work with the urls that we were using.
     */
    public NameMatcherPanel(){ super("Name Matcher");   }

    public NameMatcherPanel(Connection con ) { 
        this();
        this.con = con;
    }

    public void actionPerformed(ActionEvent event) {
        if("Edit".equals(event.getActionCommand())){
            String name = (String)matchNameList.getSelectedValue();
            openEditorInBrowser(name);
        }
        if("Search".equals(event.getActionCommand())){
            try{
                String name = (String)orgNameList.getSelectedValue();
                guessAndSetList(name);
                forceButton.setEnabled(false);
            }catch(IOException ex){System.out.println("io exception: " + ex);
            }catch(SQLException ex){System.out.println("sql exception: " + ex);}
        }
        if("reprocess".equals(event.getActionCommand())){
            try{
                doAuthorEntityMatch();
            }catch(IOException ex) {
                System.out.println("error accessing file: " + ex);
            }catch(SQLException ex){
                System.out.println("Error in reprocess, database exception: " + ex);
            }
            forceButton.setEnabled(false);
            matchNameList.setModel(new DefaultListModel());
        }
        if("force".equals(event.getActionCommand())){
            String tName = (String)orgNameList.getSelectedValue();
            String eName = (String)matchNameList.getSelectedValue();
            try{
                UpdateAuthorTokensWithMatchingEntityId.forceAssociation(tName, eName,con);
            }catch(IOException ex) {
                System.out.println("error accessing properties file: " + ex);
            }catch(SQLException ex){
                System.out.println("Error processing force, database exception: " + ex);
            }
        }
        if("article".equals(event.getActionCommand())){
            String name = (String)orgNameList.getSelectedValue();
            openArticleInBrowser(name);
        }
    }

    private void doAuthorEntityMatch() throws IOException, SQLException{
        UpdateAuthorTokensWithMatchingEntityId.doAuthorTokenUpdate(con);
        Object[] values = UpdateAuthorTokensWithMatchingEntityId.getMismatchArray();
        System.out.println("length of mismatches:" + values.length);
        orgNameList.setListData(values);
    }

    void openArticleInBrowser(String name){
        String url = null;
        String ids = "()";
        if(name != null){
            try{
                Properties props = new Properties();
                String fileName = "vivo2_jdbc.properties";
                FileInputStream in = new FileInputStream(fileName);
                props.load(in);
                url = props.getProperty("vivo.baseurl");
                ids = UpdateAuthorTokensWithMatchingEntityId.getPubIdsForAuthor(name,con);
            }catch (SQLException ex){
                System.out.println("exception while trying to find articles: " + ex);
            }catch (IOException ex){
                System.out.println("exception while trying to load properties file: " + ex);
            }
            //http://vivo.cornell.edu/
            //fetch?home=1&queryspec=private_pub&postGenLimit=-1&header=null&linkwhere=pubs.id%3D%272789%27
            BrowserControl.displayURL(url + "fetch?home=1&queryspec=public_pubs&linkwhere=pubs.id in " + ids);
        }
    }

    void openEditorInBrowser(String name){
        String url = null;
        if(name != null){
            try{
                Properties props = new Properties();
                String fileName = "vivo2_jdbc.properties";
                FileInputStream in = new FileInputStream(fileName);
                props.load(in);
                url = props.getProperty("vivo.baseurl");
            }catch (IOException ex){
                System.out.println("exception while trying to load properties file: " + ex);
            }
            //BrowserControl.displayURL(url+ "&queryspec=public_entityv&header=none&linkwhere=entities.id=5821"
            BrowserControl.displayURL(url+ "fetch?queryspec=public_entityv&header=none&linkwhere=entities.name='" + name.replaceAll("\\s","%20")+"'");
        }
    }

    private void guessAndSetList(String nameFromArticle)throws SQLException, IOException{
        if(nameFromArticle !=null){
            Object [] values = UpdateAuthorTokensWithMatchingEntityId.searchForMatches(nameFromArticle, con );
            DefaultListModel dfl = new DefaultListModel();
            for(int i =0; i< values.length; i++){
                dfl.addElement(values[i]);
            }
            matchNameList.setModel(dfl);
        }
    }

    private JList orgNameList = null, matchNameList = null;

    private void addList() throws IOException, SQLException{
        orgNameList = new JList() {
            //Subclass JList to workaround bug 4832765,
            public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(
                                visibleRect, orientation, direction);
            }
        };
        doAuthorEntityMatch();
        orgNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orgNameList.setLayoutOrientation(JList.VERTICAL);
        orgNameList.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(orgNameList);
        listScroller.setPreferredSize(new Dimension(250, 80));
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label1 = new JLabel("Original Names");
        label1.setLabelFor(orgNameList);
        listPane.add(label1);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel listsPanel = new JPanel();
        listsPanel.setLayout(new GridLayout(1,2));
            //      listsPanel.add(listPane,BorderLayout.LINE_START);
        listsPanel.add(listPane);

        matchNameList = new JList() {
            //Subclass JList to workaround bug 4832765,
            public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(
                                visibleRect, orientation, direction);
            }
        };
        matchNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matchNameList.setLayoutOrientation(JList.VERTICAL);
        matchNameList.setVisibleRowCount(-1);
        listScroller = new JScrollPane(matchNameList);
        listScroller.setPreferredSize(new Dimension(250, 80));


        MouseAdapter mouseAdap = makeMouseAdapter();
        orgNameList.addMouseListener(mouseAdap);
        matchNameList.addMouseListener(mouseAdap);

        listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        label1 = new JLabel("Possible Matches");
        label1.setLabelFor(matchNameList);
        listPane.add(label1);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        //      listsPanel.add(listPane,BorderLayout.LINE_END);
        listsPanel.add(listPane);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listsPanel, BorderLayout.CENTER);
    }

    private MouseAdapter makeMouseAdapter(){
        return new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        if(e.getSource() == orgNameList)
                            searchButton.doClick(); //emulate button click
                        //commented out, since the call to vitro doesn't work
                        //if(e.getSource() == matchNameList)
                            //editButton.doClick();
                    }
                    if(orgNameList.getSelectedValue() != null &&
                       matchNameList.getSelectedValue() != null )
                        forceButton.setEnabled(true);
                    else
                        forceButton.setEnabled(false);
                }
            };
        }

    JButton editButton = null, searchButton = null, processButton = null,
        forceButton = null, articleButton = null;
    private void addButtons(){
        //Create and initialize the buttons.
        editButton = new JButton("display Name");
        editButton.setActionCommand("Edit");
        editButton.setEnabled(false); //disabled since page opened in browser doesn't work
        editButton.addActionListener(this);

        searchButton = new JButton("Search For Matches");
        searchButton.setActionCommand("Search");
        searchButton.addActionListener(this);
        getRootPane().setDefaultButton(searchButton);

        processButton = new JButton("re-process all names");
        processButton.setActionCommand("reprocess");
        processButton.addActionListener(this);

        forceButton = new JButton("Force match");
        forceButton.setActionCommand("force");
        forceButton.addActionListener(this);
        forceButton.setEnabled(false);

        articleButton = new JButton("display article");
        articleButton.setActionCommand("article");
        articleButton.setEnabled(false); //disabled since page opened in browser doesn't work
        articleButton.addActionListener(this);

        JPanel topPane = new JPanel();
        topPane.setLayout(new BorderLayout());
        topPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        topPane.add(editButton,BorderLayout.EAST);
        topPane.add(articleButton,BorderLayout.WEST);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(forceButton,BorderLayout.EAST);
        buttonPane.add(processButton,BorderLayout.CENTER);
        buttonPane.add(searchButton,BorderLayout.WEST);

        getContentPane().add(topPane, BorderLayout.PAGE_START);
        getContentPane().add(buttonPane, BorderLayout.PAGE_END);
    }

    private  void buildAndShow() throws IOException, SQLException {
        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
                public void windowClosed(WindowEvent e){
                    windowLive = false;     }
                public void windowClosing(WindowEvent e){
                    windowLive = false; }
            });

        getContentPane().setLayout(new BorderLayout());
        addWidgets();
        pack();
        setVisible(true);
    }
    private void addWidgets() throws IOException, SQLException{
        addButtons();
        addList();
    }

    //this is set to false when the window is closed.
    boolean windowLive = true;

    public static void doNameMatchBlocking(String[] args){
        //UpdateAuthorTokensWithMatchingEntityId.main(args);

        Connection con = getConnection(args[0]);
        
        final NameMatcherPanel panel = new NameMatcherPanel( con );
        panel.args = args;
        Thread thread = new Thread() { public void run() {
            try{panel.buildAndShow();}
            catch(Exception ex){ System.out.println(ex);}
            return;}
        };
        javax.swing.SwingUtilities.invokeLater(thread);

        while( panel.windowLive ){
            try{Thread.currentThread().sleep(200);}
            catch(InterruptedException ex ){}
        }
        
        VitroConnection.close( con );
        //thread.destroy();
        return;
    }

    private String[] args = null;
    
    private static Connection  getConnection(String propFileName)  {        
            VitroConnection.establishDataSourceFromProperties( propFileName );
            try {
                return VitroConnection.getConnection();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return null;
            }        
    }
    
    public static void main(String[] args) {
        if( args == null || args.length != 1 )
            showHelp();
        else if(  goodFile( args[0] ) != null ) 
            System.out.println("configuration file problem -- " + goodFile(args[0]) );
        else               
            doNameMatchBlocking(args);        
    }
    
    private static final void showHelp(){
        System.out.println("NameMatcherPanel will show a GUI that assists in " +
                "associating entities with authors.");
        System.out.println("java NameMatcherPanel <ConfigFile> ");
        System.out.println("The ConfigFile should have db and other configuration info.");
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
