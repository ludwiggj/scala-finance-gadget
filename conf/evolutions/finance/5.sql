# --- !Ups

INSERT INTO USERS
   (NAME, PASSWORD)
VALUES
   ('Admin', 'Admin'),
   ('Me', 'Me'),
   ('Spouse', 'Spouse');

# --- !Downs

DELETE FROM USERS WHERE NAME IN ('Admin', 'Me', 'Spouse');