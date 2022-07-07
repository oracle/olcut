import time
import sys

def eprint(s: str):
    print(s, file=sys.stderr, flush=True)

if __name__ == '__main__':

    print("Ready", flush=True)
    eprint("Sent Ready")
    sec = 0
    cnt = 0
    # 'random' wait times
    r = {'part_a':10, 'part_b':5, 'part_c':12, 'part_d':11}
    while True:
        try:
            line = input()
        except EOFError:
            eprint("EOFError")
            break
        if len(line.strip()) == 0:
            eprint("Received Empty Line")
        else:
            wait_time = r[line.strip()]
            eprint("Waiting {} seconds".format(wait_time))
            sec += wait_time
            time.sleep(wait_time)
            print("{}:{}:{}".format(cnt, sec, line))
            print("")
            cnt += 1
    eprint("Finished all work")
