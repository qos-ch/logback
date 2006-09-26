// This SQL script creates the required tables by
// ch.qos.logback.classic.db.DBAppender.
//
// It is intended for HSQLDB. 
//

DROP TABLE logging_event_exception IF EXISTS;
DROP TABLE logging_event_property IF EXISTS;
DROP TABLE logging_event IF EXISTS;


CREATE TABLE logging_event 
  (
    timestmp         BIGINT NOT NULL,
    formatted_message  LONGVARCHAR NOT NULL,
    logger_name       VARCHAR(256) NOT NULL,
    level_string      VARCHAR(256) NOT NULL,
    thread_name       VARCHAR(256),
    reference_flag    SMALLINT,
    caller_filename   VARCHAR(256), 
    caller_class      VARCHAR(256), 
    caller_method     VARCHAR(256), 
    caller_line       CHAR(4), 
    event_id          INT NOT NULL IDENTITY
  );


CREATE TABLE logging_event_property
  (
    event_id	      INT NOT NULL,
    mapped_key        VARCHAR(254) NOT NULL,
    mapped_value      LONGVARCHAR,
    PRIMARY KEY(event_id, mapped_key),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );

CREATE TABLE logging_event_exception
  (
    event_id         INT NOT NULL,
    i                SMALLINT NOT NULL,
    trace_line       VARCHAR(256) NOT NULL,
    PRIMARY KEY(event_id, i),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );
