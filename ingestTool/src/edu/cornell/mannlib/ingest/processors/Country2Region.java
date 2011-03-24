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

import java.util.HashSet;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;

/**
 * Converts a country name into a region name such as
 * Africa, Asia, Canada, Europe etc.
 *
 * This should read a set of ents2ents relations out of the database
 * so we could have a property countryInRegion and then use that data.
 *
 * @author bdc34
 *
 */
public class Country2Region implements StringProcessor {
    HashSet<String> africa;
    HashSet<String> asia;
    HashSet<String> canada;
    HashSet<String> europe;
    HashSet<String> southAm;
    HashSet<String> international;

    public Country2Region(){
         africa = new HashSet<String>();
         asia = new HashSet<String>();
         canada = new HashSet<String>();
         europe = new HashSet<String>();
         southAm = new HashSet<String>();
         international = new HashSet<String>();

         setupRegions();
    }
    public String process(String in) {
        if( in == null || in.trim().length() == 0 )
            return in;

        if( africa.contains(in))
            return "Africa";
        else if ( asia.contains(in))
            return "Asia";
        else if( canada.contains(in))
            return "Canada";
        else if( europe.contains(in))
            return "Europe";
        else if( southAm.contains(in))
            return "Mexico, Central & South America";
        else
            return "International (general)";
    }

    public void setupRegions(){
        HashSet<String> area = canada;
        area.add("Canada");

        area=africa;
        area.add("Benin");
        area.add("Mauritius");
        area.add("Cameroon");
        area.add("Eritria");
        area.add("Kenya");
        area.add("Botswana");
        area.add("Morocco");
        area.add("Egypt");
        area.add("Congo");
        area.add("Monaco");
        area.add("South Africa");
        area.add("Zimbabwe");
        area.add("Tunisia");
        area.add("Niger");
        area.add("Burkina Faso");
        area.add("Ethiopia");
        area.add("Togo");
        area.add("Madagascar");
        area.add("Senegal");
        area.add("Ghana");
        area.add("Gambia");
        area.add("Gabon");
        area.add("Zambia");
        area.add("Uganda");
        area.add("Rwanda");
        area.add("Tanzania");
        area.add("Sierra Leone");
        area.add("Palau");
        area.add("Nigeria");
        area.add("Mozambique");
        area.add("Malawi");
        area.add("Burundi");



        area=europe;
        area.add("Moldova");
        area.add("Bulgaria");
        area.add("Turkey");
        area.add("Romania");
        area.add("Portugal");
        area.add("Switzerland");
        area.add("Norway");
        area.add("Netherlands");
        area.add("Latvi");
        area.add("Hungary");
        area.add("Greenland");
        area.add("France");
        area.add("Finland");
        area.add("Estonia");
        area.add("Greece");
        area.add("Belgium");
        area.add("Serbia");
        area.add("Luxembourg");
        area.add("Iceland");
        area.add("Czech Republic");
        area.add("Macedonia");
        area.add("United Kingdom");
        area.add("Ukraine");
        area.add("Spain");
        area.add("Italy");

        area.add("Poland");
        area.add("Croatia");
        area.add("Austria");
        area.add("Denmark");
        area.add("Swednen");
        area.add("Lithuania");
        area.add("Bosnia and Herzegovina");
        area.add("Germany");
        area.add("Ireland");
        area.add("Slovakia");
        area.add("Slovenia");

        area=asia;
        area.add("Azerbaijan");
        area.add("Singapore");
        area.add("Thailand");
        area.add("Pakistan");
        area.add("Russia");
        area.add("Sri Lanka");
        area.add("South Korea");
        area.add("North Korea");
        area.add("Indonesia");
        area.add("India");
        area.add("Kyrgyzstan");
        area.add("Afghanistan");
        area.add("Laos");
        area.add("Myanmar");
        area.add("Malaysia");
        area.add("Cambodia");
        area.add("China");
        area.add("Vietnam");
        area.add("Philippines");
        area.add("Japan");
        area.add("Bhutan");
        area.add("Bangladesh");
        area.add("Nepal");
        area.add("Taiwan");
        area.add("Malasia");

        area=southAm;
        area.add("Bolivia");
        area.add("Jamaica");
        area.add("Honduras");
        area.add("Peru");
        area.add("Panama");
        area.add("Venezuela");
        area.add("Chile");
        area.add("El Salvador");
        area.add("Mexico");
        area.add("Guatemala");
        area.add("Ecuador");
        area.add("Dominican Republic");
        area.add("Cuba");
        area.add("Costa Rica");
        area.add("Colombia");
        area.add("Brazil");
        area.add("Bahamas");
        area.add("Nicaragua");
        area.add("Belize");
        area.add("Argentina");
        area.add("Guyana");
        area.add("Trinidad and Tobago");

        /* Unclassified: */
//        area.add("Australia");
//        area.add("Syria");
//        area.add("New Zealand");
//        area.add("Israel");
//        area.add("Qatar");
//        area.add("United States");
//        area.add("Iran");
//        area.add("Iraq");
//        area.add("Jordan");
//
//

    }

    /*
     *
Canada

Eritria
Kenya
Botswana
Morocco
Egypt
Congo
Monaco
South Africa
Zimbabwe
Tunisia
Niger
Burkina Faso
Ethiopia
Togo
Madagascar
Senegal
Ghana
Gambia
Gabon
Zambia
Uganda
Rwanda
Tanzania
Sierra Leone
Palau
Nigeria
Mozambique
Malawi

Bulgaria
Turkey
Romania
Portugal
Switzerland
Norway
Netherlands
Latvi
Hungary
Greenland
France
Finland
Estonia
Greece
Belgium
Serbia
Luxembourg
Iceland
Czech Republic
Macedonia
United Kingdo
Ukraine
Spain
Italy
Australia
Poland
Croatia
Austria
Denmark
Swednen
Lithuania
Bosnia and Herzegovina
Germany
Ireland
Slovakia
Slovenia

Azerbaijan
Singapore
Thailand
Pakistan
Russia
Sri Lanka
South Korea
North Korea
Indonesia
India
Kyrgyzstan
Afghanistan
Laos
Myanmar
Malaysia
Cambodia
China
Vietnam
Philippines
Japan
Bhutan
Bangladesh
Nepal
Taiwan
Malasia

Bolivia
Jamaica
Honduras
Peru
Panama
Venezuela
Chile
El Salvador
Mexico
Guatemala
Ecuador
Dominican Republic
Cub
Costa Rica
Colombia
Brazil
Bahamas
Nicaragua
Belize
Argentina

* Unclassified:
Cameroon
Burundi
Guyana
Syria
New Zealand
Israel
Mauritius
Qatar
United States
Iran
Iraq
Moldova
Jordan
Benin
Trinidad and Tobago


     */
}
