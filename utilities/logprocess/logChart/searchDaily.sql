select date_format(reply_date, '%Y-%m-%d %H:00'), count( distinct(query_field) ), count(query_field) 
from queries
where page_type = 'fedsearch' 
group by date_format( reply_date, '%Y%m%d')
order by reply_date;