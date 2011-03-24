select count( query_field) as frequency, query_field as query
from queries 
where page_type ='fedsearch' 
group by query_field 
order by frequency desc;