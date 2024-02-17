package dev.coldhands.pair.stairs.core;

import java.util.List;

public final class ScoringRulesFactory {

    static List<ScoringRule<PairStreamCombination>> pairStreamScoringRules(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        return List.of(
                new WeightedRule<>(5, new PreventConsecutivePairRepeatsRule(combinationHistoryRepository)),
                new MaintainStreamKnowledgeTransferRule(combinationHistoryRepository),
                new PenaliseEarlyContextSwitchingRule(combinationHistoryRepository)
        );
    }
}
