#!/bin/bash

iterations=10
#duration between each iteration
delay=30
script_path_1="./low_small_rest.py"
script_path_2="./medium_small_rest.py"
script_path_3="./high_small_rest.py"
script_path_4="./low_medium_rest.py"
script_path_5="./medium_medium_rest.py"
script_path_6="./high_medium_rest.py"
script_path_7="./low_large_rest.py"
script_path_8="./medium_large_rest.py"
script_path_9="./high_large_rest.py"

users=50
rate=10
users_low=10
rate_low=5
runtime_burst="10s"
reset_time=110
runtime_even="120s"

printf "Burst high groups begins...\n"

for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_burst, iteration=$i"

    python $script_path_1 -u $users -r $rate --run-time $runtime_burst --iteration $i &
    python $script_path_1 -u $users -r $rate --run-time $runtime_burst --iteration $i &
    wait
    sleep $reset_time
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

printf "Burst medium groups begins...\n"
for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_burst, iteration=$i"

    python $script_path_2 -u $users -r $rate --run-time $runtime_burst --iteration $i
    sleep $reset_time
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

printf "Burst low groups begins...\n"
for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_burst, iteration=$i"

    python $script_path_3 -u $users -r $rate --run-time $runtime_burst --iteration $i
    sleep $reset_time
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

printf "Even high groups begins...\n"
for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_even, iteration=$i"
    python $script_path_4 -u $users -r $rate --run-time $runtime_even --iteration $i
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

printf "Even medium groups begins...\n"
for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_even, iteration=$i"

    python $script_path_5 -u $users -r $rate --run-time $runtime_even --iteration $i
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

printf "Even low groups begins...\n"
for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_even, iteration=$i"

    python $script_path_6 -u $users_low -r $rate_low --run-time $runtime_even --iteration $i
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

printf "Even low groups begins...\n"
for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_even, iteration=$i"

    python $script_path_7 -u $users_low -r $rate_low --run-time $runtime_even --iteration $i
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

printf "Even low groups begins...\n"
for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_even, iteration=$i"

    python $script_path_8 -u $users_low -r $rate_low --run-time $runtime_even --iteration $i
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

printf "Even low groups begins...\n"
for ((i=1; i<=iterations; i++))
do
    echo "Running iteration $i..."
    echo "Parameters: pacing=$pacing, users=$users, rate=$rate, runtime=$runtime_even, iteration=$i"

    python $script_path_9 -u $users_low -r $rate_low --run-time $runtime_even --iteration $i
    if [ $i -lt $iterations ]; then
        echo "Waiting for $delay seconds..."
        sleep $delay
    fi
done

echo "All iterations completed."