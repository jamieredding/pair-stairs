package dev.coldhands.pair.stairs.core;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StreamContextIsMaintainedRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository;

    public StreamContextIsMaintainedRule(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(PairStreamCombination pairStreamCombination) {
        return combinationHistoryRepository.getMostRecentCombination()
                .map(mostRecentCombination -> scoreComboBasedOnMostRecent(pairStreamCombination, mostRecentCombination))
                .orElse(new BasicScoreResult(0));
    }

    private ScoreResult scoreComboBasedOnMostRecent(PairStreamCombination toBeScored, PairStreamCombination mostRecentCombination) {
        final var streamToDevelopers = getStreamToDevelopers(toBeScored);
        final var mostRecentStreamToDevelopers = getStreamToDevelopers(mostRecentCombination);

        int totalScore = 0;

        for (Map.Entry<String, Set<String>> entry : streamToDevelopers.entrySet()) { // todo make this functional
            final String stream = entry.getKey();
            final Set<String> newDevelopers = entry.getValue();
            final var previousDevsInStream = mostRecentStreamToDevelopers.get(stream);

            if (previousDevsInStream.stream().noneMatch(newDevelopers::contains)) {
                totalScore += 1;
            }
        }

        return new BasicScoreResult(totalScore);
    }

    private static Map<String, Set<String>> getStreamToDevelopers(PairStreamCombination pairStreamCombination) {
        return pairStreamCombination.pairs().stream()
                .collect(Collectors.toMap(Pair::stream, Pair::developers));
    }

}
