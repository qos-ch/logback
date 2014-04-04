# This SQL script creates the required tables by ch.qos.logback.access.db.DBAppender
#
# It is intended for PostgreSQL databases.

DROP TABLE    access_event_header;
DROP TABLE    access_event;

CREATE SEQUENCE access_event_id_seq MINVALUE 1 START 1;

CREATE TABLE access_event 
  (
    timestmp          BIGINT NOT NULL,
   	requestURI        VARCHAR(254),
    requestURL        VARCHAR(254),
    remoteHost        VARCHAR(254),
    remoteUser        VARCHAR(254),
    remoteAddr        VARCHAR(254),
    protocol          VARCHAR(254),
    method            VARCHAR(254),
    serverName        VARCHAR(254),
    postContent       VARCHAR(254),
    event_id          INT NOT NULL AUTO_INCREMENT PRIMARY KEY
  );

CREATE TABLE access_event_header
  (
    event_id	      INT NOT NULL,
    header_key        VARCHAR(254) NOT NULL,
    header_value      VARCHAR(1024),
    PRIMARY KEY(event_id, header_key),
    FOREIGN KEY (event_id) REFERENCES access_event(event_id)
  );