#!/bin/bash

EXCEPTION_LOG_PATH=$1
EXCEPTION_STRING=$2
POD_DIR=$6
THRESHOLD=$3
EXCEPTION_LOGFILE=$4
INTERVAL=$5
APP_NAME=$7

# Calculate time-related variables
sub_min="-${INTERVAL} min"
tim_cur=$(date -u "+%Y-%m-%d %H:%M:%S")
tim_dif=$(date -u "+%Y-%m-%d %H:%M:%S" -d "$sub_min")

# Change directory to the specified path
cd "$EXCEPTION_LOG_PATH/$APP_NAME/$POD_DIR"

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

# Check if the exception count is zero, meaning the string was not found
if [ "$EXCEPTION_COUNT" -eq 0 ]; then
    echo "FAILURE - The string '$EXCEPTION_STRING' was not found in the log file '$EXCEPTION_LOG_PATH/$APP_NAME/$POD_DIR/$EXCEPTION_LOGFILE' within the last $INTERVAL hours."
    exit 1
else
    echo "SUCCESS - The string '$EXCEPTION_STRING' was found in the log file '$EXCEPTION_LOG_PATH/$APP_NAME/$POD_DIR/$EXCEPTION_LOGFILE' within the last $INTERVAL hours."
    exit 0
fi
