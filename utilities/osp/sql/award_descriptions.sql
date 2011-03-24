select distinct(award_description)
 from OSPWH.AWARD_PROPOSAL
where award_prop_college like 'AGRICULTURE & LIFE SCIENCES'
AND award_description  is not null
and (award_prop_status_code='ASAP' OR award_prop_status_code='APA')