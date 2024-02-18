package dev.coldhands.pair.stairs.core.usecases.pairstream.metrics;

import dev.coldhands.pair.stairs.core.domain.Metric;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class DeveloperDaysInStreamMetric implements Metric<PairStreamCombination, DeveloperDaysInStreamMetric.Result> {

    private final Collection<String> developers;
    private final Collection<String> streams;

    public DeveloperDaysInStreamMetric(Collection<String> developers, Collection<String> streams) {
        this.developers = developers;
        this.streams = streams;
    }

    @Override
    public Result compute(List<ScoredCombination<PairStreamCombination>> scoredCombinations) {
        Map<String, Map<String, Integer>> developerToDaysInStream = initialiseMap();

        scoredCombinations.stream()
                .map(ScoredCombination::combination)
                .map(PairStreamCombination::pairs)
                .flatMap(Collection::stream)
                .forEach(pair -> {
                    pair.developers().forEach(developer -> {
                        final Map<String, Integer> countPerStream = developerToDaysInStream.get(developer);
                        countPerStream.merge(pair.stream(), 1, Integer::sum);
                    });
                });

        return new Result(developerToDaysInStream);
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

    public record Result(Map<String, Map<String, Integer>> developerToDaysInStream) {

    }
}
