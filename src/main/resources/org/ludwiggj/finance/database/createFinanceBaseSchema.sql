CREATE DATABASE IF NOT EXISTS finance;
GRANT ALL PRIVILEGES ON finance.* TO 'finance'@'localhost' IDENTIFIED BY 'gecko';
FLUSH PRIVILEGES;

USE finance;

CREATE TABLE changelog (
  change_number BIGINT NOT NULL,
  complete_dt TIMESTAMP NOT NULL,
  applied_by VARCHAR(100) NOT NULL,
  description VARCHAR(500) NOT NULL
);

ALTER TABLE changelog ADD CONSTRAINT Pkchangelog PRIMARY KEY (change_number)
;