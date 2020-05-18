import subprocess
import os
import sys
from typing import List


def run(args, shell=None, capture_output=False, bail=True):
    res = subprocess.run(args, shell=shell, capture_output=capture_output)
    if bail:
        if res.returncode != 0:
            sys.exit(res.returncode)
    else:
        return res


class DockerRunner:
    def __init__(self, name: str, docker_args: List[str], help: str):
        self._name = name
        self._docker_args = docker_args
        self._help_msg = help

    def run(self):
        cmd = None
        if len(sys.argv) > 1:
            cmd = sys.argv[1].lower().strip()

        if cmd == "up":
            self._up()
        elif cmd == "down":
            self._down()
        elif cmd == "reset":
            self._reset()
        elif cmd == "test":
            self._test()
        else:
            self._help()

    def _up(self):
        run(["docker", "run", "--rm", "-d", "--name", self._name] + self._docker_args)

    def _down(self):
        run(["docker", "stop", self._name], bail=False)

    def _reset(self):
        self._down()
        self._up()

    def _test(self):
        res = run(["docker", "ps", "-a", "-f",
                   "name={}".format(self._name)], capture_output=True, bail=False)
        if len(res.stdout.splitlines()) > 1:
            print("Container is running")
            sys.exit(0)
        else:
            print("Container is down")
            sys.exit(1)

    def _help(self):
        print(self._help_msg)
        print("\tup\tStart the container")
        print("\tdown\tTears down the container")
        print("\treset\tResets the container")
        print("\ttest\tOutputs whether the container is running (supports exit codes)")
