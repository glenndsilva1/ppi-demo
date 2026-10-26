package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * Tap at a metro gate: the wallet pays the fare, the metro operator is
 * owed it until settlement.
 */
public final class MetroTap implements Entry {

    /**
     * Wallet.
     */
    private final Wallet wallet;

    /**
     * Settlement account of the metro operator.
     */
    private final Account operator;

    /**
     * Station name.
     */
    private final String station;

    /**
     * Fare, in paise.
     */
    private final long fare;

    /**
     * Ctor.
     * @param wlt Wallet
     * @param settlement Settlement account of the metro operator
     * @param stn Station name
     * @param paise Fare, in paise
     */
    public MetroTap(final Wallet wlt, final Account settlement, final String stn,
        final long paise) {
        this.wallet = wlt;
        this.operator = settlement;
        this.station = stn;
        this.fare = paise;
    }

    @Override
    public Operation operation() {
        return Operation.TRANSIT;
    }

    @Override
    public String memo() {
        return String.format("Gate at %s", this.station.trim());
    }

    @Override
    public List<Line> lines() throws IOException {
        return List.of(
            new Debit(this.wallet.account(), this.fare),
            new Credit(this.operator, this.fare)
        );
    }

    @Override
    public List<Txn> reversed() {
        return List.of();
    }
}
