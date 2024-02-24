package dev.coldhands.pair.stairs.core.usecases;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;

public class ScoringEngine<T> {

    private final List<ScoringRule<T>> scoringRules;

    public ScoringEngine(List<ScoringRule<T>> scoringRules) {
        this.scoringRules = scoringRules;
    }

    public List<ScoredCombination<T>> scoreAndSort(Set<Combination<T>> combinations) {
        return combinations.stream()
                .map(this::scoreCombination)
                .sorted()
                .toList();
    }

    private ScoredCombination<T> scoreCombination(Combination<T> combination) {
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
