package dev.coldhands.pair.stairs.core;

import java.util.Collection;
import java.util.List;

public class PairStreamEntryPoint implements EntryPoint<PairStreamCombination> {

    private final CombinationService<PairStreamCombination> combinationService;
    private final ScoringEngine<PairStreamCombination> scoringEngine;

    public PairStreamEntryPoint(Collection<String> developers, Collection<String> streams) {
        combinationService = new StreamsCombinationService(developers, streams);
        scoringEngine = new ScoringEngine<>(ScoringRulesFactory.pairStreamScoringRules());
    }

    @Override
    public List<ScoredCombination<PairStreamCombination>> computeScoredCombinations() {
        final var allCombinations = combinationService.getAllCombinations();

        return scoringEngine.scoreAndSort(allCombinations);
    }
}
