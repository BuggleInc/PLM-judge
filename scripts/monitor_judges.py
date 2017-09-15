import daemon
import subprocess
from datetime import date, datetime, time, timedelta
from threading import Timer

LOG_WAITING = "[INFO] Waiting for request...\n"
MARKED_JUDGES = dict()

def mark_judge(judge_name, pid):
    print "Marking judge: {}".format(judge_name)
    MARKED_JUDGES[judge_name] = pid

def unmark_judge(judge_name):
    print "Unmarking judge: {}".format(judge_name)
    del MARKED_JUDGES[judge_name]

def is_waiting(judge_name):
    last_log = subprocess.check_output(["docker", "logs", "--tail=1", judge_name])
    return LOG_WAITING == last_log

def restart_judge(judge_name):
    print "Restarting judge: {}".format(judge_name)
    subprocess.check_output(["docker", "restart", judge_name])
    unmark_judge(judge_name)

def retrieve_start_time(judge_name):
    started_at = subprocess.check_output(["docker", "inspect", "--format", "{{.State.StartedAt}}", judge_name])
    started_at = started_at.split(".")[0]

    started_date = started_at.split("T")[0].split("-")
    started_time = started_at.split("T")[1].split(":")

    year = int(started_date[0])
    month = int(started_date[1])
    day = int(started_date[2])

    started_date = date(year, month, day)

    hour = int(started_time[0]) + 2 # To use the set timezone as now()
    minute = int(started_time[1])
    second = int(started_time[2])

    started_time = time(hour, minute, second)

    return datetime.combine(started_date, started_time)

def monitor_judge(judge_name):
    started_at = retrieve_start_time(judge_name)
    now = datetime.now()
    if (now - started_at) > timedelta(seconds=30):
        if not is_waiting(judge_name):
            try:
                pid = int(subprocess.check_output(["docker top {} | tail -n 1".format(judge_name)], shell=True).split()[1])
                if judge_name in MARKED_JUDGES and pid == MARKED_JUDGES[judge_name]:
                    restart_judge(judge_name)
                else:
                    mark_judge(judge_name, pid)
            except (IndexError, ValueError) as error:
                # IndexError occurs if the container is restarting when processing the command docker top
                # ValueError occurs if the container has not yet (or more?) running process
                print "{} occured".format(error)
    else:
        if judge_name in MARKED_JUDGES:
            unmark_judge(judge_name)

def main():
    print "Marked judges: {}".format(MARKED_JUDGES)
    nb_judges = int(subprocess.check_output(["docker ps | grep judge | wc -l"], shell=True))
    for i in range(1, nb_judges + 1):
        judge_name = "judge_judge_{}".format(i)
        monitor_judge(judge_name)
    thread = Timer(10.0, main)
    thread.start()

with daemon.DaemonContext():
    main()
