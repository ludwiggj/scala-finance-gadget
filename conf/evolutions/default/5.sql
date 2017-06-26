# --- !Ups

INSERT IGNORE INTO USERS
   (NAME, PASSWORD)
VALUES
   ('Admin', 'Admin'),
   ('Me', 'Me'),
   ('Spouse', 'Spouse');

# --- !Downs

# --- TODO Note that this is a hack. The correct approach is to delete the entries, but this doesn't work if the
# --- tests are run against a database without a schema. The only way to work around that would be to use a
# --- stored procedure, but I don't want to do that at this stage.
DROP TABLE IF EXISTS DOES_NOT_EXIST;