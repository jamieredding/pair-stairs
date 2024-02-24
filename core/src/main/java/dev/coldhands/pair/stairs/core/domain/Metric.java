package dev.coldhands.pair.stairs.core.domain;

import java.util.List;

public interface Metric<Combination, MetricResult> {

    MetricResult compute(List<ScoredCombination<Combination>> scoredCombinations);
}
