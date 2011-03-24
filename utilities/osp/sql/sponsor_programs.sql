select distinct award_prop_program_id, award_prop_program_name,award_prop_sponsor_all_levels
 from OSPWH.AWARD_PROPOSAL
where award_prop_college like 'AGRICULTURE & LIFE SCIENCES'
AND award_prop_program_id is not null
and (award_prop_status_code='ASAP' OR award_prop_status_code='APA')
ORDER BY award_prop_program_name