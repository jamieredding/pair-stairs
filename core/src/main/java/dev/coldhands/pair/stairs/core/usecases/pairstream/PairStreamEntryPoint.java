package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.core.domain.EntryPoint;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.usecases.ScoringEngine;

import java.util.Collection;
import java.util.List;

public class PairStreamEntryPoint implements EntryPoint<Pair> {

    private final CombinationService<Pair> combinationService;
    private final ScoringEngine<Pair> scoringEngine;

    public PairStreamEntryPoint(Collection<String> developers,
                                Collection<String> streams,
                                CombinationHistoryRepository<Pair> combinationHistoryRepository,
                                PairStreamStatisticsService statisticsService) {
        combinationService = new PairStreamCombinationService(developers, streams);
        scoringEngine = new ScoringEngine<>(ScoringRulesFactory.pairStreamScoringRules(combinationHistoryRepository, statisticsService));
    }

    @Override
    public List<ScoredCombination<Pair>> computeScoredCombinations() {
        final var allCombinations = combinationService.getAllCombinations();

        return scoringEngine.scoreAndSort(allCombinations);
    }
}
