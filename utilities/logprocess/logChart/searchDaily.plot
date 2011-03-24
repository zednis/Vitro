load "setup.plot"

set output "./searchDaily.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Unique Queries per Day"
set ylabel "unique queries for a given day"
set xlabel ""
set grid
set bmargin 3
set style fill solid 0.3
set boxwidth 0.8 relative

plot 'searchDaily.txt'  using 1:4 title 'queries' with boxes, '' using 1:3 title 'unique quries' with boxes

