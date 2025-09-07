      * BG9C5700: <Automatically registered>                           *
      ******************************************************************
      *B.PD.S                                                          *
      * BATCH/ONLINE ROUTINE FOR ACCESSING THE MASTER TABLE            *
      * TO RETRIEVE THE TRANSITORY ACCOUNT.                            *
      * OPTIONS:                                                       *
      * 1 - INFORM THE ENTITY, CENTER AND ONE OR MORE TRANSITORY       *
      *     ACCOUNT CODES. THE ROUTINE ACCESSES THE TABLE              *
      *     AND RETRIEVES THE CORRESPONDING TRANSITORY ACCOUNT         *
      *     CODES.                                                     *
      * 2 - RETRIEVE ALL THE TRANSITORY ACCOUNTS FOR A                 *
      *     ENTITY AND CENTER RELATED TO FUNDS AND SECURITIES          *
      *B.PD.E                                                          *
      *B.PR.S                                                          *
      * COPYS USED                                                     *
      * ----------------                                               *
      * BGEC570: COMMAREA BG9C5700                                     *
      * BGWC099                                                        *
      * QAWCSQL                                                        *
      * QBEC999                                                        *
      *                                                                *
      * DCLGEN USED                                                    *
      * -----------------                                              *
      * BGGT041: MASTER TABLE OF ACCOUNTS                              *
      * BGVC041                                                        *
      *B.PR.E                                                          *
      ******************************************************************
      *                  IDENTIFICATION DIVISION                       *
      ******************************************************************
       IDENTIFICATION DIVISION.
      *
       PROGRAM-ID.    BG9C5700.
      *
       AUTHOR.        ALNOVA TECHNOLOGIES CORPORATION.
      *
       DATE-WRITTEN.  0001-01-01.
      *
      ******************************************************************
      *                     MODIFICATIONS LOG                          *
      ******************************************************************
      *     CODE       AUTHOR  DATE     DESCRIPTION                    *
      *     ---------- ------- -------- ------------------------------ *
      *B.MD @UK12229DAA USCDBM4 12-08-10 AUTOMATIC GENERATION T125     *
      ******************************************************************
      ******************************************************************
      *                     ENVIRONMENT DIVISION                       *
      ******************************************************************
       ENVIRONMENT DIVISION.
      *
       CONFIGURATION SECTION.
      *
       SPECIAL-NAMES.
      *
           DECIMAL-POINT IS COMMA.
      *
      ******************************************************************
      *                       DATA DIVISION                            *
      ******************************************************************
       DATA DIVISION.
      *
      ******************************************************************
      *                  WORKING-STORAGE SECTION                       *
      ******************************************************************
       WORKING-STORAGE SECTION.
      *
            COPY QAWCSQL.
      *
            COPY BGWC099.
      *
           EXEC SQL
             INCLUDE BGGT041
           END-EXEC.
      *
           EXEC SQL
             INCLUDE BGVC041
           END-EXEC.
      *
       01  VA-SWITCHES.
           05 SW-END                       PIC S9(4) COMP.
      *
           05 SW-ENDTBL                    PIC X(1)    VALUE 'N'.
              88 SW-END-YES                            VALUE 'S'.
              88 SW-END-NO                             VALUE 'N'.
      *
           05 SW-COD-FND                   PIC X(1)    VALUE 'N'.
              88 SW-FND-YES                            VALUE 'S'.
              88 SW-FND-CODNO                          VALUE 'N'.
      *
       01 CO-COUNTERS.
           05 VN-INDEX                     PIC S9(4) COMP.
      *
       01 VA-CONSTANTS.
           05 CN-1                         PIC 9(1)    VALUE 1.
           05 CA-99                        PIC X(2)    VALUE '99'.
           05 CN-50                        PIC 9(2)    VALUE 50.
           05 CA-F                         PIC X(1)    VALUE 'F'.
           05 CA-P                         PIC X(1)    VALUE 'P'.
      *
       01 CN-COD-RECOVERED                 PIC 9(3)    VALUE 0.
      *
           EXEC SQL
             INCLUDE SQLCA
           END-EXEC.
      *
      ******************************************************************
      *                      LINKAGE SECTION                           *
      ******************************************************************
       LINKAGE SECTION.
      *
      *.MC.S @UK12229DAA
      *.CO BGEC570
      *.MC.E @UK12229DAA
            COPY BGEC570.
      *
       01 CA-QBEC999-01.
      *.MC.S @UK12229DAA
      *.CO QBEC999
      *.MC.E @UK12229DAA
           COPY QBEC999.
      *
      ******************************************************************
      *                       PROCEDURE DIVISION                       *
      ******************************************************************
       PROCEDURE DIVISION USING BGEC570 CA-QBEC999-01.
      *
           PERFORM INITIALIZE-FIELDS.
      *
           IF E570-COD-RETURN = '00'
              PERFORM PROCESS-INFORMATION
           END-IF.
      *
           GOBACK.
      *
      ******************************************************************
      *.PN INITIALIZE-FIELDS.                                          *
      *B.PR.S                                                          *
      *                 I N I C I A L I Z A R                          *
      * SE INICIALIZAN LOS CAMPOS DE SALIDA Y SE VERIFICAN LOS DATOS DE*
      * ENTRADA                                                        *
      *B.PR.E                                                          *
      ******************************************************************
       INITIALIZE-FIELDS.
      *
           INITIALIZE QAWCSQL.
           INITIALIZE E570-ERR-OUT
                      CN-COD-RECOVERED.
      *
           MOVE '00'  TO E570-COD-RETURN.
      *
           MOVE CN-1   TO VN-INDEX.
      *
           SET SW-END-NO TO TRUE
      *
           PERFORM CLEARING-ACCOUNT-INITIAL
           UNTIL VN-INDEX > CN-50
      *
           IF E570-OPTION EQUAL TO CA-F OR SPACES
              IF E570-ENT = SPACES OR LOW-VALUES OR
                 E570-CEN  = SPACES OR LOW-VALUES
      *
                 MOVE '10'     TO E570-COD-RETURN
              END-IF
           ELSE
              IF E570-ENT = SPACES OR LOW-VALUES OR
                 E570-TB-ACC-CODCLACC(1) = SPACES OR LOW-VALUES
                 MOVE '10'     TO E570-COD-RETURN
              END-IF
           END-IF.
      *
           IF (E570-OPTION NOT EQUAL SPACES AND CA-F AND CA-P)
              MOVE '10'     TO E570-COD-RETURN
      *
           END-IF
      *
           SET SW-FND-CODNO TO TRUE.
      *
      ******************************************************************
      *.PN CLEARING-ACCOUNT-INITIAL.                                   *
      *B.PR.S                                                          *
      *        I N I C I A L - C T A - E S P                           *
      *B.PR.E                                                          *
      ******************************************************************
       CLEARING-ACCOUNT-INITIAL.
      *
           MOVE SPACES TO E570-TB-ACC-CLACC(VN-INDEX)
           IF E570-TB-ACC-CODCLACC(VN-INDEX) NOT EQUAL SPACES AND
               LOW-VALUES
              ADD CN-1 TO CN-COD-RECOVERED
           END-IF
      *
           ADD CN-1 TO VN-INDEX.
      *
      ******************************************************************
      *.PN PROCESS-INFORMATION.                                        *
      *B.PR.S                                                          *
      *        P R O C E S - I N F O R M A C                           *
      *B.PR.E                                                          *
      ******************************************************************
       PROCESS-INFORMATION.
      *
           MOVE CN-1 TO VN-TB-INDEX
                        VN-INDEX
           IF E570-OPTION EQUAL TO CA-F
              PERFORM ALL-VALFON-TRANSITORY-ACCOUNT
                UNTIL VN-TB-INDEX GREATER CN-MAX-ACCOUNTS  OR
                      VN-INDEX GREATER CN-50
                IF VN-INDEX EQUAL CN-1
                   MOVE '20'   TO E570-COD-RETURN
                END-IF
           ELSE
              IF E570-OPTION EQUAL CA-P
                 PERFORM ALL-TRANSITORY-ACCOUNT
                   UNTIL SW-FND-YES  OR
                      VN-TB-INDEX GREATER CN-MAX-ACCOUNTS
              ELSE
                 PERFORM LIMITED-TRANSITORY-ACCOUNT
                   UNTIL E570-TB-ACC-CODCLACC(VN-INDEX) EQUAL SPACES
                      OR VN-INDEX GREATER CN-50
                 IF SW-FND-CODNO
                   MOVE '20'   TO E570-COD-RETURN
                 END-IF
              END-IF
           END-IF.
      *
      ******************************************************************
      *.PN ALL-VALFON-TRANSITORY-ACCOUNT.                              *
      ******************************************************************
       ALL-VALFON-TRANSITORY-ACCOUNT.
      *
           MOVE E570-ENT  TO V041-ENT
           MOVE E570-CEN  TO V041-CEN-REG
           MOVE TB-TRA-CLACC(VN-TB-INDEX) TO V041-ACC
           EXEC SQL
                SELECT T041_FLG_PLGDACC
                  INTO :V041-FLG-PLGACC
                  FROM BGDT041
                 WHERE T041_ENT = :V041-ENT          AND
                       T041_CEN_REG = :V041-CEN-REG  AND
                       T041_ACC = :V041-ACC
           END-EXEC
           MOVE SQLCODE TO SQL-VALUES
           IF (NOT SQL-88-OK) AND (NOT SQL-88-NOT-FOUND)
              MOVE '99'       TO E570-COD-RETURN
              MOVE SQLCODE    TO E570-SQLCODE
              MOVE SQLERRM    TO E570-SQLERRM
              MOVE 'BGDT041'  TO E570-DES-TABLE
              MOVE 'SELECT'   TO E570-REFERENCE
           ELSE
              IF SQL-88-OK
                 IF V041-FLG-PLGACC EQUAL CA-F
                    MOVE V041-ACC TO E570-TB-ACC-CLACC(VN-INDEX)
                    MOVE TB-TRA-CODCLACC(VN-TB-INDEX)
                                  TO E570-TB-ACC-CODCLACC(VN-INDEX)
                    ADD CN-1 TO VN-INDEX
                                VN-TB-INDEX
                    IF VN-INDEX > CN-50
                       MOVE '40'   TO E570-COD-RETURN
                    END-IF
                 ELSE
                    ADD CN-1 TO VN-TB-INDEX
                 END-IF
              ELSE
                 ADD CN-1 TO VN-TB-INDEX
           END-IF.
      *
      ******************************************************************
      *.PN LIMITED-TRANSITORY-ACCOUNT.                                 *
      ******************************************************************
       LIMITED-TRANSITORY-ACCOUNT.
      *
           IF E570-TB-ACC-CODCLACC(VN-INDEX) EQUAL
                                       TB-TRA-CODCLACC(VN-TB-INDEX)
              MOVE E570-ENT  TO V041-ENT
              MOVE E570-CEN  TO V041-CEN-REG
              MOVE TB-TRA-CLACC(VN-TB-INDEX) TO V041-ACC
              EXEC SQL
                   SELECT T041_FLG_PLGDACC
                     INTO :V041-FLG-PLGACC
                     FROM BGDT041
                    WHERE T041_ENT = :V041-ENT          AND
                          T041_CEN_REG = :V041-CEN-REG  AND
                          T041_ACC =     :V041-ACC
              END-EXEC
              MOVE SQLCODE TO SQL-VALUES
              IF (NOT SQL-88-OK) AND (NOT SQL-88-NOT-FOUND)
                 MOVE '99'       TO E570-COD-RETURN
                 MOVE SQLCODE    TO E570-SQLCODE
                 MOVE SQLERRM    TO E570-SQLERRM
                 MOVE 'BGDT041'  TO E570-DES-TABLE
                 MOVE 'SELECT'   TO E570-REFERENCE
                 ADD CN-1  TO VN-INDEX
              ELSE
                 IF SQL-88-OK
                    MOVE V041-ACC TO E570-TB-ACC-CLACC(VN-INDEX)
                    SET SW-FND-YES TO TRUE
                    ADD CN-1  TO VN-INDEX
                    MOVE CN-1 TO VN-TB-INDEX
                 ELSE
                    MOVE '30'   TO E570-COD-RETURN
                    MOVE CN-1 TO VN-TB-INDEX
                    ADD  CN-1 TO VN-INDEX
                    MOVE SQLCODE    TO E570-SQLCODE
                    MOVE SQLERRM    TO E570-SQLERRM
                    MOVE 'BGDT041'  TO E570-DES-TABLE
                    MOVE 'SELECT'   TO E570-REFERENCE
                 END-IF
              END-IF
           ELSE
              ADD 1 TO VN-TB-INDEX
              IF VN-TB-INDEX GREATER CN-MAX-ACCOUNTS
                 MOVE '30'   TO E570-COD-RETURN
                 MOVE CN-1   TO VN-TB-INDEX
                 ADD  CN-1   TO VN-INDEX
              END-IF
           END-IF.
      *
      ******************************************************************
      *.PN ALL-TRANSITORY-ACCOUNT.                                     *
      ******************************************************************
       ALL-TRANSITORY-ACCOUNT.
      *
           IF E570-TB-ACC-CODCLACC(VN-INDEX) EQUAL
                                       TB-TRA-CODCLACC(VN-TB-INDEX)
              MOVE E570-ENT  TO V041-ENT
              MOVE TB-TRA-CLACC(VN-TB-INDEX) TO V041-ACC
              EXEC SQL
                   SELECT T041_FLG_PLGDACC
                     INTO :V041-FLG-PLGACC
                     FROM BGDT041
                    WHERE T041_ENT = :V041-ENT          AND
                          T041_ACC = :V041-ACC
              END-EXEC
              MOVE SQLCODE TO SQL-VALUES
              IF (NOT SQL-88-OK) AND (NOT SQL-88-SEVERAL) AND
                 (NOT SQL-88-NOT-FOUND)
                 MOVE '99'       TO E570-COD-RETURN
                 MOVE SQLCODE    TO E570-SQLCODE
                 MOVE SQLERRM    TO E570-SQLERRM
                 MOVE 'BGDT041'  TO E570-DES-TABLE
                 MOVE 'SELECT'   TO E570-REFERENCE
              ELSE
                 IF SQL-88-OK OR SQL-88-SEVERAL
                    MOVE V041-ACC TO E570-TB-ACC-CLACC(VN-INDEX)
                    SET SW-FND-YES TO TRUE
                 ELSE
                    MOVE '50'   TO E570-COD-RETURN
                 END-IF
              END-IF
           ELSE
              ADD 1 TO VN-TB-INDEX
              IF VN-TB-INDEX GREATER CN-MAX-ACCOUNTS
                 MOVE '10'   TO E570-COD-RETURN
              END-IF
           END-IF.
      *
      * ALNOVA SERIAL NUMBER: 934F7F7B ********* DO NOT REMOVE *********
