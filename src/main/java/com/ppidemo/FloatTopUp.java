package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * Agent deposits money at the sponsor bank to raise their float.
 */
public final class FloatTopUp implements Entry {

    /**
     * Escrow account at the sponsor bank.
     */
    private final Account bank;

    /**
     * Agent.
     */
    private final Agent agent;

    /**
     * Amount, in paise.
     */
    private final long amount;

    /**
     * Ctor.
     * @param pool Escrow account at the sponsor bank
     * @param agt Agent
     * @param paise Amount, in paise
     */
    public FloatTopUp(final Account pool, final Agent agt, final long paise) {
        this.bank = pool;
        this.agent = agt;
        this.amount = paise;
    }

    @Override
    public Operation operation() {
        return Operation.FLOAT_TOPUP;
    }

    @Override
    public String memo() throws IOException {
        return String.format("Deposit by %s", this.agent.name());
    }

    @Override
    public List<Line> lines() throws IOException {
        return List.of(
            new Debit(this.bank, this.amount),
            new Credit(this.agent.cash(), this.amount)
        );
    }

    @Override
    public List<Txn> reversed() {
        return List.of();
    }
}
