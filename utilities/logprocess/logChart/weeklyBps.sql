select hour, bytes
from hourly
where hour between date_sub(now(), INTERVAL 7 DAY) and now()
group by date_format(hour, '%Y%m%d%H')
order by hour;