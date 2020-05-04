#!/bin/sh
NAME=redis-dlst

DOCKER_ARGS=(
    "-p 6379:6379"
    "redis:6-alpine"
)
DOCKER_ARGS="${DOCKER_ARGS[@]}"

./base.sh "$1" "$NAME" "$DOCKER_ARGS" "Manages the redis container"
