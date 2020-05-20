#!/usr/bin/env python3
import yaml

from utils import *

CONSUMER_COUNT = 15

# Ensure compose dir exists
run(["mkdir", "-p", COMPOSE_DIR])


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

    services = {
        impl_gen(impl): add_generics({
            "image": image_tag_gen(impl)
        })
    }

    if broker:
        services["activemq"] = add_generics({
            "image": "rmohr/activemq:5.15.9-alpine"
        })

    if redis:
        services["redis"] = add_generics({
            "image": "redis:6-alpine"
        })

    for i in range(CONSUMER_COUNT):
        services[impl_con_n(impl, i)] = add_generics({
            "image": image_tag_con(impl)
        })

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
