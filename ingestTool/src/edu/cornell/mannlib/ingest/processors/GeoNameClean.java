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

public class GeoNameClean implements StringProcessor{

    public String process(String value) {
        if( value == null ) return null;
        value = value.trim();
        if( value.endsWith(".")) value = value.substring(0, value.length()-1);

        if (value.equals("U.S.") || value.equals("USA") || value.equals("All fifty states.") || value.indexOf("Many of the states")>0) {
            value = "United States";
        } else if (value.equals("Northeast U.S.") || value.equals("Northeast U.S") || value.equals("Northeast States")) {
            value = "northeastern U.S.";
        } else if (value.equals("Global") || value.equals("World") || value.equals("International")) {
            value = "international";
        } else if( value.equals("Democratic People`s Republic of Korea") ){
            value = "North Korea";
        } else if( value.equals("Province of China Taiwan") || value.equals("Provance of China")){
            value = "China";
        } else if( value.equals("United Republic of Tanzania")){
            value = "Tanzania";
        } else if( value.equals("Democratic Republic of the Congo") || value.equals("DR Congo") || value.equals("The Democratic Republic of the Congo") ){
            value = "Congo";
        } else if( value.matches("Lao Peo?le`s Democratic Republic") ){
            value = "Laos";
        } else if( value.equals("Republic of Korea") || value.equals("Korea")){
            value = "South Korea";
        } else if( value.equals("Columbia")){
            value = "Colombia";
        } else if( value.equals("Islamic Republic of Iran") || value.equals("Islamic Rebuplic of Iran")){
            value = "Iran";
        } else if( value.equals("Russian Federation")){
            value = "Russia";
        } else if( value.equals("Argentia") || value.equals("Argentininia")){
            value = "Argentina";
        } else if( value.equals("Newfoundland")){
            value = "Canada";
        } else if( value.equals("Viet Nam")){
            value = "Vietnam";
        } else if( value.equals("Republic of Serbia")){
            value = "Serbia";
        } else if( value.equals("Monzambique")){
            value = "Mozambique";
        }else if( value.equals("New Zeland")){
            value = "New Zealand";
        }else if( value.equals("Czeck Republic")){
                value = "Czech Republic";
        } else if( value.equals("US Virgin Islands")){
            value = "U.S. Virgin Islands";
        } else if( value.equals("Dominican Repuic")){
            value = "Dominican Republic";
        } else if( value.equals("Afganistan")){
            value = "Afghanistan";
        } else if( value.equals("The State of Eritrea")){
            value = "Eritrea";
        }else if( value.equals("Sengal")){
            value = "Senegal";
        }else if( value.equals("Botswanna")){
            value = "Botswana";
        }else if( value.matches("C.te D.Ivoire")){
            value = "Cote D'Ivoire";
        }else if( value.equals("Venezula")){
            value = "Venezuela";
        }else if( value.matches(".*Syrian.*")) {
            value = "Syria";
        }else if( value.equals("Holland")){
                value = "Netherlands";
        } else if (value.equals("UK") || value.equals("U.K.") || value.equals("England") || value.equals("Great Britain") || value.matches(".*United Kingdom.*")) {
            value = "United Kingdom";
        } else if( value.equals("Barzil")){
            value = "Brazil";
        } else if( value.matches(".*Bosnia.*") || value.matches(".*Herzegovina.*")){
            value = "Bosnia and Herzegovina";
        }        else if( value.equals("Phillippines") || value.equals("Phillipines") || value.equals("Pilippines") || value.equals("The Philippines")){
            value = "Philippines";
        } else if( value.equalsIgnoreCase("Latin American") || value.matches(".*Latin America")){
            value = "Latin America";
        } else if( value.matches(".*Africa.*")){
            value = "Africa";
        } else if( value.matches(".*Macedonia.*") ){
            value = "Macedonia";
        }
        return value;
    }

}
