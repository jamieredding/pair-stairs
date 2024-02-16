package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PairStreamEntryPointTest {

    @Test
    void calculateWithNoHistory() {
        final var underTest = new PairStreamEntryPoint(
                List.of("a-dev", "b-dev", "c-dev"),
                List.of("1-stream", "2-stream")
        );

        final var sortedCombinations = underTest.computeScoredCombinations()
                .stream()
                .map(ScoredCombination::combination)
                .toList();

        assertThat(sortedCombinations)
                .allSatisfy(combination -> {
                    assertThat(combination.pairs().stream().flatMap(pair -> pair.developers().stream()))
                            .containsExactlyInAnyOrder("a-dev", "b-dev", "c-dev");
                    assertThat(combination.pairs().stream().map(Pair::stream))
                            .containsExactlyInAnyOrder("1-stream", "2-stream");
                });
    }

    @Nested
    class ApplyRules {

        // todo tests for each rule
    }
}