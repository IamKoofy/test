#!/bin/bash

# Shell Script to trigger alert if the exception match string is found in the log within the last 5 minutes

EXCEPTION_LOG_PATH=$1      # Base path to the log directory
EXCEPTION_STRING=$2        # Exception string to search for
THRESHOLD=$3               # Threshold for the number of exceptions
EXCEPTION_LOGFILE=$4       # Specific log file (e.g., <podname>/service.log)
INTERVAL=${5:-5}           # Default to 5 minutes if not provided

# Calculate time-related variables
sub_min="-${INTERVAL} min"
tim_cur=$(date "+%Y-%m-%d %H:%M:%S")
tim_dif=$(date "+%Y-%m-%d %H:%M:%S" -d "$sub_min")

# Initialize a variable to track if the exception was found
EXCEPTION_FOUND=false

LOGFILE="${EXCEPTION_LOG_PATH}/${EXCEPTION_LOGFILE}"

echo "Checking logs in file: $LOGFILE"

# Extract the records based on the timestamp within the specified time range
nrec=$(awk -v tim_dif="$tim_dif" -v tim_cur="$tim_cur" '
{
    if ($0 ~ /[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}/) {
        timestamp = substr($0, 1, 19)
        if (timestamp >= tim_dif && timestamp <= tim_cur) {
            print NR
            exit
        }
    }
}
' "$LOGFILE")

# Check if there are any records found
if [ -n "$nrec" ]; then
    # Count the number of exceptions within the specified time range
    EXCEPTION_COUNT=$(awk -v strec="$nrec" -v exception_string="$EXCEPTION_STRING" 'NR >= strec && $0 ~ exception_string { count++ } END { print count }' "$LOGFILE")
    
    # Set EXCEPTION_FOUND to true if the exception count is greater than or equal to the threshold
    if [ "$EXCEPTION_COUNT" -ge "$THRESHOLD" ]; then
        EXCEPTION_FOUND=true
    fi
fi

# Output the result for the Ansible playbook to capture
if [ "$EXCEPTION_FOUND" == true ]; then
    echo "ALERT: $EXCEPTION_STRING found $EXCEPTION_COUNT times in the last $INTERVAL minutes."
    exit 0
else
    echo "NO_ALERT: $EXCEPTION_STRING not found or less than $THRESHOLD times in the log file within the last $INTERVAL minutes."
    exit 1
fi
