package edu.cornell.mannlib.ingest.actions;

/*
Copyright © 2003-2008 by the Cornell University and the Cornell
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.IngestAction;

/**
 * simple action that writes xml for one rec to a file.
 *
 * Useful to see what the IngestSaxParser is pulling out as
 * records.
 *
 * @author bdc34
 *
 */
public class OutputXmlToFile implements IngestAction {
    File outfile;
    FileWriter writer;
    String filename;
    private static final Log log = LogFactory.getLog(OutputXmlToFile.class.getName());

    public OutputXmlToFile( String filename ) throws IOException{
        this.filename = filename;
        outfile = new File(filename);
        writer = new FileWriter( outfile );
    }

    public void doAction(Element input) {
        try{
            if( input != null)
                writer.write( input.asXML());
        }catch(IOException ex){
            log.error("error writing to file " + filename, ex);
        }

    }

    public void endOfParse( ){
        try {
            writer.close();
        } catch (IOException e) {
            log.error("error closing file" , e);
        }
    }

    public void endOfParsing() {
        endOfParse();

    }

}
