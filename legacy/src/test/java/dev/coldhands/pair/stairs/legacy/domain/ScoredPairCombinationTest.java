package dev.coldhands.pair.stairs.legacy.domain;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScoredPairCombinationTest {

    @Test
    void prettyPrintScoreBreakdown() {
        var pairs = List.of(new Pair("a-dev", "b-dev"), new Pair("c-dev", "d-dev"));
        var underTest = new ScoredPairCombination(new LinkedHashSet<>(pairs), 3);

        assertThat(underTest.scoreBreakdown(Map.of(new Pair("a-dev", "b-dev"), 1, new Pair("c-dev", "d-dev"), 2)))
                .isEqualTo("[a-dev,b-dev]=1|[c-dev,d-dev]=2|total=3");
    }

    @Test
    void prettyPrintScoreBreakdownWithSoloPairs() {
        var pairs = List.of(new Pair("a-dev", "b-dev"), new Pair("c-dev"));
        var underTest = new ScoredPairCombination(new LinkedHashSet<>(pairs), 101);

        assertThat(underTest.scoreBreakdown(Map.of(new Pair("a-dev", "b-dev"), 1, new Pair("c-dev"), 100)))
                .isEqualTo("[a-dev,b-dev]=1|[c-dev]=100|total=101");
    }
}