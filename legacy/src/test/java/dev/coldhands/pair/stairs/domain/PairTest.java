package dev.coldhands.pair.stairs.domain;

import org.junit.jupiter.api.Test;
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
            "c-dev, c-dev, d-dev, true",
            "c-dev, d-dev, c-dev, true",
            "c-dev, c-dev,, true",
            "c-dev, d-dev, e-dev, false",
            "c-dev,,, false"
    })
    void contains(String singleDev, String first, String second, boolean contains) {
        assertThat(new Pair(first, second).contains(singleDev)).isEqualTo(contains);
    }

    @ParameterizedTest
    @CsvSource({
            "c-dev, d-dev, c-dev, d-dev, true",
            "c-dev, d-dev, d-dev, c-dev, true",
            "c-dev,, c-dev,, true",
            "c-dev,, d-dev, e-dev, false",
            "c-dev,,d-dev,, false"
    })
    void equivalentTo(String firstFirst, String firstSecond, String secondFirst, String secondSecond, boolean equivalentTo) {
        assertThat(new Pair(firstFirst, firstSecond).equivalentTo(new Pair(secondFirst, secondSecond))).isEqualTo(equivalentTo);
    }

    static Stream<Arguments> canBeMadeFrom() {
        return Stream.of(
                arguments(new Pair("c-dev", "d-dev"), Set.of("c-dev", "d-dev"), true),
                arguments(new Pair("d-dev", "c-dev"), Set.of("c-dev", "d-dev"), true),
                arguments(new Pair("d-dev", "c-dev"), Set.of("c-dev", "d-dev", "e-dev"), true),
                arguments(new Pair("c-dev"), Set.of("c-dev", "d-dev", "e-dev"), true),

                arguments(new Pair("c-dev", "d-dev"), Set.of("c-dev", "e-dev"), false),
                arguments(new Pair("c-dev", "d-dev"), Set.of("d-dev", "e-dev"), false),
                arguments(new Pair("c-dev", "d-dev"), Set.of("e-dev"), false),
                arguments(new Pair("c-dev", "d-dev"), Set.of(), false),
                arguments(new Pair("c-dev"), Set.of(), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void canBeMadeFrom(Pair pair, Set<String> outstandingDevelopers, boolean canBeMadeFrom) {
        assertThat(pair.canBeMadeFrom(outstandingDevelopers)).isEqualTo(canBeMadeFrom);
    }

    @Test
    void membersWhenAPair() {
        assertThat(new Pair("a-dev", "b-dev").members()).containsOnly("a-dev", "b-dev");
    }

    @Test
    void membersWhenSolo() {
        assertThat(new Pair("a-dev").members()).containsOnly("a-dev");
    }
}