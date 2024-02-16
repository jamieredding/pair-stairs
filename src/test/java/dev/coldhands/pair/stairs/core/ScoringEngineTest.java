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
                .containsExactly( // todo should I map these to just ordered test combination and scrap implementation details?
                        new ScoredCombination<>(new TestCombination(2), 20, List.of(new ScoreResult(20, ""), new ScoreResult(0, ""))),
                        new ScoredCombination<>(new TestCombination(1), 60, List.of(new ScoreResult(10, ""), new ScoreResult(50, ""))),
                        new ScoredCombination<>(new TestCombination(3), 80, List.of(new ScoreResult(30, ""), new ScoreResult(50, "")))
                );
    }

    private record TestCombination(int value) {

    }

    private static class Multiply10Rule implements ScoringRule<TestCombination> {
        @Override
        public ScoreResult score(TestCombination combination) {
            int score = combination.value * 10;
            return new ScoreResult(score, ""); // todo descriptions?
        }
    }

    private static class DebuffOddNumbersRule implements ScoringRule<TestCombination> {
        @Override
        public ScoreResult score(TestCombination combination) {
            int score = combination.value % 2 == 1 ? 50 : 0;
            return new ScoreResult(score, ""); // todo descriptions?
        }
    }
}