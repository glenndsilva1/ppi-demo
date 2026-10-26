package com.ppidemo.web;

/**
 * Coloured label of a channel, as HTML.
 */
public final class Chip {

    /**
     * Channel, e.g. "NCMC transit".
     */
    private final String channel;

    /**
     * Ctor.
     * @param chnl Channel
     */
    public Chip(final String chnl) {
        this.channel = chnl;
    }

    @Override
    public String toString() {
        return String.format(
            "<span class='chip %s'>%s</span>",
            this.channel.toLowerCase().replaceAll("[^a-z]+", "-"),
            new Escaped(this.channel)
        );
    }
}
