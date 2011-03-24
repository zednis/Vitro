select distinct award_prop_department_id, award_prop_department, award_prop_department_adw_code 
from OSPWH.AWARD_PROPOSAL 
where award_prop_college like 'AGRICULTURE & LIFE SCIENCES' 
AND (award_prop_status_code='ASAP' OR award_prop_status_code='APA')