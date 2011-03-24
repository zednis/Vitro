package edu.cornell.mannlib.ingest.kwinter;

/*
Copyright 2003-2008 by the Cornell University and the Cornell
Research Foundation, Inc.  All Rights Reserved.

Permission to use, copy, modify and distribute any part of VITRO
("WORK") and its associated copyrights for educational, research and
non-profit purposes, without fee, and without a written agreement is
hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Those desiring to incorporate WORK into commercial products or use
WORK and its associated copyrights for commercial purposes should
contact the Cornell Center for Technology Enterprise and
Commercialization at 395 Pine Tree Road, Suite 310, Ithaca, NY 14850;
email:cctecconnect@cornell.edu; Tel: 607-254-4698; FAX: 607-254-5454
for a commercial license.

IN NO EVENT SHALL THE CORNELL RESEARCH FOUNDATION, INC. AND CORNELL
UNIVERSITY BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
OUT OF THE USE OF WORK AND ITS ASSOCIATED COPYRIGHTS, EVEN IF THE
CORNELL RESEARCH FOUNDATION, INC. AND CORNELL UNIVERSITY MAY HAVE BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  */

import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;


/**
 * This document is used to pull information from the online course catalog and
 * stuff it into a flat file.
 * @author Karl
 *
 */
public class ObtainCourseCatalogFromInternet
{
    public static final String COURSE_CATALOG_URL = "http://cuinfo.cornell.edu/Academic/Courses/";

    // source:  http://forum.java.sun.com/thread.jspa?threadID=5211979&messageID=9909378
    private static final String LINK_REGEX = "\\<[Aa]\\s+[\\^]*?[Hh][Rr][Ee][Ff]\\s*=\\s*[\"']?(.*?)[\"'\\>]";


    /**
     * Opens the given file for writing, and sets it up as a XML database file that
     * can hold formatted Course objects.
     * @param fileName The file to open
     * @return A writer for the given file that can be passed to writeToTDF
     */
    public static BufferedWriter openOutputXML( String fileName ) throws IOException
    {
        // Make sure the file is empty
        File destFile = new File( fileName );
        if( destFile.exists() ) destFile.delete();

        // Open up the destination file
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( destFile ) ) );//, "UTF-8"

        // Add the course header to the XML document
    	BufferedReader headerReader = new BufferedReader( new InputStreamReader( new FileInputStream( "C:\\Documents and Settings\\Karl\\My Documents\\Work - Vivo\\code\\ingestTool\\src\\edu\\cornell\\mannlib\\ingest\\kwinter\\courseheader.xml" ) ) );
    	String line = null;
    	while( null != (line = headerReader.readLine()) )
    	{
    		writer.write( line );
    	}

        // Write the XML body
        writer.write( "\n\n<Courses>" );

        // Return the writer
        return writer;
    }

    public static void closeOutputXML( BufferedWriter writer ) throws IOException
    {
        writer.write( "</Courses>");
        writer.close();
    }

    public static void writeToXML( String unparsedTitle, String title, String info, String description, String college, String number, String department, String crosslistings, String sourceURL, BufferedWriter output ) throws IOException
    {
    	
        output.write( "<Course>\n<UnparsedTitle>" + unparsedTitle + "</UnparsedTitle>\n" +
        		      "<Title>" + title + "</Title>\n<Info>" + info + "</Info>\n<Description>" + description + "</Description>\n" +
        			  "<College>" + college + "</College>\n" + "<Number>" + number + "</Number>\n" + "<Department>" + department + "</Department>\n" +
        			  "<Crosslistings>" + crosslistings + "</Crosslistings>\n" +
        			  "<SourceURL><![CDATA[" + sourceURL + "]]></SourceURL>" + "</Course>\n" );
    }
    
    /// The XML writer flushes to the disk every certain number of iterations
    private static final int FLUSH_PERIOD = 100;

    /**
     * Runs the ingest tool for the course-webpage
     * @param args
     */
    public static void main( String args[] )
    {
        try {

            // First off, try to open the output database file
            BufferedWriter xmlWriter = openOutputXML( "C:\\courses.xml" );

            // Lists of links
            List<String> collegeLinks,  // Like http://cuinfo.cornell.edu/Academic/Courses/CoScollege.php?college=ALS
                         deptLinks   = new ArrayList<String>(),     // Like http://cuinfo.cornell.edu/Academic/Courses/CoScourses.php?college=ALS&dept=Agricultural+Sciences
                         courseLinks = new ArrayList<String>(); // Like http://cuinfo.cornell.edu/Academic/Courses/CoSdetail.php?college=ALS&number=401%284010%29&prefix=AGSCI&title=Seminar+in+Agricultural+Sciences

            // Gets the links for the main course catalog page
            collegeLinks = getWebpageLinks( COURSE_CATALOG_URL, readWebpage( COURSE_CATALOG_URL ) );

            // Throw out links that don't match our expected values
            collegeLinks = sanitize( collegeLinks, "http://cuinfo\\.cornell\\.edu/Academic/Courses/CoScollege\\.php\\?college=.*" );

            // Go through the list of college links and parse out the department pages
            for( String collegeURL : collegeLinks )
            {
                // Get the links from this webpage
                List<String> singleCollegeDeptLinks = getWebpageLinks( collegeURL, readWebpage( collegeURL ) );

                // Get rid of anything that isn't a department-link
                singleCollegeDeptLinks = sanitize( singleCollegeDeptLinks, "\\Qhttp://cuinfo.cornell.edu/Academic/Courses/CoScourses.php?college=\\E.*" );

                // Add the links in here to the main list of links
                deptLinks.addAll( singleCollegeDeptLinks );
            }

            // Go through the list of department links and parse out the course pages
            for( String deptURL : deptLinks )
            {
                // Get the links from this webpage
                List<String> singleDeptCourseLinks = getWebpageLinks( deptURL, readWebpage( deptURL ) );

                // Get rid of anything that isn't a course-link
                singleDeptCourseLinks = sanitize( singleDeptCourseLinks, "\\Qhttp://cuinfo.cornell.edu/Academic/Courses/CoSdetail.php?college=\\E.*" );

                // Add the links to the main list of course-links
                courseLinks.addAll( singleDeptCourseLinks );
            }

            // <p> ... </p> and <blockquote> ... </blockquote> patterns
            Pattern pTagPattern = Pattern.compile( "<p>(.*?)</p>" );
            Pattern blockquoteTagPattern = Pattern.compile( "<blockquote>(.*?)</blockquote>" );

            // Go through each course and parse out course information
            int coursesRead = 0;
            for( String courseURL : courseLinks )
            {
                // Get the course webpage
                String webpage = readWebpage( courseURL );

                // Remove non-essential information
                webpage = webpage.replaceFirst( ".*?<p>", "<p>" );

                // Generate the matchers for this webpage
                Matcher pTagMatcher = pTagPattern.matcher( webpage );
                Matcher blockquoteTagMatcher = blockquoteTagPattern.matcher( webpage );

                // Course information
                String courseTitle = null;
                String courseDescription = null;
                String courseInfo = null;

                // Find the two paragraph tags:  the first is the course title, like "NBA 559(5590) The Venture Capital Industry and Private Equity Markets"
                // the second is the description of the course.
                if( pTagMatcher.find() ) courseTitle = pTagMatcher.group(1);
                if( pTagMatcher.find() ) courseDescription = pTagMatcher.group(1);

                // The blockquote tag is the information about the class, like "Spring. 0.5 credit. D. BenDaniel." or "Fall or spring. 1 credit. S-U grades only. Staff."
                if( blockquoteTagMatcher.find() ) courseInfo = blockquoteTagMatcher.group(1);

                // Grab the GET variables from the URL
                Map<String,String> getVars = null;
                try
                {
                	getVars = parseGetVariablesFromURL( courseURL );
                	
                } catch( Exception e )
                {
                	System.out.println( "Failed to obtain get vars from " + courseURL );
                }

                // Set up information from the get vars
                String college = getVars.get( "college" );
                String number = getVars.get( "number" );
                String department = getVars.get( "prefix" );
                String title = getVars.get( "title" );
                
                // Save the original data so that it can be linked and more data can be pulled out later
                String unparsedTitle = title;
                
                // Before parsing crosslistings from the title, we have to remove all of the things that
                // would mess up the parsing:  "*", "#", "@", "(D)", "(HA)", "(CA)"...etc
    			title = title.replaceAll( " *[\\@\\#\\*]", "" );

    			// Finally, look for type flags
    			for( int i = 0; i < 13; ++i )
    			{
    				String type = "";
    				switch( i )
    				{
    				case 0:	type = "LA-CA"; break;
    				case 1: type = "HA-AS"; break;
    				case 2: type = "CA-AS"; break;
    				case 3: type = "MQR"; break;
    				case 4: type = "KCM-AS"; break;
    				case 5: type = "SBA-AS"; break;
    				case 6: type = "LA-AS"; break;
    				case 7: type = "LA"; break;
    				case 8: type = "HA"; break;
    				case 9: type = "KCM"; break;
    				case 10: type = "SBA"; break;
    				case 11: type = "CA"; break;
    				case 12: type = "D"; break;
    				}

    				// The ending ) is outside of the double-quotes because some
    				// codes are like "(CA"...durf
    				title = title.replaceAll( "\\Q(" + type + "\\E[\\)]?", "" );
    			}
    			
    			// Get rid of any abandoned whitespace
    			title = title.trim();
    			
    			if( !unparsedTitle.equals(title) )
    				System.out.println( unparsedTitle + " -> " + title );
                
                String crosslistings = "";

                Pattern crosslistingPattern = Pattern.compile( " *\\((?:also)? *([^)]*)\\)?" );
                Matcher m = crosslistingPattern.matcher( title );
                Pattern digitsPattern = Pattern.compile( "\\d\\d\\d" );
                
                if( m.find() )
                {
                	if( digitsPattern.matcher( m.group(1) ).find() )
                	{
	                	title = title.substring( 0, m.start() ) + title.substring( m.end() );
	                	crosslistings = m.group(1);
	
	                	String clb4 = crosslistings;
	
	                	// Get rid of braces in the crosslistings (these are sometimes misused as grouping items...)
	                	crosslistings = crosslistings.replaceAll( "}", "" );
	
	                	// Fix the crosslistings syntax for unclosed brackets
	                	Pattern incorrectCrosslistingSyntaxPattern = Pattern.compile( "\\[([0123456789\\./]*?)(?:$|[^0123456789\\.\\]/])" );
	                	int i = 0;
	                	while( (m = incorrectCrosslistingSyntaxPattern.matcher( crosslistings )).find( i ) )
	                	{
	                		crosslistings = crosslistings.substring(0, m.end(1) ) + "]" + crosslistings.substring(m.end(1));
	                		i = m.end(1) + 1;
	                	}
	
	                	// If the crosslisting was fixed, save it
	                	if( clb4.equals( crosslistings) == false  )
	                	{
	                    	System.out.println( clb4 + " => " + crosslistings );
	                	}
                	}
                	else
                		System.out.println( "Ignoring crosslisting candidate " + m.group(1) + " because it doesn't contain digits" );
                }

            	// Get rid of junk in the HTML strings
            	if( courseTitle != null )
            	{
            		courseTitle = removeHTMLMarkup( courseTitle );
                	courseTitle = fixUnicode( courseTitle );
            		courseTitle = makeHTMLEncodingLowercase( courseTitle );
            		courseTitle = StringEscapeUtils.escapeXml( StringEscapeUtils.unescapeHtml( courseTitle ) );
            	}
            	else
            		courseTitle = "";

            	if( courseInfo != null )
            	{
                	courseInfo = fixUnicode( courseInfo );
            		courseInfo = courseInfo.replaceAll( "<span.*/span>", "" );
            		courseInfo = makeHTMLEncodingLowercase( courseInfo );
    	        	courseInfo = StringEscapeUtils.escapeXml( StringEscapeUtils.unescapeHtml( courseInfo ) );
            	}
            	else
            		courseInfo = "";
            	
            	if( courseDescription != null )
            	{
                	courseDescription = fixUnicode( courseDescription );
            		courseDescription = makeHTMLEncodingLowercase( courseDescription );
            		courseDescription = StringEscapeUtils.escapeXml( StringEscapeUtils.unescapeHtml( courseDescription ) );
            	}
            	else
            		courseDescription = "";

                // Add this to the output
                writeToXML( StringEscapeUtils.escapeXml( unparsedTitle ),
                			StringEscapeUtils.escapeXml( title ),
                			courseInfo,
                			courseDescription,
                			college == null ? "" : StringEscapeUtils.escapeXml( college ),
                			number == null ? "" : StringEscapeUtils.escapeXml( number ),
                			department == null ? "" : StringEscapeUtils.escapeXml( department ),
                			crosslistings == null ? "" : StringEscapeUtils.escapeXml( crosslistings ), courseURL, xmlWriter );

                // Which course we are reading; used to periodically flush the results to disk
            	coursesRead++;

                // Flush the results if we're about to roll over
                if( coursesRead % FLUSH_PERIOD == 0 )
                {
                    System.out.println( "\n" + coursesRead + " courses read");
                    xmlWriter.flush();
                }
                //if( coursesRead > 2000 ) break;
            }

            closeOutputXML( xmlWriter );

            // All of the course links for this website
            System.out.println( "Found, read and imported " + coursesRead + " courses" );

        } catch( URISyntaxException urise )
        {
        } catch( IOException ioe )
        {
            System.out.println( "There was a problem writing the courses into the tab-delinmited file" );
        }
    }
    
    public static String removeHTMLMarkup( String string )
    {
    	return string.replaceAll( "<[^>]*>", "" );
    }

    public static Map<String,String> parseGetVariablesFromURL( String url ) throws Exception
    {
    	// Get the query part of the URL
    	String query = (new URL(url)).getQuery();
    	
    	// Parse the query into the arguments
    	String[] arguments = query.split( "\\&" );
    	
    	// Split the arguments into key, value pairs
    	Map<String,String> retval = new TreeMap<String,String>();
    	
    	// Set all of the values
    	for( int i = 0; i < arguments.length; ++i )
    	{
    		String[] vals = arguments[i].split("=");
    		String key = vals[0];
    		String value = vals.length > 1 ? vals[1] : "";
    		
    		// Decode the get variables
    		key = URLDecoder.decode(key,"UTF-8");
    		value = URLDecoder.decode(value,"UTF-8");
    		
    		// Replace &AMP; with &amp;, and convert &amp; into &
    		value = StringEscapeUtils.unescapeHtml(makeHTMLEncodingLowercase( value ));
    		
    		// Insert into the map
    		retval.put( key, value );
    	}
    	
    	// Return the map
    	return retval;
    }

    // strings to replace:
    /*
     * &rsquo; -> '
     * &ldquo; -> "
     * &rdquo; -> "
     * &ndash; -> -
     * &mdash; -> --
     * &amp;   -> &
     */

    public static String fixUnicode( String str )
    { 
        str = str.replaceAll( "[“”]", "\"" ).replaceAll("’", "'");
        return str;
    }
    
    /**
     * Removes entries from the list that don't match the given regex
     * @param list
     * @param passRegex
     */
    public static List<String> sanitize( List<String> list, String passRegex )
    {
        List<String> retList = new ArrayList<String>( list );

        Iterator<String> iter = retList.iterator();
        while( iter.hasNext() )
        {
            // Check to see if it matches
            if( !iter.next().matches( passRegex ) )
                iter.remove();
        }

        // Return the sanitized list
        return retList;
    }

    static Pattern fixPattern = Pattern.compile( "\\&[^ ]*\\;" );
    
    public static String makeHTMLEncodingLowercase( String string )
    {
    	if( string == null ) return null;
    	Matcher m = fixPattern.matcher( string );
    	int lastIndex = 0;
    	while(m.find(lastIndex))
    	{
    		// Fix the tag
    		string = string.substring(0,m.start()) + m.group().toLowerCase() + string.substring(m.end());
    		lastIndex = m.end();
    	}
    	
    	// Return the string
    	return string;
    }

    public static String fixHTMLCodesInString( String string )
    {
        string = string.replaceAll( "\\&[RrLl][Ss][Qq][Uu][Oo];", "'").replaceAll( "\\&[RrLl][Dd][Qq][Uu][Oo];", "\"");
        string = string.replaceAll( "\\&[Nn][Dd][Aa][Ss][Hh];", "-" ).replaceAll( "\\&[Mm][Dd][Aa][Ss][Hh];", "--" ).replaceAll( "\\&[Aa][Mm][Pp];", "&" );
        return string;
    }

    /**
     * Reads all of the <a href="***"></a> links in a webpage and places *** as a string into the
     * returned list.h
     * @param webpage
     * @return
     */
    public static List<String> getWebpageLinks( String pageURL, String webpage ) throws URISyntaxException
    {
        // The page URL's URI
        URI pageURI = new URI(pageURL);

        // Create the list into which to put the links we find
        List<String> links = new ArrayList<String>();

        // Create the regex parser
        Pattern pattern = Pattern.compile(LINK_REGEX);
        Matcher matcher = pattern.matcher(webpage);

        // Look through the web-page for all of the links
        while( matcher.find() )
        {
            String url = matcher.group(1);

            // Throw out email addresses
            if( url.matches("mailto\\:.*") ) continue;

            // Put the URL into the links list
            links.add( pageURI.resolve( new URI(url) ).toASCIIString() );
        }

        // Return the list we created
        return links;
    }

    /**
     * Gets the HTML source of a webpage and reads it into a string
     * @param url
     * @return
     */
    public static String readWebpage( String url )
    {
        String returnString = "";
        InputStream is = null;
        try {
        	
        	// Most webpages are at least 5k in size
            StringBuilder webpage = new StringBuilder( 5000 );
            
            URL urlObject = new URL( url );
            is = urlObject.openStream();
            boolean hasInvalidCharacter = false;
            int c;
            while( -1 != (c = is.read()) ) {
            	if( c < 0 || c > 255) 
            	{
            		hasInvalidCharacter = true;
            		System.out.println( "INVALID CHARACTER:  " + (char)c );
            	}

            	// Don't write endlines because they screw up parsing
            	if( c != '\n' && c != '\r' ) webpage.appendCodePoint(c);
            }
            
            // Close the input stream
            is.close();
            
            if( hasInvalidCharacter )
            {
	            // Change the encoding
	        	final String encoding = "Cp1252";
	        	returnString = new String( webpage.toString().getBytes( encoding ), encoding );

	            // TODO: this is just for testing to see if we actually did anything...
	            for( int i = 0; i < returnString.length(); ++i )
	            {
	            	if( returnString.codePointAt(i) < 0 || returnString.codePointAt(i) > 255 )
	            	{
	            		System.out.println( "FAILED TO RESOLVE INVALID CHAR:  " + (char)c );
	            	}
	            }
            }
            else
            	returnString = webpage.toString();
        }
        catch( MalformedURLException mfurle )
        {
            System.out.println( "Bad URL provided: \"" + url + "\"" );
        }
        catch( IOException ioe )
        {
            System.out.println( "Read error occurred for URL \"" + url + "\":" );
            ioe.printStackTrace();
        }
        finally
        {
        	
        	// Reset the input stream
        	is = null;
        }

        return returnString;

    }


}
