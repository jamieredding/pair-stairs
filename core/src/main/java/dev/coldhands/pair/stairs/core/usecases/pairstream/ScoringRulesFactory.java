package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.WeightedRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.usecases.pairstream.rules.MaintainStreamKnowledgeTransferRule;
import dev.coldhands.pair.stairs.core.usecases.pairstream.rules.PenaliseEarlyContextSwitchingRule;
import dev.coldhands.pair.stairs.core.usecases.pairstream.rules.PreferPairsOrStreamsThatHaveNotHappenedRecentlyRule;
import dev.coldhands.pair.stairs.core.usecases.pairstream.rules.PreventConsecutiveDeveloperCombinationRepeatsRule;

import java.util.List;

public final class ScoringRulesFactory {

    static List<ScoringRule<PairStream>> pairStreamScoringRules(CombinationHistoryRepository<PairStream> combinationHistoryRepository, PairStreamStatisticsService statisticsService) {
        return List.of(
                new WeightedRule<>(200, new PreventConsecutiveDeveloperCombinationRepeatsRule(combinationHistoryRepository)),
                new WeightedRule<>(100, new MaintainStreamKnowledgeTransferRule(combinationHistoryRepository)),
                new WeightedRule<>(100, new PenaliseEarlyContextSwitchingRule(combinationHistoryRepository)),
                new PreferPairsOrStreamsThatHaveNotHappenedRecentlyRule(statisticsService)
        );
    }
}
