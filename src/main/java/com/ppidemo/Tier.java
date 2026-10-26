package com.ppidemo;

/**
 * KYC tier of a wallet; it sets how much the wallet may hold.
 */
public enum Tier {

    /**
     * Minimum KYC: mobile number and self-declaration only.
     */
    MINIMUM("Minimum KYC", 1_000_000L),

    /**
     * Full KYC: identity verified.
     */
    FULL("Full KYC", 20_000_000L);

    /**
     * Human-readable title.
     */
    private final String name;

    /**
     * Most the wallet may hold, in paise.
     */
    private final long limit;

    /**
     * Ctor.
     * @param title Title
     * @param cap Most the wallet may hold, in paise
     */
    Tier(final String title, final long cap) {
        this.name = title;
        this.limit = cap;
    }

    /**
     * Human-readable title.
     * @return Title, e.g. "Minimum KYC"
     */
    public String title() {
        return this.name;
    }

    /**
     * Most the wallet may hold.
     * @return Cap, in paise
     */
    public long cap() {
        return this.limit;
    }
}
