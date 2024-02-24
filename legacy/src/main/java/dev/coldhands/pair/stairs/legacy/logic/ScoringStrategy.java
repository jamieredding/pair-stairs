package dev.coldhands.pair.stairs.legacy.logic;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.legacy.domain.Pair;
import dev.coldhands.pair.stairs.legacy.domain.ScoredPairCombination;

public interface ScoringStrategy {

    ScoredPairCombination score(Combination<Pair> pairCombination);
}
