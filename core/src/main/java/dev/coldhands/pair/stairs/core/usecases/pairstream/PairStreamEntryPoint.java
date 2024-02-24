package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.*;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.usecases.ScoringEngine;

import java.util.Collection;
import java.util.List;

public class PairStreamEntryPoint implements EntryPoint<Combination<Pair>> { // todo make this take pair only not combination

    private final CombinationService<Combination<Pair>> combinationService;
    private final ScoringEngine<Combination<Pair>> scoringEngine;

    public PairStreamEntryPoint(Collection<String> developers,
                                Collection<String> streams,
                                CombinationHistoryRepository<Pair> combinationHistoryRepository,
                                PairStreamStatisticsService statisticsService) {
        combinationService = new PairStreamCombinationService(developers, streams);
        scoringEngine = new ScoringEngine<>(ScoringRulesFactory.pairStreamScoringRules(combinationHistoryRepository, statisticsService));
    }

    @Override
    public List<ScoredCombination<Combination<Pair>>> computeScoredCombinations() {
        final var allCombinations = combinationService.getAllCombinations();

        return scoringEngine.scoreAndSort(allCombinations);
    }
}
