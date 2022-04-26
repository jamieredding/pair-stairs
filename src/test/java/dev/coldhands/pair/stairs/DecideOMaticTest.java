package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.TestData.EXAMPLE_PAIRINGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DecideOMaticTest {

    @Test
    void whenEveryoneIsIn() {
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");
        DateProvider dateProvider = () -> LocalDate.now().plusDays(1);

        DecideOMatic underTest = new DecideOMatic(dateProvider, allDevelopers, EXAMPLE_PAIRINGS, allDevelopers);

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
        DateProvider dateProvider = () -> LocalDate.now().plusDays(1);

        DecideOMatic underTest = new DecideOMatic(dateProvider, allDevelopers, EXAMPLE_PAIRINGS, availableDevelopers);

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
