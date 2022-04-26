package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DecideOMaticTest {

    @Test
    void whenEveryoneIsIn() {
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");
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
        DateProvider dateProvider = () -> LocalDate.now().plusDays(1);

        DecideOMatic underTest = new DecideOMatic(dateProvider, allDevelopers, pairings, allDevelopers);

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
                                new Pair("jamie", "jorge"),
                                new Pair("andy", "reece")}),
                arguments(Set.of("jorge", "jamie", "reece", "cip"),
                        new Pair[]{
                                new Pair("cip", "jamie"),
                                new Pair("jorge", "reece")}),
                arguments(Set.of("jorge", "jamie", "andy", "cip"),
                        new Pair[]{
                                new Pair("cip", "jamie"),
                                new Pair("andy", "jorge")}),
                arguments(Set.of("jorge", "reece", "andy", "cip"),
                        new Pair[]{
                                new Pair("jorge", "reece"),
                                new Pair("andy", "cip")}),
                arguments(Set.of("jamie", "reece", "andy", "cip"),
                        new Pair[]{
                                new Pair("cip", "jamie"),
                                new Pair("andy", "reece")}),
                arguments(Set.of("jamie", "reece", "andy"),
                        new Pair[]{
                                new Pair("andy", "jamie"),
                                new Pair("reece")})
        );
    }

    @ParameterizedTest
    @MethodSource
    void whenSomeoneIsOff(Set<String> availableDevelopers, Pair[] expectedPairs) {
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");
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
        DateProvider dateProvider = () -> LocalDate.now().plusDays(1);

        DecideOMatic underTest = new DecideOMatic(dateProvider, allDevelopers, pairings, availableDevelopers);

        Set<Pair> actualPairs = underTest.getNextPairs();
        assertThat(actualPairs)
                .containsOnly(expectedPairs);
    }

    /*
    todo
     - allow picking of pairs
     - offer multiple options
     - show bad pairings
     */
}
