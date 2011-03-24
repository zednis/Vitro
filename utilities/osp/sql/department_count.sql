select d.department_id, d.department_name, count(*)
from department_t d, award_proposal ap
where d.department_id = ap.award_prop_department_id
group by d.department_name, d.department_id