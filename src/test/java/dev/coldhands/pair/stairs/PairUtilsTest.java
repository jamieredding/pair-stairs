package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairUtilsTest {

    @Test
    void calculateAllPossiblePairs() {
        Set<Pair> actual = PairUtils.allPairs(Set.of("jamie", "jorge", "reece"));
        assertThat(actual)
                .containsOnly(
                        new Pair("jamie"),
                        new Pair("jorge"),
                        new Pair("reece"),
                        new Pair("jamie", "jorge"),
                        new Pair("jamie", "reece"),
                        new Pair("jorge", "reece")
                );

    }
}