package dev.coldhands.pair.stairs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.TestUtils.testComparator;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PairCombinationComparatorTest {

    static Stream<Arguments> compare() {
        return Stream.of(
                arguments(
                        List.of(new Pair("jamie", "jorge"),
                                new Pair("jorge", "reece")),
                        Set.of(new Pair("jamie", "jorge")),
                        Set.of(new Pair("jorge", "reece"))),
                arguments(
                        List.of(new Pair("jamie", "jorge"),
                                new Pair("jorge", "reece"),
                                new Pair("andy", "reece"),
                                new Pair("andy", "jamie")),
                        Set.of(new Pair("jamie", "jorge"), new Pair("andy", "reece")),
                        Set.of(new Pair("jorge", "reece"), new Pair("andy", "jamie"))),
                arguments(
                        List.of(new Pair("jorge", "reece"),
                                new Pair("andy", "jamie"),
                                new Pair("jamie", "jorge"),
                                new Pair("andy", "reece")),
                        Set.of(new Pair("jorge", "reece"), new Pair("andy", "jamie")),
                        Set.of(new Pair("jamie", "jorge"), new Pair("andy", "reece"))
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void compare(List<Pair> pairsSortedByPairCount,
                 Set<Pair> pairCombination1,
                 Set<Pair> pairCombination2) {
        var underTest = new PairCombinationComparator(pairsSortedByPairCount);

        testComparator(underTest, pairCombination1, pairCombination2);
    }
}