--THIS queries all awards of all subsponsors of the sponsor USDA
SELECT * FROM AWARD_PROPOSAL WHERE AWARD_PROP_SPONSOR_ID IN
 (select S1.SPONSOR_ID from sponsor_t s1, sponsor_t s2 where 
(S1.SPONSOR_LEVEL_1 = S2.SPONSOR_ABBREVIATION 
  OR S1.SPONSOR_LEVEL_2=S2.SPONSOR_ABBREVIATION ) 
and s2.sponsor_abbreviation = 'USDA'			)