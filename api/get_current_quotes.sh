#!/bin/bash
startStamp=`date +%s`
url="http://www.bcpp.cz/On-Line/Kontinual/"
scriptDir=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`

#main app directory
#appRootDir=${scriptDir:0:size=${#scriptDir}-4}
appRootDir=${scriptDir}

#configuration files
isinsConfFile=$appRootDir"/etc/included_isins.csv"

#temporary files used during processing
rawFile=$appRootDir"/tmp/raw.html"
tableFile=$appRootDir"/tmp/table.csv"
isinsFile=$appRootDir"/tmp/isins.csv"
completeFile=$appRootDir"/tmp/complete.csv"
sqlFile=$appRootDir"/tmp/update_db.sql"
logFile=$appRootDir"/log/etl.log"
dbFile=$appRootDir"/data.db"

#download the raw html file
curl -o $rawFile $url
#curl $url > $rawFile

#extract the timestamp of the data included
timeStr=`grep "Online data:" $rawFile | sed 's|<[^>]*>||g' | awk '{ print $3 $4 $5 " " $7; }' | sed 's/\x0d//g'`
year=`echo $timeStr | tr " " "." | cut -d'.' -f 3`
month=`echo $timeStr | cut -d'.' -f 2`
day=`echo $timeStr | cut -d'.' -f 1`
time=`echo $timeStr | cut -d' ' -f 2`
stamp=`TZ="Europe/Prague" date -d "$year-$month-$day $time" +%s`"000"

# take rows with securities listings, strip them from html and leading whitespaces
grep "<td class=\"nowrap\">" $rawFile | sed 's|<[^>]*>|;|g' | sed 's/^\s*//' > $tableFile

#extract ISINs
grep "<td class=\"nowrap\">" $rawFile | awk '{\
  isinBeg=index($0, "?isin=") + 6;
  isinEnd=index($0, "#OL\">");
  isinLen=isinEnd-isinBeg;
  print substr($0, isinBeg, isinLen);
}' > $isinsFile

#join the files
paste -d";" $isinsFile $tableFile > $completeFile

echo "begin transaction;" > $sqlFile
echo "DELETE FROM current_quote;" >> $sqlFile

#extract data only for securities included in the config file
#store the data into the db
for isin in `grep "^[^#;]" $isinsConfFile | cut -d";" -f1`
do
  dataRow=`grep $isin $completeFile`
  stockName=`echo $dataRow | cut -d";" -f4`
  stockPrice=`echo $dataRow | cut -d";" -f9  | tr "," "." | sed 's/\xc2\xa0//g'`
  stockDelta=`echo $dataRow | cut -d";" -f11 | tr "," "." | sed 's/\xc2\xa0//g'`
  
  echo "insert into current_quote (isin, price, delta, timeStr, stamp) \
  values ('$isin', '$stockPrice', '$stockDelta', '$timeStr', '$stamp');" >> $sqlFile
done

echo "commit;" >> $sqlFile

sqlite3 $dbFile < $sqlFile

rm $rawFile $isinsFile $tableFile $completeFile $sqlFile

endStamp=`date +%s`
duration=$((endStamp-startStamp))
now=`date +"%Y-%m-%d %H:%M:%S"`

echo "$now etl performed in $duration seconds" >> $logFile
