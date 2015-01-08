#!/bin/bash
appRootDir=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
logFile=$appRootDir"/log/del_todays_quotes.log"
closedDaysFile=$appRootDir"/etc/closed_days.csv"

#quit if exchange closed today
today=`date +%Y-%m-%d`
if  grep -q $today $closedDaysFile; then
    now=`date +"%Y-%m-%d %H:%M:%S"`
    echo "$now skipping run - stock exchange closed today" >> $logFile
    exit 0;
fi

sqlite3 data.db "delete from todays_quote; vacuum;"

now=`date +"%Y-%m-%d %H:%M:%S"`

echo "$now table todays_quote deleted" >> $logFile
