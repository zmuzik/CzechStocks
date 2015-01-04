#!/bin/bash
startStamp=`date +%s`
url_prefix="http://www.bcpp.cz/XML/ProduktKontinualJS.aspx?cnpa="
appRootDir=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
isinsConfFile=$appRootDir"/etc/included_isins.csv"
closedDaysFile=$appRootDir"/etc/closed_days.csv"
rawFile=$appRootDir"/tmp/raw.html"
tableFile=$appRootDir"/tmp/table.csv"
isinsFile=$appRootDir"/tmp/isins.csv"
completeFile=$appRootDir"/tmp/complete.csv"
sqlFile=$appRootDir"/tmp/update_todays_quotes.sql"
logFile=$appRootDir"/log/get_todays_data.log"
dbFile=$appRootDir"/data.db"

#quit if exchange closed today
today=`date +%Y-%m-%d`
if  grep -q $today $closedDaysFile; then
    now=`date +"%Y-%m-%d %H:%M:%S"`
    echo "$now skipping run - stock exchange closed today" >> $logFile
    exit 0;
fi

echo "begin transaction;" > $sqlFile

#for every stock
for confRow in `grep "^[^#;]" $isinsConfFile`
do
  isin=`echo $confRow | cut -d";" -f1`
  id=`echo $confRow | cut -d";" -f2`
  url=$url_prefix$id
  curl -o $rawFile $url
  cat $rawFile | grep "d:new Date" | tr ":(,}" " " > $tableFile

  record=`head -n 1 $tableFile`
  year=`echo $record | cut -d" " -f4`
  month=`echo $record | cut -d" " -f5`
  day=`echo $record | cut -d" " -f6`
  baseStamp=`TZ="Europe/Prague" date -d "$year-$month-$day" +%s`"000"
  oldStamp=`sqlite3 $dbFile "select max(stamp) from todays_quote;"`

  awk -v baseStamp=$baseStamp -v isin=$isin -v oldStamp=$oldStamp '{
     hour = $7;
     minute = $8;
     second = $9;
     stamp = baseStamp + 1000 * second + 60000 * minute + 3600000 * hour;
     price = $15;
     volume = $17;
     if (stamp > oldStamp) {
       printf ("insert into todays_quote (isin, stamp, price, volume) values ('\''%s'\'', %s, %s, %s);\n", isin, stamp, price, volume);
     }
  }' < $tableFile >> $sqlFile

  rm $rawFile $tableFile
done

echo "commit;" >> $sqlFile

sqlite3 $dbFile < $sqlFile

rm $sqlFile

endStamp=`date +%s`
duration=$((endStamp-startStamp))
now=`date +"%Y-%m-%d %H:%M:%S"`

echo "$now etl performed in $duration seconds" >> $logFile
