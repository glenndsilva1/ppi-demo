-- Ledger accounts. Amounts everywhere are in paise (minor units), BIGINT.
-- cap is the most an account may hold; active = FALSE freezes it.
CREATE TABLE account (
  id BIGSERIAL PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  kind TEXT NOT NULL
    CHECK (kind IN ('ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE')),
  title TEXT NOT NULL,
  cap BIGINT NOT NULL DEFAULT 9223372036854775807 CHECK (cap >= 0),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- System accounts every installation has.
INSERT INTO account (code, kind, title) VALUES
  ('BANK_POOL', 'ASSET', 'Escrow pool at the sponsor bank'),
  ('CARD_SETTLEMENT', 'LIABILITY', 'Owed to the card scheme for merchants'),
  ('TRANSIT_SETTLEMENT', 'LIABILITY', 'Owed to the metro operator'),
  ('COMMISSION_EXPENSE', 'EXPENSE', 'Commission paid to BC agents');

-- A transaction groups journal lines; each line debits or credits one account.
-- A reversal points at the transaction it undoes; each can be undone only once.
CREATE TABLE txn (
  id BIGSERIAL PRIMARY KEY,
  operation TEXT NOT NULL CHECK (operation IN
    ('FLOAT_TOPUP', 'CASH_IN', 'CASH_OUT', 'CARD', 'TRANSIT', 'REVERSAL')),
  memo TEXT NOT NULL DEFAULT '',
  reverses BIGINT UNIQUE REFERENCES txn (id),
  created TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((operation = 'REVERSAL') = (reverses IS NOT NULL))
);

CREATE TABLE line (
  id BIGSERIAL PRIMARY KEY,
  txn BIGINT NOT NULL REFERENCES txn (id),
  account BIGINT NOT NULL REFERENCES account (id),
  side CHAR(1) NOT NULL CHECK (side IN ('D', 'C')),
  amount BIGINT NOT NULL CHECK (amount > 0)
);

CREATE INDEX line_txn ON line (txn);
CREATE INDEX line_account ON line (account);

-- The journal is append-only: a mistake is fixed by a reversal, never an edit.
CREATE FUNCTION journal_append_only() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
  RAISE EXCEPTION 'journal is append-only, % on % is forbidden',
    TG_OP, TG_TABLE_NAME;
END;
$$;

CREATE TRIGGER txn_append_only BEFORE UPDATE OR DELETE ON txn
  FOR EACH ROW EXECUTE FUNCTION journal_append_only();
CREATE TRIGGER txn_no_truncate BEFORE TRUNCATE ON txn
  FOR EACH STATEMENT EXECUTE FUNCTION journal_append_only();
CREATE TRIGGER line_append_only BEFORE UPDATE OR DELETE ON line
  FOR EACH ROW EXECUTE FUNCTION journal_append_only();
CREATE TRIGGER line_no_truncate BEFORE TRUNCATE ON line
  FOR EACH STATEMENT EXECUTE FUNCTION journal_append_only();

-- At COMMIT, every touched transaction must have 2+ lines and debits = credits.
-- This is the last line of defence; Java validates first.
CREATE FUNCTION journal_balanced() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
  tid BIGINT;
  debits BIGINT;
  credits BIGINT;
  total INT;
BEGIN
  IF TG_TABLE_NAME = 'txn' THEN
    tid := NEW.id;
  ELSE
    tid := NEW.txn;
  END IF;
  SELECT
    COALESCE(SUM(amount) FILTER (WHERE side = 'D'), 0),
    COALESCE(SUM(amount) FILTER (WHERE side = 'C'), 0),
    COUNT(*)
  INTO debits, credits, total
  FROM line WHERE txn = tid;
  IF total < 2 OR debits <> credits THEN
    RAISE EXCEPTION 'transaction % is unbalanced: % lines, debits %, credits %',
      tid, total, debits, credits;
  END IF;
  RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER txn_balanced AFTER INSERT ON txn
  DEFERRABLE INITIALLY DEFERRED
  FOR EACH ROW EXECUTE FUNCTION journal_balanced();
CREATE CONSTRAINT TRIGGER line_balanced AFTER INSERT ON line
  DEFERRABLE INITIALLY DEFERRED
  FOR EACH ROW EXECUTE FUNCTION journal_balanced();
