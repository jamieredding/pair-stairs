package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.*;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MaintainStreamKnowledgeTransferRule implements ScoringRule<PairStream> {

    private final CombinationHistoryRepository<PairStream> combinationHistoryRepository;

    public MaintainStreamKnowledgeTransferRule(CombinationHistoryRepository<PairStream> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(Combination<PairStream> combination) {
        return combinationHistoryRepository.getMostRecentCombination()
                .map(mostRecentCombination -> scoreComboBasedOnMostRecent(combination, mostRecentCombination))
                .orElse(new BasicScoreResult(0));
    }

    private ScoreResult scoreComboBasedOnMostRecent(Combination<PairStream> toBeScored, Combination<PairStream> mostRecentCombination) {
        final Map<String, Set<String>> mostRecentStreamToDevelopers = getStreamToDevelopers(mostRecentCombination);

        int totalScore = toBeScored.pairs().stream()
                .mapToInt(pair -> {
                    final Set<String> newDevelopers = pair.developers();
                    final Set<String> previousDevsInStream = mostRecentStreamToDevelopers.getOrDefault(pair.stream(), Set.of());

                    return previousDevsInStream.stream().noneMatch(newDevelopers::contains) ? 1 : 0;
                })
                .sum();

        return new BasicScoreResult(totalScore);
    }

    private static Map<String, Set<String>> getStreamToDevelopers(Combination<PairStream> combination) {
        return combination.pairs().stream()
                .collect(Collectors.toMap(PairStream::stream, PairStream::developers));
    }

}
