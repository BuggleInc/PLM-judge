# WebPLM - Worker

**THIS REPOSITORY IS DEPRECATED**

The PLM judge was integrated back into the main PLM project
(https://github.com/BuggleInc/PLM). This repository now only contains
the documentation, and some old script and deprecated code chunks.

### Quick Start

Get the dependency:
```shell
sudo apt-get install rabbitmq-server
```

Get the precompiled docker image:
https://github.com/BuggleInc/plm-dockers/

Testing the judge manually:
```shell
# Make sure that rabbitMQ is running locally
$ sudo rabbitmqctl list_queues
# This should report this line (at least): worker_in     0

# Start enough judges for a sbt test in webPLM (there is 306 tests right now)
webPLM/deps/PLM$ ant dist
webPLM/deps/PLM$ for i in `seq 306` ; do java -cp dist/plm-2.6-20151010.jar:.:bin:lib/rabbitmq-client.jar plm.judge.Main ; done

# Start the server using the judge (in another terminal, but on the same host)
webPLM$ PLM_EXECUTION_MODE=TRIBUNAL sbt start

# Start the tests (in a third terminal) once the judge and the server are waiting
webPLM$ sbt test

# Kill all remaining judges (if any). Ctrl-C this command once it start complaining that no judge remain to be killed.
$ while true ; do kill `ps aux|grep java|grep judge|cut -f2 -d' '`; done

# Kill all leaked queues
$ sudo rabbitmqctl stop_app
$ sudo rabbitmqctl reset
$ sudo rabbitmqctl start_app
```

## TODO

- Update the docker files now that the code was moved to PLM.jar
- Update to rabbitMQ-client 5. I'm not even sure of the version currently used.
  - The API changed slightly but I didn't checked in all details
- Move that documentation to a better location (PLM wiki?)
- Unit test the judge.
  - We don't want to rerun all the exercises here, but simply to test:
    - The judge can compile a working code in all languages
    - A code not compiling is correctly handled
    - A code raising an exception is correctly handled
    - A code timeouting is correctly handled
    - Running the tests in parallel should be possible
  - There is some code for that in test/ but it's completely
    deprecated: it's using a server API that does not exist anymore. 
    Plus, it was not using jUnit
