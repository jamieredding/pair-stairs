package dev.coldhands.pair.stairs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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
        Comparator<PairCount> comparator = new PairCountComparator();

        assertThat(comparator.compare(first, second))
                .isEqualTo(-comparator.compare(second, first))
                .extracting(Integer::signum)
                .isEqualTo(-1);
    }
}