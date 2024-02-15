package dev.coldhands.pair.stairs.core;

import java.util.List;
import java.util.Set;

public class ScoringEngine<Combination, ScoredCombination extends Comparable<ScoredCombination>> {

    // todo inject scoring rules
    private final ScoringRule<Combination, ScoredCombination> scoringRule;

    public ScoringEngine(ScoringRule<Combination, ScoredCombination> scoringRule) {
        this.scoringRule = scoringRule;
    }

    public List<ScoredCombination> scoreAndSort(Set<Combination> combinations) {
        return combinations.stream()
                .map(scoringRule::score)
                .sorted()
                .toList();
    }
}
