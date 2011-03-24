#!/bin/sh
# script to watch links in /proc/xxxx/fd
# this needs to be run as root to access /proc/$pid/fd
#
#bdc34
#

workingDir="/usr/local/services/logProcess/fdChart"

#where to put the plot
plotdir="/usr/local/apache/htdocs/admin/charts"

#get process id
psid=$( ps -ax | grep java | grep "tomcat/bin/bootstrap.jar" | cut -f1 -d\  )
watchme="/proc/$psid/fd"
echo watching $watchme

cd $workingDir

filename="$(date +%m%d%H%M%S).txt"
ls -l $watchme | sed "s/.*->//" |  grep -v "^total" | sort > $filename

#parse the directory listing files
for i in $( ls *.txt ) ; do
	other=$( cat $i | egrep -v "socket|urandom" | wc -l )
	rand=$( cat $i | egrep  "urandom" | wc -l )
	sock=$( cat $i | egrep  "socket" | wc -l )
	month=$( echo $i | cut -b0-2 )
	day=$( echo $i | cut -b3-4 )
	hour=$( echo $i | cut -b5-6 )
	echo $rand $sock $other $month-$day$hour >> out.dat
done
# ./parse.sh >> out.dat

rm $filename

#make a plot image
/usr/local/bin/gnuplot < fdplot.script 2>&1

chmod o+r ./plot.png 
cp ./plot.png $plotdir

  
