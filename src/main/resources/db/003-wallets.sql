-- A BC agent (business correspondent) serves customers with cash. The agent
-- prefunds a float; cash-in moves value from that float into a wallet.
CREATE TABLE agent (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL CHECK (length(trim(name)) > 0),
  place TEXT NOT NULL,
  float_account BIGINT NOT NULL UNIQUE REFERENCES account (id),
  commission_account BIGINT NOT NULL UNIQUE REFERENCES account (id),
  created TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- A customer wallet: one balance behind every channel.
CREATE TABLE wallet (
  id BIGSERIAL PRIMARY KEY,
  holder TEXT NOT NULL CHECK (length(trim(holder)) > 0),
  mobile TEXT NOT NULL UNIQUE CHECK (mobile ~ '^[6-9][0-9]{9}$'),
  tier TEXT NOT NULL CHECK (tier IN ('MINIMUM', 'FULL')),
  account BIGINT NOT NULL UNIQUE REFERENCES account (id),
  created TIMESTAMPTZ NOT NULL DEFAULT now()
);
