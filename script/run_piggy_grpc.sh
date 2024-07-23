#!/bin/bash

# List of file names without extensions
files=("low_small_grpc" "medium_small_grpc" "high_small_grpc"
       "low_medium_grpc" "medium_medium_grpc" "high_medium_grpc"
       "low_large_grpc" "medium_large_grpc" "high_large_grpc"
        )

# Number of repetitions
repeat_count=1

## MongoDB details
#mongo_host="http://145.108.225.14"
#mongo_port="27017"
#database_name="your_database_name"

## Function to clear MongoDB database
#clear_mongodb() {
#    echo "Clearing MongoDB database $database_name..."
#    mongo --host "$mongo_host" --port "$mongo_port" --eval "db.getSiblingDB('$database_name').dropDatabase()" > /dev/null 2>&1
#    if [ $? -ne 0 ]; then
#        echo "Error: Failed to clear MongoDB database"
#    else
#        echo "MongoDB database cleared successfully"
#    fi
#}

for ((i=1; i<=repeat_count; i++)); do
    echo "Starting repetition $i"

    # Counter
    file_count=0
    total_files=${#files[@]}

    # Loop through each file base name
    for file_base in "${files[@]}"; do
        # Define the full file name with extension
        file="${file_base}.py"
        echo "Executing locust -f $file..."

        # Run locust command
        locust -f "$file" --csv="data_process/row_data/$file_base$i" -u 50 -r 10 --headless --host  http://145.108.225.14 > /dev/null 2>&1

        # Check the result of the locust command
        if [ $? -ne 0 ]; then
            echo "Error: locust execution for $file failed"
        else
            echo "$file executed successfully"
        fi
#
#        # Clear MongoDB database
#        clear_mongodb

        # Optional: wait time (in seconds) after each file execution
        echo "Waiting 30 seconds before the next file..."
        sleep 30  # Wait for 30 seconds

        # Update the counter
        ((file_count++))

        # Restart Docker containers every three files
        # if ((file_count % 9 == 0)) || ((file_count == total_files)); then
        #     echo "Restarting Docker containers..."
        #     docker-compose down
        #     docker-compose up -d
        #     echo "Docker containers have been restarted"
        # fi
    done

    echo "Repetition $i completed"

    # Optional: wait time (in seconds) after each repetition
    sleep 30
done

echo "All operations are complete."
