package dev.coldhands.pair.stairs.core.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WeightedRuleTest {

    @Test
    void scoreOfZeroIfDelegateRuleScoresZero() {
        final ScoringRule<TestCombination> delegate = _ -> new BasicScoreResult(0);

        final WeightedRule<TestCombination> underTest = new WeightedRule<>(1, delegate);

        assertThat(underTest.score(new Combination<>(Set.of())).score())
                .isEqualTo(0);
    }

    @Test
    void multiplyDelegateScoreByConfiguredWeight() {
        final ScoringRule<TestCombination> delegate = _ -> new BasicScoreResult(10);

        final WeightedRule<TestCombination> underTest = new WeightedRule<>(2, delegate);

        assertThat(underTest.score(new Combination<>(Set.of())).score())
                .isEqualTo(20);
    }

    @Test
    void actualScoreResultIsItsOwnType() {
        final ScoringRule<TestCombination> delegate = _ -> new BasicScoreResult(10);

        final WeightedRule<TestCombination> underTest = new WeightedRule<>(2, delegate);

        assertThat(underTest.score(new Combination<>(Set.of())))
                .isEqualTo(new WeightedScoreResult(new BasicScoreResult(10), 2));
    }

    private record TestCombination(int value) {

    }
}