package edu.cornell.mannlib.ingest.processors;

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

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
//import edu.cornell.mannlib.vitro.dao.db.CleanString;

/**
 *
 * Notice that if you run edu.cornell.mannlib.vitro.utils.CleanString=DEBUG
 * log4j will tell you what non ascii chars you have and their code points.
 *
 * w3c has a document about which unicode should and should not be in xml:
 * http://www.w3.org/TR/unicode-xml/#Suitable
 *
 *  If this is slowing things down it could be changed to make only one pass
 *  through the String.
 */
public class CleanInput implements StringProcessor {
    public String process(String in) {
    	System.out.println (CleanInput.class.getName()+" just doing trim - other stuff depends on non-existent package edu.cornell.mannlib.vitro.dao.db");
        return (in != null) ? in.trim() : null; // CleanString.clean(in);
    }
}
