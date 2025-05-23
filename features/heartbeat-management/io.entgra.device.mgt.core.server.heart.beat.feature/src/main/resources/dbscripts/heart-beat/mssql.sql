-- -----------------------------------------------------
-- Table `SERVER_HEART_BEAT_EVENTS`
-- -----------------------------------------------------

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[SERVER_HEART_BEAT_EVENTS]') AND TYPE IN (N'U'))
CREATE  TABLE SERVER_HEART_BEAT_EVENTS (
  ID INT NOT NULL AUTO_INCREMENT,
  HOST_NAME VARCHAR(100)  NOT NULL,
  UUID VARCHAR(100) NOT NULL,
  SERVER_PORT INT NOT NULL,
  LAST_UPDATED_TIMESTAMP DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ID));

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[ELECTED_LEADER_META_INFO]') AND TYPE IN (N'U'))
CREATE  TABLE ELECTED_LEADER_META_INFO (
  UUID VARCHAR(100) NOT NULL,
  ELECTED_TIME DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ACKNOWLEDGED_TASK_LIST VARCHAR(MAX) DEFAULT NULL,
  PRIMARY KEY (UUID));
