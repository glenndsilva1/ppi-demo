package com.ppidemo.web;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class PaiseTest {

    @Test
    void readsRupees() {
        MatcherAssert.assertThat(new Paise("500").value(), Matchers.equalTo(50_000L));
        MatcherAssert.assertThat(new Paise(" ₹1,000.5 ").value(), Matchers.equalTo(100_050L));
        MatcherAssert.assertThat(new Paise("0.07").value(), Matchers.equalTo(7L));
    }

    @Test
    void refusesNonsense() {
        for (final String text : new String[] {"", "0", "-5", "1.234", "abc", "1e3"}) {
            Assertions.assertThrows(
                IllegalArgumentException.class, () -> new Paise(text).value(), text
            );
        }
    }
}
