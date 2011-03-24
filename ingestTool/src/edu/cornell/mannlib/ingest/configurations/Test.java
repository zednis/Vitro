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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

public class Test {

    public String toString(){
        return "Test class";
    }

    public static void main(String argv[]){
        if( argv == null || argv.length != 1){
            System.out.println("Usage: Test configDir " + argv[0]);
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

        ApplicationContext ctx = new FileSystemXmlApplicationContext(filename+File.separatorChar+"testConfig.xml");
        SpringIngester ingester = (SpringIngester)ctx.getBean("ingester");
        System.out.println("the ingester: \n" + ingester.toString());
        //ingester.ingest();


        ApplicationContext ctx2 = new FileSystemXmlApplicationContext(filename+File.separatorChar+"jenaConfig.xml");
        com.hp.hpl.jena.db.DBConnection conn = (DBConnection)ctx2.getBean("jenaConnection");
        System.out.println("the dbConn: \n" + conn.toString());
        ModelMaker maker = (ModelMaker)ctx2.getBean("jenaModelMaker");
        ModelFactory mf = null;
        System.out.println("The maker: " + maker.toString());
        Model model = (Model)ctx2.getBean("jenaModel");

    }
}
