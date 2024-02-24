package dev.coldhands.pair.stairs.core.usecases.pairstream.metrics;

import com.google.common.math.Stats;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.Metric;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class DeveloperDaysInStreamMetric implements Metric<Combination<Pair>, DeveloperDaysInStreamMetric.Result> {

    // todo implement using PairStreamStatisticsService

    private final Collection<String> developers;
    private final Collection<String> streams;

    public DeveloperDaysInStreamMetric(Collection<String> developers, Collection<String> streams) {
        this.developers = developers;
        this.streams = streams;
    }

    @Override
    public Result compute(List<ScoredCombination<Combination<Pair>>> scoredCombinations) {
        final Map<String, Map<String, Integer>> developerToDaysInStream = computeDeveloperToDaysInStream(scoredCombinations);

        Stats summaryStatistics = computeSummaryStatistics(developerToDaysInStream);

        return new Result(developerToDaysInStream, summaryStatistics);
    }

    // todo extract and more efficient
    private Map<String, Map<String, Integer>> computeDeveloperToDaysInStream(List<ScoredCombination<Combination<Pair>>> scoredCombinations) {
        Map<String, Map<String, Integer>> developerToDaysInStream = initialiseMap();

        scoredCombinations.stream()
                .map(ScoredCombination::combination)
                .map(Combination::pairs)
                .flatMap(Collection::stream)
                .forEach(pair -> {
                    pair.developers().forEach(developer -> {
                        final Map<String, Integer> countPerStream = developerToDaysInStream.get(developer);
                        countPerStream.merge(pair.stream(), 1, Integer::sum);
                    });
                });
        return developerToDaysInStream;
    }

    private Map<String, Map<String, Integer>> initialiseMap() {
        return developers.stream()
                .collect(toMap(
                        dev -> dev,
                        _ -> streams.stream()
                                .collect(toMap(
                                        stream -> stream,
                                        _ -> 0
                                ))
                ));
    }

    private static Stats computeSummaryStatistics(Map<String, Map<String, Integer>> developersToDaysInStream) {
        return developersToDaysInStream.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Stats.toStats());
    }

    public record Result(Map<String, Map<String, Integer>> developerToDaysInStream, Stats summaryStatistics) {

    }
}
