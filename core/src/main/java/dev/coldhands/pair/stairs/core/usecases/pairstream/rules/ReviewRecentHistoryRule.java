package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.BasicScoreResult;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamCombinationService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class ReviewRecentHistoryRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> repository;
    private final Collection<String> developers;
    private final Collection<String> streams;

    public ReviewRecentHistoryRule(CombinationHistoryRepository<PairStreamCombination> repository, Collection<String> developers, Collection<String> streams) {
        this.repository = repository;
        this.developers = developers;
        this.streams = streams;
    }

    @Override
    public ScoreResult score(PairStreamCombination combination) {
        // todo extract this somewhere
        final List<PairStreamCombination> recentCombinations = repository.getAllCombinations()
                .reversed() // todo test this
                .stream()
                .limit(5) // todo configure this
                .toList();

        final Map<Set<String>, Integer> recentPairOccurrences = computeOccurrencesPerPair(recentCombinations);
        final Map<String, Map<String, Integer>> developerToDaysInStream = computeDeveloperToDaysInStream(recentCombinations);

        int score = Math.toIntExact(combination.pairs().stream()
                .filter(pair -> recentPairOccurrences.get(pair.developers()) == 0 ||
                        pair.developers().stream().anyMatch(dev -> developerToDaysInStream.get(dev).get(pair.stream()) == 0))
                .count());

        return new BasicScoreResult(- score);
    }

    private Map<Set<String>, Integer> computeOccurrencesPerPair(List<PairStreamCombination> combinations) {
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


        combinations.stream()
                .map(PairStreamCombination::pairs)
                .flatMap(Collection::stream)
                .map(Pair::developers)
                .forEach(developers ->
                        allDeveloperPairs.merge(developers, 1, Integer::sum));

        return allDeveloperPairs;
    }

    // todo extract and more efficient
    private Map<String, Map<String, Integer>> computeDeveloperToDaysInStream(List<PairStreamCombination> combinations) {
        Map<String, Map<String, Integer>> developerToDaysInStream = initialiseMap();

        combinations.stream()
                .map(PairStreamCombination::pairs)
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
}
