package dev.coldhands.pair.stairs.backend.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeveloperInfoTest {

    @Test
    void comparable() {
        DeveloperInfo a = new DeveloperInfo(1, "a");
        DeveloperInfo b = new DeveloperInfo(1, "b");

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(a.compareTo(a)).isEqualTo(0);
    }
}