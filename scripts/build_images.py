#!/usr/bin/env python3
import yaml

from utils import *

BUILD_COMPOSE = os.path.join(COMPOSE_DIR, "docker-compose.build.yml")

if __name__ == "__main__":
    services = {
        METRICS_TARGET: {
            "image": image_tag(METRICS_TARGET),
            "build": {
                "context": os.path.join("..", ".."),
                "args": {
                    "TARGET": METRICS_TARGET
                }
            }
        }
    }

    for impl in IMPLS:
        for impl_full in [impl_gen(impl), impl_con(impl)]:
            services[impl_full] = {
                "image": image_tag(impl_full),
                "build": {
                    "context": os.path.join("..", ".."),
                    "args": {
                        "TARGET": impl_full
                    }
                }
            }

    compose = {
        "version": '3',
        "services": services
    }

    with open(BUILD_COMPOSE, "w") as fd:
        yaml.dump(compose, fd)

    run([
        "docker-compose",
        "-f", BUILD_COMPOSE,
        "build"
    ])
