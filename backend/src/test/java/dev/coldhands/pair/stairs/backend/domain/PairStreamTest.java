package dev.coldhands.pair.stairs.backend.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PairStreamTest {

    @Test
    void comparable() {
        PairStream a = new PairStream(List.of(), new StreamInfo(1, "a", false));
        PairStream b = new PairStream(List.of(), new StreamInfo(1, "b", false));

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(a.compareTo(a)).isEqualTo(0);
    }
}