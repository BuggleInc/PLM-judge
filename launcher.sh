#!/usr/bin/env bash

NB_ERROR=0
MAX_ERROR=5

VERSION="v2.0.1"
FILENAME="PLM-judge-assembly-2.0.1.jar"

MESSAGEQUEUE_ADDR="plm.telecomnancy.univ-lorraine.fr"
export MESSAGEQUEUE_ADDR

terminating() {
    echo "Terminating..."
    exit 1
}

if [ ! -f "$FILENAME" ]; then
  echo "Downloading $FILENAME..."
  wget "https://github.com/BuggleInc/PLM-judge/releases/download/$VERSION/$FILENAME"
  if [ $? != 0 ] ; then
    echo "Error occurred while downloading $FILENAME..."
    terminating
  fi
fi

echo "Start running $FILENAME"

while [ "$NB_ERROR" -lt "$MAX_ERROR" ]; do
  nice java -Xms512m -Xmx1024m -jar "$FILENAME"
  let "NB_ERROR += $?"
done

echo "Too many errors occurred, stop running $FILENAME"
terminating
