package com.ppidemo;

/**
 * Amount in paise, printed as rupees with Indian grouping, e.g. "₹1,23,456.50".
 */
public final class Rupees {

    /**
     * Amount, in paise.
     */
    private final long paise;

    /**
     * Ctor.
     * @param amount Amount, in paise
     */
    public Rupees(final long amount) {
        this.paise = amount;
    }

    @Override
    public String toString() {
        final String sign;
        if (this.paise < 0L) {
            sign = "-";
        } else {
            sign = "";
        }
        final long abs = Math.abs(this.paise);
        final String digits = Long.toString(abs / 100L);
        final StringBuilder grouped = new StringBuilder();
        if (digits.length() <= 3) {
            grouped.append(digits);
        } else {
            final String head = digits.substring(0, digits.length() - 3);
            for (int idx = 0; idx < head.length(); idx += 1) {
                if (idx > 0 && (head.length() - idx) % 2 == 0) {
                    grouped.append(',');
                }
                grouped.append(head.charAt(idx));
            }
            grouped.append(',').append(digits.substring(digits.length() - 3));
        }
        return String.format("%s₹%s.%02d", sign, grouped, abs % 100L);
    }
}
