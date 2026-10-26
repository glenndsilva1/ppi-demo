package com.ppidemo;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Agent hands cash to a customer: value moves from the wallet back
 * into the agent's float, and the agent earns a commission.
 */
public final class CashOut implements Entry {

    /**
     * Agent.
     */
    private final Agent agent;

    /**
     * Wallet.
     */
    private final Wallet wallet;

    /**
     * Amount, in paise.
     */
    private final long amount;

    /**
     * Account commissions are paid from.
     */
    private final Account expense;

    /**
     * Ctor.
     * @param agt Agent
     * @param wlt Wallet
     * @param paise Amount, in paise
     * @param exp Account commissions are paid from
     */
    public CashOut(final Agent agt, final Wallet wlt, final long paise, final Account exp) {
        this.agent = agt;
        this.wallet = wlt;
        this.amount = paise;
        this.expense = exp;
    }

    @Override
    public Operation operation() {
        return Operation.CASH_OUT;
    }

    @Override
    public String memo() throws IOException {
        return String.format("At %s, %s", this.agent.name(), this.agent.place());
    }

    @Override
    public List<Line> lines() throws IOException {
        final List<Line> lines = new LinkedList<>();
        lines.add(new Debit(this.wallet.account(), this.amount));
        lines.add(new Credit(this.agent.cash(), this.amount));
        final long fee = new Commission(this.amount).value();
        if (fee > 0L) {
            lines.add(new Debit(this.expense, fee));
            lines.add(new Credit(this.agent.commission(), fee));
        }
        return lines;
    }

    @Override
    public List<Txn> reversed() {
        return List.of();
    }
}
