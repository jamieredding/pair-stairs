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
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");

        DecideOMatic underTest = new DecideOMatic(EXAMPLE_PAIRINGS, allDevelopers);

        Set<Pair> actualPairs = underTest.getNextPairs();
        assertThat(actualPairs)
                .containsOnly(new Pair("cip", "jamie"),
                        new Pair("jorge", "reece"),
                        new Pair("andy"));
    }

    static Stream<Arguments> whenSomeoneIsOff() {
        return Stream.of(
                arguments(Set.of("jorge", "jamie", "reece", "andy"),
                        new Pair[]{
                                new Pair("jorge", "reece"),
                                new Pair("andy", "jamie")}),
                arguments(Set.of("jorge", "jamie", "reece", "cip"),
                        new Pair[]{
                                new Pair("cip", "jamie"),
                                new Pair("jorge", "reece")}),
                arguments(Set.of("jorge", "jamie", "andy", "cip"),
                        new Pair[]{
                                new Pair("andy", "cip"),
                                new Pair("jamie", "jorge")}),
                arguments(Set.of("jorge", "reece", "andy", "cip"),
                        new Pair[]{
                                new Pair("jorge", "reece"),
                                new Pair("andy", "cip")}),
                arguments(Set.of("jamie", "reece", "andy", "cip"),
                        new Pair[]{
                                new Pair("jamie", "reece"),
                                new Pair("andy", "cip")}),
                arguments(Set.of("jamie", "reece", "andy"),
                        new Pair[]{
                                new Pair("andy", "jamie"),
                                new Pair("reece")})
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
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");
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
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "jamie", "reece"),
                new Pairing(now.minusDays(1), "jorge"),
                new Pairing(now.minusDays(2), "jorge", "reece"),
                new Pairing(now.minusDays(2), "jamie"),
                new Pairing(now.minusDays(3), "jamie", "reece"),
                new Pairing(now.minusDays(3), "jorge"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers, Set.of("reece"));
        assertThat(underTest.getNextPairs())
                .containsOnly(new Pair("jorge", "reece"),
                        new Pair("jamie"));
    }

    /*
    todo
     - allow picking of pairs
     - offer multiple options
     - show bad pairings
     */
}
