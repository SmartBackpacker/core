CREATE TABLE visa_category (
  id SERIAL PRIMARY KEY,
  name VARCHAR (200)
);

CREATE TABLE visa_requirements (
  from_country INT REFERENCES countries (id),
  to_country INT REFERENCES countries (id),
  visa_category INT REFERENCES visa_category (id),
  description VARCHAR (2000),
  PRIMARY KEY (from_country, to_country)
);