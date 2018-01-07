CREATE TABLE airline (
  airline_id SERIAL PRIMARY KEY,
  name VARCHAR (100) NOT NULL UNIQUE
);

CREATE TABLE baggage_policy (
  policy_id SERIAL PRIMARY KEY,
  airline_id INT REFERENCES airline (airline_id),
  extra VARCHAR (500),
  website VARCHAR (500)
);

CREATE TABLE baggage_allowance (
  allowance_id SERIAL PRIMARY KEY,
  policy_id INT REFERENCES baggage_policy (policy_id),
  baggage_type VARCHAR (25) NOT NULL,
  kgs INT,
  height INT NOT NULL,
  width INT NOT NULL,
  depth INT NOT NULL
);