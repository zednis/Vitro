load "setup.plot"

set output "./clientsPerDay.png"

set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

set title "Running record of Clients per Day"
set ylabel "unique client ip address"
set xlabel ""
#set logscale y
set grid
set bmargin 3
set style fill solid 0.3
set boxwidth 0.8 relative

plot 'clientsPerDay.txt' using 1:2 title 'unique client addresses' with boxes, '' using 1:3 title 'robots' with boxes

