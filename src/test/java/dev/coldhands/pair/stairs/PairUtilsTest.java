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
        List<Pairing> pairings = List.of(
                new Pairing(LocalDate.now(), "jamie"),
                new Pairing(LocalDate.now(), "jorge", "cip"),
                new Pairing(LocalDate.now(), "andy", "reece"),
                new Pairing(LocalDate.now().minusDays(3), "jorge"),
                new Pairing(LocalDate.now().minusDays(3), "jamie", "reece"),
                new Pairing(LocalDate.now().minusDays(3), "andy", "cip"),
                new Pairing(LocalDate.now().minusDays(4), "jamie"),
                new Pairing(LocalDate.now().minusDays(4), "jorge", "andy"),
                new Pairing(LocalDate.now().minusDays(4), "reece", "cip"),
                new Pairing(LocalDate.now().minusDays(6), "reece"),
                new Pairing(LocalDate.now().minusDays(6), "jamie", "andy"),
                new Pairing(LocalDate.now().minusDays(6), "jorge", "cip")
        );
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");

        assertThat(PairUtils.countPairs(allDevelopers, pairings))
                .containsOnly(
                        new PairCount(new Pair("andy"), 0),
                        new PairCount(new Pair("andy", "cip"), 1),
                        new PairCount(new Pair("andy", "jamie"), 1),
                        new PairCount(new Pair("andy", "jorge"), 1),
                        new PairCount(new Pair("andy", "reece"), 1),
                        new PairCount(new Pair("cip"), 0),
                        new PairCount(new Pair("cip", "jamie"), 0),
                        new PairCount(new Pair("cip", "jorge"), 2),
                        new PairCount(new Pair("cip", "reece"), 1),
                        new PairCount(new Pair("jamie"), 2),
                        new PairCount(new Pair("jamie", "jorge"), 0),
                        new PairCount(new Pair("jamie", "reece"), 1),
                        new PairCount(new Pair("jorge"), 1),
                        new PairCount(new Pair("jorge", "reece"), 0),
                        new PairCount(new Pair("reece"), 1)
                );
    }
}