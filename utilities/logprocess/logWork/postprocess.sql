update log set reply_size=0 where reply_size is null;
update log set server_status=0 where server_status is null;

-- drop table if exists per_second;
-- create table per_second 
-- ( reply_date timestamp,
--   hits int(11),
--   bytes int(11) )
-- as
-- select reply_date ,  count( reply_size) as hits ,sum(reply_size) as bytes
-- from log 
-- group by date_format(reply_date, '%Y%m%d%H%i%s')
-- order by reply_date;



