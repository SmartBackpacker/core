CREATE DATABASE sb;

CREATE TABLE airline (
  airline_id serial PRIMARY KEY,
  name varchar (100) NOT NULL
);

CREATE TABLE baggage_policy (
  policy_id serial PRIMARY KEY,
  airline_id int REFERENCES airline (airline_id),
  extra varchar (500),
  website varchar (500)
);

CREATE TABLE baggage_allowance (
  allowance_id serial PRIMARY KEY,
  policy_id int REFERENCES baggage_policy (policy_id),
  baggage_type varchar (25) NOT NULL,
  kgs smallint,
  height smallint NOT NULL,
  width smallint NOT NULL,
  depth smallint NOT NULL
);