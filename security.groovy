#!/bin/bash

LOG_PATH="$1"           # Path to the logs directory
THRESHOLD=120           # 2 minutes in seconds
ERROR_CODE="NO_LOG_FOUND"

# Function to check if no logs found for more than 2 minutes
check_no_logs() {
    # Get the latest modification time of logs
    latest_log_time=$(find "$LOG_PATH" -type f -printf '%T@\n' | sort -n | tail -1)
    
    # Calculate time difference in seconds
    time_diff=$(( $(date +%s) - latest_log_time ))
    
    # Check if time difference exceeds threshold
    if [ "$time_diff" -gt "$THRESHOLD" ]; then
        echo "ERROR: No logs found for more than 2 minutes" >&2
        exit 1
    else
        echo "Logs found within 2 minutes"
    fi
}

# Continuous monitoring for logs
while true; do
    # Check for no logs found
    check_no_logs
    
    # Wait for 1 minute before checking again
    sleep 60
done
