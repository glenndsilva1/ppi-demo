package com.ppidemo.web;

import com.ppidemo.Rupees;

/**
 * Change of a balance as HTML, e.g. green "+₹500.00" or red "−₹30.00".
 */
public final class Change {

    /**
     * Change, in paise.
     */
    private final long paise;

    /**
     * Ctor.
     * @param amount Change, in paise
     */
    public Change(final long amount) {
        this.paise = amount;
    }

    @Override
    public String toString() {
        final String html;
        if (this.paise > 0L) {
            html = String.format("<span class='up'>+%s</span>", new Rupees(this.paise));
        } else if (this.paise < 0L) {
            html = String.format("<span class='down'>−%s</span>", new Rupees(-this.paise));
        } else {
            html = "<span class='muted'>—</span>";
        }
        return html;
    }
}
