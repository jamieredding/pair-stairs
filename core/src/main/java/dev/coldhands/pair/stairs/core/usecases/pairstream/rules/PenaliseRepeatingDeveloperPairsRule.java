package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import com.google.common.collect.Sets;
import dev.coldhands.pair.stairs.core.domain.BasicScoreResult;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PenaliseRepeatingDeveloperPairsRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> repository;

    public PenaliseRepeatingDeveloperPairsRule(CombinationHistoryRepository<PairStreamCombination> repository) {
        this.repository = repository;
    }

    @Override
    public ScoreResult score(PairStreamCombination pairStreamCombination) {
        return repository.getMostRecentCombination()
                .map(mostRecentCombination -> scoreComboBasedOnMostRecent(pairStreamCombination, mostRecentCombination))
                .orElse(new BasicScoreResult(0));
    }

    private ScoreResult scoreComboBasedOnMostRecent(PairStreamCombination toBeScored, PairStreamCombination mostRecentCombination) {
        final var developersInPairs = getDevelopersInPairs(toBeScored);
        final var mostRecentDevelopersInPairs = getDevelopersInPairs(mostRecentCombination);

        final Sets.SetView<Set<String>> pairsThatDidNotChange = Sets.intersection(mostRecentDevelopersInPairs, developersInPairs);

        int score = pairsThatDidNotChange.size();

        return new BasicScoreResult(score);
    }

    private static Set<Set<String>> getDevelopersInPairs(PairStreamCombination combination) {
        return combination.pairs().stream()
                .map(Pair::developers)
                .collect(toSet());
    }
}
