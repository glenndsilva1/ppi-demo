package com.ppidemo.web;

/**
 * Amount typed by a person in rupees, e.g. "500" or "99.50", as paise.
 */
public final class Paise {

    /**
     * Text as typed.
     */
    private final String text;

    /**
     * Ctor.
     * @param typed Text as typed
     */
    public Paise(final String typed) {
        this.text = typed;
    }

    /**
     * Amount.
     * @return Amount, in paise, more than zero
     */
    public long value() {
        final String clean = this.text.trim().replace(",", "").replace("₹", "");
        if (!clean.matches("[0-9]{1,9}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException(
                String.format("\"%s\" is not an amount; type rupees, e.g. 500 or 99.50", this.text)
            );
        }
        final String[] parts = clean.split("\\.");
        long paise = Long.parseLong(parts[0]) * 100L;
        if (parts.length > 1) {
            paise += Long.parseLong(String.format("%-2s", parts[1]).replace(' ', '0'));
        }
        if (paise == 0L) {
            throw new IllegalArgumentException("Amount must be more than zero");
        }
        return paise;
    }
}
