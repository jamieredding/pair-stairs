package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import com.google.common.collect.Sets;
import dev.coldhands.pair.stairs.core.domain.*;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PenaliseRepeatingDeveloperPairsRule implements ScoringRule<PairStream> {

    private final CombinationHistoryRepository<PairStream> repository;

    public PenaliseRepeatingDeveloperPairsRule(CombinationHistoryRepository<PairStream> repository) {
        this.repository = repository;
    }

    @Override
    public ScoreResult score(Combination<PairStream> combination) {
        return repository.getMostRecentCombination()
                .map(mostRecentCombination -> scoreComboBasedOnMostRecent(combination, mostRecentCombination))
                .orElse(new BasicScoreResult(0));
    }

    private ScoreResult scoreComboBasedOnMostRecent(Combination<PairStream> toBeScored, Combination<PairStream> mostRecentCombination) {
        final var developersInPairs = getDevelopersInPairs(toBeScored);
        final var mostRecentDevelopersInPairs = getDevelopersInPairs(mostRecentCombination);

        final Sets.SetView<Set<String>> pairsThatDidNotChange = Sets.intersection(mostRecentDevelopersInPairs, developersInPairs);

        int score = pairsThatDidNotChange.size();

        return new BasicScoreResult(score);
    }

    private static Set<Set<String>> getDevelopersInPairs(Combination<PairStream> combination) {
        return combination.pairs().stream()
                .map(PairStream::developers)
                .collect(toSet());
    }
}
