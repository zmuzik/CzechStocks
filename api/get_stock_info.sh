#!/bin/bash
startStamp=`date +%s`
url_prefix="www.bcpp.cz/Cenne-Papiry/Detail.aspx?isin="
url_postfix="#OL"
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
logFile=$appRootDir"/log/get_stock_info.log"
dbFile=$appRootDir"/data.db"


echo "begin transaction;" > $sqlFile
echo "DELETE FROM stock_info;" >> $sqlFile

#for every stock
for isin in `grep "^[^#;]" $isinsConfFile | cut -d";" -f1`
do
  url=$url_prefix$isin$url_postfix
  curl -o $rawFile $url  
  cat $rawFile | awk '/Vybrané ukazatele/{f=1;next} /\/table/{f=0} f' | sed 's|</th><td>|;|' | sed 's|<[^>]*>||g' > $tableFile
  while read row
  do
    indicator=`echo $row | cut -d";" -f1`
    value=`echo $row | cut -d";" -f2 | tr "," "." | tr -d '\r' | sed 's/\xc2\xa0//g'`
    echo "insert into stock_info (isin, indicator, value) values ('$isin','$indicator', '$value');" >> $sqlFile
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

