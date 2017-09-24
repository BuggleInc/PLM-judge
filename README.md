# WebPLM - Worker

**THIS REPOSITORY IS DEPRECATED**

The PLM judge was integrated back into the main PLM project
(https://github.com/BuggleInc/PLM). This repository now only contains
some slightly deprecated doc, and some old scripts.

### Quick Start

Get the dependency:
```shell
sudo apt-get install rabbitmq-server
```

Get the precompiled docker image:
https://github.com/BuggleInc/plm-dockers/

Compile the jar manually:
```shell
javac -g -d bin -sourcepath src -cp javac -g -d bin -sourcepath src -cp "lib/*" `find src -name '*.java'`
jar cfe judge.jar main.java.Main -C bin . lib/commons-cli-1.1.jar lib/commons-io-1.2.jar lib/plm-2.6-20151010.jar lib/rabbitmq-client.jar
```


## Test cases

Workers can be tested after modification by simple test cases.
Note that the test cases are intended to handle the worker's use-cases, not the actual PLM compilation. As of now, this part already has tests in the PLM project itself.

Start the judge from the source tree directly:
```shell
java -cp lib/plm-2.6-20151010.jar:.:bin:lib/rabbitmq-client.jar plm.judge.Main
```

Check that rabbitMQ is running: `sudo rabbitmqctl list_queues` should
report: ```
Listing queues
worker_in     0
```

# The following is outdated, don't read

The test manifest is : manifest_tests.txt
It is advised to generate the JAR using the following commands :
```shell
javac -g -d bin_tests -sourcepath src -cp "lib/*" src/test/ValidTests.java
jar cfm0 judge_tests.jar manifest_tests.txt -C bin_tests . lib/commons-cli-1.1.jar lib/commons-io-1.2.jar lib/plm-2.6-pre-20150202.jar lib/rabbitmq-client.jar
```

You can then simply launch the tests using `java -jar judge_tests.jar`

#### Adding test cases.

The test cases are defined in src/tests/ValidTests.java

The list of all test cases are as followed :
```java
		new ValidTests().initWithParams("lessons.welcome", "welcome.lessons.welcome.environment.Environment", "avance();");
```


## Generator

As of 2015-08-05, the worker now has a "generator" function.
The "split" branch of WebPLM is on its way to be stripped from the PLM jar file, as thus it can't generate world data, exercises lists and demonstrations "on demand" as it was done until now.
Therefore, workers now have a build path intended to pre-generate JSON objects which contains these data.

The generator compilation can be launched as followed :
```shell
javac -g -d bin_gen -sourcepath src -cp lib/* src/generator/Generator.java
jar cfm0 judge_gen.jar manifest_gen.txt -C bin_gen . lib/commons-cli-1.1.jar lib/commons-io-1.2.jar lib/plm-2.6-pre-20150202.jar lib/rabbitmq-client.jar
```

then, you can simply launch the `java -jar judge.gen.jar` command to generate both lessonWorld, lessonDemos and webPlmData folders content.
You can also add the "-w", "-o" or "-d" options to change respectively the output world, demos and data folders.

