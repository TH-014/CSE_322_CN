#!/bin/bash

input1="scratch/manet-routing-compare.cc"
input2="scratch/rmanet-routing-compare.cc"
output1="2005014_aodv_sim.csv"
output2="2005014_raodv_sim.csv"

nodecnt=(20 40 70 100)
pktps=(100 200 300 400)
speed=(5 10 15 20)

echo "# nodes,# pkt/sec,speed,avg throughput,avg delay,% pkt received, %pkt lost" > $output1
echo "# nodes,# pkt/sec,speed,avg throughput,avg delay,% pkt received, %pkt lost" > $output2

for i in ${nodecnt[@]}; do
    ./ns3 run "$input1" -- --flowMonitor=true --nodeCount=$i
    echo "aodv done for $i nodes"
    ./ns3 run "$input2" -- --flowMonitor=true --nodeCount=$i
    echo "raodv done for $i nodes"
done

for i in ${pktps[@]}; do
    ./ns3 run "$input1" -- --flowMonitor=true --pktPERsec=$i
    echo "aodv done for $i pkt/sec"
    ./ns3 run "$input2" -- --flowMonitor=true --pktPERsec=$i
    echo "raodv done for $i pkt/sec"
done

for i in ${speed[@]}; do
    ./ns3 run "$input1" -- --flowMonitor=true --speed=$i
    echo "aodv done for $i speed"
    ./ns3 run "$input2" -- --flowMonitor=true --speed=$i
    echo "raodv done for $i speed"
done

gnuplot plot.gnuplot

output_pdf="2005014_report.pdf"

png_files=$(ls | grep -E "^[0-9]+\..*\.png$" | sort -n)

if [ -z "$png_files" ]; then
    echo "No matching PNG files found in the directory."
    exit 1
fi

convert $png_files "$output_pdf"

if [ $? -eq 0 ]; then
    echo "Report generated successfully!"
    rm $png_files
else
    echo "Report generation failed."
fi
