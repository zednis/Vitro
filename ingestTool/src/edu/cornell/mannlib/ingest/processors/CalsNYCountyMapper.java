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

public class CalsNYCountyMapper implements StringProcessor{

    public String process(String in) {
        if( in == null || in.trim().length() ==0)
            return in;

        if( in.equalsIgnoreCase("Tompkins County") )
            return "Tompkins County";
        else if( in.equalsIgnoreCase("Kings County") ||
                in.equalsIgnoreCase("Bronx County") ||
                in.equalsIgnoreCase("Queens County") ||
                in.equalsIgnoreCase("Richmond County") ||
                in.equalsIgnoreCase("New York County") )
            return "Metropolitan New York City";
        else if( in.startsWith("All"))
            return "New York State (general)";
        else
            return "Other Upstate Counties";
    }

}
