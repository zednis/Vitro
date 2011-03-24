#!/bin/bash

dbhost=localhost
dbuser=loguser
db=logs
dbpass=10:33:hasta

#not having the paths was causing odd errors on linux
#GNUPLOT=wgnuplot
GNUPLOT=/usr/local/bin/gnuplot
MYSQL=/usr/local/mysql/bin/mysql

echo "generating:"

echo "full data for bps"
cat runningBps.sql | $MYSQL -u$dbuser -p$dbpass --host=$dbhost $db > runningBps.txt 2>&1

echo "weekly data for bps"
cat weeklyBps.sql | $MYSQL -u$dbuser -p$dbpass --host=$dbhost $db > weeklyBps.txt
echo "weekly requests"
cat weeklyReq.sql | $MYSQL -u$dbuser -p$dbpass --host=$dbhost $db > weeklyReq.txt

echo "hourly sums for last 48h"
cat 48HourByteSum.sql | $MYSQL -u$dbuser -p$dbpass --host=$dbhost $db > 48HourByteSum.txt

$GNUPLOT runningBps.plot 2>&1
#$GNUPLOT 48hoursBps.plot 2>&1
$GNUPLOT weeklyBps.plot 2>&1
$GNUPLOT weeklyReq.plot 2>&1
$GNUPLOT 48HourByteSum.plot 2>&1

echo""
echo "clients per day data"
cat clientsPerDay.sql | $MYSQL -u$dbuser -p$dbpass --host=$dbhost $db > clientsPerDay.txt
echo "clients per Hour data"
cat clientsPerHour.sql | $MYSQL -u$dbuser -p$dbpass --host=$dbhost $db > clientsPerHour.txt

$GNUPLOT clientsPerDay.plot 2>&1
$GNUPLOT clientsPerHour.plot 2>&1

echo ""
echo "search queries per hour"
cat searchHourly.sql | $MYSQL -u$dbuser -p$dbpass --host=$dbhost $db > searchHourly.txt
echo "search queries per hour"
cat searchDaily.sql | $MYSQL -u$dbuser -p$dbpass --host=$dbhost $db > searchDaily.txt

$GNUPLOT searchDaily.plot 2>&1
$GNUPLOT searchHourly.plot 2>&1
$GNUPLOT activity.plot 2>&1

echo ""
echo "sql reports"
echo "query freq"
echo "select count( query_field) as frequency, query_field as query from queries where page_type ='fedsearch' group by query_field order by frequency desc;" | \
$MYSQL -u$dbuser -p$dbpass --html --host=$dbhost $db | sed -f wackPercentHex.sed > queryFreq.html
echo "tab freq"
cat tabquery.sql | $MYSQL  -u$dbuser -p$dbpass --html --host=$dbhost $db > tabFreq.html
echo "entity freq"
echo "select count( query_field) as freq, query_field  as 'entity id', entities.name as name from queries join vivo3.entities on queries.query_field=entities.id where queries.page_type ='entity' group by query_field order by freq desc;" | \
$MYSQL  -u$dbuser -p$dbpass --html --host=$dbhost $db > entityFreq.html

#clean up
#rm runningBps.txt weeklyBps.txt weeklyReq.txt 48hoursBps.txt 48HourByteSum.txt 
#rm clientsPerHour.txt clientsPerDay.txt
#rm searchHourly.txt searchDaily.txt

echo "doCharts.sh done"
exit 0
