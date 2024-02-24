package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.*;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PreventConsecutiveDeveloperCombinationRepeatsRule implements ScoringRule<Pair> {

    private final CombinationHistoryRepository<Pair> combinationHistoryRepository;

    public PreventConsecutiveDeveloperCombinationRepeatsRule(CombinationHistoryRepository<Pair> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(Combination<Pair> combination) {
        return combinationHistoryRepository.getMostRecentCombination()
                .map(mostRecentCombination -> scoreComboBasedOnMostRecent(combination, mostRecentCombination))
                .orElse(new BasicScoreResult(0));
    }

    private ScoreResult scoreComboBasedOnMostRecent(Combination<Pair> toBeScored, Combination<Pair> mostRecentCombination) {
        final var developersInPairs = getDevelopersInPairs(toBeScored);
        final var mostRecentDevelopersInPairs = getDevelopersInPairs(mostRecentCombination);

        /*
        // This increases score for every pair that is similar to yesterday
        // This is unsuitable due to causing some pairs to never happen when applied with other rules

        final Sets.SetView<Set<String>> pairsThatDidNotChange = Sets.intersection(mostRecentDevelopersInPairs, developersInPairs);

        int score = pairsThatDidNotChange.size();
         */

        int score = developersInPairs.equals(mostRecentDevelopersInPairs)
                ? 1
                : 0;

        return new BasicScoreResult(score);
    }

    private static Set<Set<String>> getDevelopersInPairs(Combination<Pair> combination) {
        return combination.pairs().stream()
                .map(Pair::developers)
                .collect(toSet());
    }
}
