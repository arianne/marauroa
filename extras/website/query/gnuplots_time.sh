#!/bin/bash 

mysql mapacman -N -e "$2" >temp_data

echo "#Comment" >  temp_script
echo "set terminal png small x000000 xFFFFFF x606060" >> temp_script
echo "set output \"$1\"" >> temp_script
echo "set size 0.40" >> temp_script
echo "set yrange [$4:$5]" >> temp_script

echo "set xdata time" >> temp_script
echo "set timefmt \"%m/%d/%H\"" >> temp_script
echo "set format x \"  %d/%H\"" >> temp_script
echo "set xtics rotate" >> temp_script
echo "set grid" >> temp_script

#echo "plot \"temp_data\" using 1:2 title \"$3\" with lines" >> temp_script
echo "plot \"temp_data\" using 1:2 notitle with lines" >> temp_script

gnuplot < temp_script
rm temp_script temp_data