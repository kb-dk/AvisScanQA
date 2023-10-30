#!/bin/bash

set -e

SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")
TEMP_DIR=/tmp/avisscanqa_tmp
BATCHES_DIR="/from-9stars/"
PROCESSED="$HOME/var/processed_batches"
SKIPPED="$HOME/var/skipped_batches"
LOG_FILE="$HOME/logs/AvisScanQA_cli.log"

BATCHES_DIR_ARG=$1

mkdir -p $TEMP_DIR

# Get batches dir from first argument; else use default
if [[ $BATCHES_DIR_ARG ]]; then
    BATCHES_DIR=$BATCHES_DIR_ARG
fi
if [[ ! -d "$BATCHES_DIR" ]]; then
    echo "ERROR - Missing dir: $BATCHES_DIR"
    exit 1
fi

if [ ! -d "$PROCESSED" ]; then
    echo "ERROR - Missing dir: $PROCESSED"
    exit 1
fi

if [ ! -d "$SKIPPED" ]; then
    echo "ERROR - Missing dir: $SKIPPED"
    exit 1
fi

if [ ! -f "$LOG_FILE" ]; then
    echo "ERROR - Could not find log file: $LOG_FILE"
    exit 1
fi

echo "INFO - Processing batches from $BATCHES_DIR"

while IFS= read -r -d '' BATCH_DIR; do
    BATCH_NAME=$(basename -- "$BATCH_DIR");

    if [ -f "$PROCESSED"/"$BATCH_NAME" ] ; then
        echo "INFO - Already processed $BATCH_NAME"
        continue
    fi

    if [ ! -f "$BATCH_DIR/transfer_complete" ]; then
        echo "WARNING - No transfer_complete file for $BATCH_NAME. Skipping batch."
        touch "$SKIPPED"/"$BATCH_NAME"
        continue
    fi

    if [ ! -f "$BATCH_DIR/$BATCH_NAME.md5.txt" ]; then
        echo "WARNING - No md5 checksum file for $BATCH_NAME. Skipping batch."
        touch "$SKIPPED"/"$BATCH_NAME"
        continue
    fi

    if [ ! -d "$BATCH_DIR/JPEG/" ]; then
        echo "WARNING - No JPEG dir found for $BATCH_NAME. Skipping batch."
        touch "$SKIPPED"/"$BATCH_NAME"
        continue
    fi

    echo "INFO - Started processing $BATCH_NAME at $(date +'%Y-%m-%d %H:%M:%S')";
    flock --nonblock --conflict-exit-code 0 "$TEMP_DIR/$BATCH_NAME.lockfile" -c "$SCRIPT_DIR/performCheck.sh '$BATCH_DIR'"
    touch "$PROCESSED"/"$BATCH_NAME"
    if grep "Registering results of QA on batch $BATCH_NAME in database" $LOG_FILE; then
        echo "INFO - Finished processing $BATCH_DIR"
    else
        echo "WARNING - Processing $BATCH_DIR may have failed"
    fi
done < <(find "$BATCHES_DIR" -maxdepth 2 \( -name "*_RT[0-9]" -o -name "*_RT[0-9][0-9]" \) -type d -print0)