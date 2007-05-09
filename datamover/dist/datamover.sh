#! /bin/sh

# Mandatory: adapt this to your environment!
LOCAL_DATA="data/local/incoming"
LOCAL_TEMP="data/local/temp"
REMOTE_DATA="data/remote"

CHECK="120"
QUIET="300"
FAIL="120"
STALL="600"
MAX="2"

java -jar lib/datamover.jar --local-datadir $LOCAL_DATA --local-tempdir $LOCAL_TEMP --remotedir $REMOTE_DATA --check-interval $CHECK --quiet-period $QUIET --failure-interval $FAIL --inactivity-period $STALL --max-retries $MAX
