package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PairStreamEntryPointTest {

    private final CombinationHistoryRepository<Pair> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();

    @Test
    void calculateWithNoHistory() {
        final var underTest = initialiseUnderTest(
                List.of("a-dev", "b-dev", "c-dev"),
                List.of("1-stream", "2-stream")
        );

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
            final var yesterdayCombination = new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            final var underTest = initialiseUnderTest(
                    List.of("a-dev", "b-dev", "c-dev"),
                    List.of("1-stream", "2-stream")
            );

            final var sortedCombinations = getSortedCombinations(underTest);

            assertThat(sortedCombinations.getFirst())
                    .satisfies(bestCombination -> {
                        assertThat(bestCombination).isNotEqualTo(yesterdayCombination);
                    });
        }

        @Test
        void someoneShouldStayInAStreamFromThePreviousDayToAllowKnowledgeTransfer() {
            final var yesterdayCombination = new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            final var underTest = initialiseUnderTest(
                    List.of("a-dev", "b-dev", "c-dev"),
                    List.of("1-stream", "2-stream")
            );

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
            final var dayBeforeYesterdayCombination = new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev"), "2-stream")
            ));
            final var yesterdayCombination = new Combination<>(Set.of(
                    new Pair(Set.of("b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev", "a-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(dayBeforeYesterdayCombination, LocalDate.now().minusDays(2));
            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            final var underTest = initialiseUnderTest(
                    List.of("a-dev", "b-dev", "c-dev"),
                    List.of("1-stream", "2-stream")
            );

            final var sortedCombinations = getSortedCombinations(underTest);

            assertThat(sortedCombinations.getFirst())
                    .isEqualTo(
                            new Combination<>(Set.of(
                                    new Pair(Set.of("b-dev", "c-dev"), "1-stream"),
                                    new Pair(Set.of("a-dev"), "2-stream")
                            ))
                    );
        }

        @Test
        void preferAPairThatHasNotHappenedRecently() {
            saveARealisticFiveDayHistory();

            final var underTest = initialiseUnderTest(
                    List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev"),
                    List.of("1-stream", "2-stream", "3-stream")
            );

            final var sortedCombinations = getSortedCombinations(underTest);

            assertThat(sortedCombinations.getFirst())
                    .isEqualTo(
                            new Combination<>(Set.of(
                                    new Pair(Set.of("a-dev", "c-dev"), "1-stream"),
                                    new Pair(Set.of("d-dev", "f-dev"), "2-stream"),
                                    new Pair(Set.of("b-dev", "e-dev"), "3-stream")
                            ))
                    );
        }

        private void saveARealisticFiveDayHistory() {
            final LocalDate now = LocalDate.now();
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "f-dev"), "2-stream"),
                    new Pair(Set.of("d-dev", "e-dev"), "3-stream"),
                    new Pair(Set.of("b-dev", "c-dev"), "1-stream")
            )), now.minusDays(1));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "e-dev"), "2-stream"),
                    new Pair(Set.of("d-dev", "c-dev"), "3-stream"),
                    new Pair(Set.of("b-dev", "f-dev"), "1-stream")
            )), now.minusDays(2));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "c-dev"), "2-stream"),
                    new Pair(Set.of("d-dev", "b-dev"), "3-stream"),
                    new Pair(Set.of("e-dev", "f-dev"), "1-stream")
            )), now.minusDays(3));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("f-dev", "c-dev"), "2-stream"),
                    new Pair(Set.of("a-dev", "b-dev"), "3-stream"),
                    new Pair(Set.of("e-dev", "d-dev"), "1-stream")
            )), now.minusDays(4));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("f-dev", "e-dev"), "2-stream"),
                    new Pair(Set.of("a-dev", "c-dev"), "3-stream"),
                    new Pair(Set.of("b-dev", "d-dev"), "1-stream")
            )), now.minusDays(5));
        }

        @Test
        @Disabled("Implemented by EveryPairShouldHappenAfterXDaysRule but is currently unsuitable")
        void everyPairShouldHappenAfterXDays() {
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("f-dev", "d-dev"), "1-stream"),
                    new Pair(Set.of("b-dev", "e-dev"), "2-stream"),
                    new Pair(Set.of("a-dev", "c-dev"), "3-stream")
            )), LocalDate.now().minusDays(10));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "f-dev"), "1-stream"),
                    new Pair(Set.of("e-dev", "d-dev"), "2-stream"),
                    new Pair(Set.of("b-dev", "c-dev"), "3-stream")
            )), LocalDate.now().minusDays(9));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("b-dev", "f-dev"), "1-stream"),
                    new Pair(Set.of("a-dev", "e-dev"), "2-stream"),
                    new Pair(Set.of("c-dev", "d-dev"), "3-stream")
            )), LocalDate.now().minusDays(8));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("b-dev", "e-dev"), "1-stream"),
                    new Pair(Set.of("c-dev", "a-dev"), "2-stream"),
                    new Pair(Set.of("d-dev", "f-dev"), "3-stream")
            )), LocalDate.now().minusDays(7));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("e-dev", "d-dev"), "1-stream"),
                    new Pair(Set.of("b-dev", "c-dev"), "2-stream"),
                    new Pair(Set.of("a-dev", "f-dev"), "3-stream")
            )), LocalDate.now().minusDays(6));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("c-dev", "d-dev"), "1-stream"),
                    new Pair(Set.of("b-dev", "f-dev"), "2-stream"),
                    new Pair(Set.of("a-dev", "e-dev"), "3-stream")
            )), LocalDate.now().minusDays(5));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("c-dev", "a-dev"), "1-stream"),
                    new Pair(Set.of("d-dev", "f-dev"), "2-stream"),
                    new Pair(Set.of("b-dev", "e-dev"), "3-stream")
            )), LocalDate.now().minusDays(4));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "f-dev"), "1-stream"),
                    new Pair(Set.of("e-dev", "d-dev"), "2-stream"),
                    new Pair(Set.of("b-dev", "c-dev"), "3-stream")
            )), LocalDate.now().minusDays(3));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("d-dev", "f-dev"), "1-stream"),
                    new Pair(Set.of("b-dev", "e-dev"), "2-stream"),
                    new Pair(Set.of("c-dev", "a-dev"), "3-stream")
            )), LocalDate.now().minusDays(2));
            combinationHistoryRepository.saveCombination(new Combination<>(Set.of(
                    new Pair(Set.of("e-dev", "d-dev"), "1-stream"),
                    new Pair(Set.of("b-dev", "c-dev"), "2-stream"),
                    new Pair(Set.of("a-dev", "f-dev"), "3-stream")
            )), LocalDate.now().minusDays(1));

            final var underTest = initialiseUnderTest(
                    List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev"),
                    List.of("1-stream", "2-stream", "3-stream")
            );

            final var sortedCombinations = getSortedCombinations(underTest);

            // this fails to pick because it won't move two fresh developers into a stream that neither of them have context on

            assertThat(sortedCombinations.getFirst())
                    .satisfies(combination -> {
                        final Set<Set<String>> pairsOfDevelopers = combination.pairs().stream()
                                .map(Pair::developers)
                                .collect(Collectors.toSet());
                        assertThat(pairsOfDevelopers)
                                .containsAnyOf(
                                        Set.of("a-dev", "b-dev"),
                                        Set.of("a-dev", "d-dev"),
                                        Set.of("b-dev", "d-dev"),
                                        Set.of("c-dev", "e-dev"),
                                        Set.of("c-dev", "f-dev"),
                                        Set.of("e-dev", "f-dev")
                                );
                    });
        }
    }

    private PairStreamEntryPoint initialiseUnderTest(List<String> developers, List<String> streams) {
        final var statisticsService = new PairStreamStatisticsService(combinationHistoryRepository, developers, streams, 5);
        statisticsService.updateStatistics();

        return new PairStreamEntryPoint(
                developers,
                streams,
                combinationHistoryRepository,
                statisticsService);
    }

    private static Pair getComboForStream(Combination<Pair> bestCombination, String wantedStream) {
        return bestCombination.pairs().stream().filter(pair -> pair.stream().equals(wantedStream)).findFirst().orElseThrow();
    }

    private static List<Combination<Pair>> getSortedCombinations(PairStreamEntryPoint underTest) {
        return underTest.computeScoredCombinations()
                .stream()
                .map(ScoredCombination::combination)
                .toList();
    }
}