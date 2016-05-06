#!/bin/sh
DIR=$(date +%Y%m%d_%H%M%S)
mkdir experiment/$1_$DIR
cd venicehub
set -m
java -Djava.net.preferIPv4Stack=true -jar VeniceHub.jar -f ../experiment/$1_$DIR/log.xio.gz & echo $$ > venice
echo "Backgroung job number for venice: $venice"
cd ..
echo "running gradle..."
gradle runNon runInc runAdapt
fg $venice
echo $venice
#echo "q" > /proc/$venice/fd/0


