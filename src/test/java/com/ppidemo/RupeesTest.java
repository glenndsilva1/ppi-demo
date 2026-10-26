package com.ppidemo;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

final class RupeesTest {

    @Test
    void groupsTheIndianWay() {
        MatcherAssert.assertThat(new Rupees(0L).toString(), Matchers.equalTo("₹0.00"));
        MatcherAssert.assertThat(new Rupees(99_950L).toString(), Matchers.equalTo("₹999.50"));
        MatcherAssert.assertThat(new Rupees(100_000L).toString(), Matchers.equalTo("₹1,000.00"));
        MatcherAssert.assertThat(new Rupees(20_000_000L).toString(), Matchers.equalTo("₹2,00,000.00"));
        MatcherAssert.assertThat(
            new Rupees(123_456_789L).toString(), Matchers.equalTo("₹12,34,567.89")
        );
        MatcherAssert.assertThat(new Rupees(-3_000L).toString(), Matchers.equalTo("-₹30.00"));
    }
}
