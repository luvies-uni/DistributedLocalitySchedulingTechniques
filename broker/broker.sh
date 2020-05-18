#!/bin/sh

BASEDIR=$(dirname $0)
ACTIVEMQ_BASE="$BASEDIR/apache-activemq-5.15.12"

export ACTIVEMQ_SUNJMX_START="-Dcom.sun.management.jmxremote.port=1099 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false"

$ACTIVEMQ_BASE/bin/activemq console
