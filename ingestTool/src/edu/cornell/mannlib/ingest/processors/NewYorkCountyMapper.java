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

/**
 * Sticks the word " County" after some ny state county.
 *
 * @author bdc34
 *
 */
public class NewYorkCountyMapper implements StringProcessor {


    public String process(String value ) {
        if( value == null ) return null;
        value = value.trim();

        if( value.equals("Albany") ||
                value.equals("Allegany") ||
                value.equals("Allyn") ||
                value.equals("Bill") ||
                value.equals("Bronx") ||
                value.equals("Broome") ||
                value.equals("Cattaraugus") ||
                value.equals("Cayuga") ||
                value.equals("Charlotte") ||
                value.equals("Chautauqua") ||
                value.equals("Chemung") ||
                value.equals("Chenango") ||
                value.equals("Cheryl") ||
                value.equals("Clinton") ||
                value.equals("Columbia") ||
                value.equals("Cortland") ||
                value.equals("Delaware") ||
                value.equals("Dutchess") ||
                value.equals("Erie") ||
                value.equals("Essex") ||
                value.equals("Franklin") ||
                value.equals("Fulton") ||
                value.equals("Genesee") ||
                value.equals("George") ||
                value.equals("Greene") ||
                value.equals("Hamilton") ||
                value.equals("Herkimer") ||
                value.equals("Jay") ||
                value.equals("Jefferson") ||
                value.equals("Johnna") ||
                value.equals("Kings") ||
                value.equals("Lewis") ||
                value.equals("Livingston") ||
                value.equals("Madison") ||
                value.equals("Marge") ||
                value.equals("Monroe") ||
                value.equals("Montgomery") ||
                value.equals("Nassau") ||
                value.equals("New York") ||
                value.equals("Niagara") ||
                value.equals("Oneida") ||
                value.equals("Onondaga") ||
                value.equals("Ontario") ||
                value.equals("Orange") ||
                value.equals("Orleans") ||
                value.equals("Oswego") ||
                value.equals("Otsego") ||
                value.equals("Putnam") ||
                value.equals("Queens") ||
                value.equals("Rensselaer") ||
                value.equals("Richmond") ||
                value.equals("Rockland") ||
                value.equals("Saratoga") ||
                value.equals("Schenectady") ||
                value.equals("Schoharie") ||
                value.equals("Schuyler") ||
                value.equals("Seneca") ||
                value.equals("St. Lawrence") ||
                value.equals("Steuben") ||
                value.equals("Suffok") ||
                value.equals("Suffolk") ||
                value.equals("Sullivan") ||
                value.equals("Tioga") ||
                value.equals("Tompkins") ||
                value.equals("Tryon") ||
                value.equals("Ulster") ||
                value.equals("Warren") ||
                value.equals("Washington") ||
                value.equals("Wayne") ||
                value.equals("Westchester") ||
                value.equals("Wyoming") ||
                value.equals("Yates") )
            value += " County";

        if( value.startsWith("Kings"))
            value = "Kings County";
        if( value.startsWith("Richmond"))
            value = "Richmond County";

        if( value.startsWith("New York"))
            value = "New York County";        
        if( value.equals("New York (Manhattan)"))
            value = "New York County";
        return value;
    }
}
