package edu.cornell.mannlib.vitro.biosis.beans;
import java.io.PrintStream;
import java.util.ArrayList;

/* 
   This is a data structure to hold articles that can load load themselves 
   into the vivo pub table
 */
public class Article{
	
	public String    abstractStr      = null; //AB
	public String    authorAddressStr = null; //AD
	public ArrayList addressTokenList = null;
	public String    emailStr         = null;
	public String    accessionNumber  = null;   //AN (too long for unsigned or int)
	public String    authorsStr       = null; //AU
	public ArrayList authorTokenList  = null; //expected in the format "LastName, First Middle"
	public String    editorsStr       = null; //AUB
	public ArrayList editorTokenList  = null; //expected in the format "LastName, First Middle"
	public String    bookTitleStr     = null; //BK
	public String    emailAddressStr  = null; //EMA
	public String[]  fullTextLinks    = null; //FT
	public String[]  fullTextSources  = null;
	public String    languageStr      = null; //LA
	public String    meetingInfoStr   = null; //MT
	public ArrayList meetingTokenList = null;
	public String    bookPublisherStr = null; //PB
	public String    pubYear          = null;    //PY
	public String    sourceStr        = null; //SO
	public ArrayList sourceTokenList  = null;
	public String    titleStr         = null; //TI
	public String    updateCodeStr    = null; //UD
	public String    linkToHoldingsStr= null; //WEBLH
	public String    citationText     = null;

	public int       authorListOrigin= UNSPECIFIED;
	public int       editorListOrigin = UNSPECIFIED;
	public int       addressListOrigin= UNSPECIFIED;
	public int       meetingListOrigin= UNSPECIFIED;
	public int       sourceListOrigin = UNSPECIFIED;
	public int       citationOrigin = UNSPECIFIED;

    //tokentypes
    public static final int UNSPECIFIED    =0;
    public static final int AUTHOR         =1; 
    public static final int MEETING        =2;
    public static final int SOURCE         =3;
    public static final int LINKTOHOLDINGS =4;
    public static final int CITATION       =5;
	/*
	  Token types:
	  0	unspecified
	  1	author
	  3	source
	  2	meeting
	  4	editor
	  5	citation
	*/

	/**
	 * Adds an author's name to the author token list and
	 * to the authorsStr.
	 * 
	 * Format: "Last, First I" no periods after the initials.
	 * This method will strip the periods for you.	 
	 * This does NOT check for duplicates.
	 */
	public void addAuthor(String in){
		if( in == null || in.length() == 0)
			return;
		
		String name = in.replaceAll("\\.", "").trim();

		if( authorsStr != null ){
			authorsStr += "; ";
			authorsStr += name;
		} else {
			authorsStr = name;
		}
		
		if(authorTokenList == null)
			authorTokenList = new ArrayList(4);
		authorTokenList.add(name);
	}

	/**
	 * adds a text link
	 * does NOT check for duplicates.
	 * No more than five links permited, others will be ignored.
	 */
	public void addTextLink(String urlIn, String sourceIn){
		if( urlIn == null || urlIn.length() == 0 ||
			sourceIn == null || sourceIn.length() == 0)
			return;
		
		if(fullTextLinks == null){
			fullTextLinks = new String[5];
			fullTextSources = new String[5];
			for(int i=0; i<fullTextLinks.length; i++){
				fullTextLinks[i]=null;
				fullTextSources[i]=null;
			}			
		}
		
		for( int i  =0; i< fullTextLinks.length; i++){
			if( fullTextLinks[i] == null ){
				fullTextLinks[i] = urlIn.trim();
				fullTextSources[i]=sourceIn.trim();
				break;
			}
		}
	}

	


	/** sql statement log */
	private PrintStream log = null;

    public void setLog(PrintStream logStream){
		log = logStream;
	}
	
	/** add a line to the private sql statement log*/
	private String log(String in){
		if(log!=null)
			log.println(in);
		return in;
	}



	public void clearAllFields(){
		abstractStr        = null; //AB
		authorAddressStr   = null; //AD
		if (addressTokenList != null ) {
			addressTokenList.clear();
		}
		emailStr           = null;
		accessionNumber    = "";   //AN
		authorsStr         = null; //AU
		if ( authorTokenList != null /*&& authorTokenList.size() > 0*/ ) {
			authorTokenList.clear();
		}
		editorsStr         = null; //AUB
		if ( editorTokenList != null ) {
			editorTokenList.clear();
		}
		bookTitleStr       = null; //BK
		emailAddressStr    = null; //EMA
		fullTextLinks      = null; //FT
		fullTextSources    = null;
		languageStr        = null; //LA
		meetingInfoStr     = null; //MT
		if ( meetingTokenList != null ) {
			meetingTokenList.clear();
		}
		bookPublisherStr   = null; //PB
		pubYear            = null; //PY
		sourceStr          = null; //SO
		if ( sourceTokenList != null ) {
			sourceTokenList.clear();
		}
		titleStr           = null; //TI
		updateCodeStr      = null; //UD as date
		linkToHoldingsStr  = null; //WEBLH
	}



	public String toString(){
		StringBuffer sb = new StringBuffer("");
		sb.append("\n		abstractStr: "+ abstractStr);
		sb.append("\n		authorAddressStr: "+ authorAddressStr);
		sb.append("\n		addressTokenList: "+ addressTokenList);
		sb.append("\n		emailStr: "+ emailStr);
		sb.append("\n		accessionNumber: "+ accessionNumber);
		sb.append("\n		authorsStr: "+ authorsStr);
		sb.append("\n		authorTokenList: "+ authorTokenList);
		sb.append("\n		editorsStr: "+ editorsStr);
		sb.append("\n		editorTokenList: "+ editorTokenList);
		sb.append("\n		bookTitleString: "+ bookTitleStr);
		sb.append("\n		emailAddressStr: "+ emailAddressStr);
		//sb.append("\n		[]  fullTextLinks: "		[]+ fullTextLinks);
		//sb.append("\n		[]  fullTextSources: "		[]+ fullTextSources);
		sb.append("\n		languageStr: "+ languageStr);
		sb.append("\n		meetingInfoStr: "+ meetingInfoStr);
		sb.append("\n		meetingTokenList: "+ meetingTokenList);
		sb.append("\n		bookPublisherStr: "+ bookPublisherStr);
		sb.append("\n		pubYear: "+ pubYear);
		sb.append("\n		sourceStr: "+ sourceStr);
		sb.append("\n		sourceTokenList: "+ sourceTokenList);
		sb.append("\n		titleStr: "+ titleStr);
		sb.append("\n		updateCodeStr: "+ updateCodeStr);
		sb.append("\n		linkToHoldingsStr: "+ linkToHoldingsStr);
		sb.append("\n		citationText: "+ citationText);
		
		sb.append("\n		citationOrigin: "+ citationOrigin);
		sb.append("\n		authorListOrigin: "+ authorListOrigin);
		sb.append("\n		editorListOrigin: "+ editorListOrigin);
		sb.append("\n		addressListOrigin: "+ addressListOrigin);
		sb.append("\n		meetingListOrigin: "+ meetingListOrigin);
		sb.append("\n		sourceListOrigin: "+ sourceListOrigin);
		return sb.toString();
	}
}
