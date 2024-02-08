#!/bin/bash

LOG_PATH="$1"
POD_NAME="$2"
LOG_FILE="$3"

# Calculate time-related variables
NOW=$(date +%s)
TWO_MIN_AGO=$((NOW - 120))

# Check if log file exists and is not empty
if [ -s "$LOG_PATH/$POD_NAME/$LOG_FILE" ]; then
    # Check if any log entries are newer than 2 minutes
    LAST_LOG=$(stat -c %Y "$LOG_PATH/$POD_NAME/$LOG_FILE")
    if [ "$LAST_LOG" -gt "$TWO_MIN_AGO" ]; then
        echo "Logs found within the last 2 minutes."
        exit 0
    else
        echo "No logs found within the last 2 minutes."
        exit 1
    fi
else
    echo "Log file not found or empty."
    exit 1
fi
