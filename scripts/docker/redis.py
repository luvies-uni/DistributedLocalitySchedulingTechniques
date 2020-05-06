#!/usr/bin/env python3
from docker_runner import DockerRunner

NAME = "redis-dlst"
DOCKER_ARGS = [
    "-p", "6379:6379",
    "redis:6-alpine"
]
HELP = "Manages the redis container"

if __name__ == "__main__":
    DockerRunner(NAME, DOCKER_ARGS, HELP).run()
