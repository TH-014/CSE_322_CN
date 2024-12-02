# Set the datafile1 separator to comma
set datafile separator ","

# Define the input CSV file
datafile1 = "2005014_aodv_sim.csv"
datafile2 = "2005014_raodv_sim.csv"

# Set the terminal to output as PNG images
set terminal png size 800,600

# Plot 1: Average Throughput
set output "01.avg_throughput_vs_nodecnt.png"
set title "Average Throughput vs # Nodes"
set grid
set xlabel "# Nodes"
set ylabel "Average Throughput"
plot datafile1 every ::0::3 using 1:4 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::0::3 using 1:4 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 2: Average Delay
set output "02.avg_delay_vs_nodecnt.png"
set title "Average Delay vs # Nodes"
set grid
set xlabel "# Nodes"
set ylabel "Average Delay"
plot datafile1 every ::0::3 using 1:5 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
    datafile2 every ::0::3 using 1:5 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 3: Percentage of Packets Received
set output "03.pkt_received_vs_nodecnt.png"
set title "Percentage of Packets Received vs # Nodes"
set grid
set xlabel "# Nodes"
set ylabel "% Packets Received"
plot datafile1 every ::0::3 using 1:6 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
    datafile2 every ::0::3 using 1:6 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 4: Percentage of Packets Lost
set output "04.pkt_lost_vs_nodecnt.png"
set title "Percentage of Packets Lost vs # Nodes"
set grid
set xlabel "# Nodes"
set ylabel "% Packets Lost"
plot datafile1 every ::0::3 using 1:7 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
    datafile2 every ::0::3 using 1:7 with linespoints title "RAODV" pointtype 5 pointsize 1.5

#======================================================================================================#
#======================================================================================================#
#======================================================================================================#
#======================================================================================================#

# Plot 1: Average Throughput
set output "05.avg_throughput_vs_pktps.png"
set title "Average Throughput vs pkt per sec"
set grid
set xlabel "pkt per sec"
set ylabel "Average Throughput"
plot datafile1 every ::4::7 using 2:4 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::4::7 using 2:4 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 2: Average Delay
set output "06.avg_delay_vs_pktps.png"
set title "Average Delay vs pkt per sec"
set grid
set xlabel "pkt per sec"
set ylabel "Average Delay"
plot datafile1 every ::4::7 using 2:5 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::4::7 using 2:5 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 3: Percentage of Packets Received
set output "07.pkt_received_vs_pktps.png"
set title "Percentage of Packets Received vs pkt per sec"
set grid
set xlabel "pkt per sec"
set ylabel "% Packets Received"
plot datafile1 every ::4::7 using 2:6 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::4::7 using 2:6 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 4: Percentage of Packets Lost
set output "08.pkt_lost_vs_pktps.png"
set title "Percentage of Packets Lost vs pkt per sec"
set grid
set xlabel "pkt per sec"
set ylabel "% Packets Lost"
plot datafile1 every ::4::7 using 2:7 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::4::7 using 2:7 with linespoints title "RAODV" pointtype 5 pointsize 1.5

#======================================================================================================#
#======================================================================================================#
#======================================================================================================#
#======================================================================================================#

# Plot 1: Average Throughput
set output "09.avg_throughput_vs_node_speed.png"
set title "Average Throughput vs node speed (m/s)"
set grid
set xlabel "node speed"
set ylabel "Average Throughput"
plot datafile1 every ::8::11 using 3:4 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::8::11 using 3:4 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 2: Average Delay
set output "10.avg_delay_vs_node_speed.png"
set title "Average Delay vs node speed"
set grid
set xlabel "node speed"
set ylabel "Average Delay"
plot datafile1 every ::8::11 using 3:5 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::8::11 using 3:5 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 3: Percentage of Packets Received
set output "11.pkt_received_vs_node_speed.png"
set title "Percentage of Packets Received vs node speed"
set grid
set xlabel "node speed"
set ylabel "% Packets Received"
plot datafile1 every ::8::11 using 3:6 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::8::11 using 3:6 with linespoints title "RAODV" pointtype 5 pointsize 1.5

# Plot 4: Percentage of Packets Lost
set output "12.pkt_lost_vs_node_speed.png"
set title "Percentage of Packets Lost vs node speed"
set grid
set xlabel "node speed"
set ylabel "% Packets Lost"
plot datafile1 every ::8::11 using 3:7 with linespoints title "AODV" pointtype 7 pointsize 1.5, \
     datafile2 every ::8::11 using 3:7 with linespoints title "RAODV" pointtype 5 pointsize 1.5