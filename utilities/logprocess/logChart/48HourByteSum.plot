load "setup.plot"


set output "48HourByteSum.png"
set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "last 48 hours: Mb transfred per hour and 'hits'"
set ylabel "Mbytes"
set xlabel ""
set y2label 'Hits: Number of requests logged'
set ytics nomirror
set y2tics
#set logscale y2
unset x2tics
set grid
set bmargin 3

plot '48HourByteSum.txt' using 1:($3/(1024*1024)) title 'Mb per hour' smooth unique, '' using 1:4 title 'hits per hour' axes x1y2 smooth unique

