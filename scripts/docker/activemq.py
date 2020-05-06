#!/usr/bin/env python3
from docker_runner import DockerRunner

ACTIVEMQ_BASE = "/opt/activemq"
JMX_PASSWORD_FILE = "{}/conf/jmx.password".format(ACTIVEMQ_BASE)
JMX_ACCESS_FILE = "{}/conf/jmx.access".format(ACTIVEMQ_BASE)
ACTIVEMQ_SUNJMX_START = (
    "-Dcom.sun.management.jmxremote.port=1616 -Dcom.sun.management.jmxremote.ssl=false "
    "-Dcom.sun.management.jmxremote.password.file={} "
    "-Dcom.sun.management.jmxremote.access.file={}"
).format(JMX_PASSWORD_FILE, JMX_ACCESS_FILE)

NAME = "activemq-dlst"
DOCKER_ARGS = [
    "--env", "ACTIVEMQ_SUNJMX_START={}".format(ACTIVEMQ_SUNJMX_START),
    "-p", "61616:61616",
    "-p", "8161:8161",
    "-p", "1616:1616",
    "rmohr/activemq:5.15.9-alpine",
    "/bin/sh", "-c", "chmod 400 {} {} && bin/activemq console".format(
        JMX_PASSWORD_FILE, JMX_ACCESS_FILE)
]
HELP = "Manages the ActiveMQ container"

if __name__ == "__main__":
    DockerRunner(NAME, DOCKER_ARGS, HELP).run()
