select  DISTINCT D.DEPARTMENT_ABBREVIATION
 from OSPWH.AWARD_PROPOSAL ap, OSPWH.OSP_DEPARTMENT D
 where ap.award_prop_college like 'AGRICULTURE & LIFE SCIENCES' 
AND ap.award_prop_status_code in ('ASAP', 'APA')
AND AP.AWARD_DESCRIPTION NOT IN ('MTA', 'NDA', 'RADS') 
and ap.award_prop_department is not null 
and ap.award_prop_sponsor_id is not null
and ap.award_prop_department_id  = d.department_id