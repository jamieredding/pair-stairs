package dev.coldhands.pair.stairs.core.usecases.pairstream.metrics;

import com.google.common.math.Stats;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.Metric;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamCombinationService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UniqueDeveloperPairsMetric implements Metric<PairStream, UniqueDeveloperPairsMetric.Result> {
    // todo implement using PairStreamStatisticsService

    private final Collection<String> developers;
    private final Collection<String> streams;

    public UniqueDeveloperPairsMetric(Collection<String> developers, Collection<String> streams) {
        this.developers = developers;
        this.streams = streams;
    }

    @Override
    public Result compute(List<ScoredCombination<PairStream>> scoredCombinations) {
        final Map<Set<String>, Integer> occurrencesPerPair = computeOccurrencesPerPair(scoredCombinations);
        final double idealOccurrencesPerPair = computeIdealOccurrencesPerPair(scoredCombinations.size(), occurrencesPerPair.size());
        final Stats summaryStatistics = computeSummaryStatistics(occurrencesPerPair);

        return new Result(occurrencesPerPair, idealOccurrencesPerPair, summaryStatistics);
    }

    private Map<Set<String>, Integer> computeOccurrencesPerPair(List<ScoredCombination<PairStream>> scoredCombinations) {
        /* todo using this is expensive,
         *       can I refactor just the developer pair part out of PairStreamCombinationService?
         *       yes, have a PairCombinationService, then have PSCS stream over this and multimap each by the number of streams?
         */
        final Set<Combination<PairStream>> allCombinations = new PairStreamCombinationService(developers, streams).getAllCombinations();

        final Map<Set<String>, Integer> allDeveloperPairs = allCombinations.stream()
                .map(Combination::pairs)
                .flatMap(Collection::stream)
                .map(PairStream::developers)
                .distinct()
                .collect(Collectors.toMap(pair -> pair, _ -> 0));


        scoredCombinations.stream()
                .map(ScoredCombination::combination)
                .map(Combination::pairs)
                .flatMap(Collection::stream)
                .map(PairStream::developers)
                .forEach(developers ->
                        allDeveloperPairs.merge(developers, 1, Integer::sum));

        return allDeveloperPairs;
    }

    private double computeIdealOccurrencesPerPair(int totalCombinations, int numberOfPossiblePairs) {
        int pairsPerCombination = Math.ceilDiv(developers.size(), 2);
        final int totalPairsThatCouldHappen = totalCombinations * pairsPerCombination;

        return (double) totalPairsThatCouldHappen / numberOfPossiblePairs;
    }

    private static Stats computeSummaryStatistics(Map<Set<String>, Integer> occurrencesPerPair) {
        return occurrencesPerPair.values().stream()
                .collect(Stats.toStats());
    }

    public record Result(
            Map<Set<String>, Integer> occurrencesPerPair,
            double idealOccurrencesPerPair,
            Stats summaryStatistics
    ) {

    }
}
