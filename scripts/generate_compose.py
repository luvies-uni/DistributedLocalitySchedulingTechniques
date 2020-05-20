#!/usr/bin/env python3
import yaml

from utils import *

CONSUMER_COUNT = 15

BROKER_NAME = "activemq"
REDIS_NAME = "redis"

# Ensure compose dir exists
run(["mkdir", "-p", COMPOSE_DIR])


def append_dict_list(d, k, v):
    if k in d:
        d[k].append(v)
    else:
        d[k] = [v]


def generate_compose(
    impl: str,
    broker: bool = True,
    redis: bool = False,
    network: Optional[str] = None
):
    def add_generics(svc):
        svc["stop_grace_period"] = "30s"
        if network is not None:
            svc["networks"] = [network]
        return svc

    def add_dependent(svc, name):
        append_dict_list(svc, "depends_on", name)
        return svc

    generator = add_generics({
        "image": image_tag_gen(impl)
    })

    services = {
        impl_gen(impl): generator
    }

    if broker:
        services[BROKER_NAME] = add_generics({
            "image": "rmohr/activemq:5.15.9-alpine"
        })
        add_dependent(generator, BROKER_NAME)

    if redis:
        services[REDIS_NAME] = add_generics({
            "image": "redis:6-alpine"
        })
        add_dependent(generator, REDIS_NAME)

    for i in range(CONSUMER_COUNT):
        consumer = add_generics({
            "image": image_tag_con(impl)
        })

        if broker:
            add_dependent(consumer, BROKER_NAME)

        if redis:
            add_dependent(consumer, REDIS_NAME)

        services[impl_con_n(impl, i)] = consumer

    compose = {
        "version": '3',
        "services": services
    }

    if network is not None:
        compose["networks"] = {
            network: {}
        }

    with open(os.path.join(COMPOSE_DIR, "docker-compose.{}.yml".format(impl)), "w") as fd:
        yaml.dump(compose, fd)


if __name__ == "__main__":
    generate_compose(IMPL_ROUND_ROBIN, network="rr-internal")
    generate_compose(IMPL_DEDICATED_QUEUE, broker=False)
    generate_compose(IMPL_KVS_QUEUE, redis=True, network="kvs-internal")
