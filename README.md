# AllTekkies PPI wallet demo

A demo of the unified PPI platform from the roadmap: **one wallet balance,
one ledger, every channel.** A cash-in at a BC agent, a card payment and a
metro tap all use the same balance, under the same limits and KYC rules.

Written in the [Elegant Objects](https://www.elegantobjects.org/) style:
immutable final classes, no null, no ORM, behaviour added by decorators,
server-rendered HTML via [Takes](https://github.com/yegor256/takes).

## Run

    createdb ppi_demo && createdb ppi_demo_test   # once
    mvn -q compile exec:java                       # local DB, http://localhost:8080/
    mvn -q compile exec:java -Dppi.profile=neon    # Neon DB, http://localhost:8081/
    mvn test                                       # uses ppi_demo_test

Each profile is a file in `config/`: `local.properties` (in git) and
`neon.properties` (not in git, it holds the password; start from
`neon.properties.example`). `PPI_DB_URL` and `PPI_PORT` in the environment
override the file; that is how Render is configured.

On first start it loads demo data: two BC agents (Ravi, ₹20,000 float;
Sunita, ₹500 float) and two wallets (Asha, empty, Minimum KYC; Rahul, with
some history). **Reset demo** on the home page wipes everything and loads
it again. Stop the server with Ctrl+C.

## Demo script

The home page lists it too.

1. Open Asha's wallet: ₹0, Minimum KYC, limit ₹10,000.
2. Cash-in ₹500 at Ravi. His float goes down, he earns ₹2.50 commission.
3. Pay ₹200 by card, tap ₹30 at the metro. Same balance: ₹270.
4. Card payment of ₹1,000: refused, not enough money.
5. Cash-in ₹15,000: refused, over the KYC limit. Upgrade to Full KYC, retry.
6. Suspend the wallet, tap at the metro: refused.
7. Reverse the card payment: money back, on a new linked receipt.
8. Cash-in ₹1,000 at Sunita: refused, float too low. Top up on BC agents.
9. The books: every rupee accounted for, debits equal credits.

## How it works

Every money movement is an `Entry` (`CashIn`, `CashOut`, `CardPayment`,
`MetroTap`, `FloatTopUp`, `Reversal`) that builds balanced journal lines.
They all go through one `ControlledLedger`:

    BalancedLedger → ActiveOnly → NoOverdraft → WithinCap → PgLedger

Each decorator enforces one control and refuses with a plain-English
reason. PostgreSQL enforces the same rules again at COMMIT (balanced,
not below zero, not above cap, not suspended), and makes `txn` and `line`
append-only. Mistakes are fixed by reversal, never by edit.

Amounts are `long` paise. Pages: `/` wallets, `/wallet/N` one wallet,
`/agents` BC agents, `/books` accounts and journal, `/txn/N` a receipt.

## Not in this demo

Tenants and programmes, maker-checker, RBAC, holds, and the UPI, FASTag
and wallet-app channels (shown as "Coming" on the home page). The earlier,
fuller codebase is in `~/ppi_issuance_bc`.
