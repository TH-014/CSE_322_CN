#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <input> <output>"
    exit 1
fi

input=$1
output=$2

nodecnt=(20 40 70 100)
pktps=(100 200 300 400)
speed=(5 10 15 20)

echo "# nodes,# pkt/sec,speed,avg throughput,avg delay,% pkt received, %pkt lost" > $output

for i in ${nodecnt[@]}; do
    ./ns3 run "$input" -- --flowMonitor=true --nodeCount=$i
    echo "done for $i nodes"
done

for i in ${pktps[@]}; do
    ./ns3 run "$input" -- --flowMonitor=true --pktPERsec=$i
    echo "done for $i pkt/sec"
done

for i in ${speed[@]}; do
    ./ns3 run "$input" -- --flowMonitor=true --speed=$i
    echo "done for $i speed"
done

gnuplot << EOF

# Set the datafile separator to comma
set datafile separator ","

# Define the input CSV file
datafile = "$output"

# Set the terminal to output as PNG images
set terminal png size 800,600

# Plot 1: Average Throughput
set output "avg_throughput_vs_nodecnt.png"
set title "Average Throughput vs # Nodes"
set grid
set xlabel "# Nodes"
set ylabel "Average Throughput"
plot datafile every ::0::3 using 1:4 with linespoints title "Avg Throughput" pointtype 7 pointsize 1.5

# Plot 2: Average Delay
set output "avg_delay_vs_nodecnt.png"
set title "Average Delay vs # Nodes"
set grid
set xlabel "# Nodes"
set ylabel "Average Delay"
plot datafile every ::0::3 using 1:5 with linespoints title "Avg Delay" pointtype 7 pointsize 1.5

# Plot 3: Percentage of Packets Received
set output "pkt_received_vs_nodecnt.png"
set title "Percentage of Packets Received vs # Nodes"
set grid
set xlabel "# Nodes"
set ylabel "% Packets Received"
plot datafile every ::0::3 using 1:6 with linespoints title "% Pkt Received" pointtype 7 pointsize 1.5

# Plot 4: Percentage of Packets Lost
set output "pkt_lost_vs_nodecnt.png"
set title "Percentage of Packets Lost vs # Nodes"
set grid
set xlabel "# Nodes"
set ylabel "% Packets Lost"
plot datafile every ::0::3 using 1:7 with linespoints title "% Pkt Lost" pointtype 7 pointsize 1.5

#======================================================================================================#
#======================================================================================================#
#======================================================================================================#
#======================================================================================================#

# Plot 1: Average Throughput
set output "avg_throughput_vs_pktps.png"
set title "Average Throughput vs pkt per sec"
set grid
set xlabel "pkt per sec"
set ylabel "Average Throughput"
plot datafile every ::4::7 using 2:4 with linespoints title "Avg Throughput" pointtype 7 pointsize 1.5

# Plot 2: Average Delay
set output "avg_delay_vs_pktps.png"
set title "Average Delay vs pkt per sec"
set grid
set xlabel "pkt per sec"
set ylabel "Average Delay"
plot datafile every ::4::7 using 2:5 with linespoints title "Avg Delay" pointtype 7 pointsize 1.5

# Plot 3: Percentage of Packets Received
set output "pkt_received_vs_pktps.png"
set title "Percentage of Packets Received vs pkt per sec"
set grid
set xlabel "pkt per sec"
set ylabel "% Packets Received"
plot datafile every ::4::7 using 2:6 with linespoints title "% Pkt Received" pointtype 7 pointsize 1.5

# Plot 4: Percentage of Packets Lost
set output "pkt_lost_vs_pktps.png"
set title "Percentage of Packets Lost vs pkt per sec"
set grid
set xlabel "pkt per sec"
set ylabel "% Packets Lost"
plot datafile every ::4::7 using 2:7 with linespoints title "% Pkt Lost" pointtype 7 pointsize 1.5

#======================================================================================================#
#======================================================================================================#
#======================================================================================================#
#======================================================================================================#

# Plot 1: Average Throughput
set output "avg_throughput_vs_node_speed.png"
set title "Average Throughput vs node speed (m/s)"
set grid
set xlabel "node speed"
set ylabel "Average Throughput"
plot datafile every ::8::11 using 3:4 with linespoints title "Avg Throughput" pointtype 7 pointsize 1.5

# Plot 2: Average Delay
set output "avg_delay_vs_node_speed.png"
set title "Average Delay vs node speed"
set grid
set xlabel "node speed"
set ylabel "Average Delay"
plot datafile every ::8::11 using 3:5 with linespoints title "Avg Delay" pointtype 7 pointsize 1.5

# Plot 3: Percentage of Packets Received
set output "pkt_received_vs_node_speed.png"
set title "Percentage of Packets Received vs node speed"
set grid
set xlabel "node speed"
set ylabel "% Packets Received"
plot datafile every ::8::11 using 3:6 with linespoints title "% Pkt Received" pointtype 7 pointsize 1.5

# Plot 4: Percentage of Packets Lost
set output "pkt_lost_vs_node_speed.png"
set title "Percentage of Packets Lost vs node speed"
set grid
set xlabel "node speed"
set ylabel "% Packets Lost"
plot datafile every ::8::11 using 3:7 with linespoints title "% Pkt Lost" pointtype 7 pointsize 1.5

EOF