package com.ppidemo;

/**
 * Agent's commission on a cash-in or cash-out: 0.5%, rounded down to a paisa.
 */
public final class Commission {

    /**
     * Amount of the cash service, in paise.
     */
    private final long amount;

    /**
     * Ctor.
     * @param paise Amount of the cash service, in paise
     */
    public Commission(final long paise) {
        this.amount = paise;
    }

    /**
     * Commission earned.
     * @return Commission, in paise
     */
    public long value() {
        return Math.multiplyExact(this.amount, 5L) / 1000L;
    }
}
