package com.ppidemo;

/**
 * What a transaction does, and through which channel it came.
 */
public enum Operation {

    /**
     * An agent puts money into their float.
     */
    FLOAT_TOPUP("Float top-up", "BC wallet"),

    /**
     * A customer hands cash to an agent; the wallet goes up.
     */
    CASH_IN("Cash-in at agent", "BC wallet"),

    /**
     * An agent hands cash to a customer; the wallet goes down.
     */
    CASH_OUT("Cash-out at agent", "BC wallet"),

    /**
     * A prepaid card payment at a shop.
     */
    CARD("Card payment", "PPI card"),

    /**
     * A tap at a metro gate.
     */
    TRANSIT("Metro tap", "NCMC transit"),

    /**
     * Undoes an earlier transaction.
     */
    REVERSAL("Reversal", "Back office");

    /**
     * Human-readable title.
     */
    private final String name;

    /**
     * Channel it comes through.
     */
    private final String chnl;

    /**
     * Ctor.
     * @param title Title
     * @param channel Channel
     */
    Operation(final String title, final String channel) {
        this.name = title;
        this.chnl = channel;
    }

    /**
     * Human-readable title.
     * @return Title, e.g. "Metro tap"
     */
    public String title() {
        return this.name;
    }

    /**
     * Channel it comes through.
     * @return Channel, e.g. "NCMC transit"
     */
    public String channel() {
        return this.chnl;
    }
}
