#!/bin/sh
INSTALLDIR=`dirname $0`
echo $INSTALLDIR
java -DauraGroup=$AURAGROUP -Djava.security.policy=$INSTALLDIR/jsk-all.policy -DauraPolicy=$INSTALLDIR/jsk-all.policy -jar $INSTALLDIR/lib/start.jar $INSTALLDIR/nobrowse.config
