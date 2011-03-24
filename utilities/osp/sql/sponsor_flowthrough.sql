select distinct award_prop_sponsor_all_levels, flow_through_spons_all_levels
 from OSPWH.AWARD_PROPOSAL
where award_prop_college like 'AGRICULTURE & LIFE SCIENCES'
AND flow_through_spons_all_levels is not null
and (award_prop_status_code='ASAP' OR award_prop_status_code='APA')
ORDER BY flow_through_spons_all_levels