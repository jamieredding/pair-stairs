package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.PairUtils.scorePairCombinationUsing;
import static dev.coldhands.pair.stairs.TestUtils.testComparator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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

    @Test
    void calculateAllPairCombinations() {
        Set<String> allDevelopers = Set.of("jamie", "jorge", "reece");
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(allDevelopers);

        assertThat(allPairCombinations)
                .containsOnly(
                        Set.of(new Pair("jamie", "jorge"),
                                new Pair("reece")),
                        Set.of(new Pair("jorge", "reece"),
                                new Pair("jamie")),
                        Set.of(new Pair("jamie", "reece"),
                                new Pair("jorge")));
    }

    @Test
    void calculateAllPairCombinationsOfAnEvenNumberOfDevelopers() {
        Set<String> allDevelopers = Set.of("jamie", "jorge", "reece", "andy");
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(allDevelopers);

        assertThat(allPairCombinations)
                .containsOnly(
                        Set.of(new Pair("jamie", "jorge"),
                                new Pair("andy", "reece")),
                        Set.of(new Pair("jorge", "reece"),
                                new Pair("andy", "jamie")),
                        Set.of(new Pair("andy", "jorge"),
                                new Pair("jamie", "reece")));
    }

    @Test
    void calculateAllPairCombinationsWhereThereShouldBeThreePairsInEachPairing() {
        Set<String> allDevelopers = Set.of("jamie", "jorge", "reece", "andy", "cip");
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(allDevelopers);

        assertThat(allPairCombinations)
                .hasSize(15)
                .allSatisfy(pairCombination ->
                        assertThat(pairCombination).hasSize(3));
    }



    static Stream<Arguments> compare() {
        return Stream.of(
                arguments(
                        List.of(new Pair("jamie", "jorge"),
                                new Pair("jorge", "reece")),
                        Set.of(new Pair("jamie", "jorge")),
                        Set.of(new Pair("jorge", "reece")),
                        Set.of()),
                arguments(
                        List.of(new Pair("jamie", "jorge"),
                                new Pair("jorge", "reece"),
                                new Pair("andy", "reece"),
                                new Pair("andy", "jamie")),
                        Set.of(new Pair("jamie", "jorge"), new Pair("andy", "reece")),
                        Set.of(new Pair("jorge", "reece"), new Pair("andy", "jamie")),
                        Set.of()),
                arguments(
                        List.of(new Pair("jorge", "reece"),
                                new Pair("andy", "jamie"),
                                new Pair("jamie", "jorge"),
                                new Pair("andy", "reece")),
                        Set.of(new Pair("jorge", "reece"), new Pair("andy", "jamie")),
                        Set.of(new Pair("jamie", "jorge"), new Pair("andy", "reece")),
                        Set.of()),
                arguments(
                        List.of(new Pair("jamie", "jorge"),
                                new Pair("jorge", "reece"),
                                new Pair("jamie", "reece"),
                                new Pair("andy", "reece"),
                                new Pair("jorge"),
                                new Pair("jamie"),
                                new Pair("reece")),
                        Set.of(new Pair("jamie", "reece"), new Pair("jorge")),
                        Set.of(new Pair("jamie", "jorge"), new Pair("reece")),
                        Set.of("jamie", "reece"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void compare(List<Pair> pairsSortedByPairCount,
                 Set<Pair> pairCombination1,
                 Set<Pair> pairCombination2,
                 Set<String> newJoiners) {
        var underTest = Comparator.comparing(scorePairCombinationUsing(pairsSortedByPairCount, newJoiners)::score);

        testComparator(underTest, pairCombination1, pairCombination2);
    }
}