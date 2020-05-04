#!/bin/bash
BASE_DIR="$(pwd)/$(dirname $0)"
NAME=activemq-dlst

ACTIVEMQ_BASE="/opt/activemq"
JMX_PASSWORD_FILE="${ACTIVEMQ_BASE}/conf/jmx.password"
JMX_ACCESS_FILE="${ACTIVEMQ_BASE}/conf/jmx.access"
ACTIVEMQ_SUNJMX_START=(
    "-Dcom.sun.management.jmxremote.port=1616 -Dcom.sun.management.jmxremote.ssl=false"
    "-Dcom.sun.management.jmxremote.password.file=$JMX_PASSWORD_FILE"
    "-Dcom.sun.management.jmxremote.access.file=$JMX_ACCESS_FILE"
)
ACTIVEMQ_SUNJMX_START="${ACTIVEMQ_SUNJMX_START[@]}"

DOCKER_ARGS=(
    "--env ACTIVEMQ_SUNJMX_START=\"$ACTIVEMQ_SUNJMX_START\""
    "-p 61616:61616"
    "-p 8161:8161"
    "-p 1616:1616"
    "rmohr/activemq:5.15.9-alpine"
    "/bin/sh -c \"chmod 400 $JMX_PASSWORD_FILE $JMX_ACCESS_FILE && bin/activemq console\""
)
DOCKER_ARGS="${DOCKER_ARGS[@]}"

./base.sh "$1" "$NAME" "$DOCKER_ARGS" "Manages the ActiveMQ broker container"
