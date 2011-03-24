load "setup.plot"

set output "./weeklyBps.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Request freq for each hour of the last 7 days"
set ylabel "Requests"
set xlabel ""
set grid
set bmargin 3

plot 'weeklyReq.txt' using 1:4 title 'requests' smooth unique 

