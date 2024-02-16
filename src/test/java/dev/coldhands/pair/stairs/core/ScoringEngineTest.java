package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringEngineTest {

    @Test
    void scoreAndSort() {
        final var underTest = new ScoringEngine<>(List.of(
                new Multiply10Rule(),
                new DebuffOddNumbersRule()
        ));

        final var actual = underTest.scoreAndSort(Set.of(new TestCombination(1), new TestCombination(2), new TestCombination(3)));

        assertThat(actual)
                .containsExactly(
                        new ScoredCombination<>(new TestCombination(2), 20, List.of(new TestScoreResult(20), new TestScoreResult(0))),
                        new ScoredCombination<>(new TestCombination(1), 60, List.of(new TestScoreResult(10), new TestScoreResult(50))),
                        new ScoredCombination<>(new TestCombination(3), 80, List.of(new TestScoreResult(30), new TestScoreResult(50)))
                );
    }

    private record TestCombination(int value) {

    }

    private record TestScoreResult(int score) implements ScoreResult {

    }

    private static class Multiply10Rule implements ScoringRule<TestCombination> {
        @Override
        public ScoreResult score(TestCombination combination) {
            int score = combination.value * 10;
            return new TestScoreResult(score);
        }
    }

    private static class DebuffOddNumbersRule implements ScoringRule<TestCombination> {
        @Override
        public ScoreResult score(TestCombination combination) {
            int score = combination.value % 2 == 1 ? 50 : 0;
            return new TestScoreResult(score);
        }
    }
}