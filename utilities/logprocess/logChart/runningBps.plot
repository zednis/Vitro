load "setup.plot"

set output "./runningBps.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Per Day hits and mbytes sent"
set ylabel "Mbytes per day"
set y2label "hits per day"
set xlabel ""
set ytics nomirror
set y2tics
set grid
set bmargin 3

plot 'runningBps.txt' using 1:($4/(1024*1024)) title 'Mbytes' smooth unique, '' using 1:3 title 'hits' smooth unique axes x1y2


