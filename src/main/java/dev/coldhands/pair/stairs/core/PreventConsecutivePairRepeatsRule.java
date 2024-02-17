package dev.coldhands.pair.stairs.core;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PreventConsecutivePairRepeatsRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository;

    public PreventConsecutivePairRepeatsRule(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
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
