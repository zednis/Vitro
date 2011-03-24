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

/**
 * This is the code that was used to load the impact statements in 2005.
 *
 * @version 0.9 2005-12-03
 * @author Jon Corson-Rikert
 */

import java.net.*;
import java.sql.*;
import java.io.*;
import java.util.*;

class Load2005ImpactStatements {
    static final String  IMPACT_DB_PROPS = "localhost_calsdata_jdbc";
    static final String  VIVO_DB_PROPS   = "localhost_vivo4_jdbc";

    static final int     NETID_LINKTYPE=101;
    static final int     IMPACT_STATEMENT_VCLASS_ID=270;
    static final int     SUBMITTER_PROPERTY_ID=566;                     // domain class Impact Statement, range class Academic Employee
    static final int     AFFECTED_GEOGRAPHICAL_REGION_PROPERTY_ID=576; // domain class Impact Statement, range class Geographical Region

    static final int     NEW_ENTITY_STATUS_ID=0; // visible to all: 0, anonymous guests: 1, registered user: 2, individual users: 3, system: 4, webmaster: 5
    static final int     ARCHIVED_ENTITY_STATUS_ID=6;
    static final boolean NOISY=true;

    static ArrayList portalsList=null,projectTypesList=null,geographyList=null,objectivesList=null,topicAreasList=null,topicDescriptionsList=null,fundingSourcesList=null,otherSourcesList=null,collaboratorsList=null,keyPersonnelList=null;
    //static TreeMap deptTreeMap=null;
    static ArrayList missingPlacesList=null;
    static Hashtable locationHashtable;

    public static void main (String args[]) {
        try {
            Connection impactCon = getConnection(IMPACT_DB_PROPS);
            Connection vivodbCon = getConnection(VIVO_DB_PROPS);

            String currentDateTimeStr=getCurrentDateTimeStr();
            PrintWriter out=new PrintWriter(new FileWriter("impacts2005."+currentDateTimeStr+".messages.html"));
            out.println("<html><head></head><body>");

            PrintWriter partialOut = new PrintWriter( new FileWriter("impacts2005."+currentDateTimeStr+".partial.txt"));
            partialOut.println("line\tnetId\tentityId\ttitle\tpartial");

            boolean doDBInserts=false;
            if (args.length > 0) {
                String updateParameter = args[0];
                if ( updateParameter.equalsIgnoreCase("update")) {
                    doDBInserts = true;
                }
            } else {
                System.out.println("Please specify whether to do actual inserts (true or false)");
                System.exit(0);
            }
            String tableName="impacts";
            loadImpactData(impactCon,vivodbCon,tableName,out,partialOut,doDBInserts);

            if (impactCon!=null) {
                impactCon.close();
            }
            if (vivodbCon!=null) {
                vivodbCon.close();
            }
            if (out !=null) {
                out.flush();
                out.close();
            }
            if (partialOut!=null) {
                partialOut.flush();
                partialOut.close();
            }
        } catch (SQLException ex) {
            System.out.println ("SQLException:");
            while (ex != null) {
                System.out.println ("SQLState: " + ex.getSQLState());
                System.out.println ("Message:  " + ex.getMessage());
                System.out.println ("Vendor:   " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println ("");
            }
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace ();
        }
    }

    /**
     * This method has the following overall approach:
     * 1) Read records from the MySQL database table into a result set, then iterate through and match against entities using the netId
     *
     * 2) Process each fields of each record, retrieving in some cases related data from the VIVO database
     *    (e.g., the entity id and campus affiliation of the submitter).  Values in certain
     *    cells known to have been selected from a list or intended at least to form a finite
     *    set are tested against previously encountered values.  Values are prepared for insert
     *    into the database with appropriate formatting but are not yet inserted.
     *    <p>
     * 3) Test to see if the record is already in the database (by title and typeId), then follow
     *    the insertion (optionally just in test mode) with creating links to related entities
     *    and/or tabs.
     *    <p>
     *
     * @param con       : an open database connection
     * @param in        : an open text file
     * @param out       : an open output file for detailed messages beyond what appear in the console window
     * @param doInserts : a boolean value
     */

    public static void loadImpactData(Connection imCon,Connection vivoCon,String table_name,PrintWriter out,PrintWriter partialOut,boolean doInserts)
        throws IOException, SQLException
    {
        int impactCount=0;
        int impactsInsertedCount=0,submitterLinkCount=0,placeInsertCount=0,geographyLinkCount=0,geographyTabCount=0,objectivesTabCount=0,topicAreaTabCount=0, alreadyLinkedCount=0;
        int fundingSourceTabCount=0,keytermLinkCount=0,priorityLinkCount=0,projectTypeTabCount=0, calsTabsInserted=0;
        int facultyCampusFlag=0;
        if (locationHashtable==null) {
            locationHashtable=new Hashtable();
        }

        Statement impactStmt=imCon.createStatement();
        String impactQuery="SELECT * FROM "+table_name+" ORDER BY NetId";
        ResultSet impactRS=impactStmt.executeQuery(impactQuery);
        while (impactRS.next()) {
            ++impactCount;

            ArrayList calsObjectivesTabList=new ArrayList();

            /**** netId ****/
            String netIdStr = cleanInput(impactRS.getString("NetId"));
            int facultyEntityId=0;
            facultyEntityId=findEntityViaNetId(vivoCon,out,netIdStr);
            if (facultyEntityId==0) {
                out.println("<p>skipping to next record after result "+impactCount+" due to missing netId</p><hr/>");
                out.flush();
                break;
            } else {
                facultyCampusFlag=getCampusFlag(vivoCon,out,facultyEntityId);
            }

            /**** project types ****/
            if (projectTypesList==null) {
                projectTypesList=new ArrayList();
            }
            String projectTypesStr=impactRS.getString("ProjectTypes");
            if (projectTypesStr==null || projectTypesStr.equalsIgnoreCase("null") || projectTypesStr.equals("")) {
                projectTypesStr=null;
            } else {
                StringTokenizer pTokens = new StringTokenizer(projectTypesStr,",");
                int phraseCount = pTokens.countTokens();
                int nonNullTokenCount=0;
                for ( int p=0; p<phraseCount; p++ ) {
                    String tokenStr=pTokens.nextToken().trim();
                    if (tokenStr != null && !tokenStr.equals("")) {
                        ++nonNullTokenCount;
                        boolean foundMatch=false;
                        if ("Research".equalsIgnoreCase(tokenStr)) {
                            String researchTypeStr=impactRS.getString("ResearchType");
                            if ("Basic".equalsIgnoreCase(researchTypeStr)) {
                                tokenStr="Basic Research";
                            } else if ("Applied".equalsIgnoreCase(researchTypeStr)) {
                                tokenStr="Applied Research";
                            }
                        }
                        if (projectTypesList.size()>0) {
                            Iterator iter = projectTypesList.iterator();
                            while ( iter.hasNext() ) {
                                String matchStr = (String)iter.next();
                                if (matchStr.equalsIgnoreCase(tokenStr)) {
                                    foundMatch=true;
                                    break;
                                }
                            }

                        }
                        if (!foundMatch) {
                            projectTypesList.add(tokenStr);
                        }
                        projectTypesStr= nonNullTokenCount==1 ? tokenStr : projectTypesStr + "," + tokenStr;
                    }
                }
            }

            /**** affected areas ****/

            if (geographyList==null) {
                geographyList=new ArrayList();
            }
            if (missingPlacesList==null) {
                missingPlacesList=new ArrayList();
            }

            String affectedAreasStr=cleanInput(impactRS.getString("AffectedAreas")); // Yes or No
            if ("Yes".equalsIgnoreCase(affectedAreasStr)) {
                int nonNullPlaceTokenCount=0;
                String affectedStatesStr = cleanInput(impactRS.getString("AffectedUSStates"));
                if (affectedStatesStr==null || affectedStatesStr.equalsIgnoreCase("null") || affectedStatesStr.equals("")) {
                    affectedStatesStr=null;
                } else {
                    StringTokenizer gTokens=new StringTokenizer(affectedStatesStr,",");
                    int phraseCount=gTokens.countTokens();
                    for (int g=0;g<phraseCount;g++) {
                        String placeName=gTokens.nextToken().trim();
                        if (placeName!=null && !placeName.equals("")) {
                            ++nonNullPlaceTokenCount;
                            boolean foundPlace=false;
                            if (geographyList.size()>0) {
                                Iterator geographyIter = geographyList.iterator();
                                while ( geographyIter.hasNext() && !foundPlace) {
                                    String existingPlace = (String)geographyIter.next();
                                    if (existingPlace.equalsIgnoreCase(placeName)) {
                                        foundPlace=true;
                                        placeName=existingPlace;
                                    }
                                }
                            }
                            if (!foundPlace) {
                                geographyList.add(placeName);
                            }
                            affectedAreasStr= nonNullPlaceTokenCount==1 ? placeName : affectedAreasStr + "|" + placeName;
                        }
                    }
                }

                String affectedCountiesStr = cleanInput(impactRS.getString("AffectedNYCounties"));
                if (affectedCountiesStr==null || affectedCountiesStr.equalsIgnoreCase("null") || affectedCountiesStr.equals("")) {
                    affectedCountiesStr=null;
                } else {
                    StringTokenizer gTokens=new StringTokenizer(affectedCountiesStr,",");
                    int phraseCount=gTokens.countTokens();
                    for (int g=0;g<phraseCount;g++) {
                        String placeName=gTokens.nextToken().trim();
                        if (placeName!=null && !placeName.equals("")) {
                            ++nonNullPlaceTokenCount;
                            boolean foundPlace=false;
                            if (geographyList.size()>0) {
                                Iterator geographyIter = geographyList.iterator();
                                while ( geographyIter.hasNext() && !foundPlace) {
                                    String existingPlace = (String)geographyIter.next();
                                    if (existingPlace.equalsIgnoreCase(placeName)) {
                                        foundPlace=true;
                                        placeName=existingPlace;
                                    }
                                }
                            }
                            if (!foundPlace) {
                                geographyList.add(placeName);
                            }
                            affectedAreasStr= nonNullPlaceTokenCount==1 ? placeName : affectedAreasStr + "|" + placeName;
                        }
                    }
                }

                String affectedCountriesStr = cleanInput(impactRS.getString("AffectedCountries"));
                if (affectedCountriesStr==null || affectedCountriesStr.equalsIgnoreCase("null") || affectedCountriesStr.equals("")) {
                    affectedCountriesStr=null;
                } else {
                    StringTokenizer gTokens=new StringTokenizer(affectedStatesStr,",");
                    int phraseCount=gTokens.countTokens();
                    for (int g=0;g<phraseCount;g++) {
                        String placeName=gTokens.nextToken().trim();
                        if (placeName!=null && !placeName.equals("")) {
                            ++nonNullPlaceTokenCount;
                            boolean foundPlace=false;
                            if (geographyList.size()>0) {
                                Iterator geographyIter = geographyList.iterator();
                                while ( geographyIter.hasNext() && !foundPlace) {
                                    String existingPlace = (String)geographyIter.next();
                                    if (existingPlace.equalsIgnoreCase(placeName)) {
                                        foundPlace=true;
                                        placeName=existingPlace;
                                    }
                                }
                            }
                            if (!foundPlace) {
                                geographyList.add(placeName);
                            }
                            affectedAreasStr= nonNullPlaceTokenCount==1 ? placeName : affectedAreasStr + "|" + placeName;
                        }
                    }
                }
            }

            /* apparently missing from 2005 data
            String univObjectivesStr = cleanInput(impactRS.getString("UniversityObjectives"));
            if (univObjectivesStr.equalsIgnoreCase("null") || univObjectivesStr.equals("")) {
                univObjectivesStr=null;
            } else {
                if (objectivesList==null) {
                    objectivesList=new ArrayList();
                }
                StringTokenizer pTokens = new StringTokenizer(univObjectivesStr,",");
                int phraseCount = pTokens.countTokens();
                int nonNullTokenCount=0;
                for ( int p=0; p<phraseCount; p++ ) {
                    String tokenStr=pTokens.nextToken().trim();
                        if (tokenStr != null && !tokenStr.equals("")) {
                        ++nonNullTokenCount;
                        boolean foundMatch=false;
                        if (objectivesList.size()>0) {
                            Iterator iter = objectivesList.iterator();
                            while ( iter.hasNext() ) {
                                String matchStr = (String)iter.next();
                                if (matchStr.equalsIgnoreCase(tokenStr)) {
                                    foundMatch=true;
                                    break;
                                }
                            }
                        }
                        if (!foundMatch) {
                            objectivesList.add(tokenStr);
                        }
                        univObjectivesStr= nonNullTokenCount==1 ? tokenStr : univObjectivesStr + "," + tokenStr;
                    }
                }
            } */

            String portalPreferences=cleanInput(impactRS.getString("AcademicPriorities"));
            int impactsFlag1Numeric=64; // put everything in Impact portal
            if (portalPreferences==null || portalPreferences.equalsIgnoreCase("null") || portalPreferences.equals("")) {
                portalPreferences=null;
                out.println("no academic priority areas specified in record "+impactCount);
            } else {
                if (portalsList==null) {
                    portalsList=new ArrayList();
                }
                StringTokenizer pTokens = new StringTokenizer(portalPreferences,",");
                int phraseCount = pTokens.countTokens();
                int nonNullTokenCount=0;
                for ( int p=0; p<phraseCount; p++ ) {
                    String academicPriority=pTokens.nextToken().trim();
                    if (academicPriority != null && !academicPriority.equals("")) {
                        ++nonNullTokenCount;
                        boolean foundPriority=false;
                        if (portalsList.size()>0) {
                            Iterator iter = portalsList.iterator();
                            while ( iter.hasNext() ) {
                                String existingPriority = (String)iter.next();
                                if (existingPriority.equalsIgnoreCase(academicPriority)) {
                                    foundPriority=true;
                                    break;
                                }
                            }

                        }
                        if (!foundPriority) {
                            portalsList.add(academicPriority);
                        }
                        // College objectives tabs      // keyterm
                        // Environmental Sciences  424  // 437
                        // New Life Sciences       422  // 2429
                        // Applied Social Sciences 423  // 2430
                        // Land-Grant Mission      425  // 319

                        if (academicPriority.equalsIgnoreCase("Applied Social Sciences")) {
                            impactsFlag1Numeric=impactsFlag1Numeric | 32;
                            calsObjectivesTabList.add("423");
                        } else if (academicPriority.equalsIgnoreCase("Environmental Sciences")) {
                            impactsFlag1Numeric=impactsFlag1Numeric | 8;
                            calsObjectivesTabList.add("424");
                        } else if (academicPriority.equalsIgnoreCase("Land-Grant Mission")) {
                            impactsFlag1Numeric=impactsFlag1Numeric | 16;
                            calsObjectivesTabList.add("425");
                        } else if (academicPriority.equalsIgnoreCase("New Life Sciences")) {
                            impactsFlag1Numeric=impactsFlag1Numeric | 4;
                            impactsFlag1Numeric=impactsFlag1Numeric | 2; // add to VIVO as well
                            calsObjectivesTabList.add("422");
                        } else {
                            System.out.println("Error: unexpected academic priority value "+academicPriority);
                            out.println("<p>Error: unexpected academic priority value "+academicPriority+"</p>");
                        }
                        portalPreferences= nonNullTokenCount==1 ? academicPriority : portalPreferences + "," + academicPriority;
                    }
                }
            }

            String topicAreasStr=cleanInput(impactRS.getString("USDATopicAreas"));
            if (topicAreasStr==null || topicAreasStr.equalsIgnoreCase("null") || topicAreasStr.equals("")) {
                topicAreasStr=null;
                out.println("no USDA topic areas specified in record "+impactCount);
            } else {
                if (topicAreasList==null) {
                    topicAreasList=new ArrayList();
                }
                StringTokenizer pTokens = new StringTokenizer(topicAreasStr,",");
                int phraseCount = pTokens.countTokens();
                int nonNullTokenCount=0;
                for ( int p=0; p<phraseCount; p++ ) {
                    String tokenStr=pTokens.nextToken().trim();
                    if (tokenStr != null && !tokenStr.equals("")) {
                        ++nonNullTokenCount;
                        boolean foundMatch=false;
                        if (topicAreasList.size()>0) {
                            Iterator iter = topicAreasList.iterator();
                            while ( iter.hasNext() ) {
                                String matchStr = (String)iter.next();
                                if (matchStr.equalsIgnoreCase(tokenStr)) {
                                    foundMatch=true;
                                    break;
                                }
                            }

                        }
                        if (!foundMatch) {
                            topicAreasList.add(tokenStr);
                        }
                        topicAreasStr= nonNullTokenCount==1 ? tokenStr : topicAreasStr + "," + tokenStr;
                    }
                }
            }

            String topicDescriptionsStr=cleanInput(impactRS.getString("TopicDescription"));
    out.println("Topic description field for record "+impactCount+": "+topicDescriptionsStr);
            if (topicDescriptionsStr==null || topicDescriptionsStr.equalsIgnoreCase("null") || topicDescriptionsStr.equals("")) {
                topicDescriptionsStr=null;
                out.println("no topic descriptions specified in record "+impactCount);
            } else {
                if (topicDescriptionsList==null) {
                    topicDescriptionsList=new ArrayList();
                }
                StringTokenizer pTokens = new StringTokenizer(topicDescriptionsStr,",");
                int phraseCount = pTokens.countTokens();
                int matchCount=0, nonNullTokenCount=0;
                for ( int p=0; p<phraseCount; p++ ) {
                    String tokenStr=pTokens.nextToken().trim();
                    if (tokenStr != null && !tokenStr.equals("")) {
                        ++nonNullTokenCount;
                        boolean foundMatch=false;
                        if (topicDescriptionsList.size()>0) {
                            Iterator iter = topicDescriptionsList.iterator();
                            while ( iter.hasNext() ) {
                                String matchStr = (String)iter.next();
                                if (matchStr.equalsIgnoreCase(tokenStr)) {
                                    foundMatch=true;
                                    break;
                                }
                            }

                        }
                        if (foundMatch) {
                            ++matchCount;
                        } else {
                            topicDescriptionsList.add(tokenStr);
                        }
                        topicDescriptionsStr= nonNullTokenCount==1 ? "<h3>Topic Description</h3><ul><li>"+tokenStr+"</li>" : topicDescriptionsStr+"<li>"+tokenStr+"</li>";
                    }
                }
                topicDescriptionsStr+="</ul>";
            }

            String titleStr=cleanInput(impactRS.getString("Title"));
            if (titleStr==null || titleStr.equalsIgnoreCase("null") || titleStr.equals("")) {
                out.println("no title specified in record "+impactCount);
            } else {
                if (titleStr.length()>255) {
                    System.out.println("Will be truncating title for: "+titleStr);
                    out.println("<p>Will be truncating title for:<br/>"+titleStr+"</p");
                }

                String abstractStr=cleanInput(impactRS.getString("Abstract"));
                if (abstractStr==null || abstractStr.equalsIgnoreCase("null") || abstractStr.equals("")) {
                    abstractStr="";
                } else {
                    abstractStr = abstractStr.replaceAll("[(]+I[)]+","<i>");
                    abstractStr = abstractStr.replaceAll("[(]+/I[)]+","</i>");
                    abstractStr = abstractStr.replaceAll("[|]+","</p><p>");
                    abstractStr="<h3>Abstract</h3><p>"+abstractStr+"</p>";
                }

                String issueStr=cleanInput(impactRS.getString("Issue"));
                if (issueStr==null || issueStr.equalsIgnoreCase("null") || issueStr.equals("")) {
                    issueStr="";
                } else {
                    issueStr = issueStr.replaceAll("[(]+I[)]+","<i>");
                    issueStr = issueStr.replaceAll("[(]+/I[)]+","</i>");
                    issueStr = issueStr.replaceAll("[|]+","</p><p>");
                    issueStr = "<h3>Issue</h3><p>"+issueStr+"</p>";
                }

                String responseStr=cleanInput(impactRS.getString("Response"));
                if (responseStr==null || responseStr.equalsIgnoreCase("null") || responseStr.equals("")) {
                    responseStr="";
                } else {
                    responseStr = responseStr.replaceAll("[(]+I[)]+","<i>");
                    responseStr = responseStr.replaceAll("[(]+/I[)]+","</i>");
                    responseStr = responseStr.replaceAll("[|]+","</p><p>");
                    responseStr = "<h3>Response</h3><p>"+responseStr+"</p>";
                }

                String impactStr=cleanInput(impactRS.getString("Impact"));
                if (impactStr==null || impactStr.equalsIgnoreCase("null") || impactStr.equals("")) {
                    impactStr="";
                } else {
                    impactStr = impactStr.replaceAll("[(]+I[)]+","<i>");
                    impactStr = impactStr.replaceAll("[(]+/I[)]+","</i>");
                    impactStr = impactStr.replaceAll("[|]+","</p><p>");
                    impactStr = "<h3>Impact</h3><p>"+impactStr+"</p>";
                }

                // we want 2 versions of funding sources -- one formatted for display as part of the entity description
                // and another to parse via the | separator character between entries for creating links to funding sources tabs
                String fundingSourcesStr=cleanInput(impactRS.getString("FundingSources"));
                String fundingSourcesHtmlStr=null;
                if (fundingSourcesStr==null || fundingSourcesStr.equalsIgnoreCase("null") || fundingSourcesStr.equals("")) {
                    fundingSourcesStr=null;
                    fundingSourcesHtmlStr="";
                } else {
                    if (fundingSourcesList==null) {
                        fundingSourcesList=new ArrayList();
                    }
                    StringTokenizer pTokens = new StringTokenizer(fundingSourcesStr,"|");
                    int phraseCount = pTokens.countTokens();
                    int nonNullTokenCount=0;
                    for ( int p=0; p<phraseCount; p++ ) {
                        String tokenStr=pTokens.nextToken().trim();
                        if (tokenStr != null && !tokenStr.equals("") && !tokenStr.equals("0")) {
                            ++nonNullTokenCount;
                            boolean foundMatch=false;
                            if (fundingSourcesList.size()>0) {
                                Iterator iter = fundingSourcesList.iterator();
                                while ( iter.hasNext() ) {
                                    String matchStr = (String)iter.next();
                                    if (matchStr.equalsIgnoreCase(tokenStr)) {
                                        foundMatch=true;
                                        break;
                                    }
                                }

                            }
                            if (!foundMatch) {
                                fundingSourcesList.add(tokenStr);
                            }
                            fundingSourcesHtmlStr= nonNullTokenCount==1 ? "<h3>Funding Sources</h3><ul><li>"+tokenStr+"</li>" : fundingSourcesHtmlStr+"<li>"+tokenStr+"</li>";
                            fundingSourcesStr= nonNullTokenCount==1 ? tokenStr : fundingSourcesStr + "|" + tokenStr;
                        }
                    }
                    // don't close the list yet -- may be other funding sources
                    // fundingSourcesHtmlStr+="</ul>";
                }

                String otherSourcesStr=cleanInput(impactRS.getString("OtherFundingSources"));
                if (otherSourcesStr==null || otherSourcesStr.equalsIgnoreCase("null") || "none.".equalsIgnoreCase(otherSourcesStr) || otherSourcesStr.equals("")) {
                    otherSourcesStr="";
                } else {
                    if (otherSourcesList==null) {
                        otherSourcesList=new ArrayList();
                    }
                    StringTokenizer pTokens = new StringTokenizer(otherSourcesStr,",");
                    int phraseCount = pTokens.countTokens();
                    int nonNullTokenCount=0;
                    for ( int p=0; p<phraseCount; p++ ) {
                        String tokenStr=pTokens.nextToken().trim();
                        if (tokenStr != null && !tokenStr.equals("") && !tokenStr.equals("0") && !tokenStr.equalsIgnoreCase("n/a") && !tokenStr.equalsIgnoreCase("none") && !tokenStr.equalsIgnoreCase("No")) {
                            ++nonNullTokenCount;
                            boolean foundMatch=false;
                            if (otherSourcesList.size()>0) {
                                Iterator iter = otherSourcesList.iterator();
                                while ( iter.hasNext() ) {
                                    String matchStr = (String)iter.next();
                                    if (matchStr.equalsIgnoreCase(tokenStr)) {
                                        foundMatch=true;
                                        break;
                                    }
                                }

                            }
                            if (!foundMatch) {
                                otherSourcesList.add(tokenStr);
                            }
                            otherSourcesStr= nonNullTokenCount==1 ? "<li>"+tokenStr+"</li>" : otherSourcesStr+"<li>"+tokenStr+"</li>";
                        }
                    }
                }

                if (fundingSourcesHtmlStr!=null && !fundingSourcesHtmlStr.equals("")) {
                    if (otherSourcesStr==null || otherSourcesStr.equals("") && !otherSourcesStr.equals("0")) {
                        fundingSourcesHtmlStr+="</ul>";
                    } else {
                        fundingSourcesHtmlStr+=otherSourcesStr+"</ul>";
                    }
                }

                String collaboratorsStr = null;
                int collabCount=0;
                for (int i=1; i<11; i++) {
                    String indivStr = cleanInput(impactRS.getString("Collaborator"+String.valueOf(i)));
                    if (indivStr!=null && !indivStr.equalsIgnoreCase("null") && !indivStr.equals("")) {
                        if (collaboratorsList==null) {
                            collaboratorsList=new ArrayList();
                        }
                        ++collabCount;
                        boolean foundMatch=false;
                        if (collaboratorsList.size()>0) {
                            Iterator iter = collaboratorsList.iterator();
                            while ( iter.hasNext() ) {
                                String matchStr = (String)iter.next();
                                if (matchStr.equalsIgnoreCase(indivStr)) {
                                    foundMatch=true;
                                    break;
                                }
                            }

                        }
                        if (!foundMatch) {
                            collaboratorsList.add(indivStr);
                        }
                        collaboratorsStr= collabCount==1 ? "<h3>Collaborators</h3><ul><li>"+indivStr+"</li>" : collaboratorsStr+"<li>"+indivStr+"</li>";
                    }
                }
                if (collabCount>0 && collaboratorsStr!=null) {
                    collaboratorsStr+="</ul>";
                }

                String keyPersonnelStr=null;
                int personnelCount=0;
                for (int j=1; j<11; j++) {
                    String indivStr = cleanInput(impactRS.getString("Name"+String.valueOf(j)));
                    if (indivStr!=null && !indivStr.equalsIgnoreCase("null") && !indivStr.equals("")) {
                        if (keyPersonnelList==null) {
                            keyPersonnelList=new ArrayList();
                        }
                        ++personnelCount;
                        boolean foundMatch=false;
                        if (keyPersonnelList.size()>0) {
                            Iterator iter = keyPersonnelList.iterator();
                            while ( iter.hasNext() ) {
                                String matchStr = (String)iter.next();
                                if (matchStr.equalsIgnoreCase(indivStr)) {
                                    foundMatch=true;
                                    break;
                                }
                            }

                        }
                        if (!foundMatch) {
                            keyPersonnelList.add(indivStr);
                        }
                        keyPersonnelStr= personnelCount==1 ? "<h3>Key Personnel</h3><ul><li>"+indivStr+"</li>" : keyPersonnelStr+"<li>"+indivStr+"</li>";
                    }
                }
                if (personnelCount>0 && keyPersonnelStr!=null) {
                    keyPersonnelStr+="</ul>";
                }


                /**** NOW finally test to see if this statement already exists ****/
                Statement vivoStmt = vivoCon.createStatement();
                int entityId=-1;
                String newEntityName=null;
                String existingEntityQuery="SELECT id,name FROM entities "
                                          +"WHERE vClassId="+IMPACT_STATEMENT_VCLASS_ID+" AND name like '"+titleStr+"'";
                try {
                    ResultSet entityRS=vivoStmt.executeQuery(existingEntityQuery);
                    int entityByTitleCount=0;
                    String entityName=null;
                    while (entityRS.next()) {
                        ++entityByTitleCount;
                        entityId=entityRS.getInt(1);
                        entityName=entityRS.getString(2);
                    }
                    entityRS.close();

                    /**** if doesn't exist, insert new entity ****/
                    switch (entityByTitleCount) {
                        case 0: break;
                        case 1: System.out.println("impact statement "+impactCount+" for "+netIdStr+" matches title of entity "+entityId+"; last year's will be archived");
                                out.println("<p>impact statement "+impactCount+" for "+netIdStr+" matches title of entity "+entityId+"; last year's will be archived</p>");
                                if (doInserts) {
                                    String archiveErrorMessage=archiveEntity(vivoCon,entityId);
                                    if (archiveErrorMessage!=null) {
                                        System.out.println(archiveErrorMessage);
                                        out.println("<p>"+archiveErrorMessage+"</p>");
                                    }
                                }
                                entityId=0;
                                break;
                        default: System.out.println("Error: found " + entityByTitleCount + " matches by title for "+titleStr);
                                 out.println("Error: found " + entityByTitleCount + " matches by title for "+titleStr);
                                 entityId=-1;
                    }
                    if (doInserts && entityId==0) {
                        String insertQuery="INSERT INTO entities (name,vClassId,moniker,description,sunrise,timekey,sunset,citation,statusId,flag1Set,flag2Set,flag3Set) VALUES ('";
                        insertQuery+=titleStr+"',"+IMPACT_STATEMENT_VCLASS_ID+",'impact statement','";
                        insertQuery+=abstractStr+issueStr+responseStr+impactStr+fundingSourcesHtmlStr;
                      //merged up above so part of same bulleted list:
                      //if (otherSourcesStr!=null  && !otherSourcesStr.equals(""))           { insertQuery+=otherSourcesStr;      }
                        if (topicDescriptionsStr!=null && !topicDescriptionsStr.equals(""))  { insertQuery+=topicDescriptionsStr; }
                        if (collaboratorsStr!=null && !collaboratorsStr.equals(""))          { insertQuery+=collaboratorsStr;     }
                        if (keyPersonnelStr!=null  && !keyPersonnelStr.equals(""))           {
                            //insertQuery+="<h3>Key Personnel</h3><p><i>Note: key personnel will be posted here once processed to remove personal contact information.</i></p>";
                            insertQuery+=keyPersonnelStr;
                        }
                        insertQuery+="','2006-03-01',now(),'2007-08-31','submitted as part of CALS annual faculty reporting, February 2006',"+NEW_ENTITY_STATUS_ID+","+impactsFlag1Numeric+",'CALS',"+facultyCampusFlag+")";
                        try {
                            impactsInsertedCount+=vivoStmt.executeUpdate(insertQuery);
                            String maxQuery="SELECT MAX(id) FROM entities";
                            try {
                                ResultSet maxRS=vivoStmt.executeQuery(maxQuery);
                                if (maxRS.next()) {
                                    entityId=maxRS.getInt(1);
                                    String nameQuery="SELECT name FROM entities WHERE id="+entityId;
                                    try {
                                        ResultSet newRS=vivoStmt.executeQuery(nameQuery);
                                        if (newRS.next()) {
                                            newEntityName=newRS.getString(1);
                                            partialOut.println(impactCount+"\t"+netIdStr+"\t"+entityId+"\t"+newEntityName+"\t"+keyPersonnelStr);
                                            partialOut.flush();
                                        }
                                    } catch (SQLException ex) {
                                        System.out.println ("SQLException from retrieving new impact statement entity name id via " + nameQuery + ": " + ex.getMessage());
                                        return;
                                    }
                                }
                                maxRS.close();
                            } catch (SQLException ex) {
                                System.out.println ("SQLException from retrieving new impact statement entity id via " + maxQuery + ": " + ex.getMessage());
                                return;
                            }
                        } catch (SQLException ex) {
                            System.out.println ("SQLException from inserting new impact statement via " + insertQuery + ": " + ex.getMessage());
                            return;
                        }
                    } else {
                        ++impactsInsertedCount;

                        out.println("<h3>"+titleStr+"</h3>");
                        out.println(abstractStr+issueStr+responseStr+impactStr+fundingSourcesHtmlStr);
                        if (otherSourcesStr!=null  && !otherSourcesStr.equals(""))           { out.println(otherSourcesStr);      }
                        if (topicDescriptionsStr!=null && !topicDescriptionsStr.equals(""))  { out.println(topicDescriptionsStr); }
                        if (collaboratorsStr!=null && !collaboratorsStr.equals(""))          { out.println(collaboratorsStr);     }
                        if (keyPersonnelStr!=null  && !keyPersonnelStr.equals(""))           { out.println(keyPersonnelStr);      }
                        out.println("<p>Flag1Set "+impactsFlag1Numeric+"</p>");
                        entityId=impactCount;
                        partialOut.println(impactCount+"\t"+netIdStr+"\t"+entityId+"\t"+titleStr+"\t"+keyPersonnelStr);
                    }

                } catch (SQLException ex) {
                    System.out.println ("SQLException from finding existing entity via " + existingEntityQuery + ": " + ex.getMessage());
                    return;
                }
                if (entityId>0) { // now go on to create relationships to other records and to tabs
                    // link to submitter's entity
                    if (facultyEntityId>0) {
                        if (doInserts) {
                            String insertQuery="INSERT INTO ents2ents (domainId,rangeId,propertyId) values ("+entityId+","+facultyEntityId+","+SUBMITTER_PROPERTY_ID+")";
                            try {
                                submitterLinkCount+=vivoStmt.executeUpdate(insertQuery);
                            } catch (SQLException ex) {
                                System.out.println("Error inserting relationship to submitter "+facultyEntityId+" via "+insertQuery+": "+ex.getMessage());
                                out.println(       "Error inserting relationship to submitter "+facultyEntityId+" via "+insertQuery+": "+ex.getMessage());
                                return;
                            }
                        } else {
                            ++submitterLinkCount;
                        }
                    }

                    // link to geography entities
                    if (affectedAreasStr!=null && !affectedAreasStr.equals("")) {
                        ArrayList tabList=new ArrayList();
                        StringTokenizer pTokens = new StringTokenizer(affectedAreasStr,"|");
                        int placeCount = pTokens.countTokens();
                        for ( int p=0; p<placeCount; p++ ) {
                            String tokenStr=pTokens.nextToken().trim();
                            if (tokenStr != null && !tokenStr.equals("")) {
                                if (tokenStr.equals("New York")) {
                                    tokenStr = "New York State";
                                } else if (tokenStr.equals("U.S.") || tokenStr.equals("USA") || tokenStr.equals("All fifty states.")
                                            || tokenStr.indexOf("Many of the states")>0) {
                                    tokenStr = "United States";
                                } else if (tokenStr.equals("Northeast U.S.") || tokenStr.equals("Northeast U.S") || tokenStr.equals("Northeast States")) {
                                    tokenStr = "northeastern U.S.";
                                } else if (tokenStr.equals("UK") || tokenStr.equals("U.K.")) {
                                    tokenStr = "United Kingdom";
                                } else if (tokenStr.equals("Global") || tokenStr.equals("World") || tokenStr.equals("International")) {
                                    tokenStr = "international";
                                } else if (tokenStr.equalsIgnoreCase("Washington State")) {
                                    tokenStr = "Washington";
                                } else if (tokenStr.equalsIgnoreCase("Washington, D.C.")) {
                                    tokenStr = "Washington D.C.";
                                } else if (tokenStr.equalsIgnoreCase("Pennsylvania.")) {
                                    tokenStr = "Pennsylvania";
                                }
                                String placeQuery="SELECT entities.id,entities.name,tempflag FROM entities WHERE entities.name like '"+tokenStr+"'";
                                try {
                                    ResultSet placeRS=vivoStmt.executeQuery(placeQuery);
                                    int placeEntityId=-1, propertyId=-1;
                                    String placeName=null;
                                    if (placeRS.next()) {
                                        placeEntityId     = placeRS.getInt(1);
                                        placeName         = placeRS.getString(2);
                                        int geoTabId      = placeRS.getInt(3);
                                        placeRS.close();
                                        int projectsTabId=0;
                                        switch (geoTabId) {
                                            //case 0:   break;
                                            case 389: projectsTabId=429; break; // New York State (general)
                                            case 390: projectsTabId=428; break; // United States (general)
                                            case 391: projectsTabId=430; break; // international (general)
                                            case 392: projectsTabId=392; break; // Ithaca and Tompkins County -- no projects tab
                                            case 393: projectsTabId=393; break; // Metropolitan NYC          -- no projects tab
                                            case 394: projectsTabId=394; break; // other upstate counties -- no projects tab
                                            case 395: projectsTabId=395; break; // northeastern U.S.  -- no projects tab
                                            case 396: projectsTabId=396; break; // midwestern U.S. -- no projects tab
                                            case 397: projectsTabId=397; break; // southern U.S. -- no projects tab
                                            case 398: projectsTabId=398; break; // western U.S. -- no projects tab
                                            case 399: projectsTabId=399; break; // Europe  -- no projects tab
                                            case 400: projectsTabId=400; break; // Canada  -- no projects tab
                                            case 401: projectsTabId=401; break; // Latin America -- no projects tab
                                            case 402: projectsTabId=402; break; // Africa  -- no projects tab
                                            case 403: projectsTabId=403; break; // Asia   -- no projects tab
                                            default:  System.out.println("error: unexpected geo tab id "+geoTabId+" for location entity "+placeName);
                                        }
                                        if (projectsTabId>0) {
                                            if (tabList.size()>0) {
                                                boolean tabFound=false;
                                                Iterator iter=tabList.iterator();
                                                while (iter.hasNext()) {
                                                    String alreadyTab=(String)iter.next();
                                                    if (Integer.parseInt(alreadyTab)==projectsTabId) {
                                                        tabFound=true;
                                                        break;
                                                    }
                                                }
                                                if (!tabFound) {
                                                    tabList.add(String.valueOf(projectsTabId));
                                                }
                                            } else {
                                                tabList.add(String.valueOf(projectsTabId));
                                            }
                                        }
                                    } else {
                                        // / save the placename since we don't have it on the db.
                                        boolean foundPlace=false;
                                        if (missingPlacesList.size()>0) {
                                            Iterator placeIter = missingPlacesList.iterator();
                                            while ( placeIter.hasNext() && !foundPlace) {
                                                String existingPlace = (String)placeIter.next();
                                                if (existingPlace.equalsIgnoreCase(tokenStr)) {
                                                    foundPlace=true;
                                                    tokenStr=existingPlace;
                                                    //break;
                                                }
                                            }
                                            if (!foundPlace) {
                                                missingPlacesList.add(tokenStr);
                                            }
                                        } else {
                                            missingPlacesList.add(tokenStr);
                                        }
                                        ++placeInsertCount;
                                    }
                                    if (placeEntityId>0) {
                                        if (doInserts) {
                                            Statement checkStmt=vivoCon.createStatement();
                                            String alreadyThereQuery="SELECT id FROM ents2ents WHERE domainId="+entityId+" AND rangeId="+placeEntityId+" AND propertyId="+AFFECTED_GEOGRAPHICAL_REGION_PROPERTY_ID;
                                            try {
                                                ResultSet alreadyLinkedRS=checkStmt.executeQuery(alreadyThereQuery);
                                                if (alreadyLinkedRS.next()) {
                                                    ++alreadyLinkedCount;
                                                    int ents2entsId=alreadyLinkedRS.getInt(1);
                                                    //System.out.println("Already link between impact line "+lineCount+" (entity "+entityId+") and "+placeName+" (entity "+placeEntityId+") via ents2ents "+alreadyLinkedRS.getInt(1));
                                                    out.println("<br/>Already link between impact record "+impactCount+" (entity "+entityId+") and "+placeName+" (entity "+placeEntityId+") via ents2ents "+alreadyLinkedRS.getInt(1));
                                                } else {
                                                    String insertQuery="INSERT INTO ents2ents (domainId,rangeId,propertyId) values ("+entityId+","+placeEntityId+","+AFFECTED_GEOGRAPHICAL_REGION_PROPERTY_ID+")";
                                                    try {
                                                        geographyLinkCount+=checkStmt.executeUpdate(insertQuery);
                                                    } catch (SQLException ex) {
                                                        System.out.println("Error inserting relationship to place "+placeEntityId+" via "+insertQuery+": "+ex.getMessage());
                                                        out.println(       "Error inserting relationship to place "+placeEntityId+" via "+insertQuery+": "+ex.getMessage());
                                                        return;
                                                    }
                                                }
                                                alreadyLinkedRS.close();
                                            } catch (SQLException ex) {
                                                System.out.println("Error testing whether relationship to place "+placeEntityId+" via "+alreadyThereQuery+" already exists: "+ex.getMessage());
                                                out.println(       "Error testing whether relationship to place "+placeEntityId+" via "+alreadyThereQuery+" already exists: "+ex.getMessage());
                                                return;
                                            }
                                            checkStmt.close();
                                        } else {
                                            ++geographyLinkCount;
                                        }
                                    } else {
                                        //System.out.println("Unexpected error condition -- placeEntityId "+placeEntityId+" when preparing to link new impact statement to place");
                                        //out.println(       "Unexpected error condition -- placeEntityId "+placeEntityId+" when preparing to link new impact statement to place");
                                        //return;
                                    }
                                } catch (SQLException ex) {
                                    System.out.println("Error retrieving place "+tokenStr+" via "+placeQuery+": "+ex.getMessage());
                                    out.println(       "Error retrieving place "+tokenStr+" via "+placeQuery+": "+ex.getMessage());
                                    return;
                                }
                            }
                        } // end if (p<placeCount)
                        if (tabList.size()>0) {
                            Iterator iter=tabList.iterator();
                            while (iter.hasNext()) {
                                int tabId=Integer.parseInt((String)iter.next());
                                if (doInserts) {
                                    String insertQuery="INSERT INTO tabs2ents(entId,tabId) VALUES ("+entityId+","+tabId+")";
                                    try {
                                        geographyTabCount+=vivoStmt.executeUpdate(insertQuery);
                                    } catch (SQLException ex) {
                                        System.out.println("Error inserting relationship to place tab "+tabId+" via "+insertQuery+": "+ex.getMessage());
                                        out.println(       "Error inserting relationship to place tab "+tabId+" via "+insertQuery+": "+ex.getMessage());
                                        return;
                                    }
                                } else {
                                    ++geographyTabCount;
                                }
                            }
                        }
                    } // end linking to geographic entities

                    /* link to objectivesLinkCount=0 and objective tab
                    if (univObjectivesStr!=null && !univObjectivesStr.equals("")) {
                        StringTokenizer uTokens = new StringTokenizer(univObjectivesStr,",");
                        int uCount = uTokens.countTokens();
                        for ( int u=0; u<uCount; u++ ) {
                            String tokenStr=uTokens.nextToken().trim();
                            if (tokenStr != null && !tokenStr.equals("")) {
                                //int objectiveEntityId=-1;
                                int tabId=-1;
                                if (tokenStr.equalsIgnoreCase("Life in the age of the genome")) {
                                    //objectiveEntityId=15485;
                                    tabId=385;
                                } else if (tokenStr.equalsIgnoreCase("Sustainability in the age of development")) {
                                    //objectiveEntityId=15562;
                                    tabId=386;
                                } else if (tokenStr.equalsIgnoreCase("Wisdom in the age of digital information")) {
                                    //objectiveEntityId=15715;
                                    tabId=387;
                                }
                                if (tabId>0) {
                                    if (doInserts) {
                                        String insertQuery="INSERT INTO tabs2ents (entId,tabId) values ("+entityId+","+tabId+")";
                                        try {
                                            objectivesTabCount+=vivoStmt.executeUpdate(insertQuery);
                                        } catch (SQLException ex) {
                                            System.out.println("Error inserting relationship to objective "+tokenStr+" via "+insertQuery+": "+ex.getMessage());
                                            out.println(       "Error inserting relationship to objective "+tokenStr+" via "+insertQuery+": "+ex.getMessage());
                                            return;
                                        }
                                    } else {
                                        ++objectivesTabCount;
                                    }
                                } else {
                                    System.out.println("Error: unexpected university objective "+tokenStr);
                                    out.println(       "Error: unexpected university objective "+tokenStr);
                                    return;
                                }
                            }
                        }
                    } */

                    // link to topicAreaTab
                    if (topicAreasStr!=null && !topicAreasStr.equals("")) {
                        StringTokenizer tTokens = new StringTokenizer(topicAreasStr,",");
                        int tCount = tTokens.countTokens();
                        for ( int t=0; t<tCount; t++ ) {
                            String tokenStr=tTokens.nextToken().trim();
                            if (tokenStr != null && !tokenStr.equals("")) {
                                int tabId=-1;
                                if (tokenStr.equalsIgnoreCase("Enhance Economic Opportunities for Agricultural Producers")) {
                                    tabId=321;
                                } else if (tokenStr.equalsIgnoreCase("Enhance Protection and Safety of Agriculture and Food Supply")) {
                                    tabId=322;
                                } else if (tokenStr.equalsIgnoreCase("Improve Nutrition and Health")) {
                                    tabId=325;
                                } else if (tokenStr.equalsIgnoreCase("Protect and Enhance Natural Resource Base and Environment")) {
                                    tabId=323;
                                } else if (tokenStr.equalsIgnoreCase("Society Ready Graduates")) {
                                    tabId=324;
                                } else if (tokenStr.equalsIgnoreCase("Support Increased Economic Opportunities and Improved Quality of Life in Rural America")) {
                                    tabId=382;
                                } else if (tokenStr.equalsIgnoreCase("None of the above")) {
                                    tabId=0;
                                }
                                if (tabId>-1) {
                                    if (tabId==0) {
                                        // do nothing -- no link needed
                                    } else if (doInserts) {
                                        String insertQuery="INSERT INTO tabs2ents (entId,tabId) values ("+entityId+","+tabId+")";
                                        try {
                                            topicAreaTabCount+=vivoStmt.executeUpdate(insertQuery);
                                        } catch (SQLException ex) {
                                            System.out.println("Error inserting relationship to tab "+tabId+" via "+insertQuery+": "+ex.getMessage());
                                            out.println(       "Error inserting relationship to tab "+tabId+" via "+insertQuery+": "+ex.getMessage());
                                            return;
                                        }
                                    } else {
                                        ++topicAreaTabCount;
                                    }
                                } else {
                                    System.out.println("Error: unexpected topic area "+tokenStr);
                                    out.println(       "Error: unexpected topic area "+tokenStr);
                                    return;
                                }
                            }
                        }
                    }

                    // link to fundingSourceTabCount=0;
                    if (fundingSourcesStr!=null && !fundingSourcesStr.equals("")) {
                        StringTokenizer fTokens = new StringTokenizer(fundingSourcesStr,"|");
                        int fCount = fTokens.countTokens();
                        for ( int f=0; f<fCount; f++ ) {
                            String tokenStr=fTokens.nextToken().trim();
                            if (tokenStr != null && !tokenStr.equals("")) {
                                int tabId=-1;
                                if (tokenStr.indexOf("Academic Programs Instructional Support")==0) {
                                    tabId=368;
                                } else if (tokenStr.indexOf("Federal Formula Funds - Extension")==0) {
                                    tabId=369;
                                } else if (tokenStr.indexOf("Federal Formula Funds - Research")==0) {
                                    tabId=370;
                                } else if (tokenStr.indexOf("Other Federal")==0) {
                                    tabId=374;
                                } else if (tokenStr.indexOf("Other USDA")==0) {
                                    tabId=375;
                                } else if (tokenStr.indexOf("Private")==0) {
                                    tabId=383;
                                } else if (tokenStr.indexOf("State or Municipal")==0) {
                                    tabId=404;
                                }
                                if (tabId>0) {
                                    if (doInserts) {
                                        String insertQuery="INSERT INTO tabs2ents (entId,tabId) values ("+entityId+","+tabId+")";
                                        try {
                                            fundingSourceTabCount+=vivoStmt.executeUpdate(insertQuery);
                                        } catch (SQLException ex) {
                                            System.out.println("Error inserting relationship to tab "+tabId+" via "+insertQuery+": "+ex.getMessage());
                                            out.println(       "Error inserting relationship to tab "+tabId+" via "+insertQuery+": "+ex.getMessage());
                                            return;
                                        }
                                    } else {
                                        ++fundingSourceTabCount;
                                    }
                                } else {
                                    System.out.println("Error: unexpected funding source "+tokenStr);
                                    out.println(       "Error: unexpected funding source "+tokenStr);
                                    return;
                                }
                            }
                        }
                    }

                    // link to keywords
                    if (projectTypesStr!=null && !projectTypesStr.equals("")) {
                        StringTokenizer ptTokens = new StringTokenizer(projectTypesStr,",");
                        int ptCount = ptTokens.countTokens();
                        for ( int pt=0; pt<ptCount; pt++ ) {
                            String tokenStr=ptTokens.nextToken().trim();
                            if (tokenStr != null && !tokenStr.equals("")) {
                                int firstKeytermId=-1,secondKeytermId=-1,tabId=-1;
                                if (tokenStr.equalsIgnoreCase("Extension/Outreach")) {
                                    firstKeytermId  = 659;
                                    secondKeytermId = 1718;
                                    tabId=407;
                                } else if (tokenStr.indexOf("Research")>=0) {
                                    if (tokenStr.equalsIgnoreCase("Research")) {
                                        firstKeytermId = 1352;
                                    } else if (tokenStr.equalsIgnoreCase("Applied Research")){
                                        firstKeytermId = 573;
                                    } else if (tokenStr.equalsIgnoreCase("Basic Research")){
                                        firstKeytermId = 2456;
                                    }
                                    tabId=406;
                                } else if (tokenStr.equalsIgnoreCase("Teaching")) {
                                    firstKeytermId  = 89;
                                    tabId=408;
                                }
                                if (firstKeytermId>0) {
                                    if (doInserts) {
                                        String insertQuery="INSERT INTO keys2ents (entId,keyId) values ("+entityId+","+firstKeytermId+")";
                                        try {
                                            keytermLinkCount+=vivoStmt.executeUpdate(insertQuery);
                                        } catch (SQLException ex) {
                                            System.out.println("Error inserting relationship to keyterm "+firstKeytermId+" via "+insertQuery+": "+ex.getMessage());
                                            out.println(       "Error inserting relationship to keyterm "+firstKeytermId+" via "+insertQuery+": "+ex.getMessage());
                                            return;
                                        }
                                    } else {
                                        ++keytermLinkCount;
                                    }
                                } else {
                                    System.out.println("Error: unexpected project type "+tokenStr);
                                    out.println(       "Error: unexpected project type "+tokenStr);
                                    return;
                                }
                                if (secondKeytermId>0) {
                                    if (doInserts) {
                                        String insertQuery="INSERT INTO keys2ents (entId,keyId) values ("+entityId+","+secondKeytermId+")";
                                        try {
                                            keytermLinkCount+=vivoStmt.executeUpdate(insertQuery);
                                        } catch (SQLException ex) {
                                            System.out.println("Error inserting relationship to keyterm "+secondKeytermId+" via "+insertQuery+": "+ex.getMessage());
                                            out.println(       "Error inserting relationship to keyterm "+secondKeytermId+" via "+insertQuery+": "+ex.getMessage());
                                            return;
                                        }
                                    } else {
                                        ++keytermLinkCount;
                                    }
                                }
                                if (tabId>0) {
                                    if (doInserts) {
                                        String insertQuery="INSERT INTO tabs2ents (entId,tabId) values ("+entityId+","+tabId+")";
                                        try {
                                            projectTypeTabCount+=vivoStmt.executeUpdate(insertQuery);
                                        } catch (SQLException ex) {
                                            System.out.println("Error inserting relationship to tab "+tabId+" via "+insertQuery+": "+ex.getMessage());
                                            out.println(       "Error inserting relationship to tab "+tabId+" via "+insertQuery+": "+ex.getMessage());
                                            return;
                                        }
                                    } else {
                                        ++projectTypeTabCount;
                                    }
                                } else {
                                    System.out.println("Error: unexpected funding source "+tokenStr);
                                    out.println(       "Error: unexpected funding source "+tokenStr);
                                    return;
                                }
                            }
                        }
                    }

                    if (portalPreferences!=null && !portalPreferences.equals("")) {
                        StringTokenizer ppTokens = new StringTokenizer(portalPreferences,",");
                        int ppCount = ppTokens.countTokens();
                        for ( int pp=0; pp<ppCount; pp++ ) {
                            String tokenStr=ppTokens.nextToken().trim();
                            if (tokenStr != null && !tokenStr.equals("")) {
                                int keytermId=-1;
                                if (tokenStr.equalsIgnoreCase("Applied Social Sciences")) {
                                    keytermId  = 2430;
                                } else if (tokenStr.equalsIgnoreCase("Environmental Sciences")) {
                                    keytermId  = 437;
                                } else if (tokenStr.equalsIgnoreCase("Land-Grant Mission")) {
                                    keytermId  = 319;
                                } else if (tokenStr.equalsIgnoreCase("New Life Sciences")) {
                                    keytermId  = 2429;
                                }
                                if (keytermId>0) {
                                    if (doInserts) {
                                        String insertQuery="INSERT INTO keys2ents (entId,keyId) values ("+entityId+","+keytermId+")";
                                        try {
                                            priorityLinkCount+=vivoStmt.executeUpdate(insertQuery);
                                        } catch (SQLException ex) {
                                            System.out.println("Error inserting relationship to keyterm "+keytermId+" via "+insertQuery+": "+ex.getMessage());
                                            out.println(       "Error inserting relationship to keyterm "+keytermId+" via "+insertQuery+": "+ex.getMessage());
                                            return;
                                        }
                                    } else {
                                        ++priorityLinkCount;
                                    }
                                } else {
                                    System.out.println("Error: unexpected priority area "+tokenStr);
                                    out.println(       "Error: unexpected priority area "+tokenStr);
                                    return;
                                }
                            }
                        }
                    }
                    // College objectives tabs      // keyterm
                    // Environmental Sciences  424  // 437
                    // New Life Sciences       422  // 2429
                    // Applied Social Sciences 423  // 2430
                    // Land-Grant Mission      425  // 319

                    if (doInserts && calsObjectivesTabList!=null && calsObjectivesTabList.size()>0) {
                        Iterator calsIter=calsObjectivesTabList.iterator();
                        while (calsIter.hasNext()) {
                            String calsTabIdStr=(String)calsIter.next();
                            if (calsTabIdStr!=null && !calsTabIdStr.equals("")) {
                                String insertQuery="INSERT INTO tabs2ents (tabId,entId) values ('"+calsTabIdStr+"','"+entityId+"')";
                                try {
                                    calsTabsInserted+=vivoStmt.executeUpdate(insertQuery);
                                } catch (SQLException ex) {
                                    System.out.println("Error inserting relationship to cals objectives tab "+calsTabIdStr+" via "+insertQuery+": "+ex.getMessage());
                                    out.println(       "Error inserting relationship to cals objectives tab "+calsTabIdStr+" via "+insertQuery+": "+ex.getMessage());
                                    return;
                                }
                            }
                        }
                    }

                } // end if entityId>0
                vivoStmt.close();
                if (impactCount%10==0) {
                    System.out.println("processed statement "+impactCount);
                }
            }
        }
        impactRS.close();
        impactStmt.close();

        System.out.println("inserted "+impactsInsertedCount+" impacts, "+submitterLinkCount+" submitter links, "+placeInsertCount+" new places inserted, "+geographyLinkCount+" place links, "+geographyTabCount+" place tab links\n");
        out.println(       "inserted "+impactsInsertedCount+" impacts, "+submitterLinkCount+" submitter links, "+placeInsertCount+" new places inserted, "+geographyLinkCount+" place links, "+geographyTabCount+" place tab links\n");
        System.out.println("inserted "+objectivesTabCount+" objective tab links, "+topicAreaTabCount+" topic tab links, "+fundingSourceTabCount+" funding tab links, "+keytermLinkCount+" keyword links, "+priorityLinkCount+" priority links, "+projectTypeTabCount+" project tab links");
        out.println(       "inserted "+objectivesTabCount+" objective tab links, "+topicAreaTabCount+" topic tab links, "+fundingSourceTabCount+" funding tab links, "+keytermLinkCount+" keyword links, "+priorityLinkCount+" priority links, "+projectTypeTabCount+" project tab links");
        System.out.println("found "+alreadyLinkedCount+" existing place entity links; inserted "+calsTabsInserted);
        out.println("<br/>found "+alreadyLinkedCount+" existing place entity links; inserted "+calsTabsInserted);

        if (projectTypesList != null && projectTypesList.size()>0) {
            if (projectTypesList.size() > 1 ) {
                out.println("<h4>PROJECT TYPES ("+projectTypesList.size()+"):</h4><ul>");
                Collections.sort( projectTypesList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = projectTypesList.iterator();
            while (iter.hasNext()) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>No project types list entries found.</p>");
        }

        if (portalsList != null && portalsList.size()>0) {
            if (portalsList.size() > 1 ) {
                out.println("<h4>ACADEMIC PRIORITY AREAS ("+portalsList.size()+"):</h4><ul>");
                Collections.sort( portalsList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
        /*  for printing wiki tables:
            int columnCount=5;
            int columnIndex=0; */
            Iterator portalsIter = portalsList.iterator();
            while ( portalsIter.hasNext() ) {
            /*  ++columnIndex; */
                String priority = (String)portalsIter.next();
                out.println("<li>"+priority+"</li>");
            /*  out.print("|"+keywd);
                if (columnIndex % columnCount==0) {
                    out.println("|");
                } */
            }
            out.println("</ul>");
        } else {
            out.println("<p>No academic priority areas list entries found.</p>");
        }

        if (geographyList != null && geographyList.size()>0) {
            if (geographyList.size() > 1 ) {
                out.println("<h4>PLACES ("+geographyList.size()+"):</h4><ul>");
                Collections.sort( geographyList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first=(String)obj1;
                        String second=(String)obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = geographyList.iterator();
            while ( iter.hasNext() ) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>no places list entries found.</p>");
        }

        if (missingPlacesList != null && missingPlacesList.size()>0) {
            if (missingPlacesList.size() > 1 ) {
                out.println("<h4>MISSING PLACES ("+missingPlacesList.size()+"):</h4><ul>");
                Collections.sort( missingPlacesList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first=(String)obj1;
                        String second=(String)obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = missingPlacesList.iterator();
            while ( iter.hasNext() ) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println();
        } else {
            out.println("<p>no missing places list entries found</p>");
        }

        if (objectivesList != null && objectivesList.size()>0) {
            if (objectivesList.size() > 1 ) {
                out.println("<h4>UNIVERSITY OBJECTIVES ("+objectivesList.size()+"):</h4><ul>");
                Collections.sort( objectivesList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = objectivesList.iterator();
            while (iter.hasNext()) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>No university objectives list entries found.</p>");
        }

        if (topicAreasList != null && topicAreasList.size()>0) {
            if (topicAreasList.size() > 1 ) {
                out.println("<h4>TOPIC AREAS ("+topicAreasList.size()+"):</h4><ul>");
                Collections.sort( topicAreasList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = topicAreasList.iterator();
            while (iter.hasNext()) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>No topic areas list found.</p>");
        }

        if (topicDescriptionsList != null && topicDescriptionsList.size()>0) {
            if (topicDescriptionsList.size() > 1 ) {
                out.println("<h4>TOPIC DESCRIPTIONS ("+topicDescriptionsList.size()+"):</h4><ul>");
                Collections.sort( topicDescriptionsList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = topicDescriptionsList.iterator();
            while (iter.hasNext()) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>No topic descriptions list entries found.</p>");
        }

        if (fundingSourcesList != null && fundingSourcesList.size()>0) {
            if (fundingSourcesList.size() > 1 ) {
                out.println("<h4>FUNDING SOURCES ("+fundingSourcesList.size()+"):</h4><ul>");
                Collections.sort( fundingSourcesList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = fundingSourcesList.iterator();
            while (iter.hasNext()) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>No funding source list entries found.</p>");
        }

        if (otherSourcesList != null && otherSourcesList.size()>0) {
            if (otherSourcesList.size() > 1 ) {
                out.println("<h4>OTHER FUNDING SOURCES ("+otherSourcesList.size()+"):</h4><ul>");
                Collections.sort( otherSourcesList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = otherSourcesList.iterator();
            while (iter.hasNext()) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>No 'other funding source' list entries found.</p>");
        }

        if (collaboratorsList != null && collaboratorsList.size()>0) {
            if (collaboratorsList.size() > 1 ) {
                out.println("<h4>COLLABORATORS ("+collaboratorsList.size()+"):</h4><ul>");
                Collections.sort( collaboratorsList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = collaboratorsList.iterator();
            while (iter.hasNext()) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>No collaborator list entries found.</p>");
        }

        if (keyPersonnelList != null && keyPersonnelList.size()>0) {
            if (keyPersonnelList.size() > 1 ) {
                out.println("<h4>KEY PERSONNEL ("+keyPersonnelList.size()+"):</h4><ul>");
                Collections.sort( keyPersonnelList, new Comparator() {
                    public int compare( Object obj1, Object obj2 ) {
                        String first  = (String) obj1;
                        String second = (String) obj2;
                        return first.toLowerCase().compareTo(second.toLowerCase());
                    }
                });
            }
            Iterator iter = keyPersonnelList.iterator();
            while (iter.hasNext()) {
                out.println("<li>"+(String)iter.next()+"</li>");
            }
            out.println("</ul>");
        } else {
            out.println("<p>No key personnel list entries found.</p>");
        }

        /*
        if (deptTreeMap != null && deptTreeMap.size()>0) {
            out.println("DEPARTMENTS ("+deptTreeMap.size()+"):");
            Set keySet=deptTreeMap.keySet();
            Iterator keySetIter=keySet.iterator();
            while (keySetIter.hasNext()) {
                String deptKey = (String)keySetIter.next();
                out.println("|"+deptKey+"|"+deptTreeMap.get(deptKey)+"|");
            }
            out.println();
        } */

        out.flush();
        out.close();
        System.out.println("<p>processed " + impactCount + " impact statement records</p>");
    }

    private static int updateEntityFlags(Connection con,PrintWriter out,int entity_id,int current_flag1,int impacts_flag1,boolean do_updates,String net_id_str,String input_str)
        throws SQLException
    {
        int count=0;

        String updateQuery=null;
        if (current_flag1<0) { //flag1Set+0 is null
            updateQuery="UPDATE entities SET flag1Set="+impacts_flag1+" WHERE id="+entity_id;
        } else {
            int newFlagVal=current_flag1 | impacts_flag1;
            updateQuery="UPDATE entities SET flag1Set="+newFlagVal+" WHERE id="+entity_id;
        }
        if (do_updates) {
            Statement stmt=con.createStatement();
            try {
                count=stmt.executeUpdate(updateQuery);
            } catch (SQLException ex) {
                System.out.println("SQLException in updating entity flag via "+updateQuery+": "+ex.getMessage());
                out.println("SQLException in updating entity flag via "+updateQuery+": "+ex.getMessage());
                stmt.close();
                return 0;
            }
            stmt.close();
        } else {
            out.println("Would be updating entity flag for "+net_id_str+" from "+current_flag1+" to "+impacts_flag1+" ("+input_str+") via "+updateQuery);
        }
        return count;
    }

/*  private static String fixCapitalization(String input_str)
    {
        String outputStr=null;
        StringTokenizer fTokens = new StringTokenizer(input_str," ");
        int fragmentCount=fTokens.countTokens();
        for (int f=0; f<fragmentCount; f++) {
            String fragment=fTokens.nextToken().trim();
            int firstChar=(int)fragment.charAt(0);
            if (firstChar==40 && fragment.length()==3) { //open parenthesis for (I) or (O) terms -- throw out term
                fragment=null;
            } else if (firstChar>64 && firstChar<91) {
                //System.out.println("found uppercase start to " + fragment);
                if (fragment.equalsIgnoreCase("Christmas")) { ;
                } else if (fragment.equalsIgnoreCase("Africa") || fragment.equalsIgnoreCase("African")) { ;
                } else if (fragment.equalsIgnoreCase("Asia")) { ;
                } else if (fragment.equalsIgnoreCase("South") || fragment.equalsIgnoreCase("Southeast")) { ;
                } else if (fragment.equalsIgnoreCase("American")) { ;
                } else if (fragment.equalsIgnoreCase("Adirondack")) { ;
                } else if (fragment.equalsIgnoreCase("Indian")) { ;
                } else if (fragment.equalsIgnoreCase("Bt")) { ;
                } else if (fragment.equalsIgnoreCase("Cuba")) { ;
                } else {
                    if (fragment.length()>1) {
                        int secondChar=(int)fragment.charAt(1);
                        if (secondChar<91) { // catches C. elegans
                            ;
                        } else {
                            fragment=fragment.toLowerCase();
                        }
                    }
                }
            }
            if (f==0 || outputStr==null) {
                outputStr=fragment;
            } else if (fragment != null) {
                outputStr += " " + fragment;
            }
        }
        return outputStr;
    } */

    private static int findEntityViaNetId(Connection con,PrintWriter out,String netIdStr) {
        int entityId=0;
        try {
            Statement stmt=con.createStatement();
            String existingEntityQuery="SELECT entities.id FROM entities,externalIds "
                                      +"WHERE externalIds.entityId=entities.id AND externalIds.externalIdType="+NETID_LINKTYPE+" AND externalIds.value like '" + netIdStr + "@cornell.edu' "
                                      +"GROUP BY entities.id";
            try {
                ResultSet entityRS=stmt.executeQuery(existingEntityQuery);
                int entityByNetIdCount=0;
                while (entityRS.next()) {
                    ++entityByNetIdCount;
                    entityId=entityRS.getInt(1);
                }
                entityRS.close();
                switch (entityByNetIdCount) {
                    case 0: System.out.println("no match found by netId for: "+netIdStr);
                            /*
                            Statement nameStmt=con.createStatement();
                            String nameQuery="SELECT id FROM entities WHERE name like '"+compositeNameStr+"%'";
                            int entityByNameCount=0;
                            try {
                                ResultSet nameRS=nameStmt.executeQuery(nameQuery);
                                while (nameRS.next()) {
                                    entityId=nameRS.getInt(1);
                                    ++entityByNameCount;
                                }
                                nameRS.close();
                            } catch (SQLException ex) {
                                System.out.println ("SQLException from finding existing entity via " + existingEntityQuery + ": " + ex.getMessage());
                                return;
                            }
                            switch (entityByNameCount) {
                                case 0: System.out.println("no match found by name or netId: "+compositeNameStr+" ("+netIdStr+")");
                                        out.println("no match found by name or netId: "+compositeNameStr+" ("+netIdStr+")");
                                        break;
                                case 1: String externalIdInsertQuery="INSERT INTO externalIds (externalIdType,value,entityId) VALUES (101,'"+netIdStr+"@cornell.edu',"+entityId+")";
                                        if (doInserts) {
                                            try {
                                                newExternalIdCount+=nameStmt.executeUpdate(externalIdInsertQuery);
                                            } catch (SQLException ex) {
                                                System.out.println("Error: could not insert a new link for existing entity via " + externalIdInsertQuery + ": " + ex.getMessage());
                                                return;
                                            }
                                        } else {
                                            ++newExternalIdCount;
                                            out.println("would have done net id insert via: " + externalIdInsertQuery);
                                        }
                                        if (keywordStr != null && !keywordStr.equals("") && existingKeytermCount==0) {
                                            impactsUpdatedCount += updateEntityKeywords(con,out,entityId,netIdStr,keywordStr,true,doInserts);
                                        }
                                        updateEntityFlags(con,out,entityId,currentFlag1Numeric,impactsFlag1Numeric,doInserts,netIdStr,portalPreferences);
                                        break;
                                default: System.out.println("Error: found " + entityByNameCount + " matches by name for " + firstName + " " + lastName + " (" + netIdStr + ")");
                                         out.println("Error: found " + entityByNameCount + " matches by netId for " + firstName + " " + lastName + " (" + netIdStr + ")");
                            }
                            nameStmt.close();*/
                            break;
                    case 1: break;
                    default: System.out.println("Error: found " + entityByNetIdCount + " matches by netId for "+netIdStr);
                             out.println("Error: found " + entityByNetIdCount + " matches by netId for "+netIdStr);
                }
            } catch (SQLException ex) {
                System.out.println ("SQLException from finding existing entity via " + existingEntityQuery + ": " + ex.getMessage());
                return 0;
            }
        } catch (SQLException ex) {
            System.out.println ("SQLException from creating statement in findEntityViaNetId(): " + ex.getMessage());
            return 0;
        }
        return entityId;
    }

    private static int getCampusFlag(Connection con,PrintWriter out,int eId) {
        int returnFlag=0;
        Statement stmt=null;
        try {
            stmt=con.createStatement();
            String flagQuery="SELECT flag3Set+0 FROM entities WHERE id="+eId;
            try {
                ResultSet entityRS=stmt.executeQuery(flagQuery);
                if (entityRS.next()) {
                    returnFlag=entityRS.getInt(1);
                }
                entityRS.close();
            } catch (SQLException ex) {
                System.out.println ("SQLException from finding existing entity flag3 via " + flagQuery + ": " + ex.getMessage());
                return 0;
            }
        } catch (SQLException ex) {
            System.out.println ("SQLException from creating statement in findEntityViaNetId(): " + ex.getMessage());
            return 0;
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
                return 0;
            }
        }
        return returnFlag;
    }

    private static String archiveEntity(Connection con, int entity_id) {
        Statement stmt=null;
        try {
            stmt=con.createStatement();
            String query="UPDATE entities SET statusId="+ARCHIVED_ENTITY_STATUS_ID+" WHERE id="+entity_id;
            try {
                stmt.executeUpdate(query);
            } catch (SQLException ex) {
                return "Failed to archive entity: "+ex.getMessage();
            }
        } catch (SQLException ex) {
            return "SQLException from creating statement in archiveEntity(): " + ex.getMessage();
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
                return "Cannot close statement: "+ex.getMessage();
            }
        }
        return null;
    }

    private static String cleanInput( String inputStr ) {
        if (inputStr==null) {
            return null;
        }
        inputStr.trim();
        // for the following refer to Hunter & Crawford's Java Servlet Programming, Appendix D
        inputStr = replaceCharacters( inputStr, 130, "," );    // &#130 weird comma to regular comma
        inputStr = replaceCharacters( inputStr, 131, "F" );    // &#131 florin to capital F
        inputStr = replaceCharacters( inputStr, 132, "&quot;");// &#132 right double quote to named entity
        inputStr = replaceCharacters( inputStr, 133, "..." );  // &#133 ellipsis to 3 periods
        inputStr = replaceCharacters( inputStr, 134, "" );     // &#134 dagger to nothing
        inputStr = replaceCharacters( inputStr, 135, "" );     // &#135 double dagger to nothing
        inputStr = replaceCharacters( inputStr, 136, "" );     // &#136 circumflex to nothing
        inputStr = replaceCharacters( inputStr, 137, "%" );    // &#137 permil to percent
        inputStr = replaceCharacters( inputStr, 138, "S" );    // &#138 capatal S caron to capital S
        inputStr = replaceCharacters( inputStr, 139, "&lt;" ); // &#139 less than sign to named entity
        inputStr = replaceCharacters( inputStr, 140, "OE" );   // &#140 OE ligature to OE
        inputStr = replaceCharacters( inputStr, 145, "'" );    // &#141 left single quote (back tic) to apostrophe
        inputStr = replaceCharacters( inputStr, 146, "'" );    // &#146 right single quote (forward tic) to apostrophe
        inputStr = replaceCharacters( inputStr, 147, "&quot;");// &#147 left or back double tic to named entity
        inputStr = replaceCharacters( inputStr, 148, "&quot;");// &#148 right or forward double tic to named entity
        inputStr = replaceCharacters( inputStr, 149, "*" );    // &#149 bullet to asterisk
        inputStr = replaceCharacters( inputStr, 150, "-" );    // &#150 en dash to single dash
        inputStr = replaceCharacters( inputStr, 151, "--" );   // &#151 em dash to double dash
      //inputStr = replaceCharacters( inputStr, 152, "~" );    // &#152 try leaving tilde as tilde for web addresses
        inputStr = replaceCharacters( inputStr, 153, "[TM]" ); // &#153 trademark
        inputStr = replaceCharacters( inputStr, 154, "s" );    // &#154 small s caron to small s
        inputStr = replaceCharacters( inputStr, 155, "&gt;" ); // &#155 greater than sign to named entity
        inputStr = replaceCharacters( inputStr, 156, "oe" );   // &#156 small OE ligature to oe
        inputStr = replaceCharacters( inputStr, 159, "Y" );    // &#159 Y umlaut to capital Y
        inputStr = replaceCharacters( inputStr, 162, "&#162;" ); // cents
        inputStr = replaceCharacters( inputStr, 176, "&#176;" ); // degrees?
        inputStr = replaceCharacters( inputStr, 186, "&#186;" ); // not sure
        inputStr = replaceCharacters( inputStr, 189, "&#189;" ); // one-half
        inputStr = replaceCharacters( inputStr, 233, "&#233;" ); // e accent ague
        inputStr = replaceCharacters( inputStr, 239, "&#239;" ); // i umlaut

        inputStr = escapeQuotes( inputStr, 34 );
        inputStr = escapeQuotes( inputStr, 39 );
        return inputStr;
    }

    private static String escapeQuotes( String termStr, int whichChar ) {
        if (termStr==null || termStr.equals("")) {
            return termStr;
        }
        int characterPosition= -1;
        // strip leading spaces
        while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
            termStr = termStr.substring(characterPosition+1);
        }
        characterPosition=-1;
        while ( ( characterPosition = termStr.indexOf( whichChar, characterPosition+1 ) ) >= 0 ) {
            if ( characterPosition == 0 ) // just drop it
                termStr = termStr.substring( characterPosition+1 );
            else if ( termStr.charAt(characterPosition-1) != 92 ) // already escaped
                termStr = termStr.substring(0,characterPosition) + "\\" + termStr.substring(characterPosition);
            ++characterPosition;
        }
        return termStr;
    }

    private static String unEscapeQuotes( String termStr ) {
        int characterPosition= -1;

        while ( ( characterPosition = termStr.indexOf( 92, characterPosition+1 ) ) >= 0 ) {
            if ( termStr.charAt( characterPosition+1 )== 34 || termStr.charAt( characterPosition+1 )== 39 ) {
                termStr = termStr.substring(0,characterPosition) + termStr.substring(characterPosition+1);
            }
        }
        return termStr;
    }

    private static String replaceCharacters( String termStr, int removeChar, String replaceStr ) {
        if (termStr==null || termStr.equals("")) {
            return termStr;
        }
        int characterPosition= -1;
        // strip leading spaces
        while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
            termStr = termStr.substring(characterPosition+1);
        }
        characterPosition=-1;
        while ( ( characterPosition = termStr.indexOf( removeChar, characterPosition+1 ) ) >= 0 ) {
            if ( characterPosition == 0 ) {
                termStr = replaceStr + termStr.substring( characterPosition+1 );
            } else {
                termStr = termStr.substring(0,characterPosition) + replaceStr + termStr.substring(characterPosition+1);
            }
            characterPosition += replaceStr.length() + 1;
        }
        return termStr;
    }
/*
    private static String cleanLines(String inputLine) {
        /*** from http://www.regular-expressions.info/quickstart.html
        You can use special character sequences to put non-printable characters in your regular expression.
        Use \t to match a tab character (ASCII 0x09), \r for carriage return (0x0D) and \n for line feed (0x0A).
        More exotic non-printables are \a (bell, 0x07), \e (escape, 0x1B), \f (form feed, 0x0C) and \v (vertical tab, 0x0B).
        Remember that Windows text files use \r\n to terminate lines, while UNIX text files use \n.
        Use \xFF to match a specify character by its hexadecimal index in the character set. E.g. \xA9 matches the copyright symbol in the Latin-1 character set.
        If your regular expression engine supports Unicode, use \uFFFF to insert a Unicode character. E.g. \u20A0 matches the euro currency sign.
        All non-printable characters can be used directly in the regular expression, or as part of a character class.
        *
        System.out.println("starting cleanLines:\n");
        Pattern a = Pattern.compile("\\x80");   Matcher a1 = a.matcher("");   a1.reset(inputLine);   String done = a1.replaceAll("83"); //131
        Pattern b = Pattern.compile("\\x82");   Matcher b1 = b.matcher("");   b1.reset(done);        String done1 = b1.replaceAll("82");
        Pattern c = Pattern.compile("\\x83");   Matcher c1 = c.matcher("");   c1.reset(done1);       String done2 = c1.replaceAll("83");
        Pattern d = Pattern.compile("\\x84");   Matcher d1 = d.matcher("");   d1.reset(done2);       String done3 = d1.replaceAll("84");
        Pattern e = Pattern.compile("\\x85");   Matcher e1 = e.matcher("");   e1.reset(done3);       String done4 = e1.replaceAll("85");
        Pattern f = Pattern.compile("\\x86");   Matcher f1 = f.matcher("");   f1.reset(done4);       String done5 = f1.replaceAll("86");
        Pattern g = Pattern.compile("\\x87");   Matcher g1 = g.matcher("");   g1.reset(done5);       String done6 = g1.replaceAll("87");
        Pattern h = Pattern.compile("\\x88");   Matcher h1 = h.matcher("");   h1.reset(done6);       String done7 = h1.replaceAll("88");
        Pattern i = Pattern.compile("\\x89");   Matcher i1 = i.matcher("");   i1.reset(done7);       String done8 = i1.replaceAll("89");
        Pattern j = Pattern.compile("\\x8a");   Matcher j1 = j.matcher("");   j1.reset(done8);       String done9 = j1.replaceAll("8a");
        Pattern k = Pattern.compile("\\x8b");   Matcher k1 = k.matcher("");   k1.reset(done9);       String done10= k1.replaceAll("8b");
        Pattern l = Pattern.compile("\\x8c");   Matcher l1 = l.matcher("");   l1.reset(done10);      String done11 = l1.replaceAll("8c");
        Pattern m = Pattern.compile("\\x8e");   Matcher m1 = m.matcher("");   m1.reset(done11);      String done12 = m1.replaceAll("8e");
        Pattern n = Pattern.compile("\\x91");   Matcher n1 = n.matcher("");
        n1.reset(done12);
        String done13 = n1.replaceAll("&lsquo;");
        Pattern o = Pattern.compile("\\x92");
        Matcher o1 = o.matcher("");
        o1.reset(inputLine);
        String done14 = o1.replaceAll("&rsquo;");
        Pattern p = Pattern.compile("\\x93");
        Matcher p1 = p.matcher("");
        p1.reset(done14);
        String done15 = p1.replaceAll("\"");
        Pattern q = Pattern.compile("\\x94");
        Matcher q1 = q.matcher("");
        q1.reset(done15);
        String done16 = q1.replaceAll("94");
        Pattern r = Pattern.compile("\\x95");
        Matcher r1 = r.matcher("");
        r1.reset(done16);
        String done17 = r1.replaceAll("&bull;");
        Pattern s = Pattern.compile("\\x96");
        Matcher s1 = s.matcher("");
        s1.reset(done17);
        String done18 = s1.replaceAll("-");
        Pattern t = Pattern.compile("\\x97");
        Matcher t1 = t.matcher("");
        t1.reset(done18);
        String done19 = t1.replaceAll("97");
        Pattern u = Pattern.compile("\\x98");
        Matcher u1 = u.matcher("");
        u1.reset(done19);
        String done20 = u1.replaceAll("&tilde;");
        Pattern v = Pattern.compile("\\x99");
        Matcher v1 = v.matcher("");
        v1.reset(done20);
        String done21 = v1.replaceAll("&trade;");
        Pattern w = Pattern.compile("\\x9a");
        Matcher w1 = w.matcher("");
        w1.reset(done21);
        String done22 = w1.replaceAll("&scaron;");
        Pattern x = Pattern.compile("\\x9b");
        Matcher x1 = x.matcher("");
        x1.reset(done22);
        String done23 = x1.replaceAll("&rsaquo;");
        Pattern y = Pattern.compile("\\x9c");
        Matcher y1 = y.matcher("");
        y1.reset(done23);
        String done24 = y1.replaceAll("&oelig;");
        Pattern z = Pattern.compile("\\x9e");
        Matcher z1 = z.matcher("");
        z1.reset(done24);
        String done25 = z1.replaceAll("8e");
        Pattern aa= Pattern.compile("\\x9f");
        Matcher aa1 = aa.matcher("");
        aa1.reset(done25);
        String done26 = aa1.replaceAll("&Yuml;");

//        Pattern p = Pattern.compile(hexCodeIn);
//        Matcher m = p.matcher("");
//        m.reset(inputLine);
//        String done = m.replaceAll(replaceChar);

        return done26;
    } */

    public static String getCurrentDateTimeStr() {
        // Specify current date and time information
        Calendar calendar = new GregorianCalendar();
        int month      = calendar.get(Calendar.MONTH)+1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay  = calendar.get(Calendar.HOUR_OF_DAY);
        int minute     = calendar.get(Calendar.MINUTE);
        int second     = calendar.get(Calendar.SECOND);
        int millisec   = calendar.get(Calendar.MILLISECOND);
        String currentDateTimeStr = calendar.get(Calendar.YEAR)+"-"
            + (month<10      ? "0" + String.valueOf(month)     : String.valueOf(month))      +"-"
            + (dayOfMonth<10 ? "0" + String.valueOf(dayOfMonth): String.valueOf(dayOfMonth)) +"-"
            + (hourOfDay<10  ? "0" + String.valueOf(hourOfDay) : String.valueOf(hourOfDay))  +"-"
            + (minute<10     ? "0" + String.valueOf(minute)    : String.valueOf(minute))     +"-"
            + (second<10     ? "0" + String.valueOf(second)    : String.valueOf(second))     +"-"
            + (millisec<100  ? "0" + String.valueOf(millisec)  : (millisec<10 ? "00" + String.valueOf(millisec) : String.valueOf(millisec)));
        return currentDateTimeStr;
    }


    public static Connection getConnection(String which_properties_file)
        throws SQLException, IOException
    {
        Properties props = new Properties();
        String fileName = which_properties_file+".properties";
        FileInputStream in = new FileInputStream(fileName);
        props.load(in);

        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null) {
            System.setProperty("jdbc.drivers", drivers);
        }
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");

        return DriverManager.getConnection( url, username, password );
    }

}



