@echo off

set CHECK="120"
set QUIET="300"
set FAIL="60"
set STALL="600"
set MAX="2"

rem Example for a path with drive letter: "d:/data/remote"
java -jar lib\datamover.jar --local-datadir "data/local/incoming" --local-tempdir "data/local/temp" --remotedir "data/remote" --check-interval %CHECK% --quiet-period %QUIET% --failure-interval %FAIL% --inactivity-period %STALL% --max-retries %MAX% --rsync-executable "bin/rsync" --ssh-executable "bin/ssh"
