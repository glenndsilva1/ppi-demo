package com.ppidemo;

/**
 * Side of a journal line.
 */
public enum Side {

    /**
     * Debit.
     */
    DEBIT("D"),

    /**
     * Credit.
     */
    CREDIT("C");

    /**
     * Code in the database.
     */
    private final String code;

    /**
     * Ctor.
     * @param cde Code
     */
    Side(final String cde) {
        this.code = cde;
    }

    /**
     * Code in the database.
     * @return Code, "D" or "C"
     */
    public String code() {
        return this.code;
    }
}
