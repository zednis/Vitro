select hour, bytes, request_count
from hourly
where hour between date_sub(now(), INTERVAL 48 HOUR) and now()
order by hour;