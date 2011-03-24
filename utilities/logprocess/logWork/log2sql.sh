#!/bin/bash
sed \
-e 's/(.*) (.*) (.*) \[(.*)\] "(.*)" (.*) (.*) "(.*)" "(.*)"/\
insert into log (ip, host, user, date, request, status, size, ref, agent) values ( '\''\1'\'', '\''\2'\'', '\''\3'\'', '\''\4'\'', '\''\5'\'', \6, \7, '\''\8'\'', '\''\9'\'' ) ;/'\
 $* > log.sql