load "setup.plot"

reset
set autoscale 
#set terminal png
#set output "c:/cygwin/home/bdc34/vitro/logCharts/test.png"

cd "c:/cygwin/home/bdc34/vitro/logCharts"
set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Record of Clients per Hour for last 48 hours"
set ylabel "unique client ip address"
set y2label "avg(bps)"
set xlabel ""
set grid
set bmargin 3

plot 'clientsPerHour.txt' using 1:3 title 'unique client addresses' smooth unique , 
'48hoursBps.txt' using 1:3 title 'hourly average bps' smooth unique axes x1y2

