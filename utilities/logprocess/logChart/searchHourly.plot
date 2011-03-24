load "setup.plot"

set output "./searchHourly.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Unique Queries per Hour over the last 48 hours"
set ylabel "unique queries for a given hour"
set xlabel ""
set grid
set bmargin 3
set style fill solid 0.3
set boxwidth 0.8 relative

plot 'searchHourly.txt' using 1:4 title 'queries' with boxes , '' using 1:3 title 'unique quries' with boxes

