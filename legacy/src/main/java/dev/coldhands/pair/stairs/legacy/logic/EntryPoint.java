package dev.coldhands.pair.stairs.legacy.logic;

import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.legacy.domain.PairCombination;
import dev.coldhands.pair.stairs.legacy.domain.ScoredPairCombination;

import java.util.List;

import static java.util.Comparator.comparing;

public class EntryPoint {

    private final CombinationService<PairCombination> combinationService;
    private final ScoringStrategy scoringStrategy;

    public EntryPoint(CombinationService<PairCombination> combinationService, ScoringStrategy scoringStrategy) {
        this.combinationService = combinationService;
        this.scoringStrategy = scoringStrategy;
    }

    public List<ScoredPairCombination> computeScoredPairCombinations() {
        final var allPairCombinations = combinationService.getAllCombinations();

        return allPairCombinations.stream()
                .map(scoringStrategy::score)
                .sorted(comparing(ScoredPairCombination::score))
                .toList();

    }
}
