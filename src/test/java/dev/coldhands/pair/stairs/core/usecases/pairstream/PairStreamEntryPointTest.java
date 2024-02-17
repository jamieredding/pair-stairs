package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairStreamEntryPointTest {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();

    @Test
    void calculateWithNoHistory() {
        final var underTest = new PairStreamEntryPoint(
                List.of("a-dev", "b-dev", "c-dev"),
                List.of("1-stream", "2-stream"),
                combinationHistoryRepository);

        final var sortedCombinations = getSortedCombinations(underTest);

        assertThat(sortedCombinations)
                .allSatisfy(combination -> {
                    assertThat(combination.pairs().stream().flatMap(pair -> pair.developers().stream()))
                            .containsExactlyInAnyOrder("a-dev", "b-dev", "c-dev");
                    assertThat(combination.pairs().stream().map(Pair::stream))
                            .containsExactlyInAnyOrder("1-stream", "2-stream");
                });
    }

    @Nested
    class ApplyRules {

        @Test
        void shouldNotHaveSameCombinationTwoDaysInARow() {
            final var yesterdayCombination = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            final var underTest = new PairStreamEntryPoint(
                    List.of("a-dev", "b-dev", "c-dev"),
                    List.of("1-stream", "2-stream"),
                    combinationHistoryRepository
            );

            final var sortedCombinations = getSortedCombinations(underTest);

            assertThat(sortedCombinations.getFirst())
                    .satisfies(bestCombination -> {
                        assertThat(bestCombination).isNotEqualTo(yesterdayCombination);
                    });
        }

        @Test
        void someoneShouldStayInAStreamFromThePreviousDayToAllowKnowledgeTransfer() {
            final var yesterdayCombination = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            final var underTest = new PairStreamEntryPoint(
                    List.of("a-dev", "b-dev", "c-dev"),
                    List.of("1-stream", "2-stream"),
                    combinationHistoryRepository);

            final var sortedCombinations = getSortedCombinations(underTest);

            assertThat(sortedCombinations.getFirst())
                    .satisfies(bestCombination -> {
                        final var stream1Combo = getComboForStream(bestCombination, "1-stream");
                        final var stream2Combo = getComboForStream(bestCombination, "2-stream");

                        assertThat(stream1Combo.developers()).containsAnyOf("a-dev", "b-dev");
                        assertThat(stream2Combo.developers()).containsAnyOf("c-dev");
                    });
        }

        @Test
        void ideallyStayInAStreamForMoreThan1Day() {
            final var dayBeforeYesterdayCombination = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev"), "2-stream")
            ));
            final var yesterdayCombination = new PairStreamCombination(Set.of(
                    new Pair(Set.of("b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev", "a-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(dayBeforeYesterdayCombination, LocalDate.now().minusDays(2));
            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            final var underTest = new PairStreamEntryPoint(
                    List.of("a-dev", "b-dev", "c-dev"),
                    List.of("1-stream", "2-stream"),
                    combinationHistoryRepository);

            final var sortedCombinations = getSortedCombinations(underTest);

            assertThat(sortedCombinations.getFirst())
                    .isEqualTo(
                            new PairStreamCombination(Set.of(
                                    new Pair(Set.of("b-dev", "c-dev"), "1-stream"),
                                    new Pair(Set.of("a-dev"), "2-stream")
                            ))
                    );
        }
    }

    private static Pair getComboForStream(PairStreamCombination bestCombination, String wantedStream) {
        return bestCombination.pairs().stream().filter(pair -> pair.stream().equals(wantedStream)).findFirst().orElseThrow();
    }

    private static List<PairStreamCombination> getSortedCombinations(PairStreamEntryPoint underTest) {
        return underTest.computeScoredCombinations()
                .stream()
                .map(ScoredCombination::combination)
                .toList();
    }
}