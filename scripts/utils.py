import os
import subprocess
import sys
from typing import *


def run(
    args: List[str],
    shell: Optional[bool] = None,
    capture_output: bool = False,
    bail: bool = True
):
    res = subprocess.run(args, shell=shell, capture_output=capture_output)
    if bail:
        if res.returncode != 0:
            sys.exit(res.returncode)
    else:
        return res


CWD = os.getcwd()
COMPOSE_DIR = os.path.join(CWD, "scripts", "compose")

IMPL_ROUND_ROBIN = "impl-round-robin"
IMPL_DEDICATED_QUEUE = "impl-dedicated-queue"
IMPL_KVS_QUEUE = "impl-redis-queue"

IMPLS = [
    IMPL_ROUND_ROBIN,
    IMPL_DEDICATED_QUEUE,
    IMPL_KVS_QUEUE
]

METRICS_TARGET = "job-metrics"


def impl_gen(impl: str) -> str:
    return "{}-generator".format(impl)


def impl_con(impl: str) -> str:
    return "{}-consumer".format(impl)


def impl_con_n(impl: str, n: int) -> str:
    return "{}-{}".format(impl_con(impl), n)


def image_tag(impl_full: str) -> str:
    return "dlst-{}:latest".format(impl_full)


def image_tag_gen(impl: str) -> str:
    return image_tag(impl_gen(impl))


def image_tag_con(impl: str) -> str:
    return image_tag(impl_con(impl))
