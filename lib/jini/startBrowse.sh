#!/bin/bash

INSTALLDIR=$(dirname $0)

if [ -z "$JINI_GROUP" ]; then
    export JINI_GROUP=test-jini
fi

echo "Starting JINI Lookup Service from $INSTALLDIR"
java -cp $INSTALLDIR'/lib/*':~/Projects/olcut/work/dist/slcut.jar \
    -Djava.security.policy=$INSTALLDIR/jsk-all.policy \
    -DauraGroup=$JINI_GROUP \
    -DjiniDir=$INSTALLDIR \
    com.sun.jini.start.ServiceStarter \
    $INSTALLDIR/browse.config
 
