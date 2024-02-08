package dev.coldhands.pair.stairs.logic;

import dev.coldhands.pair.stairs.domain.ScoredPairCombination;

import java.util.List;

import static java.util.Comparator.comparing;

public class EntryPoint {

    private final PairCombinationService pairCombinationService;
    private final ScoringStrategy scoringStrategy;

    public EntryPoint(PairCombinationService pairCombinationService, ScoringStrategy scoringStrategy) {
        this.pairCombinationService = pairCombinationService;
        this.scoringStrategy = scoringStrategy;
    }

    public List<ScoredPairCombination> computeScoredPairCombinations() {
        final var allPairCombinations = pairCombinationService.getAllPairCombinations();

        return allPairCombinations.stream()
                .map(scoringStrategy::score)
                .sorted(comparing(ScoredPairCombination::score))
                .toList();

    }
}
