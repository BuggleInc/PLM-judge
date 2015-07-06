# WebPLM - Worker

This is a standalone program intended to serve as the compilation unit for the Programmer's Learning Machine project.

The PLM project can be found at : https://github.com/oster/PLM

The WebPLM project can be found at : https://github.com/BuggleInc/webPLM

### How to :
To use the worker, you need to launch it using the command :
>       java -cp src;lib/commons-cli-1.1.jar;lib/commons-io-1.2.jar;lib/junit.jar;lib/rabbitmq-client-tests.jar;lib/rabbitmq-client.jar;lib/plm-2.6-pre-20150202.jar server.Main

An easier use will be available later, ultimatly leading to a docker file.

Note that this program is intended to be used with the "separation-webPLM" branch of WebPLM. You can find it here :
https://github.com/BuggleInc/webPLM/tree/split

This code is WiP. Use it at your own risk.