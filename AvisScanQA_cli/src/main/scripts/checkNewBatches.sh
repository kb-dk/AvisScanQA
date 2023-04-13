#!/bin/bash

set -e

SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")

BATCHES_DIR_ARG=$1

# Get batches dir from first argument; else use default
BATCHES_DIR="/from-9stars/"
if [[ $BATCHES_DIR_ARG ]]; then
    BATCHES_DIR=$BATCHES_DIR_ARG
fi
if [[ ! -d "$BATCHES_DIR" ]]; then
    echo "ERROR - Missing dir: $BATCHES_DIR"
    exit 1
fi

PROCESSED="$HOME/var/processed_batches"
if [ ! -d "$PROCESSED" ]; then
    echo "ERROR - Missing dir: $PROCESSED"
    exit 1
fi

LOG_FILE="$HOME/logs/AvisScanQA_cli.log"
if [ ! -f "$LOG_FILE" ]; then
    echo "ERROR - Could not find log file: $LOG_FILE"
    exit 1
fi

echo "INFO - Processing batches from $BATCHES_DIR"

while IFS= read -r -d '' BATCH_DIR; do
    # if [ ! -f "$BATCH_DIR/checksums.txt" ]; then
    #     echo "No checksum file. Skipping batch."
    #     continue
    # fi

    if [ ! -f "$BATCH_DIR/transfer_completed" ]; then
        echo "No transfer_completed file. Skipping batch."
        continue
    fi

    BATCH_NAME=$(basename -- "$BATCH_DIR");
    if [ -f "$PROCESSED"/"$BATCH_NAME" ] ; then
        echo "INFO - Already processed $BATCH_NAME"
        continue
    fi
    echo "INFO - Started processing $BATCH_NAME at $(date +'%Y-%m-%d %H:%M:%S')";
    flock --nonblock --conflict-exit-code 0 "/tmp/$BATCH_NAME.lockfile" -c "$SCRIPT_DIR/performCheck.sh '$BATCH_DIR'"
    touch "$PROCESSED"/"$BATCH_NAME"
    if grep "Finished handling of batch $BATCH_NAME" $LOG_FILE; then
        echo "INFO - Finished processed $BATCH_DIR"
    else
        echo "WARNING - Processing $BATCH_DIR may have failed"
    fi
done < <(find "$BATCHES_DIR" -maxdepth 2 \( -name "*_RT[0-9]" -o -name "*_RT[0-9][0-9]" \) -type d -print0)
