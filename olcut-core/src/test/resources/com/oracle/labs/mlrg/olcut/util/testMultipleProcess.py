#
# Copyright (c) 2004-2022, Oracle and/or its affiliates.
#
# Licensed under the 2-clause BSD license.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

import time
import sys

def eprint(s: str):
    print(s, file=sys.stderr, flush=True)

if __name__ == '__main__':

    print("Ready", flush=True)
    eprint("Sent Ready")
    sec = 0
    cnt = 0
    # expected results
    r = {'part_a':10, 'part_b':15, 'part_c':27, 'part_d':38}
    while True:
        try:
            line = input()
        except EOFError:
            eprint("EOFError")
            break
        if len(line.strip()) == 0:
            eprint("Received Empty Line")
        else:
            return_rows = r[line.strip()]
            eprint("returning {} rows".format(return_rows))
            for i in range(return_rows):
                print("{}:{}:{}".format(cnt, i, line))
            print("")
            cnt += 1
    eprint("Finished all work")
