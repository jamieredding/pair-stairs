package dev.coldhands.pair.stairs.core.usecases.pairstream.metrics;

import dev.coldhands.pair.stairs.core.domain.Metric;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamCombinationService;

import java.util.*;
import java.util.stream.Collectors;

public class UniqueDeveloperPairsMetric implements Metric<PairStreamCombination, UniqueDeveloperPairsMetric.Result> {

    private final Collection<String> developers;
    private final Collection<String> stream;

    public UniqueDeveloperPairsMetric(Collection<String> developers, Collection<String> stream) {
        this.developers = developers;
        this.stream = stream;
    }

    @Override
    public Result compute(List<ScoredCombination<PairStreamCombination>> scoredCombinations) {
        final Map<Set<String>, Integer> occurrencesPerPair = computeOccurrencesPerPair(scoredCombinations);
        final double idealOccurrencesPerPair = computeIdealOccurrencesPerPair(scoredCombinations.size(), occurrencesPerPair.size());
        final DoubleSummaryStatistics summaryStatistics = computeSummaryStatistics(occurrencesPerPair);

        return new Result(occurrencesPerPair, idealOccurrencesPerPair, summaryStatistics);
    }

    private Map<Set<String>, Integer> computeOccurrencesPerPair(List<ScoredCombination<PairStreamCombination>> scoredCombinations) {
        /* todo using this is expensive,
         *       can I refactor just the developer pair part out of PairStreamCombinationService?
         *       yes, have a PairCombinationService, then have PSCS stream over this and multimap each by the number of streams?
         */
        final Set<PairStreamCombination> allCombinations = new PairStreamCombinationService(developers, stream).getAllCombinations();

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

    private static DoubleSummaryStatistics computeSummaryStatistics(Map<Set<String>, Integer> occurrencesPerPair) {
        return occurrencesPerPair.values().stream()
                .mapToDouble(v -> v)
                .summaryStatistics();
    }

    public record Result(
            Map<Set<String>, Integer> occurrencesPerPair,
            double idealOccurrencesPerPair,
            DoubleSummaryStatistics summaryStatistics
    ) {

    }
}
