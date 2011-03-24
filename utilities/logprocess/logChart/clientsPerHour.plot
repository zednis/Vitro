load "setup.plot"

set output "./clientsPerHour.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Record of Clients per Hour for last 48 hours"
set ylabel "unique client ip address"
set xlabel ""
set grid
set bmargin 3

plot 'clientsPerHour.txt' using 1:3 title 'unique client addresses' smooth unique 

