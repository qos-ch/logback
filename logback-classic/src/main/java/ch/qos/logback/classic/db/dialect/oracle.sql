-- This SQL script creates the required tables by org.apache.log4j.db.DBAppender and 
-- org.apache.log4j.db.DBReceiver.
--
-- It is intended for Oracle databases.

-- Tested successfully on Oracle9i Release 9.2.0.3.0 by James Stauffer

-- The following lines are useful in cleaning any previous tables 

--drop TRIGGER logging_event_id_seq_trig; 
--drop SEQUENCE logging_event_id_seq; 
--drop table logging_event_property; 
--drop table logging_event_exception; 
--drop table logging_event; 


CREATE SEQUENCE logging_event_id_seq MINVALUE 1 START WITH 1;

CREATE TABLE logging_event 
  (
    sequence_number   NUMBER(20) NOT NULL,
    timestamp         NUMBER(20) NOT NULL,
    rendered_message  VARCHAR2(4000) NOT NULL,
    logger_name       VARCHAR2(254) NOT NULL,
    level_string      VARCHAR2(254) NOT NULL,
    ndc               VARCHAR2(4000),
    thread_name       VARCHAR2(254),
    reference_flag    SMALLINT,
    caller_filename   VARCHAR2(254) NOT NULL,
    caller_class      VARCHAR2(254) NOT NULL,
    caller_method     VARCHAR2(254) NOT NULL,
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
  END logging_event_id_seq_trig;


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
  



