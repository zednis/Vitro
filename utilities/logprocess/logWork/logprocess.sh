#!/bin/bash 
#process vivo logs
#
# break the log on the command line into 
# jsp pages, servlet pages and other pages
#
# Then run scripts on each of these files
# to load them into the db
#
# the *.sed files generate the sql and
# there you will find an example of the 
# table that they target.
#
# processes the standard input:
# ./logprocess.sh someserver.log
#
#bdc34

#cd /usr/local/services/logProcess/logWork

MYSQL=/c/usr/local/mysql/bin/mysql

dbuser=loguser
db=logs
dbpasswd=10:33:hasta

jspgrep='GET [^" ]*\.jsp'
servletgrep='GET [^ "]*(fedsearch|entities|entity|fetch)\?'

echo "cleaning up old sql files"
rm index.sql entity.sql fed.sql

#get jsp lines
grep "$jspgrep" $* > jsplines.log
#get servlet lines with no jsp lines
egrep "$servletgrep" $* > servletlines.log
#get non jsp, non servlet lines
egrep -v "$jspgrep" $* | egrep -v "$servletgrep" > otherlines.log

#quick sanity check
orgcount=$( cat $* | wc -l ) 
jspcount=$( cat jsplines.log | wc -l  ) 
sletcount=$( cat servletlines.log | wc -l )
othercount=$( cat otherlines.log | wc -l  )
echo "orginal linecount: $orgcount "
echo "jsp requests: $jspcount "
echo "servlet requests: $sletcount "
echo "other requests: $othercount "
echo "difference: $( expr $jspcount + $sletcount + $othercount - $orgcount )"

echo "remove old stuff from the table log, it's just a temp table "
$MYSQL -u$dbuser -p$dbpasswd $db < wacklog.sql 2>&1

#============== process servlets ================
echo "processing fedsearch lines"
echo "start transaction;" > fed.sql
cat servletlines.log | grep "GET [^ \"]*fedsearch?[^ \"]*querytext=" | sed -f date.sed | sed -f fedsearch2sql.sed >> fed.sql
echo "commit;" >> fed.sql
echo "loading fedsearch lines -- linecount: $( cat fed.sql | wc -l )"
$MYSQL -u$dbuser -p$dbpasswd $db < fed.sql 2>&1

echo "processing entity lines"
echo "start transaction;" > entity.sql
cat servletlines.log | grep "GET [^ \"]*entity?[^ \"]*id=" | sed -f date.sed | sed -f entity2sql.sed >> entity.sql
echo "commit;" >> entity.sql
echo "loading entity lines -- linecount: $( cat entity.sql | wc -l )"
$MYSQL -u$dbuser -p$dbpasswd $db < entity.sql 2>&1

# very few of these so far
#cat servletlines.log | grep "GET /entities?" | 

#============= process jsps ====================
echo "processing index.jsp lines"
echo "start transaction;" > index.sql
cat jsplines.log | grep "GET [^ \"]*index.jsp?[^ \"]*primary=" | sed -f date.sed | sed -f index2sql.sed >> index.sql
echo "commit;" >> index.sql
echo "loading index -- linecount: $( cat index.sql | wc -l )"
$MYSQL -u$dbuser -p$dbpasswd $db < index.sql 2>&1

#=========== other pages ==============
echo "processing other lines"
echo "start transaction;" > other.sql
cat otherlines.log | sed -f date.sed | sed -f other2sql.sed >> other.sql
echo "commit;" >> other.sql
echo "loading other lines -- linecount: $( cat other.sql | wc -l )"
$MYSQL -u$dbuser -p$dbpasswd $db < other.sql 2>&1

echo "looking for bot agents"
$MYSQL -u$dbuser -p$dbpasswd $db < botprocess.sql 2>&1
echo "processing tables to hourly, client, etc. "
$MYSQL -u$dbuser -p$dbpasswd $db < processTables.sql 2>&1

#$MYSQL -u$dbuser -p$dbpasswd $db < wacklog.sql 2>&1

echo "doing postprocess script to clean some things up"
$MYSQL -u$dbuser -p$dbpasswd $db < postprocess.sql 2>&1
echo "done"

exit 0
#================= sql for log analysis ===============
# # serch term freq
# select count(*) as freq, query_field as 'search term'
#  from log 
# where page_type = 'fedsearch'
# group by query_field
# order by freq desc

# # entity freq
# select count(*) as freq, query_field as 'entity id'
#  from log 
# where page_type = 'entity'
# group by query_field
# order by freq desc

# # tab freq
# select count(*) as freq, query_field as 'tab frequency'
#  from log 
# where page_type = 'index'
# group by query_field
# order by freq desc

# # agent freq
#select count(*) as freq, agent
# from log 
#group by agent
#order by freq desc

# distinct clients per hour
# select reply_date,  count( distinct(client_ip)) from log
# group by date_format( reply_date, '%Y%m%d%H')
# order by reply_date;