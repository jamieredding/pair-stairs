package dev.coldhands.pair.stairs.core.domain;

import java.util.List;

public interface Metric<T, MetricResult> {

    MetricResult compute(List<ScoredCombination<T>> scoredCombinations);
}
