select 
    date_format(request_time, '%Y-%m-%d %H:00'), 
    count( distinct(ip_hash)) ,
    count( distinct(robot))
from clients
where request_time between date_sub(now(), INTERVAL 48 HOUR) and now()
group by date_format( request_time, '%Y%m%d%H')
order by request_time;