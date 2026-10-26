package com.ppidemo.web;

/**
 * Text made safe to put into HTML.
 */
public final class Escaped {

    /**
     * Raw text.
     */
    private final Object raw;

    /**
     * Ctor.
     * @param text Raw text
     */
    public Escaped(final Object text) {
        this.raw = text;
    }

    @Override
    public String toString() {
        return String.valueOf(this.raw)
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
