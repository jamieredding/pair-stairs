package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import com.google.common.collect.Sets;
import dev.coldhands.pair.stairs.core.domain.*;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PenaliseRepeatingDeveloperPairsRule implements ScoringRule<Combination<Pair>> {

    private final CombinationHistoryRepository<Pair> repository;

    public PenaliseRepeatingDeveloperPairsRule(CombinationHistoryRepository<Pair> repository) {
        this.repository = repository;
    }

    @Override
    public ScoreResult score(Combination<Pair> combination) {
        return repository.getMostRecentCombination()
                .map(mostRecentCombination -> scoreComboBasedOnMostRecent(combination, mostRecentCombination))
                .orElse(new BasicScoreResult(0));
    }

    private ScoreResult scoreComboBasedOnMostRecent(Combination<Pair> toBeScored, Combination<Pair> mostRecentCombination) {
        final var developersInPairs = getDevelopersInPairs(toBeScored);
        final var mostRecentDevelopersInPairs = getDevelopersInPairs(mostRecentCombination);

        final Sets.SetView<Set<String>> pairsThatDidNotChange = Sets.intersection(mostRecentDevelopersInPairs, developersInPairs);

        int score = pairsThatDidNotChange.size();

        return new BasicScoreResult(score);
    }

    private static Set<Set<String>> getDevelopersInPairs(Combination<Pair> combination) {
        return combination.pairs().stream()
                .map(Pair::developers)
                .collect(toSet());
    }
}
