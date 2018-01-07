CREATE TABLE visa_restrictions_index (
  country_code VARCHAR (2) PRIMARY KEY,
  rank SMALLINT NOT NULL,
  acc SMALLINT NOT NULL,
  sharing SMALLINT DEFAULT 0
);