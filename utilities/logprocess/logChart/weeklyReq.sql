select reply_date, sum(reply_size), count(reply_size)
from log
where reply_date between date_sub(now(), INTERVAL 7 DAY) and now()
group by date_format(reply_date, '%Y%m%d%H')
order by reply_date;