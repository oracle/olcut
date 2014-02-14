#!/bin/bash
INSTALLDIR=`dirname $0`
unset CLASSPATH
java -DjiniGroup=$JINI_GROUP -DjiniHome=$INSTALLDIR -DjiniPolicy=$INSTALLDIR/jsk-all.policy -Djava.security.policy=$INSTALLDIR/jsk-all.policy -jar $INSTALLDIR/lib/start.jar $INSTALLDIR/start.config
