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
import java.util.Set;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;

public class State2Region implements StringProcessor {
    HashSet<String> northeast;
    HashSet<String> midwest;
    HashSet<String> southern;
    HashSet<String> west;
    HashSet<String> general;

    public State2Region(){
         northeast = new HashSet<String>();
         midwest= new HashSet<String>();
         southern= new HashSet<String>();
         west= new HashSet<String>();
         general= new HashSet<String>();

         Set<String> area = west;
         area.add("Arizona");
         area.add("California");
         area.add("Colorado");
         area.add("Idaho");
         area.add("Montana");
         area.add("Nevada");
         area.add("New Mexico");
         area.add("North Dakota");
         area.add("Oregon");
         area.add("South Dakota");
         area.add("Washington");
         area.add("Utah");
         area.add("Hawaii");
         area.add("Wyoming");
         area.add("Alaska");

         area = southern;
         area.add("Arkansas");
         area.add("Florida");
         area.add("Georgia");
         area.add("Kentucky");
         area.add("Louisiana");
         area.add("Mississippi");
         area.add("North Carolina");
         area.add("South Carolina");
         area.add("Tennessee");
         area.add("Texas");
         area.add("Virginia");
         area.add("West Virginia");

         area = northeast;
         area.add("Connecticut");
         area.add("Delaware");
         area.add("District of Columbia");
         area.add("Maine");
         area.add("Maryland");
         area.add("Massachusetts");
         area.add("Missouri");
         area.add("New Hampshire");
         area.add("New Jersey");
         area.add("New York");
         area.add("Vermont");
         area.add("Pennsylvania");
         area.add("Rhode Island");

         area = midwest;
         area.add("Illinois");
         area.add("Indiana");
         area.add("Iowa");
         area.add("Kansas");
         area.add("Michigan");
         area.add("Minnesota");
         area.add("Nebraska");
         area.add("Ohio");
         area.add("Oklahoma");
         area.add("Wisconsin");
    }
    public String process(String in) {
        if( in == null || in.trim().length() == 0 )
            return in;

        if( northeast.contains(in))
            return "Northeastern U.S.";
        else if ( midwest.contains(in))
            return "Midwestern U.S.";
        else if( southern.contains(in))
            return "Southern U.S.";
        else if( west.contains(in))
            return "Western U.S.";
        else
            return "U.S. (general)";
    }
/*
Alaska
Arizona
Arkansas
California
Colorado
Connecticut
Delaware
Florida
Georgia
Hawaii
Idaho
Illinois
Indiana
Iowa
Kansas
Kentucky
Louisiana
Maine
Maryland
Massachusetts
Michigan
Minnesota
Mississippi
Missouri
Montana
Nebraska
Nevada
New Hampshire
New Jersey
New Mexico
New York
North Carolina
North Dakota
Ohio
Oklahoma
Oregon
Pennsylvania
Rhode Island
South Carolina
South Dakota
Tennessee
Texas
Utah
Vermont
Virginia
Washington
West Virginia
Wisconsin
Wyoming
 */
    /*
     *
west
Arizona
California
Colorado
Idaho
Montana
Nevada
New Mexico
North Dakota
Oregon
South Dakota
Washington
Utah
Hawaii
Wyoming
Alaska


Arkansas
Florida
Georgia
Kentucky
Louisiana
Mississippi
North Carolina
South Carolina
Tennessee
Texas
Virginia
West Virginia

neast
Connecticut
Delaware
Maine
Maryland
Massachusetts
Missouri
New Hampshire
New Jersey
New York
Vermont
Pennsylvania
Rhode Island

mid
Illinois
Indiana
Iowa
Kansas
Michigan
Minnesota
Nebraska
Ohio
Oklahoma
Wisconsin


    *
     */
}
