package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.usecases.pair.PairCombinationService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

public class PairStreamStatisticsService {

    private final Map<Set<String>, Integer> recentPairOccurrences;
    private final Map<String, Map<String, Integer>> developerToDaysInStream;
    private final CombinationHistoryRepository<Pair> repository;
    private final int numberOfPreviousCombinationsToConsider;

    public PairStreamStatisticsService(CombinationHistoryRepository<Pair> repository, Collection<String> developers, Collection<String> streams, int numberOfPreviousCombinationsToConsider) {
        this.repository = repository;
        this.recentPairOccurrences = initialiseOccurrencesPerPair(developers);
        this.developerToDaysInStream = initialiseDeveloperToDaysInStream(developers, streams);
        this.numberOfPreviousCombinationsToConsider = numberOfPreviousCombinationsToConsider;
    }

    public void updateStatistics() {
        final List<Combination<Pair>> combinationsToConsider = repository.getMostRecentCombinations(numberOfPreviousCombinationsToConsider);

        updateOccurrencesPerPair(combinationsToConsider);
        updateDeveloperToDaysInStream(combinationsToConsider);
    }

    public int getRecentOccurrencesOfDeveloperPair(Set<String> developers) {
        return recentPairOccurrences.get(developers);
    }

    public int getRecentOccurrenceOfDeveloperInStream(String developer, String stream) {
        return developerToDaysInStream.get(developer).get(stream);
    }

    private void updateOccurrencesPerPair(List<Combination<Pair>> combinations) {
        recentPairOccurrences.forEach((pair, _) -> {
            recentPairOccurrences.merge(pair, 0, (_, zeroCount)  -> zeroCount);
        });


        combinations.stream()
                .map(Combination::pairs)
                .flatMap(Collection::stream)
                .map(Pair::developers)
                .forEach(developers ->
                        recentPairOccurrences.merge(developers, 1, Integer::sum));
    }

    private static Map<Set<String>, Integer> initialiseOccurrencesPerPair(Collection<String> developers) {
        final Set<Set<Set<String>>> allCombinations = new PairCombinationService(developers).getAllCombinations();

        final Map<Set<String>, Integer> allDeveloperPairs = allCombinations.stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(toMap(pair -> pair, _ -> 0));

        return allDeveloperPairs;
    }

//    // todo extract and more efficient
    private void updateDeveloperToDaysInStream(List<Combination<Pair>> combinations) {
        developerToDaysInStream.forEach((developer, _) -> {
            final Map<String, Integer> developerStreamCounts = developerToDaysInStream.get(developer);
            developerStreamCounts.forEach((stream, _) -> {
                developerStreamCounts.merge(stream, 0, (_, zeroCount) -> zeroCount);
            });
        });

        combinations.stream()
                .map(Combination::pairs)
                .flatMap(Collection::stream)
                .forEach(pair -> {
                    pair.developers().forEach(developer -> {
                        final Map<String, Integer> countPerStream = developerToDaysInStream.get(developer);
                        countPerStream.merge(pair.stream(), 1, Integer::sum);
                    });
                });

    }

    private static Map<String, Map<String, Integer>> initialiseDeveloperToDaysInStream(Collection<String> developers, Collection<String> streams) {
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
}
