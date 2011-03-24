package edu.cornell.mannlib.ingest.fieldHandlers;

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
import java.util.List;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

public class HrMonikerHandler implements FieldHandler {

    private JdbcTemplate    jdbcTemplate = null;
    private String[] overridingJobCodes = { "Dean", "Dean Academic", "Provost", "Vice Provost", "Provost Associate", "Vice Provost Associate" };
    private Set overridingJobCodeSet;
    // ^ these are job codes that get used for the moniker even if they're on a secondary job

    public HrMonikerHandler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        overridingJobCodeSet = new HashSet();
        for (int i=0; i<overridingJobCodes.length; i++)
            overridingJobCodeSet.add(overridingJobCodes[i]);
    }

    /* if workingTitle was null or entity has generic 'Cornell Faculty Member' moniker,
     * first attempt to set moniker to EndowChairDesc, otherwise
     * use JobCodeLdesc to get a suitable moniker
     */
    public void setFields( Element ele, Object targetEnt ) {
        try {
            Individual theEnt = (Individual) targetEnt;
            String theMoniker = "";
            if (theEnt.getMoniker().length() == 0 ||
                    theEnt.getMoniker().equals("Cornell faculty member")) {
                Node endowChair = ele.selectSingleNode("//person/EndowChairCodeDesc");
                String endowChairStr = endowChair.getText();
                if (endowChairStr.length() > 0) {
                    theMoniker = endowChairLowercaser(endowChairStr);
                } else {
                    List jobCodeNodes = ele.selectNodes("//person/job[child::JobIndicatorLdesc='Primary Job']/JobcodeLdesc");
                    List sJobCodeNodes = ele.selectNodes("//person/job[child::JobIndicatorLdesc='Secondary Job']/JobcodeLdesc");
                    String jobCodeStr = "";
                    String sJobCodeStr = "";
                    if (!(jobCodeNodes==null) && jobCodeNodes.size()>0)
                        jobCodeStr = ((Node)jobCodeNodes.get(0)).getText();
                    if (!(sJobCodeNodes==null) && sJobCodeNodes.size()>0)
                        sJobCodeStr = ((Node)sJobCodeNodes.get(0)).getText();
                    if (overridingJobCodeSet.contains(sJobCodeStr))
                        jobCodeStr = sJobCodeStr;
                    String query = "SELECT moniker FROM jobcodes WHERE jobCodeLdesc = '"+jobCodeStr+"' LIMIT 1";
                    theMoniker = jobCodeStr;
                    try {
                        theMoniker = (String) jdbcTemplate.queryForObject(query, String.class);
                    } catch (Exception e) {
                        System.out.println("Error mapping to moniker from jobcode "+jobCodeStr);
                    }
                }
                theEnt.setMoniker(theMoniker);
            }
        } catch (ClassCastException e) {
            // log.error("ClassCastException: HrMonikerHandler expects Entity targetEnt");
        }
    }

    private String endowChairLowercaser (String endowChair) {
        String[] words = endowChair.split(" ");
        for (int i=0; i<words.length; i++) {
            words[i]=words[i].toLowerCase();
            if ((words[i].length()>1) && !(words[i].equals("of") || words[i].equals("in"))) {
                String init = words[i].substring(0,1);
                init=init.toUpperCase();
                words[i] = init+words[i].substring(1);
            }
        }
        String endowChairStr = "";
        for (int i=0; i<words.length; i++) {
            endowChairStr += words[i];
            if (i+1 < words.length)
                endowChairStr += " ";
        }
        return endowChairStr;
    }

    public void endOfParsing() {
        // do I need to worry about this?
    }

}
