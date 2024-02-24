package dev.coldhands.pair.stairs.core.usecases.pairstream.metrics;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class UniqueDeveloperPairsMetricTest {

    @Nested
    class OccurrencesPerPair {

        @Test
        void whenNoCombinationsThenShowAllPossiblePairsAtZeroCount() {
            final var underTest = new UniqueDeveloperPairsMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

            final UniqueDeveloperPairsMetric.Result result = underTest.compute(List.of());

            assertThat(result.occurrencesPerPair())
                    .containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
                                    entry(Set.of("a-dev", "b-dev"), 0),
                                    entry(Set.of("a-dev", "c-dev"), 0),
                                    entry(Set.of("b-dev", "c-dev"), 0),
                                    entry(Set.of("a-dev"), 0),
                                    entry(Set.of("b-dev"), 0),
                                    entry(Set.of("c-dev"), 0)
                            )
                    );
        }

        @Test
        void whenSomeCombinationsThenAddThoseToTheCountForTheCorrectPair() {
            final var underTest = new UniqueDeveloperPairsMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

            List<ScoredCombination<PairStream>> scoredCombinations = Stream.of(
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                                    new PairStream(Set.of("c-dev"), "2-stream")
                            )),
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev", "b-dev"), "2-stream"),
                                    new PairStream(Set.of("c-dev"), "1-stream")
                            )),
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev", "c-dev"), "1-stream"),
                                    new PairStream(Set.of("b-dev"), "2-stream")
                            )),
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev"), "1-stream"),
                                    new PairStream(Set.of("b-dev", "c-dev"), "2-stream")
                            ))
                    )
                    .map(combo -> new ScoredCombination<>(combo, 0, List.of()))
                    .toList();

            final UniqueDeveloperPairsMetric.Result result = underTest.compute(scoredCombinations);

            assertThat(result.occurrencesPerPair())
                    .containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
                                    entry(Set.of("a-dev", "b-dev"), 2),
                                    entry(Set.of("a-dev", "c-dev"), 1),
                                    entry(Set.of("b-dev", "c-dev"), 1),
                                    entry(Set.of("a-dev"), 1),
                                    entry(Set.of("b-dev"), 1),
                                    entry(Set.of("c-dev"), 2)
                            )
                    );
        }
    }

    @Nested
    class IdealOccurrencesPerPair {

        @Test
        void whenNoCombinationsThenIdealOccurrencesIsZero() {
            final var underTest = new UniqueDeveloperPairsMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

            final UniqueDeveloperPairsMetric.Result result = underTest.compute(List.of());

            assertThat(result.idealOccurrencesPerPair()).isEqualTo(0);
        }

        @ParameterizedTest
        @CsvSource({
                "3,1",
                "12,4",
                "1,0.3333"
        })
        void idealOccurrencesIsNumCombinationsTimesPairsPerCombinationDividedByNumPossiblePairs_smallNumberOfDevs(int numberOfCombinations, double expectedIdealOccurrences) {
            final var underTest = new UniqueDeveloperPairsMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

            List<ScoredCombination<PairStream>> scoredCombinations = IntStream.range(0, numberOfCombinations)
                    .mapToObj(_ ->
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                                    new PairStream(Set.of("c-dev"), "2-stream")
                            )))
                    .map(combo -> new ScoredCombination<>(combo, 0, List.of()))
                    .toList();

            final UniqueDeveloperPairsMetric.Result result = underTest.compute(scoredCombinations);

            assertThat(result.idealOccurrencesPerPair()).isCloseTo(expectedIdealOccurrences, offset(0.1));
        }

        @ParameterizedTest
        @CsvSource({
                "5,1",
                "10,2",
                "31,6.2"
        })
        void idealOccurrencesIsNumCombinationsTimesPairsPerCombinationDividedByNumPossiblePairs_largeNumberOfDevs(int numberOfCombinations, double expectedIdealOccurrences) {
            final var underTest = new UniqueDeveloperPairsMetric(Set.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev"), Set.of("1-stream", "2-stream", "3-stream"));

            List<ScoredCombination<PairStream>> scoredCombinations = IntStream.range(0, numberOfCombinations)
                    .mapToObj(_ ->
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                                    new PairStream(Set.of("c-dev", "d-dev"), "2-stream"),
                                    new PairStream(Set.of("e-dev"), "3-stream")
                            )))
                    .map(combo -> new ScoredCombination<>(combo, 0, List.of()))
                    .toList();

            final UniqueDeveloperPairsMetric.Result result = underTest.compute(scoredCombinations);

            assertThat(result.idealOccurrencesPerPair()).isEqualTo(expectedIdealOccurrences);
        }

        /*
        todo
            - should people being off impact the number of pairs per combination
         */
    }

    @Nested
    class SummaryStatistics {

        @Test
        void whenNoCombinationsThenZeroSummaryStatistics() {
            final var underTest = new UniqueDeveloperPairsMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

            final UniqueDeveloperPairsMetric.Result result = underTest.compute(List.of());

            assertThat(result.summaryStatistics())
                    .satisfies(summary -> {
                        assertThat(summary.count()).isEqualTo(6);
                        assertThat(summary.mean()).isEqualTo(0);
                    });
        }

        @Test
        void calculateSummaryStatistics() {
            final var underTest = new UniqueDeveloperPairsMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

            List<ScoredCombination<PairStream>> scoredCombinations = Stream.of(
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                                    new PairStream(Set.of("c-dev"), "2-stream")
                            )),
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev", "b-dev"), "2-stream"),
                                    new PairStream(Set.of("c-dev"), "1-stream")
                            )),
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev", "c-dev"), "1-stream"),
                                    new PairStream(Set.of("b-dev"), "2-stream")
                            )),
                            new Combination<>(Set.of(
                                    new PairStream(Set.of("a-dev"), "1-stream"),
                                    new PairStream(Set.of("b-dev", "c-dev"), "2-stream")
                            ))
                    )
                    .map(combo -> new ScoredCombination<>(combo, 0, List.of()))
                    .toList();


            final UniqueDeveloperPairsMetric.Result result = underTest.compute(scoredCombinations);

            assertThat(result.summaryStatistics())
                    .satisfies(summary -> {
                        assertThat(summary.count()).isEqualTo(6);
                        assertThat(summary.min()).isEqualTo(1);
                        assertThat(summary.max()).isEqualTo(2);
                        assertThat(summary.mean()).isCloseTo(1.333d, offset(0.1));
                        assertThat(summary.populationVariance()).isCloseTo(0.222, offset(0.01));
                    });
        }
    }
}