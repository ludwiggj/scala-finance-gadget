# --- !Ups

INSERT IGNORE INTO FUNDS
   (NAME)
VALUES
   ('H Bear Beer Emporium'),
   ('Hoobs Soups'),
   ('Quantum Inc');

# --- !Downs

DELETE FROM FUNDS;