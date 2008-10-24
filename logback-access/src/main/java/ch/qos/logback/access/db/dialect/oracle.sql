-- This SQL script creates the required tables by ch.qos.logback.access.db.DBAppender
--
-- It is intended for Oracle databases.


CREATE SEQUENCE access_event_id_seq MINVALUE 1 START WITH 1;

CREATE TABLE access_event 
  (
    timestmp          NUMBER(20) NOT NULL,
   	requestURI        VARCHAR(254),
    requestURL        VARCHAR(254),
    remoteHost        VARCHAR(254),
    remoteUser        VARCHAR(254),
    remoteAddr        VARCHAR(254),
    protocol          VARCHAR(254),
    method            VARCHAR(254),
    serverName        VARCHAR(254),
    postContent       VARCHAR(254),
    event_id          NUMBER(10) PRIMARY KEY
  );


CREATE TRIGGER access_event_id_seq_trig
  BEFORE INSERT ON access_event
  FOR EACH ROW  
  BEGIN  
    SELECT access_event_id_seq.NEXTVAL 
    INTO   :NEW.event_id 
    FROM   DUAL;  
  END access_event_id_seq_trig;


CREATE TABLE access_event_header
  (
    event_id	      NUMBER(10) NOT NULL,
    header_key        VARCHAR2(254) NOT NULL,
    header_value      VARCHAR2(1024),
    PRIMARY KEY(event_id, header_key),
    FOREIGN KEY (event_id) REFERENCES access_event(event_id)
  );
  



