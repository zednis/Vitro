update ignore log set reply_size=0 where reply_size is null;
update ignore log set server_status=0 where server_status is null;

-- how to stick stuff into hourly table:
 insert ignore into hourly    
     select 
         date_format(reply_date, '%Y-%m-%d %H:00:00') as 'hour', 
         sum(reply_size) as 'bytes', 
        count(reply_date)  as 'request_count',
        count(distinct(client_ip))  as 'client_count'
    from log 
    group by date_format(reply_date, '%Y%m%d%H');

--how to stick stuff into queries table:
insert ignore into queries
    select 
        query_field,
        reply_date,
        page_type,
        left(md5(concat(client_ip, request, ref)), 5) as 'hash_id'
    from log 
    where page_type not like 'other';


--how to stick stuff in the client table:
insert ignore into clients 
    select 
    md5(client_ip),
    left(md5(concat(request,ref)),5),
    reply_date,
    robot_agent.id,
    right(client_ip, 3),
    locate('cornell',client_ip)  > 0
    from robot_agent right outer join log 
    on log.agent = robot_agent.agent;

-- this doesn't work:
-- insert into agent  (agent, tally)
-- select agent , 1 from log
--   on duplicate key update tally = tally+1

update ignore hourly set bytes=0 where bytes is null;
update ignore hourly set request_count=0 where request_count is null;
update ignore hourly set client_count=0 where client_count is null;