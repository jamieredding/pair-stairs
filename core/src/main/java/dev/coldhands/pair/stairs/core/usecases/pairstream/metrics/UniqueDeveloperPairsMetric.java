package dev.coldhands.pair.stairs.core.usecases.pairstream.metrics;

import com.google.common.math.Stats;
import dev.coldhands.pair.stairs.core.domain.Metric;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamCombinationService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UniqueDeveloperPairsMetric implements Metric<PairStreamCombination, UniqueDeveloperPairsMetric.Result> {
    // todo implement using PairStreamStatisticsService

    private final Collection<String> developers;
    private final Collection<String> streams;

    public UniqueDeveloperPairsMetric(Collection<String> developers, Collection<String> streams) {
        this.developers = developers;
        this.streams = streams;
    }

    @Override
    public Result compute(List<ScoredCombination<PairStreamCombination>> scoredCombinations) {
        final Map<Set<String>, Integer> occurrencesPerPair = computeOccurrencesPerPair(scoredCombinations);
        final double idealOccurrencesPerPair = computeIdealOccurrencesPerPair(scoredCombinations.size(), occurrencesPerPair.size());
        final Stats summaryStatistics = computeSummaryStatistics(occurrencesPerPair);

        return new Result(occurrencesPerPair, idealOccurrencesPerPair, summaryStatistics);
    }

    private Map<Set<String>, Integer> computeOccurrencesPerPair(List<ScoredCombination<PairStreamCombination>> scoredCombinations) {
        /* todo using this is expensive,
         *       can I refactor just the developer pair part out of PairStreamCombinationService?
         *       yes, have a PairCombinationService, then have PSCS stream over this and multimap each by the number of streams?
         */
        final Set<PairStreamCombination> allCombinations = new PairStreamCombinationService(developers, streams).getAllCombinations();

        final Map<Set<String>, Integer> allDeveloperPairs = allCombinations.stream()
                .map(PairStreamCombination::pairs)
                .flatMap(Collection::stream)
                .map(Pair::developers)
                .distinct()
                .collect(Collectors.toMap(pair -> pair, _ -> 0));


        scoredCombinations.stream()
                .map(ScoredCombination::combination)
                .map(PairStreamCombination::pairs)
                .flatMap(Collection::stream)
                .map(Pair::developers)
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
