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
