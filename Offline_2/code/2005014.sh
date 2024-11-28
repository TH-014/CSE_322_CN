#!/bin/bash

nodecnt=(20 40 70 100)
pktps=(100 200 300 400)
speed=(5 10 15 20)

echo "# nodes,# pkt/sec,speed,avg throughput,avg delay,% pkt received, %pkt lost" > 2005014_sim.csv

for i in ${nodecnt[@]}; do
    ./ns3 run "scratch/manet-routing-compare.cc" -- --flowMonitor=true --nodeCount=$i
    echo "done for $i nodes"
done

for i in ${pktps[@]}; do
    ./ns3 run "scratch/manet-routing-compare.cc" -- --flowMonitor=true --pktPERsec=$i
    echo "done for $i pkt/sec"
done

for i in ${speed[@]}; do
    ./ns3 run "scratch/manet-routing-compare.cc" -- --flowMonitor=true --speed=$i
    echo "done for $i speed"
done

gnuplot plot.gnuplot