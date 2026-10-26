package com.ppidemo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Random valid Indian mobile number, so tests don't collide.
 */
public final class TestMobile {

    @Override
    public String toString() {
        return String.format("9%09d", ThreadLocalRandom.current().nextLong(1_000_000_000L));
    }
}
