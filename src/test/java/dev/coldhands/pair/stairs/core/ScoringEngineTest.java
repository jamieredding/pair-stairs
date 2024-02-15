package dev.coldhands.pair.stairs.core;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringEngineTest {

    @Test
    void scoreAndSort() {
        final ScoringRule<TestCombination, TestScoredCombination> scoringRule = combination -> {
            int score = combination.value * 10;
            return new TestScoredCombination(combination, score);
        };

        final var underTest = new ScoringEngine<>(scoringRule);

        final var actual = underTest.scoreAndSort(Set.of(new TestCombination(1), new TestCombination(2), new TestCombination(3)));

        assertThat(actual)
                .containsExactly(
                        new TestScoredCombination(new TestCombination(1), 10),
                        new TestScoredCombination(new TestCombination(2), 20),
                        new TestScoredCombination(new TestCombination(3), 30)
                );
    }

    private record TestCombination(int value) {

    }

    // todo make score a list of subscores... and have a description...
    private record TestScoredCombination(TestCombination combination, int score) implements Comparable<TestScoredCombination> {

        @Override
        public int compareTo(@NotNull TestScoredCombination o) {
            return Integer.compare(this.score, o.score);
        }
    }
}