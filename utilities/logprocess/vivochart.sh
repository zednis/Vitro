#!/bin/bash
#run the scripts that will make the vivo logs
#bdc34
#2005-05-25
#
#note that this should be run before 23:59 so that
# when the logs rotate on the last day of the month that
# day's data will be processed.  

echo "doing vivo chart generation on $(date) as $(whoami)"

cd /usr/local/services/logProcess/logWork
echo "in dir $(pwd) to do logprocess.sh"
echo "executing: ./logprocess.sh /usr/local/apache/logs/vivo_access.$(date +%Y-%m).log"
./logprocess.sh "/usr/local/apache/logs/vivo_access.$(date +%Y-%m).log"

cd /usr/local/services/logProcess/logChart
echo "in dir $(pwd) to doCharts.sh"
/usr/local/services/logProcess/logChart/doCharts.sh
echo "in dir $(pwd) to doMoveToApache.sh"
/usr/local/services/logProcess/logChart/doMoveToApache.sh


