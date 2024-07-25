#!/bin/bash

# List of file names without extensions
files=("low_small_grpc" "medium_small_grpc" "high_small_grpc"
 "low_medium_grpc" "medium_medium_grpc" "high_medium_grpc"
 "low_large_grpc" "medium_large_grpc" "high_large_grpc")

# Number of repetitions
repeat_count=10

sudo_password="green1234"

for ((i=1; i<=repeat_count; i++)); do
    echo "Starting repetition $i"

    # Loop through each file base name
    for file_base in "${files[@]}"; do
        # Define the full file name with extension
        file="${file_base}.py"
        echo "Executing locust -f $file..."

        # Start powertop in background to collect data
        echo $sudo_password | sudo -S powertop --time=150 --html="data_process/row_data/power_data/${file_base}_rep${i}_powertop.html" &
        powertop_pid=$!

        # Run locust command
        locust -f "$file" --csv="data_process/row_data/$file_base$i" -u 50 -r 10 --headless --host http://145.108.225.14 > /dev/null 2>&1

        # Check the result of the locust command
        if [ $? -ne 0 ]; then
            echo "Error: locust execution for $file failed"
        else
            echo "$file executed successfully"
        fi

        # Wait for powertop to finish
        wait $powertop_pid
        if [ $? -ne 0 ]; then
            echo "Error: powertop execution for $file_base failed"
        else
            echo "Powertop for $file_base completed successfully"
        fi

        echo "Waiting 30 seconds before the next file..."
        sleep 30  # Wait for 30 seconds

    done

    echo "Starting idle measurement for repetition $i"

    # Start powertop in background to collect idle data
    echo $sudo_password | sudo -S powertop --time=150 --html="data_process/row_data/power_data/idle_rep${i}_powertop.html" &
    powertop_pid=$!

    # Wait for powertop to finish
    wait $powertop_pid
    if [ $? -ne 0 ]; then
        echo "Error: powertop execution for idle measurement failed"
    else
        echo "Powertop for idle measurement completed successfully"
    fi

    echo "Repetition $i completed"

    # Optional: wait time (in seconds) after each repetition
    sleep 30
done

echo "All operations are complete."