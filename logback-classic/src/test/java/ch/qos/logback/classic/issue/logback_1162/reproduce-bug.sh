#!/bin/bash

set -e

DD="dd"
TOUCH="touch"

TARGET_DIR=/home/ceki/logback/logback-classic/target/test-output/logback_issue_1162/

rm -rf $TARGET_DIR && \
  mkdir -p $TARGET_DIR  && \
  $DD if=/dev/urandom of=$TARGET_DIR/info.log bs=1M count=50 && \
  $TOUCH -d "24 hours ago" $TARGET_DIR/info.log 
#./gradlew run && \
#du -hs logs/*
