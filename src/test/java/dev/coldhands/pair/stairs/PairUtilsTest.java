package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.PairUtils.scorePairCombinationUsing;
import static dev.coldhands.pair.stairs.TestUtils.testComparator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PairUtilsTest {

    @Test
    void calculateAllPossiblePairs() {
        Set<Pair> actual = PairUtils.allPairs(Set.of("c-dev", "d-dev", "e-dev"));
        assertThat(actual)
                .containsOnly(
                        new Pair("c-dev"),
                        new Pair("d-dev"),
                        new Pair("e-dev"),
                        new Pair("c-dev", "d-dev"),
                        new Pair("c-dev", "e-dev"),
                        new Pair("d-dev", "e-dev")
                );

    }

    @Test
    void countPairs() {
        List<Pairing> pairings = TestData.EXAMPLE_PAIRINGS;
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");

        assertThat(PairUtils.countPairs(allDevelopers, pairings))
                .containsOnly(
                        new PairCount(new Pair("a-dev"), 0, null),
                        new PairCount(new Pair("a-dev", "b-dev"), 1, LocalDate.now().minusDays(3)),
                        new PairCount(new Pair("a-dev", "c-dev"), 1, LocalDate.now().minusDays(6)),
                        new PairCount(new Pair("a-dev", "d-dev"), 1, LocalDate.now().minusDays(4)),
                        new PairCount(new Pair("a-dev", "e-dev"), 1, LocalDate.now()),
                        new PairCount(new Pair("b-dev"), 0, null),
                        new PairCount(new Pair("b-dev", "c-dev"), 0, null),
                        new PairCount(new Pair("b-dev", "d-dev"), 2, LocalDate.now()),
                        new PairCount(new Pair("b-dev", "e-dev"), 1, LocalDate.now().minusDays(4)),
                        new PairCount(new Pair("c-dev"), 2, LocalDate.now()),
                        new PairCount(new Pair("c-dev", "d-dev"), 0, null),
                        new PairCount(new Pair("c-dev", "e-dev"), 1, LocalDate.now().minusDays(3)),
                        new PairCount(new Pair("d-dev"), 1, LocalDate.now().minusDays(3)),
                        new PairCount(new Pair("d-dev", "e-dev"), 0, null),
                        new PairCount(new Pair("e-dev"), 1, LocalDate.now().minusDays(6))
                );
    }

    @Test
    void noPairsShouldBeRecentIfNoneArePresentInPairings() {
        List<Pairing> pairings = List.of();
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");

        assertThat(PairUtils.countPairs(allDevelopers, pairings))
                .containsOnly(
                        new PairCount(new Pair("a-dev"), 0, null),
                        new PairCount(new Pair("a-dev", "b-dev"), 0, null),
                        new PairCount(new Pair("a-dev", "c-dev"), 0, null),
                        new PairCount(new Pair("a-dev", "d-dev"), 0, null),
                        new PairCount(new Pair("a-dev", "e-dev"), 0, null),
                        new PairCount(new Pair("b-dev"), 0, null),
                        new PairCount(new Pair("b-dev", "c-dev"), 0, null),
                        new PairCount(new Pair("b-dev", "d-dev"), 0, null),
                        new PairCount(new Pair("b-dev", "e-dev"), 0, null),
                        new PairCount(new Pair("c-dev"), 0, null),
                        new PairCount(new Pair("c-dev", "d-dev"), 0, null),
                        new PairCount(new Pair("c-dev", "e-dev"), 0, null),
                        new PairCount(new Pair("d-dev"), 0, null),
                        new PairCount(new Pair("d-dev", "e-dev"), 0, null),
                        new PairCount(new Pair("e-dev"), 0, null)
                );
    }

    @Test
    void calculateAllPairCombinations() {
        Set<String> allDevelopers = Set.of("c-dev", "d-dev", "e-dev");
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(allDevelopers);

        assertThat(allPairCombinations)
                .containsOnly(
                        Set.of(new Pair("c-dev", "d-dev"),
                                new Pair("e-dev")),
                        Set.of(new Pair("d-dev", "e-dev"),
                                new Pair("c-dev")),
                        Set.of(new Pair("c-dev", "e-dev"),
                                new Pair("d-dev")));
    }

    @Test
    void calculateAllPairCombinationsOfAnEvenNumberOfDevelopers() {
        Set<String> allDevelopers = Set.of("c-dev", "d-dev", "e-dev", "a-dev");
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(allDevelopers);

        assertThat(allPairCombinations)
                .containsOnly(
                        Set.of(new Pair("c-dev", "d-dev"),
                                new Pair("a-dev", "e-dev")),
                        Set.of(new Pair("d-dev", "e-dev"),
                                new Pair("a-dev", "c-dev")),
                        Set.of(new Pair("a-dev", "d-dev"),
                                new Pair("c-dev", "e-dev")));
    }

    @Test
    void calculateAllPairCombinationsWhereThereShouldBeThreePairsInEachPairing() {
        Set<String> allDevelopers = Set.of("c-dev", "d-dev", "e-dev", "a-dev", "b-dev");
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(allDevelopers);

        assertThat(allPairCombinations)
                .hasSize(15)
                .allSatisfy(pairCombination ->
                        assertThat(pairCombination).hasSize(3));
    }

    static Stream<Arguments> compare() {
        return Stream.of(
                arguments(
                        Map.of(new Pair("c-dev", "d-dev"), 0,
                                new Pair("d-dev", "e-dev"), 0,
                                new Pair("a-dev", "e-dev"), 100,
                                new Pair("a-dev", "c-dev"), 1000),
                        Set.of(new Pair("c-dev", "d-dev"), new Pair("a-dev", "e-dev")),
                        Set.of(new Pair("d-dev", "e-dev"), new Pair("a-dev", "c-dev")),
                        Set.of()),
                arguments(
                        Map.of(new Pair("d-dev", "e-dev"), 0,
                                new Pair("a-dev", "c-dev"), 100,
                                new Pair("c-dev", "d-dev"), 1000,
                                new Pair("a-dev", "e-dev"), 10000),
                        Set.of(new Pair("d-dev", "e-dev"), new Pair("a-dev", "c-dev")),
                        Set.of(new Pair("c-dev", "d-dev"), new Pair("a-dev", "e-dev")),
                        Set.of()),
                arguments(
                        Map.of(new Pair("c-dev", "d-dev"), 0,
                                new Pair("d-dev", "e-dev"), 0,
                                new Pair("c-dev", "e-dev"), 100,
                                new Pair("a-dev", "e-dev"), 1000,
                                new Pair("d-dev"), 10000,
                                new Pair("c-dev"), 10000,
                                new Pair("e-dev"), 100000),
                        Set.of(new Pair("c-dev", "e-dev"), new Pair("d-dev")),
                        Set.of(new Pair("c-dev", "d-dev"), new Pair("e-dev")),
                        Set.of("c-dev", "e-dev"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void compare(Map<Pair, Integer> allPairsAndTheirScore,
                 Set<Pair> pairCombination1,
                 Set<Pair> pairCombination2) {
        var underTest = Comparator.comparing(scorePairCombinationUsing(allPairsAndTheirScore)::score);

        testComparator(underTest, pairCombination1, pairCombination2);
    }

    @Test
    void mostRecentDate() {
        assertThat(PairUtils.mostRecentDate(List.of(
                new PairCount(new Pair("a-dev"), 1, LocalDate.now().minusDays(1)),
                new PairCount(new Pair("b-dev"), 2, LocalDate.now()),
                new PairCount(new Pair("c-dev"), 3, LocalDate.now().minusDays(2)),
                new PairCount(new Pair("c-dev"), 0, null))))
                .contains(LocalDate.now());
    }
}