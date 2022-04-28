package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.TestUtils.testComparator;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PairCountComparatorTest {

    static Stream<Arguments> compare() {
        return Stream.of(
                arguments(
                        new PairCount(new Pair("jamie", "jorge"), 0, false),
                        new PairCount(new Pair("jorge", "reece"), 1, false)),
                arguments(
                        new PairCount(new Pair("jamie", "jorge"), 0, false),
                        new PairCount(new Pair("jorge", "reece"), 0, false)),
                arguments(
                        new PairCount(new Pair("jamie", "jorge"), 0, false),
                        new PairCount(new Pair("jamie", "reece"), 0, false)),
                arguments(
                        new PairCount(new Pair("jorge", "reece"), 1, false),
                        new PairCount(new Pair("jamie"), 0, false)),
                arguments(
                        new PairCount(new Pair("cip", "jamie"), 0, false),
                        new PairCount(new Pair("andy", "cip"), 1, false)),
                arguments(
                        new PairCount(new Pair("jamie", "jorge"), 1, false),
                        new PairCount(new Pair("jorge", "reece"), 0, true))
        );
    }

    @ParameterizedTest
    @MethodSource
    void compare(PairCount first, PairCount second) {
        testComparator(new PairCountComparator(Set.of()), first, second);
    }

    @Test
    void preferNonNewJoinersAsSolo() {
        final PairCount first = new PairCount(new Pair("jamie"), 1, true);
        final PairCount second = new PairCount(new Pair("cip"), 0, false);;
        testComparator(new PairCountComparator(Set.of("cip")), first, second);
    }
}