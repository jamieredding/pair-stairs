package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairUtilsTest {

    @Test
    void calculateAllPossiblePairs() {
        Set<Pair> actual = PairUtils.allPairs(Set.of("jamie", "jorge", "reece"));
        assertThat(actual)
                .containsOnly(
                        new Pair("jamie"),
                        new Pair("jorge"),
                        new Pair("reece"),
                        new Pair("jamie", "jorge"),
                        new Pair("jamie", "reece"),
                        new Pair("jorge", "reece")
                );

    }

    @Test
    void countPairs() {
        List<Pairing> pairings = TestData.EXAMPLE_PAIRINGS;
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");

        assertThat(PairUtils.countPairs(allDevelopers, pairings))
                .containsOnly(
                        new PairCount(new Pair("andy"), 0, false),
                        new PairCount(new Pair("andy", "cip"), 1, false),
                        new PairCount(new Pair("andy", "jamie"), 1, false),
                        new PairCount(new Pair("andy", "jorge"), 1, false),
                        new PairCount(new Pair("andy", "reece"), 1, true),
                        new PairCount(new Pair("cip"), 0, false),
                        new PairCount(new Pair("cip", "jamie"), 0, false),
                        new PairCount(new Pair("cip", "jorge"), 2, true),
                        new PairCount(new Pair("cip", "reece"), 1, false),
                        new PairCount(new Pair("jamie"), 2, true),
                        new PairCount(new Pair("jamie", "jorge"), 0, false),
                        new PairCount(new Pair("jamie", "reece"), 1, false),
                        new PairCount(new Pair("jorge"), 1, false),
                        new PairCount(new Pair("jorge", "reece"), 0, false),
                        new PairCount(new Pair("reece"), 1, false)
                );
    }

    @Test
    void noPairsShouldBeRecentIfNoneArePresentInPairings() {
        List<Pairing> pairings = List.of();
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");

        assertThat(PairUtils.countPairs(allDevelopers, pairings))
                .containsOnly(
                        new PairCount(new Pair("andy"), 0, false),
                        new PairCount(new Pair("andy", "cip"), 0, false),
                        new PairCount(new Pair("andy", "jamie"), 0, false),
                        new PairCount(new Pair("andy", "jorge"), 0, false),
                        new PairCount(new Pair("andy", "reece"), 0, false),
                        new PairCount(new Pair("cip"), 0, false),
                        new PairCount(new Pair("cip", "jamie"), 0, false),
                        new PairCount(new Pair("cip", "jorge"), 0, false),
                        new PairCount(new Pair("cip", "reece"), 0, false),
                        new PairCount(new Pair("jamie"), 0, false),
                        new PairCount(new Pair("jamie", "jorge"), 0, false),
                        new PairCount(new Pair("jamie", "reece"), 0, false),
                        new PairCount(new Pair("jorge"), 0, false),
                        new PairCount(new Pair("jorge", "reece"), 0, false),
                        new PairCount(new Pair("reece"), 0, false)
                );
    }
}