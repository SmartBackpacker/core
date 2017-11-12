WITH new_airline AS (
    INSERT INTO airline (name)
    VALUES ('Aer Lingus')
    RETURNING airline_id
), new_policy AS (
    INSERT INTO baggage_policy (airline_id, website)
    VALUES (SELECT airline_id FROM new_airline, 'https://www.aerlingus.com/travel-information/baggage-information/cabin-baggage/')
    RETURNING policy_id
)
INSERT INTO baggage_allowance (policy_id, baggage_type, kgs, height, width, depth)
VALUES (SELECT policy_id FROM new_policy, 'CabinBag', 10, 55, 40, 24);

INSERT INTO baggage_allowance (policy_id, baggage_type, height, width, depth)
VALUES (SELECT policy_id FROM new_policy, 'SmallBag', 25, 33, 20);