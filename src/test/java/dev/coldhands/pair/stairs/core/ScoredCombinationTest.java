package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoredCombinationTest {

    @ParameterizedTest
    @CsvSource({
            "1,1,0",
            "1,2,-1",
            "2,1,1",
    })
    void compareTo(int score1, int score2, int compareResult) {
        final var combo1 = new ScoredCombination<>("", score1, List.of());
        final var combo2 = new ScoredCombination<>("", score2, List.of());

        assertThat(combo1.compareTo(combo2)).isEqualTo(compareResult);

        assertThat(combo2.compareTo(combo1)).isEqualTo(compareResult * -1);
    }
}