select 
       count(query_field) as freq, 
       query_field as 'tab' ,  
       vivo3.tabs.title 
from queries join vivo3.tabs 
on vivo3.tabs.id = 
    (case when (query_field LIKE '%sub%')  
             then substring(query_field from locate('sub', query_field)+3 )
        when (query_field LIKE '%c%')  
             then substring(query_field from locate('c', query_field)+1)
        when (query_field LIKE '%s%')  
             then substring(query_field from locate('s', query_field)+1 )
        when (query_field LIKE '%p%')  
             then substring(query_field from locate('p', query_field)+1) 
    end)
where page_type='index' 
group by query_field 
order by freq desc;