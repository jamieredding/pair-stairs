package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.WeightedRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.usecases.pairstream.rules.MaintainStreamKnowledgeTransferRule;
import dev.coldhands.pair.stairs.core.usecases.pairstream.rules.PenaliseEarlyContextSwitchingRule;
import dev.coldhands.pair.stairs.core.usecases.pairstream.rules.PreventConsecutivePairRepeatsRule;

import java.util.List;

public final class ScoringRulesFactory {

    static List<ScoringRule<PairStreamCombination>> pairStreamScoringRules(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        return List.of(
                new WeightedRule<>(2, new PreventConsecutivePairRepeatsRule(combinationHistoryRepository)),
                new MaintainStreamKnowledgeTransferRule(combinationHistoryRepository),
                new PenaliseEarlyContextSwitchingRule(combinationHistoryRepository)
        );
    }
}
