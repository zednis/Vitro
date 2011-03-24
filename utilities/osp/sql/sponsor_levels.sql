select s.sponsor_name, s.sponsor_all_levels, s.sponsor_level_1, s.sponsor_level_2, s.sponsor_level_3
 from OSPWH.AWARD_PROPOSAL ap, ospwh.sponsor s
 where ap.award_prop_college like 'AGRICULTURE & LIFE SCIENCES' 
AND (ap.award_prop_status_code='ASAP' OR ap.award_prop_status_code='APA')
and ap.award_prop_sponsor_id = s.sponsor_id
AND AP.AWARD_DESCRIPTION NOT IN ('MTA', 'NDA', 'RADS')