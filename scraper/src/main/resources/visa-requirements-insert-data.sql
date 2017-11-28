WITH from_view AS (
  SELECT id AS from_id FROM countries WHERE code = 'AR'
),
to_view AS (
  SELECT id AS to_id FROM countries WHERE code = 'BG'
),
visa_cat_view AS (
  SELECT id AS visa_id FROM visa_category WHERE name = 'VisaRequired'
),
desc_view AS (
  SELECT 'hola' AS description
)
INSERT INTO visa_requirements (from_country, to_country, visa_category, description)
SELECT from_id, to_id, visa_id, description FROM from_view, to_view, visa_cat_view, desc_view;