/* Formatted on 7/7/2015 2:32:30 PM (QP5 v5.256.13226.35538) */
CREATE OR REPLACE PROCEDURE ncs3.sps_rowset (sqlstmt   IN     VARCHAR2,
                                             idlist    IN     NBR_ARRAY_TYP,
                                             ocursor      OUT SYS_REFCURSOR)
AS
BEGIN
   OPEN ocursor FOR sqlstmt USING idlist;
END;
/

CREATE OR REPLACE PROCEDURE ncs3.sps_site (idlist    IN     STR_ARRAY_TYP,
                                           ocursor      OUT SYS_REFCURSOR)
AS
BEGIN
   OPEN ocursor FOR
        SELECT ANTENNA_HEIGHT,
               DT_CRTD,
               DT_UPDT,
               ICON_NAME,
               SITE,
               SITE_ANTENNA_ID,
               SITE_ANTENNA,
               SITE_CONFIG_ID,
               SITE_CONFIG,
               SITE_ID,
               SITE_TYPE_ID,
               SITE_TYPE,
               UNIT_ID,
               UNIT,
               CONTACT_ID,
               CONTACT,
               CITYSTATE_ID,
               CITY,
               STATE,
               ZIP
          FROM ncs3.vwsite
         WHERE site_id IN (SELECT * FROM TABLE (idlist))
      ORDER BY site_id;
END;
/

CREATE OR REPLACE PROCEDURE ncs3.spu_site (
   iANTENNA_HEIGHT    IN     NUMBER,
   iDT_CRTD           IN     TIMESTAMP,
   iDT_UPDT           IN     TIMESTAMP,
   iICON_NAME         IN     VARCHAR2,
   iSITE              IN     VARCHAR2,
   iSITE_ANTENNA_ID   IN     NUMBER,
   iSITE_ANTENNA      IN     VARCHAR2,
   iSITE_CONFIG_ID    IN     NUMBER,
   iSITE_CONFIG       IN     VARCHAR2,
   iSITE_ID           IN     NUMBER,
   iSITE_TYPE_ID      IN     NUMBER,
   iSITE_TYPE         IN     VARCHAR2,
   iUNIT_ID           IN     NUMBER,
   iUNIT              IN     VARCHAR2,
   iCONTACT_ID        IN     NUMBER,
   iCONTACT           IN     VARCHAR2,
   iCITYSTATE_ID      IN     NUMBER,
   iCITY              IN     VARCHAR2,
   iSTATE             IN     VARCHAR2,
   iZIP               IN     VARCHAR2,
   oCOUNT                OUT NUMBER,
   oSQLCODE              OUT NUMBER,
   oSQLMESSAGE           OUT VARCHAR2)
AS
   lSITE_ANTENNA_ID   NUMBER;
   lSITE_CONFIG_ID    NUMBER;
   lSITE_TYPE_ID      NUMBER;
   lUNIT_ID           NUMBER;
BEGIN
   -- initialize return
   oSQLCODE := 0;
   oSQLMESSAGE := '';

   -- validate supplemental IDs
   IF (iSITE_ANTENNA_ID IS NULL)
   THEN
      SELECT SITE_ANTENNA_ID
        INTO lSITE_ANTENNA_ID
        FROM NCS3.VWSITE_ANTENNA
       WHERE SITE_ANTENNA = iSITE_ANTENNA;
   ELSE
      lSITE_ANTENNA_ID := iSITE_ANTENNA_ID;
   END IF;

   IF (iSITE_CONFIG_ID IS NULL)
   THEN
      SELECT SITE_CONFIG_ID
        INTO lSITE_CONFIG_ID
        FROM NCS3.VWSITE_CONFIG
       WHERE SITE_CONFIG = iSITE_CONFIG;
   ELSE
      lSITE_CONFIG_ID := iSITE_CONFIG_ID;
   END IF;

   IF (iSITE_TYPE_ID IS NULL)
   THEN
      SELECT SITE_TYPE_ID
        INTO lSITE_TYPE_ID
        FROM NCS3.VWSITE_TYPE
       WHERE SITE_TYPE = iSITE_TYPE;
   ELSE
      lSITE_TYPE_ID := iSITE_TYPE_ID;
   END IF;

   IF (iUNIT_ID IS NULL)
   THEN
      SELECT UNIT_ID
        INTO lUNIT_ID
        FROM NCS3.VWUNIT
       WHERE UNIT = iUNIT;
   ELSE
      lUNIT_ID := iUNIT_ID;
   END IF;

   -- update the event to close it
   oCOUNT := 0;

   UPDATE ncs3.site
      SET antenna_height = iantenna_height,
          icon_name = iicon_name,
          site = isite,
          site_antenna_id = lsite_antenna_id,
          site_config_id = lsite_config_id,
          site_type_id = lsite_type_id,
          unit_id = lUnit_id
    WHERE site_id = isite_id;

   oCOUNT := SQL%ROWCOUNT;

   -- complete the transaction
   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      oSQLCODE := SQLCODE;
      oSQLMESSAGE := SUBSTR (SQLERRM, 1, 200);
END;
/

CREATE OR REPLACE PROCEDURE ncs3.spd_site (iSITE_ID      IN     NUMBER,
                                           oCOUNT           OUT NUMBER,
                                           oSQLCODE         OUT NUMBER,
                                           oSQLMESSAGE      OUT VARCHAR2)
AS
BEGIN
   -- initialize return
   oSQLCODE := 0;
   oSQLMESSAGE := '';

   -- update the event to close it
   oCOUNT := 0;

   /*
    DELETE ncs3.site
    WHERE site_id = isite_id;
   */
   
   UPDATE ncs3.site
      SET site = site || '_DEL'
    WHERE site_id = isite_id;

   oCOUNT := SQL%ROWCOUNT;

   -- complete the transaction
   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      oSQLCODE := SQLCODE;
      oSQLMESSAGE := SUBSTR (SQLERRM, 1, 200);
END;
/

CREATE OR REPLACE PROCEDURE ncs3.spi_site (
   iANTENNA_HEIGHT    IN     NUMBER,
   iDT_CRTD           IN     TIMESTAMP,
   iDT_UPDT           IN     TIMESTAMP,
   iICON_NAME         IN     VARCHAR2,
   iSITE              IN     VARCHAR2,
   iSITE_ANTENNA_ID   IN     NUMBER,
   iSITE_ANTENNA      IN     VARCHAR2,
   iSITE_CONFIG_ID    IN     NUMBER,
   iSITE_CONFIG       IN     VARCHAR2,
   iSITE_ID           IN     NUMBER,
   iSITE_TYPE_ID      IN     NUMBER,
   iSITE_TYPE         IN     VARCHAR2,
   iUNIT_ID           IN     NUMBER,
   iUNIT              IN     VARCHAR2,
   iCONTACT_ID        IN     NUMBER,
   iCONTACT           IN     VARCHAR2,
   iCITYSTATE_ID      IN     NUMBER,
   iCITY              IN     VARCHAR2,
   iSTATE             IN     VARCHAR2,
   iZIP               IN     VARCHAR2,
   oRECORDID             OUT NUMBER,
   oSQLCODE              OUT NUMBER,
   oSQLMESSAGE           OUT VARCHAR2)
AS
   lSITE_ANTENNA_ID   NUMBER;
   lSITE_CONFIG_ID    NUMBER;
   lSITE_TYPE_ID      NUMBER;
   lUNIT_ID           NUMBER;
BEGIN
   -- initialize return
   oSQLCODE := 0;
   oSQLMESSAGE := '';

   -- validate supplemental IDs
   IF (iSITE_ANTENNA_ID IS NULL)
   THEN
      SELECT SITE_ANTENNA_ID
        INTO lSITE_ANTENNA_ID
        FROM NCS3.VWSITE_ANTENNA
       WHERE SITE_ANTENNA = iSITE_ANTENNA;
   ELSE
      lSITE_ANTENNA_ID := iSITE_ANTENNA_ID;
   END IF;

   IF (iSITE_CONFIG_ID IS NULL)
   THEN
      SELECT SITE_CONFIG_ID
        INTO lSITE_CONFIG_ID
        FROM NCS3.VWSITE_CONFIG
       WHERE SITE_CONFIG = iSITE_CONFIG;
   ELSE
      lSITE_CONFIG_ID := iSITE_CONFIG_ID;
   END IF;

   IF (iSITE_TYPE_ID IS NULL)
   THEN
      SELECT SITE_TYPE_ID
        INTO lSITE_TYPE_ID
        FROM NCS3.VWSITE_TYPE
       WHERE SITE_TYPE = iSITE_TYPE;
   ELSE
      lSITE_TYPE_ID := iSITE_TYPE_ID;
   END IF;

   IF (iUNIT_ID IS NULL)
   THEN
      SELECT UNIT_ID
        INTO lUNIT_ID
        FROM NCS3.VWUNIT
       WHERE UNIT = iUNIT;
   ELSE
      lUNIT_ID := iUNIT_ID;
   END IF;

   -- update the event to close it
   oRECORDID := 0;

   INSERT INTO ncs3.site (SITE_ID,
                          ANTENNA_HEIGHT,
                          ICON_NAME,
                          SITE,
                          SITE_ANTENNA_ID,
                          SITE_CONFIG_ID,
                          SITE_TYPE_ID,
                          UNIT_ID)
        VALUES (iSITE_ID,
                iANTENNA_HEIGHT,
                iICON_NAME,
                iSITE,
                lSITE_ANTENNA_ID,
                lSITE_CONFIG_ID,
                lSITE_TYPE_ID,
                lUNIT_ID)
     RETURNING SITE_ID
          INTO oRECORDID;

   -- complete the transaction
   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      oSQLCODE := SQLCODE;
      oSQLMESSAGE := SUBSTR (SQLERRM, 1, 200);
END;
/

CREATE OR REPLACE PROCEDURE ncs3.sps_site_control_status (
   idlist    IN     STR_ARRAY_TYP,
   ocursor      OUT SYS_REFCURSOR)
AS
BEGIN
   OPEN ocursor FOR
        SELECT DT_CRTD,
               DT_UPDT,
               SITE_CONTROL_STATUS_ID,
               SITE_CORR_GEN_METHOD_ID,
               SITE_CORR_GEN_METHOD,
               SITE_ID,
               SITE,
               SITE_SIDE_ID,
               SITE_SIDE,
               SITE_STATE_ID,
               SITE_STATE,
               SITE_STATUS_ID,
               SITE_STATUS
          FROM ncs3.vwsite_control_status
         WHERE site_id IN (SELECT * FROM TABLE (idlist))
      ORDER BY site_id;
END;
/


CREATE OR REPLACE PROCEDURE ncs3.spu_site_control_status (
   iDT_CRTD                   IN     TIMESTAMP,
   iDT_UPDT                   IN     TIMESTAMP,
   iSITE_CONTROL_STATUS_ID    IN     NUMBER,
   iSITE_CORR_GEN_METHOD_ID   IN     NUMBER,
   iSITE_CORR_GEN_METHOD      IN     VARCHAR2,
   iSITE_ID                   IN     NUMBER,
   iSITE                      IN     VARCHAR2,
   iSITE_SIDE_ID              IN     NUMBER,
   iSITE_SIDE                 IN     VARCHAR2,
   iSITE_STATE_ID             IN     NUMBER,
   iSITE_STATE                IN     VARCHAR2,
   iSITE_STATUS_ID            IN     NUMBER,
   iSITE_STATUS               IN     VARCHAR2,
   oCOUNT                        OUT NUMBER,
   oSQLCODE                      OUT NUMBER,
   oSQLMESSAGE                   OUT VARCHAR2)
AS
    lsite_corr_gen_method_id NUMBER;
    lsite_side_id NUMBER;
    lsite_state_id NUMBER;
    lsite_status_id NUMBER;
BEGIN
   -- initialize return
   oSQLCODE := 0;
   oSQLMESSAGE := '';

   -- validate supplemental IDs
   IF (isite_corr_gen_method_id IS NULL)
   THEN
      SELECT site_corr_gen_method_id
        INTO lsite_corr_gen_method_id
        FROM NCS3.VWSITE_CORR_GEN_METHOD
       WHERE site_corr_gen_method = isite_corr_gen_method;
   ELSE
      lsite_corr_gen_method_id := isite_corr_gen_method_id;
   END IF;

   IF (iSITE_SIDE_ID IS NULL)
   THEN
      SELECT SITE_SIDE_ID
        INTO lSITE_SIDE_ID
        FROM NCS3.VWSITE_SIDE
       WHERE SITE_SIDE = iSITE_SIDE;
   ELSE
      lSITE_SIDE_ID := iSITE_SIDE_ID;
   END IF;

   IF (iSITE_STATE_ID IS NULL)
   THEN
      SELECT SITE_STATE_ID
        INTO lSITE_STATE_ID
        FROM NCS3.VWSITE_STATE
       WHERE SITE_STATE = iSITE_STATE;
   ELSE
      lSITE_STATE_ID := iSITE_STATE_ID;
   END IF;

   IF (iSITE_STATUS_ID IS NULL)
   THEN
      SELECT SITE_STATUS_ID
        INTO lSITE_STATUS_ID
        FROM NCS3.VWSITE_STATUS
       WHERE SITE_STATUS = iSITE_STATUS;
   ELSE
      lSITE_STATUS_ID := iSITE_STATUS_ID;
   END IF;

   -- update the event to close it
   oCOUNT := 0;

   UPDATE ncs3.site_control_status
      SET site_corr_gen_method_id = lsite_corr_gen_method_id,
          site_side_id = lsite_side_id,
          site_state_id = lsite_state_id,
          site_status_id = lsite_status_id
    WHERE site_id = isite_id;

   oCOUNT := SQL%ROWCOUNT;

   -- complete the transaction
   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      oSQLCODE := SQLCODE;
      oSQLMESSAGE := SUBSTR (SQLERRM, 1, 200);
END;
/

CREATE OR REPLACE PROCEDURE ncs3.spd_site_control_status (iSITE_ID      IN     NUMBER,
                                           oCOUNT           OUT NUMBER,
                                           oSQLCODE         OUT NUMBER,
                                           oSQLMESSAGE      OUT VARCHAR2)
AS
BEGIN
   -- initialize return
   oSQLCODE := 0;
   oSQLMESSAGE := '';

   -- update the event to close it
   oCOUNT := 0;

   /*
   UPDATE ncs3.site
      SET site = site || '_DEL'
    WHERE site_id = isite_id;
  */

   oCOUNT := SQL%ROWCOUNT;

   -- complete the transaction
   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      oSQLCODE := SQLCODE;
      oSQLMESSAGE := SUBSTR (SQLERRM, 1, 200);
END;
/

CREATE OR REPLACE PROCEDURE ncs3.spi_site_control_status (
   iDT_CRTD                   IN     TIMESTAMP,
   iDT_UPDT                   IN     TIMESTAMP,
   iSITE_CONTROL_STATUS_ID    IN     NUMBER,
   iSITE_CORR_GEN_METHOD_ID   IN     NUMBER,
   iSITE_CORR_GEN_METHOD      IN     VARCHAR2,
   iSITE_ID                   IN     NUMBER,
   iSITE                      IN     VARCHAR2,
   iSITE_SIDE_ID              IN     NUMBER,
   iSITE_SIDE                 IN     VARCHAR2,
   iSITE_STATE_ID             IN     NUMBER,
   iSITE_STATE                IN     VARCHAR2,
   iSITE_STATUS_ID            IN     NUMBER,
   iSITE_STATUS               IN     VARCHAR2,
   oRECORDID             OUT NUMBER,
   oSQLCODE              OUT NUMBER,
   oSQLMESSAGE           OUT VARCHAR2)
AS
    lsite_corr_gen_method_id NUMBER;
    lsite_side_id NUMBER;
    lsite_state_id NUMBER;
    lsite_status_id NUMBER;
BEGIN
   -- initialize return
   oSQLCODE := 0;
   oSQLMESSAGE := '';

   -- validate supplemental IDs
   IF (isite_corr_gen_method_id IS NULL)
   THEN
      SELECT site_corr_gen_method_id
        INTO lsite_corr_gen_method_id
        FROM NCS3.VWSITE_CORR_GEN_METHOD
       WHERE site_corr_gen_method = isite_corr_gen_method;
   ELSE
      lsite_corr_gen_method_id := isite_corr_gen_method_id;
   END IF;

   IF (iSITE_SIDE_ID IS NULL)
   THEN
      SELECT SITE_SIDE_ID
        INTO lSITE_SIDE_ID
        FROM NCS3.VWSITE_SIDE
       WHERE SITE_SIDE = iSITE_SIDE;
   ELSE
      lSITE_SIDE_ID := iSITE_SIDE_ID;
   END IF;

   IF (iSITE_STATE_ID IS NULL)
   THEN
      SELECT SITE_STATE_ID
        INTO lSITE_STATE_ID
        FROM NCS3.VWSITE_STATE
       WHERE SITE_STATE = iSITE_STATE;
   ELSE
      lSITE_STATE_ID := iSITE_STATE_ID;
   END IF;

   IF (iSITE_STATUS_ID IS NULL)
   THEN
      SELECT SITE_STATUS_ID
        INTO lSITE_STATUS_ID
        FROM NCS3.VWSITE_STATUS
       WHERE SITE_STATUS = iSITE_STATUS;
   ELSE
      lSITE_STATUS_ID := iSITE_STATUS_ID;
   END IF;

   -- update the event to close it
   oRECORDID := 0;

   INSERT INTO ncs3.site_control_status (
               SITE_CONTROL_STATUS_ID,
               SITE_CORR_GEN_METHOD_ID,
               SITE_ID,
               SITE_SIDE_ID,
               SITE_STATE_ID,
               SITE_STATUS_ID)
        VALUES (iSITE_CONTROL_STATUS_ID,
                lsite_corr_gen_method_id,
                iSITE_id,
                lsite_side_id,
                lsite_state_id,
                lsite_status_id)
     RETURNING SITE_CONTROL_STATUS_ID
          INTO oRECORDID;

   -- complete the transaction
   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      oSQLCODE := SQLCODE;
      oSQLMESSAGE := SUBSTR (SQLERRM, 1, 200);
END;
/


/*
 90    3/30/2015 10:56:39.000000 PM    7/6/2015 2:10:54.000000 PM    DET    DETROIT_RST    9    ROHN    4    V4    838    2    PRIMARY    16    ESD DETROIT    -1    UNDEFINED    -1    UNDEFINED    UNDEFINED    0
*/
DESC ncs3.site;

SELECT *
  FROM ncs3.vwsite
 WHERE site_id IN (830, 838);

SELECT *
  FROM ncs3.site
 WHERE site = 'DETROIT';

SELECT *
  FROM ncs3.vwsite
 WHERE site_id IN (111, 830, 838) OR site IN ('MORICHES', 'DHALIWAL', 'DHALIWAL2') OR (site like 'DHALIWAL%');

UPDATE ncs3.site
   SET SITE_ANTENNA_ID = 2, SITE_CONFIG_ID = 1
 WHERE site_id = 111;

UPDATE ncs3.site
   SET site = 'DETROIT', SITE_ANTENNA_ID = 9, SITE_CONFIG_ID = 4
 WHERE site_id = 838;

UPDATE ncs3.site
   SET site = 'WISCONSIN POINT'
 WHERE site_id = 830;

DELETE ncs3.site
 WHERE site LIKE 'DHALIWAL%';

SET SERVEROUTPUT ON SIZE 1000000;

DECLARE
   ocursor    SYS_REFCURSOR;
   lsite_id   NUMBER;
   lsite      VARCHAR2 (150);
BEGIN
   ncs3.sps_site (NBR_ARRAY_TYP (838, 830), ocursor);

   LOOP
      FETCH ocursor INTO lsite_id, lsite;

      EXIT WHEN (ocursor%NOTFOUND);

      DBMS_OUTPUT.put_line (lsite_id || '/' || lsite);
   END LOOP;

   CLOSE ocursor;
END;
/

DECLARE
   ocursor    SYS_REFCURSOR;
   lsite_id   NUMBER;
   lsite      VARCHAR2 (150);
BEGIN
   ncs3.sps_rowset (
      'select site_id, site from ncs3.vwsite where site_id in (select * from table(:idlist))',
      NBR_ARRAY_TYP (838, 830),
      ocursor);

   LOOP
      FETCH ocursor INTO lsite_id, lsite;

      EXIT WHEN (ocursor%NOTFOUND);

      DBMS_OUTPUT.put_line (lsite_id || '/' || lsite);
   END LOOP;

   CLOSE ocursor;
END;
/