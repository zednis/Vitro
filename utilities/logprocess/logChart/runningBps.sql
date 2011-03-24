select hour, sum(request_count) as 'hits', sum(bytes) as 'bytes sent'
from hourly
group by date_format(hour, '%Y%m%d')
order by hour;