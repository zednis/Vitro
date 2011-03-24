select 
    date_format( request_time, '%Y-%m-%d'), 
    count( distinct(ip_hash) ) , 
    count( distinct(robot))
from clients
group by date_format( request_time, '%Y%m%d')
order by request_time;