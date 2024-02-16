package dev.coldhands.pair.stairs.core;

import java.util.List;
import java.util.Set;

public class ScoringEngine<Combination> {

    private final List<ScoringRule<Combination>> scoringRules;

    public ScoringEngine(List<ScoringRule<Combination>> scoringRules) {
        this.scoringRules = scoringRules;
    }

    public List<ScoredCombination<Combination>> scoreAndSort(Set<Combination> combinations) {
        return combinations.stream()
                .map(this::scoreCombination)
                .sorted()
                .toList();
    }

    private ScoredCombination<Combination> scoreCombination(Combination combination) {
        final List<ScoreResult> scoreResults = scoringRules.stream()
                .map(rule -> rule.score(combination))
                .toList();

        final int totalScore = scoreResults.stream()
                .mapToInt(ScoreResult::score)
                .sum();

        // todo use new java stream feature

        return new ScoredCombination<>(combination, totalScore, scoreResults);
    }
}
