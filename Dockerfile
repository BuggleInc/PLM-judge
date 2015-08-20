from java:8
ADD [".", "/app/webPLM_Worker"]
WORKDIR app/webPLM_Worker
run javac -classpath src/:lib/plm-2.6-pre-20150202.jar:lib/commons-cli-1.1.jar:lib/commons-io-1.2.jar:lib/rabbitmq-client.jar  src/server/Main.java
CMD ["java", "-Djava.security.policy=conf.txt", "-Djava.security.manager", "-cp", "src:lib/*", "server.Main"]
