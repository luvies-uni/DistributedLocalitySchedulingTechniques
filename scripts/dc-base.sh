#!/bin/sh

docker-compose -f ./scripts/compose/docker-compose.$IMPL.yml -p $IMPL "$@"
