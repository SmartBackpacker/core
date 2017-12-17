CREATE TABLE vaccine (
  id SERIAL PRIMARY KEY,
  disease VARCHAR (200) NOT NULL,
  description VARCHAR (3000) NOT NULL,
  categories VARCHAR (1000)
);

CREATE TABLE vaccine_recommendations (
  country_code VARCHAR (2),
  vaccine_id INT REFERENCES vaccine (id) NOT NULL,
  PRIMARY KEY (country_code, vaccine_id)
);

CREATE TABLE vaccine_optional (
  country_code VARCHAR (2),
  vaccine_id INT REFERENCES vaccine (id) NOT NULL,
  PRIMARY KEY (country_code, vaccine_id)
);

CREATE TABLE health_alert (
  id SERIAL PRIMARY KEY,
  title VARCHAR (500) NOT NULL,
  weblink VARCHAR (1000),
  description VARCHAR (3000)
);

CREATE TABLE health_notice (
  country_code VARCHAR (2),
  alert_id INT REFERENCES health_alert (id),
  PRIMARY KEY (country_code, alert_id)
);

CREATE TABlE health_alert_level (
  country_code VARCHAR (2) PRIMARY KEY,
  alert_level VARCHAR (500) NOT NULL
);
