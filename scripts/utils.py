import os
from docker.utils import *

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
