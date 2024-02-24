package dev.coldhands.pair.stairs.core.domain;

import java.util.List;

public interface Metric<Combination, MetricResult> { // todo make this take pair

    MetricResult compute(List<ScoredCombination<Combination>> scoredCombinations);
}
