package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.BasicScoreResult;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PreventConsecutiveDeveloperCombinationRepeatsRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository;

    public PreventConsecutiveDeveloperCombinationRepeatsRule(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(PairStreamCombination pairStreamCombination) {
        return combinationHistoryRepository.getMostRecentCombination()
                .map(mostRecentCombination -> scoreComboBasedOnMostRecent(pairStreamCombination, mostRecentCombination))
                .orElse(new BasicScoreResult(0));
    }

    private ScoreResult scoreComboBasedOnMostRecent(PairStreamCombination toBeScored, PairStreamCombination mostRecentCombination) {
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

    private static Set<Set<String>> getDevelopersInPairs(PairStreamCombination combination) {
        return combination.pairs().stream()
                .map(Pair::developers)
                .collect(toSet());
    }
}
