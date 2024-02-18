package dev.coldhands.pair.stairs.core.domain;

import java.util.List;

public interface Metric<Combination, MetricResult> {

    MetricResult compute(List<ScoredCombination<Combination>> scoredCombinations); // todo should this just be for a single combination and aggregate at higher level?
}
