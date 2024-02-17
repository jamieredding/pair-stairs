package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public interface BaseRuleTest<Combination> {

    ScoringRule<Combination> underTest();

    Combination exampleCombination();

    @Test
    default void doNotContributeToScoreIfNoHistory() {
        assertThat(underTest().score(exampleCombination()).score())
                .isEqualTo(0);
    }
}
