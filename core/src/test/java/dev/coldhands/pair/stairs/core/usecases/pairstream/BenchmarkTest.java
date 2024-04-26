package dev.coldhands.pair.stairs.core.usecases.pairstream;

import com.google.common.math.Stats;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.Metric;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.usecases.pairstream.metrics.DeveloperDaysInStreamMetric;
import dev.coldhands.pair.stairs.core.usecases.pairstream.metrics.UniqueDeveloperPairsMetric;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

public class BenchmarkTest {

    @Test
    @Disabled
    void runBenchmark() {
        Collection<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev");
        Collection<String> streams = List.of("1-stream", "2-stream", "3-stream");

        final Set<Combination<PairStream>> allCombinations = new PairStreamCombinationService(developers, streams).getAllCombinations();

        record StaticMetricData(double ideal, String description) {

        }

        final var staticMetricDataMap = Map.of(
                UniqueDeveloperPairsMetric.class, new StaticMetricData(26, "lower indicates more even counts of pairing between everyone"),
                DeveloperDaysInStreamMetric.class, new StaticMetricData(10, "lower indicates more even number of days on each stream for everyone")
        );

        final Map<? extends Class<? extends Metric<?, ?>>, List<MetricData>> allMetricData = allCombinations.stream()
                .parallel()
                .map(combination -> simulateStartingFrom(combination, developers, streams))
                .flatMap(Collection::stream)
                .collect(groupingBy(MetricData::metricType));

        allMetricData.forEach((type, data) -> {
            final var staticMetricData = staticMetricDataMap.get(type);

            final var stats = data.stream()
                    .map(MetricData::actualScore)
                    .collect(Stats.toStats());

            System.out.println(data);

            assertThat(stats.mean())
                    .describedAs(staticMetricData.description() + " " + stats)
                    .isLessThan(staticMetricData.ideal);
        });

    }

    private record MetricData(double actualScore, Class<? extends Metric<?, ?>> metricType) {
    }

    private interface MetricProcessor {
        double assertAndCalculateScore(List<ScoredCombination<PairStream>> scoredCombinations);

        Class<? extends Metric<?, ?>> metricType();
    }

    private record UniqueDevelopers(Collection<String> developers,
                                    Collection<String> streams) implements MetricProcessor {

        @Override
        public double assertAndCalculateScore(List<ScoredCombination<PairStream>> scoredCombinations) {
            final UniqueDeveloperPairsMetric metric = new UniqueDeveloperPairsMetric(developers, streams);
            final UniqueDeveloperPairsMetric.Result result = metric.compute(scoredCombinations);

//            assertThat(result.occurrencesPerPair())
//                    .allSatisfy((developerPair, occurrences) -> {
//                        assertThat(occurrences)
//                                .describedAs(STR."developers \{developerPair} did not pair")
//                                .isGreaterThan(0);
//                    });

            return result.summaryStatistics().populationVariance();
        }

        @Override
        public Class<? extends Metric<?, ?>> metricType() {
            return UniqueDeveloperPairsMetric.class;
        }
    }

    private record DaysInStream(Collection<String> developers,
                                Collection<String> streams) implements MetricProcessor {

        @Override
        public double assertAndCalculateScore(List<ScoredCombination<PairStream>> scoredCombinations) {
            final DeveloperDaysInStreamMetric metric = new DeveloperDaysInStreamMetric(developers, streams);
            final DeveloperDaysInStreamMetric.Result result = metric.compute(scoredCombinations);

//            assertThat(result.developerToDaysInStream())
//                    .allSatisfy((developer, streamDays) -> {
//                        assertThat(streamDays.entrySet())
//                                .allSatisfy(streamToCount ->
//                                        assertThat(streamToCount.getValue())
//                                                .describedAs(STR."developer \{developer} did not work on stream \{streamToCount.getKey()}")
//                                                .isGreaterThan(0));
//                    });

            return result.summaryStatistics().populationVariance();
        }

        @Override
        public Class<? extends Metric<?, ?>> metricType() {
            return DeveloperDaysInStreamMetric.class;
        }
    }

    private static List<MetricData> simulateStartingFrom(Combination<PairStream> combination, Collection<String> developers, Collection<String> streams) {
        var repository = new InMemoryCombinationHistoryRepository<PairStream>();
        repository.saveCombination(combination, LocalDate.now());

        final PairStreamRotationSimulator pairStreamRotationSimulator = new PairStreamRotationSimulator(developers, streams, repository);

        final List<ScoredCombination<PairStream>> scoredCombinations = pairStreamRotationSimulator.runSimulation(60);

        final List<MetricProcessor> metricProcessors = List.of(
                new UniqueDevelopers(developers, streams),
                new DaysInStream(developers, streams)
        );

        return metricProcessors.stream()
                .map(metricProcessor -> {

                    final double actualScore = metricProcessor.assertAndCalculateScore(scoredCombinations);
                    return new MetricData(actualScore, metricProcessor.metricType());
                })
                .toList();
    }

}
