#!/usr/bin/env python3
from docker_runner import DockerRunner
import os

CWD = os.getcwd()

NAME = "activemq-dlst"
DOCKER_ARGS = [
    "-p", "61616:61616",
    "-p", "8161:8161",
    "-v", "{}/scripts/conf:/opt/activemq/conf".format(CWD),
    "rmohr/activemq:5.15.9-alpine",
]
HELP = "Manages the ActiveMQ container"

if __name__ == "__main__":
    DockerRunner(NAME, DOCKER_ARGS, HELP).run()
