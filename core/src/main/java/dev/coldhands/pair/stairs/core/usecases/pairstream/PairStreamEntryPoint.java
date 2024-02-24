package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.core.domain.EntryPoint;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.usecases.ScoringEngine;

import java.util.Collection;
import java.util.List;

public class PairStreamEntryPoint implements EntryPoint<PairStream> {

    private final CombinationService<PairStream> combinationService;
    private final ScoringEngine<PairStream> scoringEngine;

    public PairStreamEntryPoint(Collection<String> developers,
                                Collection<String> streams,
                                CombinationHistoryRepository<PairStream> combinationHistoryRepository,
                                PairStreamStatisticsService statisticsService) {
        combinationService = new PairStreamCombinationService(developers, streams);
        scoringEngine = new ScoringEngine<>(ScoringRulesFactory.pairStreamScoringRules(combinationHistoryRepository, statisticsService));
    }

    @Override
    public List<ScoredCombination<PairStream>> computeScoredCombinations() {
        final var allCombinations = combinationService.getAllCombinations();

        return scoringEngine.scoreAndSort(allCombinations);
    }
}
