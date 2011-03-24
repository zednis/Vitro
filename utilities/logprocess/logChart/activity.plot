load "setup.plot"

set output "./activity.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Record of Clients and Transfer Rate for last 48 hours"
set ylabel "unique client ip address"
set y2label "Mbytes transfered"
set xlabel ""
set ytics nomirror
set y2tics
unset x2tics
set grid
set bmargin 3
set boxwidth 0.8 relative
set style fill solid 0.3

plot 'clientsPerHour.txt' using 1:3 title 'unique client addresses' with boxes , '' using 1:4 title 'robots' with boxes, '48HourByteSum.txt' using 1:($3/(1024*1024)) title 'Mb per hour' smooth unique axes x1y2

