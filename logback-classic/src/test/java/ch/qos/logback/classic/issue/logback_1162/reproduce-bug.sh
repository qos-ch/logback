#!/bin/bash

#
# Logback: the reliable, generic, fast and flexible logging framework.
# Copyright (C) 1999-2026, QOS.ch. All rights reserved.
#
# This program and the accompanying materials are dual-licensed under
# either the terms of the Eclipse Public License v2.0 as published by
# the Eclipse Foundation
#
#   or (per the licensee's choosing)
#
# under the terms of the GNU Lesser General Public License version 2.1
# as published by the Free Software Foundation.
#

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
