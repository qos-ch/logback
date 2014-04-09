# Logback: the reliable, generic, fast and flexible logging framework.
# Copyright (C) 1999-2010, QOS.ch. All rights reserved.
#
# See http://logback.qos.ch/license.html for the applicable licensing 
# conditions.

# This SQL script creates the required tables by ch.qos.logback.access.db.DBAppender
#
# It is intended for HSQL databases.


DROP TABLE access_event_header IF EXISTS;
DROP TABLE access_event IF EXISTS;

CREATE TABLE access_event (
  timestmp BIGINT NOT NULL,
  requestURI VARCHAR(254),
  requestURL VARCHAR(254),
  remoteHost VARCHAR(254),
  remoteUser VARCHAR(254),
  remoteAddr VARCHAR(254),
  protocol VARCHAR(254),
  method VARCHAR(254),
  serverName VARCHAR(254),
  postContent VARCHAR(254),
  event_id BIGINT NOT NULL IDENTITY);


CREATE TABLE access_event_header (
  event_id BIGINT NOT NULL,
  header_key  VARCHAR(254) NOT NULL,
  header_value LONGVARCHAR,
  PRIMARY KEY(event_id, header_key),
  FOREIGN KEY (event_id) REFERENCES access_event(event_id));