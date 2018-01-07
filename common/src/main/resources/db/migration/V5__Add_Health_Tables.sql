CREATE TABLE vaccine (
  id SERIAL PRIMARY KEY,
  disease VARCHAR (200) NOT NULL,
  description VARCHAR (3000) NOT NULL,
  categories VARCHAR (1000)
);

CREATE TABLE vaccine_mandatory (
  country_id INT REFERENCES countries (id),
  vaccine_id INT REFERENCES vaccine (id),
  PRIMARY KEY (country_id, vaccine_id)
);

CREATE TABLE vaccine_recommendations (
  country_id INT REFERENCES countries (id),
  vaccine_id INT REFERENCES vaccine (id),
  PRIMARY KEY (country_id, vaccine_id)
);

CREATE TABLE vaccine_optional (
  country_id INT REFERENCES countries (id),
  vaccine_id INT REFERENCES vaccine (id),
  PRIMARY KEY (country_id, vaccine_id)
);

CREATE TABLE health_alert (
  id SERIAL PRIMARY KEY,
  title VARCHAR (500) NOT NULL,
  weblink VARCHAR (1000),
  description VARCHAR (3000)
);

CREATE TABLE health_notice (
  country_id INT REFERENCES countries (id),
  alert_id INT REFERENCES health_alert (id),
  PRIMARY KEY (country_id, alert_id)
);

CREATE TABlE health_alert_level (
  country_id INT PRIMARY KEY,
  alert_level VARCHAR (500) NOT NULL
);

ALTER TABLE health_alert_level
ADD CONSTRAINT fk_country FOREIGN KEY (country_id) REFERENCES countries (id);