package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.core.domain.EntryPoint;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.usecases.ScoringEngine;

import java.util.Collection;
import java.util.List;

public class PairStreamEntryPoint implements EntryPoint<PairStreamCombination> {

    private final CombinationService<PairStreamCombination> combinationService;
    private final ScoringEngine<PairStreamCombination> scoringEngine;

    public PairStreamEntryPoint(Collection<String> developers,
                                Collection<String> streams,
                                CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        combinationService = new PairStreamCombinationService(developers, streams);
        scoringEngine = new ScoringEngine<>(ScoringRulesFactory.pairStreamScoringRules(combinationHistoryRepository));
    }

    @Override
    public List<ScoredCombination<PairStreamCombination>> computeScoredCombinations() {
        final var allCombinations = combinationService.getAllCombinations();

        return scoringEngine.scoreAndSort(allCombinations);
    }
}
