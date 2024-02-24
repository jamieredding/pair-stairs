package dev.coldhands.pair.stairs.core.usecases;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
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

        final var actual = underTest.scoreAndSort(Set.of(combination(1), combination(2), combination(3)));

        assertThat(actual)
                .containsExactly(
                        new ScoredCombination<>(combination(2), 20, List.of(new TestScoreResult(20), new TestScoreResult(0))),
                        new ScoredCombination<>(combination(1), 60, List.of(new TestScoreResult(10), new TestScoreResult(50))),
                        new ScoredCombination<>(combination(3), 80, List.of(new TestScoreResult(30), new TestScoreResult(50)))
                );
    }

    private record TestScoreResult(int score) implements ScoreResult {

    }

    private static Combination<Integer> combination(int i) {
        return new Combination<>(Set.of(i));
    }

    private static class Multiply10Rule implements ScoringRule<Integer> {
        @Override
        public ScoreResult score(Combination<Integer> combination) {
            int score = getFirstElement(combination) * 10;
            return new TestScoreResult(score);
        }
    }

    private static class DebuffOddNumbersRule implements ScoringRule<Integer> {
        @Override
        public ScoreResult score(Combination<Integer> combination) {
            int score = getFirstElement(combination) % 2 == 1 ? 50 : 0;
            return new TestScoreResult(score);
        }
    }

    private static int getFirstElement(Combination<Integer> combination) {
        return combination.pairs().stream().findFirst().orElse(0);
    }
}