# Scripts

## monitor_judges

Despite our efforts to stop and restart properly the judges after each execution request, in some cases the judges stay alive but as zombies.

In this weird state, the judge behaves accordingly to one of the following:
  - The judge seems to be idle, but is still connected to the message queue. Thus it still receives execution requests but does not handle them. Then all execution requests send to the judge result in timeouts.
  - The judge seems to be still running the student's code despite the scheduled interruption (10s of execution). Thus it is still consuming ressources uselessly.

This goal of this script is to monitor started containers and to restart containers identified as zombies.
A judge is identified as a zombie if:
  - It is alive for longer than 30s.
  - It is not waiting for an execution request (thus it is processing one).
  - It is still processing the same execution request 10s later (the PID of the java command is still the same).

### Usage

This script is compatible with Python >= 2.7

First, you may need to install the dependency of this script:

```
pip install python-daemon
```

Then run the script with:

```
python monitor_judges.py
```
