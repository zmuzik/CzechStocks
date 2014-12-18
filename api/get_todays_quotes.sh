#!/bin/bash
startStamp=`date +%s`
url_prefix="http://www.bcpp.cz/XML/ProduktKontinualJS.aspx?cnpa="
scriptDir=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`

#main app directory
#appRootDir=${scriptDir:0:size=${#scriptDir}-4}
appRootDir=${scriptDir}

#configuration files
isinsConfFile=$appRootDir"/etc/included_isins"

#temporary files used during processing
rawFile=$appRootDir"/tmp/raw.html"
tableFile=$appRootDir"/tmp/table.csv"
isinsFile=$appRootDir"/tmp/isins.csv"
completeFile=$appRootDir"/tmp/complete.csv"
sqlFile=$appRootDir"/tmp/update_db.sql"
logFile=$appRootDir"/log/get_todays_data.log"
dbFile=$appRootDir"/data.db"

echo "begin transaction;" > $sqlFile
echo "DELETE FROM todays_quote;" >> $sqlFile

#for every stock
for confRow in `grep "^[^#;]" $isinsConfFile`
do
  isin=`echo $confRow | cut -d";" -f1`
  id=`echo $confRow | cut -d";" -f2`
  url=$url_prefix$id
  curl -o $rawFile $url

  cat $rawFile | grep "d:new Date" > $tableFile
  while read row
  do
    record=`echo $row | tr ":(,}" " "`
    year=`echo $record | cut -d" " -f4`
    month=`echo $record | cut -d" " -f5`
    if [ $month -lt 12 ]; then
      ((month=1+$month))
    fi
    day=`echo $record | cut -d" " -f6`
    hour=`echo $record | cut -d" " -f7`
    minute=`echo $record | cut -d" " -f8`
    second=`echo $record | cut -d" " -f9`
    
    stamp=`TZ="Europe/Prague" date -d "$year-$month-$day $hour:$minute:$second" +%s`
    price=`echo $record | cut -d" " -f15`
    volume=`echo $record | cut -d" " -f17`
    echo "insert into todays_quote (isin, stamp, price, volume) values ('$isin','$stamp', '$price', '$volume');" >> $sqlFile
  done < $tableFile
  rm $rawFile $tableFile
done

echo "commit;" >> $sqlFile

sqlite3 $dbFile < $sqlFile

rm $sqlFile

endStamp=`date +%s`
duration=$((endStamp-startStamp))
now=`date +"%Y-%m-%d %H:%M:%S"`

echo "$now etl performed in $duration seconds" >> $logFile

