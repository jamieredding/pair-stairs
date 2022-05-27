package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.PairStatsScorer.score;
import static dev.coldhands.pair.stairs.TestUtils.testComparator;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PairStatsScorerTest {

    static Stream<Arguments> compare() {
        return Stream.of(
                arguments(
                        new PairStats(new Pair("c-dev", "d-dev"), 0, LocalDate.now().minusDays(2)),
                        new PairStats(new Pair("d-dev", "e-dev"), 1, LocalDate.now().minusDays(1))),
                arguments(
                        new PairStats(new Pair("d-dev", "e-dev"), 1, LocalDate.now().minusDays(2)),
                        new PairStats(new Pair("c-dev"), 0, LocalDate.now().minusDays(1))),
                arguments(
                        new PairStats(new Pair("b-dev", "c-dev"), 0, LocalDate.now().minusDays(2)),
                        new PairStats(new Pair("a-dev", "b-dev"), 1, LocalDate.now().minusDays(1))),
                arguments(
                        new PairStats(new Pair("c-dev", "d-dev"), 2, LocalDate.now().minusDays(1)),
                        new PairStats(new Pair("d-dev", "e-dev"), 1, LocalDate.now()))
        );
    }

    @ParameterizedTest
    @MethodSource
    void compare(PairStats first, PairStats second) {
        testComparator(new PairStatsComparator(Set.of(), LocalDate.now()), first, second);
    }

    @Test
    void preferNonNewJoinersAsSolo() {
        final PairStats first = new PairStats(new Pair("c-dev"), 1, LocalDate.now());
        final PairStats second = new PairStats(new Pair("b-dev"), 0, LocalDate.now().minusDays(1));
        testComparator(new PairStatsComparator(Set.of("b-dev"), LocalDate.now()), first, second);
    }

    @Test
    void preferPairThatHasAnOlderLastPairingDate() {
        final PairStats first = new PairStats(new Pair("b-dev", "c-dev"), 2, LocalDate.now().minusDays(3));
        final PairStats second = new PairStats(new Pair("a-dev", "b-dev"), 2, LocalDate.now().minusDays(2));
        testComparator(new PairStatsComparator(Set.of(), LocalDate.now()), first, second);
    }

    @Test
    void preferPairThatHasNotPairedOverOneThatHasPaired() {
        final PairStats first = new PairStats(new Pair("b-dev", "c-dev"), 0, null);
        final PairStats second = new PairStats(new Pair("a-dev", "b-dev"), 2, LocalDate.now().minusDays(2));
        testComparator(new PairStatsComparator(Set.of(), LocalDate.now()), first, second);
    }

    private record PairStatsComparator(Set<String> newJoiners,
                                       LocalDate mostRecentDate) implements Comparator<PairStats> {

        @Override
        public int compare(PairStats o1, PairStats o2) {
            return Comparator.<PairStats, Integer>comparing(pairStats -> score(pairStats, newJoiners, mostRecentDate))
                    .compare(o1, o2);
        }
    }
}