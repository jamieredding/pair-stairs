package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.usecases.pair.PairCombinationService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class PairStreamStatisticsService {

    private final Map<Set<String>, Integer> recentPairOccurrences;
    private final Map<String, Map<String, Integer>> developerToDaysInStream;
    private final CombinationHistoryRepository<PairStream> repository;
    private final int numberOfPreviousCombinationsToConsider;

    public PairStreamStatisticsService(CombinationHistoryRepository<PairStream> repository, Collection<String> developers, Collection<String> streams, int numberOfPreviousCombinationsToConsider) {
        this.repository = repository;
        this.recentPairOccurrences = initialiseOccurrencesPerPair(developers);
        this.developerToDaysInStream = initialiseDeveloperToDaysInStream(developers, streams);
        this.numberOfPreviousCombinationsToConsider = numberOfPreviousCombinationsToConsider;
        updateStatistics();
    }

    public void updateStatistics() {
        final List<Combination<PairStream>> combinationsToConsider = repository.getMostRecentCombinations(numberOfPreviousCombinationsToConsider);

        updateOccurrencesPerPair(combinationsToConsider);
        updateDeveloperToDaysInStream(combinationsToConsider);
    }

    public int getRecentOccurrencesOfDeveloperPair(Set<String> developers) {
        return recentPairOccurrences.get(developers);
    }

    public int getRecentOccurrenceOfDeveloperInStream(String developer, String stream) {
        return ofNullable(developerToDaysInStream.get(developer))
                .map(countPerStream -> countPerStream.get(stream))
                .orElse(0);
    }

    private void updateOccurrencesPerPair(List<Combination<PairStream>> combinations) {
        recentPairOccurrences.forEach((pair, _) -> {
            recentPairOccurrences.merge(pair, 0, (_, zeroCount) -> zeroCount);
        });


        combinations.stream()
                .map(Combination::pairs)
                .flatMap(Collection::stream)
                .map(PairStream::developers)
                .forEach(developers ->
                        recentPairOccurrences.merge(developers, 1, Integer::sum));
    }

    private static Map<Set<String>, Integer> initialiseOccurrencesPerPair(Collection<String> developers) {
        final Set<Combination<Set<String>>> allCombinations = new PairCombinationService(developers).getAllCombinations();

        final Map<Set<String>, Integer> allDeveloperPairs = allCombinations.stream()
                .map(Combination::pairs)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toMap(pair -> pair, _ -> 0));

        return allDeveloperPairs;
    }

    // todo extract and more efficient
    private void updateDeveloperToDaysInStream(List<Combination<PairStream>> combinations) {
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
                        ofNullable(developerToDaysInStream.get(developer))
                                .ifPresent(countPerStream ->
                                        countPerStream.merge(pair.stream(), 1, Integer::sum)
                                );
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
