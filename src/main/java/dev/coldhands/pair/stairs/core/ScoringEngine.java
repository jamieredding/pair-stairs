package dev.coldhands.pair.stairs.core;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;

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
        record ScoreResults(List<ScoreResult> individualResults, int totalScore) {
        }

        final ScoreResults scoreResults = scoringRules.stream()
                .map(rule -> rule.score(combination))
                .collect(teeing(
                        toList(),
                        summingInt(ScoreResult::score),
                        ScoreResults::new
                ));

        return new ScoredCombination<>(combination, scoreResults.totalScore, scoreResults.individualResults);
    }
}
