package dev.coldhands.pair.stairs.core.usecases.pairstream;

import com.google.common.collect.Sets;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.usecases.pairstream.metrics.DeveloperDaysInStreamMetric;
import dev.coldhands.pair.stairs.core.usecases.pairstream.metrics.UniqueDeveloperPairsMetric;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

class PairStreamRotationSimulatorTest {

    @Test
    void runForThreeSteps() {
        final var repository = new InMemoryCombinationHistoryRepository<PairStreamCombination>();

        final List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        final List<String> streams = List.of("1-stream", "2-stream");

        final var underTest = new PairStreamRotationSimulator(developers, streams, repository);

        final List<ScoredCombination<PairStreamCombination>> scoredCombinations = underTest.runSimulation(3);

        record AllParties(Set<String> developers, Set<String> streams) {

        }

        assertThat(scoredCombinations)
                .hasSize(3)
                .allSatisfy(scoredCombination -> {
                    final var allParties = scoredCombination.combination().pairs().stream()
                            .collect(teeing(
                                    flatMapping(pair -> pair.developers().stream(), toSet()),
                                    mapping(Pair::stream, toSet()),
                                    AllParties::new
                            ));
                    assertThat(allParties.developers).containsAll(developers);
                    assertThat(allParties.streams).containsAll(streams);
                });

        final var actualCombinations = scoredCombinations.stream()
                .map(ScoredCombination::combination)
                .toArray(PairStreamCombination[]::new);

        assertThat(repository.getAllCombinations())
                .containsExactly(actualCombinations)
        ;
    }

    @Test
    void trialWithRealData() {
        Collection<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev");
        Collection<String> streams = List.of("1-stream", "2-stream", "3-stream");

        CombinationHistoryRepository<PairStreamCombination> repository = new InMemoryCombinationHistoryRepository<>();

        populateOldData(repository);

        final PairStreamRotationSimulator pairStreamRotationSimulator = new PairStreamRotationSimulator(developers, streams, repository);

        final List<ScoredCombination<PairStreamCombination>> scoredCombinations = pairStreamRotationSimulator.runSimulation(60);

        displayResults(scoredCombinations);

        final UniqueDeveloperPairsMetric uniqueDeveloperPairsMetric = new UniqueDeveloperPairsMetric(developers, streams);
        final UniqueDeveloperPairsMetric.Result uniqueDeveloperPairsResult = uniqueDeveloperPairsMetric.compute(scoredCombinations);
        displayMetric(uniqueDeveloperPairsResult);

        final DeveloperDaysInStreamMetric developerDaysInStreamMetric = new DeveloperDaysInStreamMetric(developers, streams);
        final DeveloperDaysInStreamMetric.Result developerDaysInStreamResult = developerDaysInStreamMetric.compute(scoredCombinations);
        displayMetric(developerDaysInStreamResult);

        assertThat(uniqueDeveloperPairsResult)
                .satisfies(result -> {
                    assertThat(result.occurrencesPerPair())
                            .allSatisfy((developerPair, occurrences) -> {
                                assertThat(occurrences)
                                        .describedAs(STR."developers \{developerPair} did not pair")
                                        .isGreaterThan(0);
                            });

                    assertThat(result.summaryStatistics().populationVariance())
                            .isLessThan(19); // Current best score is 18.8000 (or 9.59999 via maven)
                });

        assertThat(developerDaysInStreamResult)
                .satisfies(result -> {
                    assertThat(result.developerToDaysInStream())
                            .allSatisfy((developer, streamDays) -> {
                                assertThat(streamDays.entrySet())
                                        .allSatisfy(streamToCount ->
                                                assertThat(streamToCount.getValue())
                                                        .describedAs(STR."developer \{developer} did not work on stream \{streamToCount.getKey()}")
                                                        .isGreaterThan(0));
                            });

                    assertThat(result.summaryStatistics().populationVariance())
                            .isLessThan(4); // Current best score is 2.33333 (or 3.777777 via maven)
                });
    }

    private void displayMetric(DeveloperDaysInStreamMetric.Result result) {
        System.out.println("-".repeat(20));
        System.out.println(STR."summary: \{result.summaryStatistics()}");
        System.out.println(STR."variance: \{result.summaryStatistics().populationVariance()}");
        result.developerToDaysInStream().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    final String formattedStreamToCount = entry.getValue().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(stream -> stream.getKey() + "=" + stream.getValue())
                            .collect(joining(", ", "[", "]"));
                    System.out.println(entry.getKey() + " " + formattedStreamToCount);
                });
        System.out.println("-".repeat(20));
    }

    private static void displayMetric(UniqueDeveloperPairsMetric.Result result) {
        System.out.println("-".repeat(20));
        System.out.println(STR."ideal: \{result.idealOccurrencesPerPair()}");
        System.out.println(STR."summary: \{result.summaryStatistics()}");
        System.out.println(STR."variance: \{result.summaryStatistics().populationVariance()}");
        System.out.println("all:");
        record PrettyEntry(List<String> pair, int count) {

        }
        result.occurrencesPerPair().entrySet().stream()
                .map(entry -> new PrettyEntry(entry.getKey().stream().sorted().toList(), entry.getValue()))
                .sorted(Comparator.comparing((PrettyEntry entry) -> entry.pair.getFirst()).thenComparing((PrettyEntry entry) -> entry.pair.get(1)))
                .forEach((entry) -> System.out.println(STR."\{entry.pair} = \{entry.count}"));
        System.out.println("-".repeat(20));
    }

    private static void displayResults(List<ScoredCombination<PairStreamCombination>> scoredCombinations) {
        System.out.println("-".repeat(20));
        for (int i = 0; i < scoredCombinations.size(); i++) {
            ScoredCombination<PairStreamCombination> combo = scoredCombinations.get(i);
            System.out.println("=".repeat(10));
            combo.combination().pairs().stream().sorted(Comparator.comparing(Pair::stream)).forEach(pair -> {
                System.out.println(pair.stream() + ": " + String.join(", ", pair.developers()));
            });
            System.out.println("Score: " + combo.totalScore() + " results: " + combo.scoreResults().stream().map(ScoreResult::score).map(String::valueOf).collect(joining(", ")));
            if (i != 0) {
                final Map<String, Set<String>> oldCombo = keyedByStream(scoredCombinations.get(i - 1).combination());
                final Map<String, Set<String>> newCombo = keyedByStream(combo.combination());

                final ArrayList<String> switched = new ArrayList<>();
                newCombo.forEach((stream, devs) -> {
                    final Set<String> oldDevs = oldCombo.get(stream);
                    switched.addAll(Sets.difference(devs, oldDevs));
                });
                System.out.println("switched: " + switched.stream().sorted().toList());
            }
            System.out.println("=".repeat(10));
        }
        System.out.println("-".repeat(20));
    }

    private static Map<String, Set<String>> keyedByStream(PairStreamCombination combination) {
        return combination.pairs().stream()
                .collect(toMap(Pair::stream, Pair::developers));
    }

    private void populateOldData(CombinationHistoryRepository<PairStreamCombination> repository) {
        final LocalDate now = LocalDate.now();
        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "f-dev"), "2-stream"),
                new Pair(Set.of("d-dev", "e-dev"), "3-stream"),
                new Pair(Set.of("b-dev", "c-dev"), "1-stream")
        )), now.minusDays(1));
        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "e-dev"), "2-stream"),
                new Pair(Set.of("d-dev", "c-dev"), "3-stream"),
                new Pair(Set.of("b-dev", "f-dev"), "1-stream")
        )), now.minusDays(2));
        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "c-dev"), "2-stream"),
                new Pair(Set.of("d-dev", "b-dev"), "3-stream"),
                new Pair(Set.of("e-dev", "f-dev"), "1-stream")
        )), now.minusDays(3));
        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("f-dev", "c-dev"), "2-stream"),
                new Pair(Set.of("a-dev", "b-dev"), "3-stream"),
                new Pair(Set.of("e-dev", "d-dev"), "1-stream")
        )), now.minusDays(4));
        repository.saveCombination(new PairStreamCombination(Set.of(
                new Pair(Set.of("f-dev", "e-dev"), "2-stream"),
                new Pair(Set.of("a-dev", "c-dev"), "3-stream"),
                new Pair(Set.of("b-dev", "d-dev"), "1-stream")
        )), now.minusDays(5));
    }
}