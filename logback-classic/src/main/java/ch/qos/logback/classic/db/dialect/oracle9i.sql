-- This SQL script creates the required tables by ch.qos.logback.classic.db.DBAppender
--
-- It is intended for Oracle 9i databases. Tested on version 9.2

-- The following lines are useful in cleaning any previous tables 

--drop TRIGGER logging_event_id_seq_trig; 
--drop SEQUENCE logging_event_id_seq; 
--drop table logging_event_property; 
--drop table logging_event_exception; 
--drop table logging_event; 


CREATE SEQUENCE logging_event_id_seq MINVALUE 1 START WITH 1;

CREATE TABLE logging_event 
  (
    timestmp         NUMBER(20) NOT NULL,
   	formatted_message  VARCHAR2(4000) NOT NULL,
    logger_name       VARCHAR(254) NOT NULL,
    level_string      VARCHAR(254) NOT NULL,
    thread_name       VARCHAR(254),
    reference_flag    SMALLINT,
    caller_filename   VARCHAR(254) NOT NULL,
    caller_class      VARCHAR(254) NOT NULL,
    caller_method     VARCHAR(254) NOT NULL,
    caller_line       CHAR(4) NOT NULL,
    event_id          NUMBER(10) PRIMARY KEY
  );


CREATE TRIGGER logging_event_id_seq_trig
  BEFORE INSERT ON logging_event
  FOR EACH ROW  
  BEGIN  
    SELECT logging_event_id_seq.NEXTVAL 
    INTO   :NEW.event_id 
    FROM   DUAL;  
  END;
/
-- the / suffix may or may not be needed depending on your SQL Client


CREATE TABLE logging_event_property
  (
    event_id	      NUMBER(10) NOT NULL,
    mapped_key        VARCHAR2(254) NOT NULL,
    mapped_value      VARCHAR2(1024),
    PRIMARY KEY(event_id, mapped_key),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );
  
CREATE TABLE logging_event_exception
  (
    event_id         NUMBER(10) NOT NULL,
    i                SMALLINT NOT NULL,
    trace_line       VARCHAR2(254) NOT NULL,
    PRIMARY KEY(event_id, i),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );
  



