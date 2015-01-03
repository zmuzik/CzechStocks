#!/bin/bash
logFile=$appRootDir"/log/get_todays_data.log"

sqlite3 data.db "delete from todays_quote;"

now=`date +"%Y-%m-%d %H:%M:%S"`

echo "$now table todays_quote deleted" >> $logFile
