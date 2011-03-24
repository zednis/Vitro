select count ( ap.award_proposal_id )
 from OSPWH.AWARD_PROPOSAL ap
 where ap.award_prop_college like 'AGRICULTURE & LIFE SCIENCES' 
AND ap.award_prop_status_code in ('ASAP', 'APA')
AND AP.AWARD_DESCRIPTION NOT IN ('MTA', 'NDA', 'RADS') 
and award_prop_department is not null 
and award_prop_sponsor_id is not null