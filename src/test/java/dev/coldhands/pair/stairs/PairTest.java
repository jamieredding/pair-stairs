package dev.coldhands.pair.stairs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PairTest {

    @ParameterizedTest
    @CsvSource({
            "jamie, jamie, jorge, true",
            "jamie, jorge, jamie, true",
            "jamie, jamie,, true",
            "jamie, jorge, reece, false",
            "jamie,,, false"
    })
    void contains(String singleDev, String first, String second, boolean contains) {
        assertThat(new Pair(first, second).contains(singleDev)).isEqualTo(contains);
    }

    @ParameterizedTest
    @CsvSource({
            "jamie, jorge, jamie, jorge, true",
            "jamie, jorge, jorge, jamie, true",
            "jamie,, jamie,, true",
            "jamie,, jorge, reece, false",
            "jamie,,jorge,, false"
    })
    void equivalentTo(String firstFirst, String firstSecond, String secondFirst, String secondSecond, boolean equivalentTo) {
        assertThat(new Pair(firstFirst, firstSecond).equivalentTo(new Pair(secondFirst, secondSecond))).isEqualTo(equivalentTo);
    }

    static Stream<Arguments> canBeMadeFrom() {
        return Stream.of(
                arguments(new Pair("jamie", "jorge"), Set.of("jamie", "jorge"), true),
                arguments(new Pair("jorge", "jamie"), Set.of("jamie", "jorge"), true),
                arguments(new Pair("jorge", "jamie"), Set.of("jamie", "jorge", "reece"), true),
                arguments(new Pair("jamie"), Set.of("jamie", "jorge", "reece"), true),

                arguments(new Pair("jamie", "jorge"), Set.of("jamie", "reece"), false),
                arguments(new Pair("jamie", "jorge"), Set.of("jorge", "reece"), false),
                arguments(new Pair("jamie", "jorge"), Set.of("reece"), false),
                arguments(new Pair("jamie", "jorge"), Set.of(), false),
                arguments(new Pair("jamie"), Set.of(), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void canBeMadeFrom(Pair pair, Set<String> outstandingDevelopers, boolean canBeMadeFrom) {
        assertThat(pair.canBeMadeFrom(outstandingDevelopers)).isEqualTo(canBeMadeFrom);
    }
}