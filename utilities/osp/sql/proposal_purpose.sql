select  award_prop_project_id, award_proposal_id ,award_prop_sponsor_all_levels, award_prop_pi_netid,  award_prop_department, award_prop_full_title
 from OSPWH.AWARD_PROPOSAL
 where award_prop_college like 'AGRICULTURE & LIFE SCIENCES' 
AND (award_prop_status_code='ASAP' OR award_prop_status_code='APA')