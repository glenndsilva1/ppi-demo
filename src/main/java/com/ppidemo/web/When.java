package com.ppidemo.web;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Moment printed for people in India, e.g. "25 Oct, 21:04".
 */
public final class When {

    /**
     * Moment.
     */
    private final OffsetDateTime moment;

    /**
     * Ctor.
     * @param time Moment
     */
    public When(final OffsetDateTime time) {
        this.moment = time;
    }

    @Override
    public String toString() {
        return this.moment
            .atZoneSameInstant(ZoneId.of("Asia/Kolkata"))
            .format(DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH));
    }
}
