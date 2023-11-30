#!/bin/bash

EXCEPTION_LOG_PATH=$1
EXCEPTION_STRING=$2
THRESHOLD=$3
EXCEPTION_LOGFILE=$4
INTERVAL=$5
APP_NAME=$7
POD_NAME=$8

# Calculate time-related variables
sub_min="-${INTERVAL} min"
tim_cur=$(date -u "+%Y-%m-%d %H:%M:%S")
tim_dif=$(date -u "+%Y-%m-%d %H:%M:%S" -d "$sub_min")

# Initialize a variable to track if the exception was found
EXCEPTION_FOUND=false

# Loop through the pods (1-4)
for PODdir in $(find "$EXCEPTION_LOG_PATH/$POD_NAME" -name "$EXCEPTION_LOGFILE" -type f -exec ls -t1 {} + | head -4 | awk -F'/' '{print $10}')
do
    echo "Checking logs for pod: $PODdir"
    
    # Change directory to the specified pod path
    cd "$EXCEPTION_LOG_PATH/$POD_NAME/$PODdir"

    # Extract the records based on the timestamp within the specified time range
    nrec=$(cat "$EXCEPTION_LOGFILE" | /usr/local/bin/jq -r '.timestamp' | awk -v tst="$tim_dif" -v tend="$tim_cur" '{vard="{print $1 $2}";if (($vard >= tst) && ($vard <= tend )) {print NR;exit}} ')

    # Check if there are any records found
    if [ -n "$nrec" ]; then
        if [ "$nrec" -ge 1 ]; then
            # Count the number of exceptions within the specified time range
            EXCEPTION_COUNT=$(cat "$EXCEPTION_LOGFILE" | awk -v strec="$nrec" '{if (NR >= strec) print $0}' | grep -c "$EXCEPTION_STRING")

            # Set EXCEPTION_FOUND to true if the exception is found
            if [ "$EXCEPTION_COUNT" -ge "$THRESHOLD" ]; then
                EXCEPTION_FOUND=true
            fi
        fi
    else
        EXCEPTION_COUNT=0
    fi
done

# Check if the exception was not found or is less than the threshold
if [ "$EXCEPTION_FOUND" == false ]; then
    echo "FAILURE - $EXCEPTION_STRING not found or less than $THRESHOLD times in the log files within the last $INTERVAL hours."
    exit 1
else
    echo "SUCCESS - $EXCEPTION_STRING found and count is within the threshold."
    exit 0
fi
