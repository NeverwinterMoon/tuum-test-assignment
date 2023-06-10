-- DROP SCHEMA public CASCADE;
-- CREATE SCHEMA public;

CREATE TABLE country
(
    id   SERIAL PRIMARY KEY,
    name CHAR(50) UNIQUE NOT NULL
);

CREATE TABLE customer
(
    id INTEGER PRIMARY KEY
);

CREATE TABLE currency
(
    id   SERIAL PRIMARY KEY,
    name CHAR(3) UNIQUE NOT NULL
);

CREATE TABLE account
(
    id          SERIAL PRIMARY KEY,
    country_id  INTEGER REFERENCES country (id)  NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT now(),
    customer_id INTEGER REFERENCES customer (id) NOT NULL
);

CREATE TABLE balance
(
    id          SERIAL PRIMARY KEY,
    account_id  INTEGER REFERENCES account (id)  NOT NULL,
    amount      DECIMAL(14, 2)                   NOT NULL CHECK (amount >= 0),
    currency_id INTEGER REFERENCES currency (id) NOT NULL
);

CREATE TYPE transaction_direction AS ENUM ('IN', 'OUT');
CREATE CAST (varchar AS transaction_direction) WITH INOUT AS IMPLICIT;

-- This can be normalized by removing account_id and currency_id, as both can be infered from balance table. But seeing as this is banking system where currency of the balance won't change most probably, I've decided to denormalize data to have potentially more performant queries of transactions
CREATE TABLE transaction
(
    id          SERIAL PRIMARY KEY,
--     denormalization here; we can infer account_id from balance, but to have more performant transaction history querying, we duplicate this here
    account_id  INTEGER REFERENCES account (id)  NOT NULL,
    amount      DECIMAL(14, 2)                   NOT NULL CHECK (amount >= 0),
    balance_id  INTEGER REFERENCES balance (id)  NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT now(),
--     another denormalization, same reason as above
    currency_id INTEGER REFERENCES currency (id) NOT NULL,
    description CHAR(140)                        NOT NULL,
    direction   transaction_direction            NOT NULL
);
