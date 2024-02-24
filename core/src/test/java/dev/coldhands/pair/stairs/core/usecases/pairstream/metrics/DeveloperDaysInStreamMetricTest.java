package dev.coldhands.pair.stairs.core.usecases.pairstream.metrics;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class DeveloperDaysInStreamMetricTest {

    @Test
    void whenNoCombinationsThenShowAllDevsAndStreamsAtZeroCount() {
        final var underTest = new DeveloperDaysInStreamMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

        final DeveloperDaysInStreamMetric.Result result = underTest.compute(List.of());

        assertThat(result.developerToDaysInStream())
                .containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
                        entry("a-dev", Map.of("1-stream", 0, "2-stream", 0)),
                        entry("b-dev", Map.of("1-stream", 0, "2-stream", 0)),
                        entry("c-dev", Map.of("1-stream", 0, "2-stream", 0)))
                );

        assertThat(result.summaryStatistics())
                .satisfies(summary -> {
                    assertThat(summary.count()).isEqualTo(6);
                    assertThat(summary.mean()).isEqualTo(0);
                });
    }

    @Test
    void whenSomeCombinationsThenCountTheNumberOfTimesEachDeveloperIsInAStream() {
        final var underTest = new DeveloperDaysInStreamMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

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

        final DeveloperDaysInStreamMetric.Result result = underTest.compute(scoredCombinations);

        assertThat(result.developerToDaysInStream())
                .containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
                        entry("a-dev", Map.of("1-stream", 3, "2-stream", 1)),
                        entry("b-dev", Map.of("1-stream", 1, "2-stream", 3)),
                        entry("c-dev", Map.of("1-stream", 2, "2-stream", 2)))
                );

        assertThat(result.summaryStatistics())
                .satisfies(summary -> {
                    assertThat(summary.count()).isEqualTo(6);
                    assertThat(summary.min()).isEqualTo(1);
                    assertThat(summary.max()).isEqualTo(3);
                    assertThat(summary.mean()).isEqualTo(2);
                    assertThat(summary.populationVariance()).isCloseTo(0.666, offset(0.01));
                });
    }
}