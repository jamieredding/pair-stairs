package dev.coldhands.pair.stairs.core;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MaintainStreamKnowledgeTransferRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository;

    public MaintainStreamKnowledgeTransferRule(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(PairStreamCombination pairStreamCombination) {
        return combinationHistoryRepository.getMostRecentCombination()
                .map(mostRecentCombination -> scoreComboBasedOnMostRecent(pairStreamCombination, mostRecentCombination))
                .orElse(new BasicScoreResult(0));
    }

    private ScoreResult scoreComboBasedOnMostRecent(PairStreamCombination toBeScored, PairStreamCombination mostRecentCombination) {
        final Map<String, Set<String>> mostRecentStreamToDevelopers = getStreamToDevelopers(mostRecentCombination);

        int totalScore = toBeScored.pairs().stream()
                .mapToInt(pair -> {
                    final Set<String> newDevelopers = pair.developers();
                    final Set<String> previousDevsInStream = mostRecentStreamToDevelopers.get(pair.stream());

                    return previousDevsInStream.stream().noneMatch(newDevelopers::contains) ? 1 : 0;
                })
                .sum();

        return new BasicScoreResult(totalScore);
    }

    private static Map<String, Set<String>> getStreamToDevelopers(PairStreamCombination pairStreamCombination) {
        return pairStreamCombination.pairs().stream()
                .collect(Collectors.toMap(Pair::stream, Pair::developers));
    }

}
