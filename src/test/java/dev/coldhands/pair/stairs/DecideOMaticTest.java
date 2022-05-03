package dev.coldhands.pair.stairs;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.TestData.EXAMPLE_PAIRINGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DecideOMaticTest {

    @Test
    void whenEveryoneIsIn() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");

        DecideOMatic underTest = new DecideOMatic(EXAMPLE_PAIRINGS, allDevelopers);

        Set<Pair> actualPairs = underTest.getNextPairs();
        assertThat(actualPairs)
                .containsOnly(new Pair("b-dev", "c-dev"),
                        new Pair("d-dev", "e-dev"),
                        new Pair("a-dev"));
    }

    static Stream<Arguments> whenSomeoneIsOff() {
        return Stream.of(
                arguments(Set.of("d-dev", "c-dev", "e-dev", "a-dev"),
                        new Pair[]{
                                new Pair("d-dev", "e-dev"),
                                new Pair("a-dev", "c-dev")}),
                arguments(Set.of("d-dev", "c-dev", "e-dev", "b-dev"),
                        new Pair[]{
                                new Pair("b-dev", "c-dev"),
                                new Pair("d-dev", "e-dev")}),
                arguments(Set.of("d-dev", "c-dev", "a-dev", "b-dev"),
                        new Pair[]{
                                new Pair("a-dev", "b-dev"),
                                new Pair("c-dev", "d-dev")}),
                arguments(Set.of("d-dev", "e-dev", "a-dev", "b-dev"),
                        new Pair[]{
                                new Pair("d-dev", "e-dev"),
                                new Pair("a-dev", "b-dev")}),
                arguments(Set.of("c-dev", "e-dev", "a-dev", "b-dev"),
                        new Pair[]{
                                new Pair("a-dev", "c-dev"),
                                new Pair("b-dev", "e-dev")}),
                arguments(Set.of("c-dev", "e-dev", "a-dev"),
                        new Pair[]{
                                new Pair("c-dev", "e-dev"),
                                new Pair("a-dev")})
        );
    }

    @ParameterizedTest
    @MethodSource
    void whenSomeoneIsOff(Set<String> availableDevelopers, Pair[] expectedPairs) {
        DecideOMatic underTest = new DecideOMatic(EXAMPLE_PAIRINGS, availableDevelopers);

        Set<Pair> actualPairs = underTest.getNextPairs();
        assertThat(actualPairs)
                .containsOnly(expectedPairs);
    }

    @Test
    void pairsShouldAlwaysRotate() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = new ArrayList<>();

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers);
        Set<Pair> firstPairs = underTest.getNextPairs();
        firstPairs.stream()
                .map(pair -> new Pairing(now.plusDays(1), pair))
                .forEach(pairings::add);

        underTest = new DecideOMatic(pairings, allDevelopers);
        Set<Pair> nextPairs = underTest.getNextPairs();

        Sets.SetView<Pair> intersection = Sets.intersection(firstPairs, nextPairs);

        assertThat(intersection)
                .isEmpty();
    }

    @Test
    void doNotAllowNewJoinersToSolo() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev"),
                new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                new Pairing(now.minusDays(2), "c-dev"),
                new Pairing(now.minusDays(3), "c-dev", "e-dev"),
                new Pairing(now.minusDays(3), "d-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers, Set.of("e-dev"));
        assertThat(underTest.getNextPairs())
                .containsOnly(new Pair("d-dev", "e-dev"),
                        new Pair("c-dev"));
    }

    @Test
    void doNotAllowNewJoinersToSoloEvenWhenTheyHaveInThePast() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev"),
                new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                new Pairing(now.minusDays(2), "c-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers, Set.of("c-dev", "e-dev"));
        assertThat(underTest.getNextPairs())
                .containsOnly(new Pair("c-dev", "e-dev"),
                        new Pair("d-dev"));
    }

    @Test
    void showAllPairsWithTheirScoreInOrder() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev"),
                new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                new Pairing(now.minusDays(2), "c-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers);

        List<ScoredPairCombination> actual = underTest.getScoredPairCombinations();
        assertThat(actual)
                .containsOnly(
                        new ScoredPairCombination(
                                Set.of(new Pair("c-dev", "d-dev"),
                                        new Pair("e-dev")),
                                2),
                        new ScoredPairCombination(
                                Set.of(new Pair("d-dev", "e-dev"),
                                        new Pair("c-dev")),
                                4),
                        new ScoredPairCombination(
                                Set.of(new Pair("c-dev", "e-dev"),
                                        new Pair("d-dev")),
                                9)
                );
    }
}
