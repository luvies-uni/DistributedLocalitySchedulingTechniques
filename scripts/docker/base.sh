#!/bin/bash

CMD=$1
NAME=$2
DOCKERS_ARGS=$3
HELP=$4

case $CMD in
    up)
        docker run \
            --rm -d \
            --name $NAME \
            $DOCKERS_ARGS
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
        echo -e $HELP
        echo -e "\tup\tStart the container"
        echo -e "\tdown\tTears down the container"
        echo -e "\ttest\tExit code of 0 if the container is running, else 1"
        ;;
esac
