package dev.coldhands.pair.stairs.core;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public interface BaseRuleTest<T> {

    ScoringRule<T> underTest();

    Combination<T> exampleCombination();

    @Test
    default void doNotContributeToScoreIfNoHistory() {
        assertThat(underTest().score(exampleCombination()).score())
                .isEqualTo(0);
    }
}
