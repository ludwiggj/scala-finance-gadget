# --- !Ups

INSERT INTO USERS
   (NAME, PASSWORD)
VALUES
   ('Admin', 'Admin'),
   ('Me', 'Me'),
   ('Spouse', 'Spouse');

# --- !Downs

DROP TABLE IF EXISTS DOES_NOT_EXIST;