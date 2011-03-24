package edu.cornell.mannlib.ingest.configurations;

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

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.cornell.mannlib.ingest.interfaces.IngestParser;

/**
 * A class that runs can set up a Ingest process from
 * a Spring xml config.
 *
 * @author bdc34
 */
public class SpringIngester {
    IngestParser parser;
    Logger log;

    /**
     * @param dloader - how to get or download the bytes to ingest
     * @param parser  - What to do with the bytes from the download
     */
    public SpringIngester( IngestParser parser ){
        log = Logger.getLogger(SpringIngester.class);
        this.parser    = parser;
    }

    /* ######################## Getters and Setters ############################ */
    public IngestParser getParser() {
        return parser;
    }
    public void setParser(IngestParser parser) {
        this.parser = parser;
    }
    /* ######################## end of Getters and Setters ########################*/


    public void ingest(){
        if( getParser() == null ){
            log.error("no parser was set, doing nothing");
            return;
        }
        getParser().parse();
    }

    /**
     * Load a springconfig.xml to setup a ingester and all its
     * related objects, then run the ingest.
     *
     * @param argv
     */
    public static void main(String argv[]){
        if( argv == null || argv.length != 1){
            System.out.println("Usage: Ingester (springconfig.xml) " + argv[0]);
            System.exit(1);
        }

        String filename = argv[0];
        File f = new File(filename);
        if(! f.exists() ){
            System.out.println("The file " + filename + " does not exist.");
            System.exit(1);
        }
        if( !f.canRead() ){
            System.out.println("The file " + filename + " is not readable.");
            System.exit(1);
        }

        ApplicationContext ctx = new FileSystemXmlApplicationContext(filename);
        SpringIngester ingester = (SpringIngester)ctx.getBean("ingester");
        System.out.println("the ingester: \n" + ingester.toString());
        ingester.ingest();
    }
}
