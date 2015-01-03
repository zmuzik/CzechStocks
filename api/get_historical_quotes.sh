#!/bin/bash

startStamp=`date +%s`
appRootDir=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
hist_dir=$appRootDir"/hist_data/"
tmp_dir=$appRootDir"/tmp/"
root_url="http://ftp.pse.cz"
isinsConfFile=$appRootDir"/etc/included_isins.csv"
sqlFile=$appRootDir"/tmp/update_db.sql"
logFile=$appRootDir"/log/get_historical_data.log"
dbFile=$appRootDir"/data.db"

for fpath in `ls $hist_dir`
do
  unzip $hist_dir$fpath -d $tmp_dir

  fsize=$((${#fpath}-10))
  datestr=${fpath:$fsize:6}
  akfile=$tmp_dir"AK"$datestr".csv"
  bofile=$tmp_dir"BO"$datestr".csv"

  year="20"${datestr:0:2}
  month=${datestr:2:2}
  day=${datestr:4:2}
  stamp=`TZ="Europe/Prague" date -d "$year-$month-$day" +%s`"000"

  echo "begin transaction;" > $sqlFile
  echo "DELETE FROM current_quote;" >> $sqlFile

  for isin in `grep "^[^#;]" $isinsConfFile | cut -d";" -f1`
  do
    row=`grep $isin $bofile`
    if [ ${#row} -gt 0 ]; then
      price=`echo $row | cut -d"," -f6 | sed 's/ *$//'`
      volume=`echo $row | cut -d"," -f10 | sed 's/ *$//'`
      echo "insert into historical_quote (isin, stamp, price, volume) values ('$isin','$stamp', $price, $volume);" >> $sqlFile
    fi
  done

  echo "commit;" >> $sqlFile
  sqlite3 $dbFile < $sqlFile

  rm $akfile $bofile $sqlFile
done

endStamp=`date +%s`
duration=$((endStamp-startStamp))
now=`date +"%Y-%m-%d %H:%M:%S"`

echo "$now etl performed in $duration seconds" >> $logFile
