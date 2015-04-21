#!/bin/bash
pkg="zmuzik.czechstocks.debug"
adb pull /data/data/$pkg/shared_prefs/$pkg.xml
cat $pkg.xml
rm $pkg.xml
