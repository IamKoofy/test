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
        fi
    else
        EXCEPTION_COUNT=0
    fi

    # Check if the exception count exceeds the threshold
    if [ "$EXCEPTION_COUNT" -ge "$THRESHOLD" ]; then
        echo "FAILURE - $EXCEPTION_COUNT exception(s) found for the string '$EXCEPTION_STRING' in log path '$EXCEPTION_LOG_PATH/$POD_NAME/$PODdir/$EXCEPTION_LOGFILE'."
    else
        echo "SUCCESS - $EXCEPTION_COUNT exception(s) found for the string '$EXCEPTION_STRING' in log path '$EXCEPTION_LOG_PATH/$POD_NAME/$PODdir/$EXCEPTION_LOGFILE'. Exception(s) count is within the threshold."
    fi
done
