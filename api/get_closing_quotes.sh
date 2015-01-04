#!/bin/bash
startStamp=`date +%s`
url="http://www.bcpp.cz/Kurzovni-Listek/Oficialni-KL/"
appRootDir=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
isinsConfFile=$appRootDir"/etc/included_isins.csv"
closedDaysFile=$appRootDir"/etc/closed_days.csv"
rawFile=$appRootDir"/tmp/raw.html"
tableFile=$appRootDir"/tmp/table.csv"
isinsFile=$appRootDir"/tmp/isins.csv"
completeFile=$appRootDir"/tmp/complete.csv"
sqlFile=$appRootDir"/tmp/update_db.sql"
logFile=$appRootDir"/log/get_closing_quotes.log"
dbFile=$appRootDir"/data.db"

#quit if exchange closed today
today=`date +%Y-%m-%d`

if  grep -q $today $closedDaysFile; then
    now=`date +"%Y-%m-%d %H:%M:%S"`
    echo "$now skipping run - stock exchange closed today" >> $logFile
    exit 0;
fi

curl -o $rawFile $url

timeStr=`grep "<strong>" $rawFile | sed 's/&nbsp;//g' | sed 's/strong//g' | tr "<>." "   "`

year=`echo $timeStr | cut -d' ' -f 3`
month=`echo $timeStr | cut -d' ' -f 2`
day=`echo $timeStr | cut -d' ' -f 1`
hour=17
minute=15
stamp=`TZ="Europe/Prague" date -d "$year-$month-$day $hour:$minute" +%s`
timeStr=`date -d \\@$stamp "+%Y-%m-%d %H:%M"`
stamp=$stamp"000"

# take rows with securities listings, strip them from html and leading whitespaces
grep "Cenne-Papiry/Detail" $rawFile | sed 's|<[^>]*>|;|g' | sed 's/^\s*//' > $tableFile

#extract ISINs
grep "<td class=\"nowrap\">" $rawFile | awk '{\
  isinBeg=index($0, "?isin=") + 6;
  isinEnd=index($0, "#KL\">");
  isinLen=isinEnd-isinBeg;
  print substr($0, isinBeg, isinLen);
}' > $isinsFile

#join the files
paste -d";" $isinsFile $tableFile > $completeFile

echo "begin transaction;" > $sqlFile
echo "delete from current_quote;" >> $sqlFile

#extract data only for securities included in the config file
#store the data into the db
for isin in `grep "^[^#;]" $isinsConfFile | cut -d";" -f1`
do
  dataRow=`grep $isin $completeFile`
  stockName=`echo $dataRow | cut -d";" -f4`
  stockPrice=`echo $dataRow | cut -d";" -f9  | tr "," "." | sed 's/\xc2\xa0//g'`
  stockDelta=`echo $dataRow | cut -d";" -f13 | tr "," "." | sed 's/\xc2\xa0//g'`
  
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
