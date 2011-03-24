select date_format(reply_date, '%Y-%m-%d %H:00'), count( distinct(query_field) ), count(query_field) 
from queries
where page_type = 'fedsearch' and ( reply_date between date_sub(now(), INTERVAL 48 HOUR) and now() )
group by date_format( reply_date, '%Y%m%d%H')
order by reply_date;