load "setup.plot"

set output "./weeklyBps.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "avg(Mbps) for each hour of the last 7 days"
set ylabel "Mbytes per second"
set xlabel ""
set grid
set bmargin 3

plot 'weeklyBps.txt' using 1:($3/(1024*1024)) title 'average Mbps' smooth unique 

