#!/bin/sh
INSTALLDIR=`dirname $0`
java -DauraGroup=$AURAGROUP -DauraPolicy=$INSTALLDIR/jsk-all.policy -Djava.security.policy=$INSTALLDIR/jsk-all.policy -jar $INSTALLDIR/lib/start.jar $INSTALLDIR/start.config
