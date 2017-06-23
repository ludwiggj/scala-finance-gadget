# --- !Ups

INSERT INTO USERS
   (NAME, PASSWORD)
VALUES
   ('Admin', 'Admin'),
   ('MissA', 'A'),
   ('MisterB', 'B');

# --- !Downs

DELETE FROM USERS WHERE NAME IN ('Admin', 'MissA', 'MisterB');