load "setup.plot"

set output "./searchHourly.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Unique Queries per Hour"
set ylabel "unique queries"
set xlabel ""
set grid
set bmargin 3

plot 'searchHourly.txt' using 1:3 title 'unique quries' smooth unique 

