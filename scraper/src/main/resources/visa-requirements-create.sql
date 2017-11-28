CREATE TABLE countries (
  id SERIAL PRIMARY KEY,
  code VARCHAR (2) NOT NULL UNIQUE,
  name VARCHAR (100) NOT NULL UNIQUE
);

CREATE TABLE visa_category (
  id SERIAL PRIMARY KEY,
  name VARCHAR (200)
);

CREATE TABLE visa_requirements (
  from_country INT REFERENCES countries (id) NOT NULL,
  to_country INT REFERENCES countries (id) NOT NULL,
  visa_category INT REFERENCES visa_category (id) NOT NULL,
  description VARCHAR (2000),
  PRIMARY KEY (from_country, to_country)
);