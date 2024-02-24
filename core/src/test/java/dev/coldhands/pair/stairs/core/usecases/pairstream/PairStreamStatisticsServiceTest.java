package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PairStreamStatisticsServiceTest {

    private final CombinationHistoryRepository<PairStreamCombination> repository = new InMemoryCombinationHistoryRepository<>();
    private PairStreamStatisticsService underTest;

    @Test
    void startWithZeroForAllCounts() {
        final List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        final List<String> streams = List.of("1-stream", "2-stream");
        initialiseUnderTest(
                developers,
                streams,
                5
        );

        developers.forEach(dev -> {
            streams.forEach(stream -> {
                assertThat(underTest.getRecentOccurrenceOfDeveloperInStream(dev, stream)).isEqualTo(0);
            });
        });

        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev", "b-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev", "c-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("b-dev", "c-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("b-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("c-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("c-dev"))).isEqualTo(0);

        assertThatThrownBy(() -> underTest.getRecentOccurrencesOfDeveloperPair(Set.of("d-dev")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updatesStatisticsWhenACombinationIsAddedToRepository() {
        final List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        final List<String> streams = List.of("1-stream", "2-stream");

        initialiseUnderTest(
                developers,
                streams,
                5
        );

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), LocalDate.now());

        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("a-dev", "1-stream")).isEqualTo(0);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("b-dev", "1-stream")).isEqualTo(0);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("c-dev", "2-stream")).isEqualTo(0);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("c-dev", "1-stream")).isEqualTo(0);

        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev", "b-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("c-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev"))).isEqualTo(0);

        underTest.updateStatistics();

        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("a-dev", "1-stream")).isEqualTo(1);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("b-dev", "1-stream")).isEqualTo(1);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("c-dev", "2-stream")).isEqualTo(1);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("c-dev", "1-stream")).isEqualTo(0);

        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev", "b-dev"))).isEqualTo(1);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("c-dev"))).isEqualTo(1);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev"))).isEqualTo(0);
    }

    @Test
    void onlyConsidersRecentHistoryOfRepository() {
        final List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        final List<String> streams = List.of("1-stream", "2-stream");

        initialiseUnderTest(
                developers,
                streams,
                5
        );

        final LocalDate now = LocalDate.now();

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "c-dev"), "1-stream"),
                new Pair(Set.of("b-dev"), "2-stream")
        )), now.minusDays(5));

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), now.minusDays(4));

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), now.minusDays(3));

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), now.minusDays(2));

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), now.minusDays(1));

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), now);

        underTest.updateStatistics();

        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("a-dev", "1-stream")).isEqualTo(5);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("b-dev", "1-stream")).isEqualTo(5);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("c-dev", "2-stream")).isEqualTo(5);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("c-dev", "1-stream")).isEqualTo(0);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("b-dev", "2-stream")).isEqualTo(0);

        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev", "b-dev"))).isEqualTo(5);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("c-dev"))).isEqualTo(5);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev", "c-dev"))).isEqualTo(0);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("b-dev"))).isEqualTo(0);
    }

    @Test
    void canConfigureNumberOfPreviousCombinationsToConsider() {
        final List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        final List<String> streams = List.of("1-stream", "2-stream");

        initialiseUnderTest(
                developers,
                streams,
                2
        );

        final LocalDate now = LocalDate.now();

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), now.minusDays(2));

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), now.minusDays(1));

        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        )), now);

        underTest.updateStatistics();

        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("a-dev", "1-stream")).isEqualTo(2);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("b-dev", "1-stream")).isEqualTo(2);
        assertThat(underTest.getRecentOccurrenceOfDeveloperInStream("c-dev", "2-stream")).isEqualTo(2);

        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("a-dev", "b-dev"))).isEqualTo(2);
        assertThat(underTest.getRecentOccurrencesOfDeveloperPair(Set.of("c-dev"))).isEqualTo(2);
    }

    private void initialiseUnderTest(List<String> developers, List<String> streams, int numberOfPreviousCombinationsToConsider) {
        underTest = new PairStreamStatisticsService(repository, developers, streams, numberOfPreviousCombinationsToConsider);
        underTest.updateStatistics();
    }

}