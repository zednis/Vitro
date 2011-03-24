#sed script to map an apache combined log to
#simple sql inserts.
#bdc34

# goes into a table such as:
# CREATE TABLE log (
#   client_ip varchar(255) default NULL,
#   client_host varchar(255) default NULL,
#   reply_date date default NULL,
#   request varchar(255) default NULL,
#   server_status int(11) default NULL,
#   reply_size int(11) default NULL,
#   ref varchar(255) default NULL,
#   agent varchar(255) default NULL,
#   query_field varchar(255) default NULL,
#   full_log_line text default NULL,
#   page_type varchar (30) default NULL
# ) ENGINE=InnoDB DEFAULT CHARSET=latin1

# run this using something like:
# cat yoursite.log | grep "GET /entity?" | sed -f entity2sql.sed > yoursite.sql

#extract query_field text and stick at end of line
#we'll stick this in the db later
s|.*GET [^" ]*entity?[^" ]*id=\([0-9]*\).*|& \1|

#replace single quotes to double quotes for sql quote escape
s/'/''/g

#convert into sql statement
#for lines with the appended extra query_field info
s|\(.*\) \(.*\) .* \[\(.*\)\] "\(.*\)" \(.*\) \(.*\) "\(.*\)" "\(.*\)" \(.*\)|insert ignore into log (client_ip, client_host, reply_date, request, server_status, reply_size, ref, agent, query_field, full_log_line, page_type) values ( '\1', '\2', '\3', '\4', \5, \6, '\7', '\8', '\9', '&', 'entity') ;|

#by default apache puts a - for a zero sized reply
#convert - and '-' to NULL
s/, *'*-'*, /, NULL, /g

#for gnu sed 3.0 and greater
#sticks a commit/start tran every 10000 lines
10000~10000icommit; start transaction;
