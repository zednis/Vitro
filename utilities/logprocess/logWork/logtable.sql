#table to store log data
#this is intended for mysql 4.1.7
#just remove the character set and collate stuff otherwise

--  drop index log_indx on log;
--  drop index agent_indx on agent;
--  drop index hourly_indx on hourly;
--  drop index queries_indx on queries;
--  drop table log;
--  drop table hourly;
--  drop table queries;
--  drop table clients;
--  drop table robot_agent;
--  drop index agent_indx on agent;
--  drop index client_indx on client;

CREATE database logs;
use logs;

CREATE TABLE log (
  client_ip varchar(255) default NULL,
  client_host varchar(255) default NULL,
  reply_date datetime ,
  request varchar(255) default NULL,
  server_status int(6) default NULL,
  reply_size int(11) default NULL,
  ref varchar(255) default NULL,
  agent varchar(255) default NULL,
  query_field varchar(255) default NULL,
  full_log_line text default NULL,
  page_type varchar (30) default NULL
) ENGINE=InnoDB ;
--  character set latin1 collate latin1_general_cs ;

CREATE TABLE hourly (
    hour datetime,
    bytes int(11) default 0,
    request_count int(7) default 0,
    client_count int(6) default 0
) ENGINE=InnoDB;
--character set latin1 collate latin1_general_cs ;

## note: ip_hash is from 
## left(md5(concat(log.client_ip, request, ref)),5)
CREATE TABLE queries (
    query_field varchar(255) default NULL,
    reply_date datetime,
    page_type varchar(30) default NULL,
    hash_id char(5)
);
--character set latin1 collate latin1_general_cs ;

## hash_salt is left(md5(concat(request,ref)),5)
CREATE TABLE clients (
    ip_hash varchar(32),
    hash_salt char(5),
    request_time datetime,
    robot int default NULL,
    tld char(3),
    cornell int(1) default 0
);
--character set latin1 collate latin1_general_cs;

CREATE table robot_agent (
    id int not null auto_increment primary key,
    agent varchar(255)
);
--character set latin1, collate latin1_general_cs ;

create unique index client_indx on clients (ip_hash, hash_salt, request_time);
CREATE UNIQUE INDEX log_indx ON log (client_ip, reply_date, request);
create unique index hourly_indx on hourly (hour);
create unique index queries_indx on queries (reply_date, hash_id);
create unique index agent_indx on robot_agent( agent );