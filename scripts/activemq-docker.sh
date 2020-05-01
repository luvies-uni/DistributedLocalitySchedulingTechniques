#!/bin/bash
BASE_DIR="$(pwd)/$(dirname $0)"
NAME=activemq

ACTIVEMQ_BASE="/opt/activemq"
JMX_PASSWORD_FILE="${ACTIVEMQ_BASE}/conf/jmx.password"
JMX_ACCESS_FILE="${ACTIVEMQ_BASE}/conf/jmx.access"
ACTIVEMQ_SUNJMX_START=(
 "-Dcom.sun.management.jmxremote.port=1616 -Dcom.sun.management.jmxremote.ssl=false"
 "-Dcom.sun.management.jmxremote.password.file=$JMX_PASSWORD_FILE"
 "-Dcom.sun.management.jmxremote.access.file=$JMX_ACCESS_FILE"
)
ACTIVEMQ_SUNJMX_START="${ACTIVEMQ_SUNJMX_START[@]}"

case $1 in
    up)
        docker run \
            --rm -d \
            --name $NAME \
            --env ACTIVEMQ_SUNJMX_START="$ACTIVEMQ_SUNJMX_START" \
            -p 61616:61616 \
            -p 8161:8161 \
            -p 1616:1616 \
            rmohr/activemq:5.15.9-alpine \
            /bin/sh -c "chmod 400 $JMX_PASSWORD_FILE $JMX_ACCESS_FILE && bin/activemq console"
        ;;
    down)
        docker stop $NAME
        ;;
    test)
        CONTAINER_SEARCH="$(docker ps -a -f name=$NAME | wc -l)"
        if (($CONTAINER_SEARCH > 1)); then
            exit 0
        else
            exit 1
        fi
        ;;
    *)
        echo -e "Manages the ActiveMQ broker"
        echo -e "\tup\tStart the message broker"
        echo -e "\tdown\tTears down the message broker"
        echo -e "\ttest\tExit code of 0 if the message broker is running, else 1"
        ;;
esac
