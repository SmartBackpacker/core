ALTER TABLE countries
ADD COLUMN currency VARCHAR (3),
ADD COLUMN schengen BOOLEAN;

ALTER TABLE countries
ALTER COLUMN currency SET NOT NULL,
ALTER COLUMN schengen SET NOT NULL;

-- Data update
UPDATE countries SET schengen = 'no';

INSERT INTO countries (code, name, currency, schengen) VALUES ('XK', 'Kosovo', 'EUR', 'no');