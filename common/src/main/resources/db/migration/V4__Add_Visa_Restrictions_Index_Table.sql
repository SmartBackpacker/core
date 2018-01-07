CREATE TABLE visa_restrictions_index (
  country_code VARCHAR (2) PRIMARY KEY,
  rank INT NOT NULL,
  acc INT NOT NULL,
  sharing INT DEFAULT 0
);