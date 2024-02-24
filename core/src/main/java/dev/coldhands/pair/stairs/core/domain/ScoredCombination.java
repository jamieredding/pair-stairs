package dev.coldhands.pair.stairs.core.domain;

import java.util.List;

// todo stop parameterising combination and use T instead
public record ScoredCombination<Combination>(Combination combination, int totalScore, List<ScoreResult> scoreResults) implements Comparable<ScoredCombination<?>> {

    @Override
    public int compareTo(ScoredCombination<?> o) {
        return Integer.compare(this.totalScore, o.totalScore);
    }
}
