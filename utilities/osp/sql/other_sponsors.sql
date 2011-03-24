-- get all sponsor_abbreviations where there isn't a sponsor_t row
-- these are the sponsor_level_1 and sponsor_level_2 sponsors
-- that are not directly referenced by AWARD_PROPOSAL.AWARD_PROP_SPONSOR_ID
(select distinct sponsor_level_2 from sponsor_t s2 
where 
s2.sponsor_level_2 not in (select sponsor_abbreviation from sponsor_t))
union
(select distinct sponsor_level_1 from sponsor_t s1
where
s1.sponsor_level_1 not in (select sponsor_abbreviation from sponsor_t ))