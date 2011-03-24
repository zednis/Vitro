select s.sponsor_name,  count(*)
from sponsor_t s,  award_proposal ap
where s.sponsor_id = ap.award_prop_sponsor_id
group by s.sponsor_name