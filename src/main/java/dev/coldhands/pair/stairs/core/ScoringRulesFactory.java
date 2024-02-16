package dev.coldhands.pair.stairs.core;

import java.util.List;

public final class ScoringRulesFactory {

    static List<ScoringRule<PairStreamCombination>> pairStreamScoringRules(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        return List.of(
                new PreventConsecutivePairRepeatsRule(combinationHistoryRepository),
                new MaintainStreamKnowledgeTransferRule(combinationHistoryRepository)
        );
    }
}
