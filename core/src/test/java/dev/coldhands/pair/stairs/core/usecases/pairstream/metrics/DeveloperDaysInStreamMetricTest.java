package dev.coldhands.pair.stairs.core.usecases.pairstream.metrics;

import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

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
    }

    @Test
    void whenSomeCombinationsThenCountTheNumberOfTimesEachDeveloperIsInAStream() {
        final var underTest = new DeveloperDaysInStreamMetric(Set.of("a-dev", "b-dev", "c-dev"), Set.of("1-stream", "2-stream"));

        List<ScoredCombination<PairStreamCombination>> scoredCombinations = Stream.of(
                        new PairStreamCombination(Set.of(
                                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                                new Pair(Set.of("c-dev"), "2-stream")
                        )),
                        new PairStreamCombination(Set.of(
                                new Pair(Set.of("a-dev", "b-dev"), "2-stream"),
                                new Pair(Set.of("c-dev"), "1-stream")
                        )),
                        new PairStreamCombination(Set.of(
                                new Pair(Set.of("a-dev", "c-dev"), "1-stream"),
                                new Pair(Set.of("b-dev"), "2-stream")
                        )),
                        new PairStreamCombination(Set.of(
                                new Pair(Set.of("a-dev"), "1-stream"),
                                new Pair(Set.of("b-dev", "c-dev"), "2-stream")
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
    }
}