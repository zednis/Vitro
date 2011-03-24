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

import edu.cornell.mannlib.ingest.interfaces.FieldHandler;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HrVclassHandler implements FieldHandler {

    Set otherFacultyJobCodes;
    String[] otherFacJobCodes = {"Prof Adj Asst", "Prof Adj", "Prof Adj Assoc", "Prof Courtesy", "Professor Assistant Courtesy",
               "Prof Assoc Vis", "Prof Asst Visit", "Professor Associate Courtesy", "Fellow Faculty", "Prof Visiting", "Andrew D. White Prof-At-Large", "Vice Provost", "Provost Associate", "Vice Provost Research Admin", "Dean Assoc"};
    Set nonAcadStaffSalAdminPlans;
    String[] staffSalAdminPlans = {"Non-Academic Staff", "Cooperative Extension Exempt", "Cooperative Extension Agent", "Puerto Rico Exempt"};
    HashSet otherAcadStaffJobcodes;
    String[] OtherAcadStaffJobcodes = {"Fellow Visting", "Post Dr Assoc", "Scient Visit", "Scientist Sr", "Scholar Visit",
            "Lect Visit Sr", "Lect Visit", "Lect Courtesy Sr", "Instr Visiting", "Scholar Sr", "Instr Courtesy",
            "Critic Visiting", "Lect Courtesy", "Fellow Postdoc", "Research Associate,Sr", "Scientist Sr"};
    Set otherStaffCodes;
    String[] OtherStaffCodes = {"Dir Development", "Gannett Resident", "Sr Resident", "Special Assistant to Provost", "VP/Sr Asst to President", "Assistant Vice President", "Associate Vice President"};
    public HrVclassHandler () {
        otherFacultyJobCodes = new HashSet();
        for (int i=0; i<otherFacJobCodes.length; i++)
            otherFacultyJobCodes.add(otherFacJobCodes[i]);
        nonAcadStaffSalAdminPlans = new HashSet();
        for (int i=0; i<staffSalAdminPlans.length; i++)
            nonAcadStaffSalAdminPlans.add(staffSalAdminPlans[i]);
        otherAcadStaffJobcodes = new HashSet();
        for (int i=0; i<OtherAcadStaffJobcodes.length; i++)
            otherAcadStaffJobcodes.add(OtherAcadStaffJobcodes[i]);
        otherStaffCodes = new HashSet();
        for (int i=0; i<OtherStaffCodes.length; i++)
            otherStaffCodes.add(OtherStaffCodes[i]);
    }


    public void setFields (Element ele, Object targetEnt) {
        Individual theEnt = (Individual) targetEnt;
        List familyList = ele.selectNodes("//person/job[child::JobIndicatorLdesc='Primary Job']/JobFamilyLdesc");
        List codeList = ele.selectNodes("//person/job[child::JobIndicatorLdesc='Primary Job']/JobcodeLdesc");
        String family = ((Node)familyList.get(0)).getText();
        String jobcode = ((Node)codeList.get(0)).getText();
        if (family.equals("Professorial") || otherFacultyJobCodes.contains(jobcode)) {
            //TODO: update for semweb-align
            if(true) throw new Error("TODO: update for semweb-align");
            //theEnt.setVClassId(295);
        } else {
            List salAdminPlanList = ele.selectNodes("//person/job[child::JobIndicatorLdesc='Primary Job']/SalAdminPlanLdesc");
            String salAdminPlan = ((Node)salAdminPlanList.get(0)).getText();
            if (nonAcadStaffSalAdminPlans.contains(salAdminPlan) || otherStaffCodes.contains(jobcode)) {
//TODO: update for semweb-align
                if(true) throw new Error("TODO: update for semweb-align");
            //                theEnt.setVClassId(297);
            } else {
                List jobFamilyList = ele.selectNodes("//person/job[child::JobIndicatorLdesc='Primary Job']/JobFamilyLdesc");
                String jobFamily = ((Node)jobFamilyList.get(0)).getText();
                if (jobFamily.equals("Library - Academic")) {
//TODO: update for semweb-align
                    if(true) throw new Error("TODO: update for semweb-align");
            //                    theEnt.setVClassId(296);
                } else {
//TODO: update for semweb-align
                    if(true) throw new Error("TODO: update for semweb-align");
            //                    theEnt.setVClassId(331); // academic staff
                }
            }

        }
    }

    public void endOfParsing() {
        // ?
    }

}
