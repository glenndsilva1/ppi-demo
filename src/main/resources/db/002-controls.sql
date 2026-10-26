-- At COMMIT, every account a new line touches must be active, must not go
-- below zero and must not go above its cap. Java refuses first, with a
-- friendlier message; this catches anything that slips past, e.g. a race.
CREATE FUNCTION account_controls() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
  acc account%ROWTYPE;
  debits BIGINT;
  credits BIGINT;
  balance BIGINT;
BEGIN
  SELECT * INTO acc FROM account WHERE id = NEW.account;
  IF NOT acc.active THEN
    RAISE EXCEPTION '% is suspended, no money can move in or out', acc.title;
  END IF;
  SELECT
    COALESCE(SUM(amount) FILTER (WHERE side = 'D'), 0),
    COALESCE(SUM(amount) FILTER (WHERE side = 'C'), 0)
  INTO debits, credits
  FROM line WHERE account = acc.id;
  IF acc.kind IN ('ASSET', 'EXPENSE') THEN
    balance := debits - credits;
  ELSE
    balance := credits - debits;
  END IF;
  IF balance < 0 THEN
    RAISE EXCEPTION '% would go below zero', acc.title;
  END IF;
  IF balance > acc.cap THEN
    RAISE EXCEPTION '% would go above its limit', acc.title;
  END IF;
  RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER line_controls AFTER INSERT ON line
  DEFERRABLE INITIALLY DEFERRED
  FOR EACH ROW EXECUTE FUNCTION account_controls();
