package com.ppidemo;

/**
 * Receipt number of a transaction, e.g. "PPI-000012".
 */
public final class Receipt {

    /**
     * Transaction number.
     */
    private final long txn;

    /**
     * Ctor.
     * @param id Transaction number
     */
    public Receipt(final long id) {
        this.txn = id;
    }

    @Override
    public String toString() {
        return String.format("PPI-%06d", this.txn);
    }
}
