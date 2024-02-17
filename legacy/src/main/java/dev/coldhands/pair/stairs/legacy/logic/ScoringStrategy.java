package dev.coldhands.pair.stairs.legacy.logic;

import dev.coldhands.pair.stairs.legacy.domain.PairCombination;
import dev.coldhands.pair.stairs.legacy.domain.ScoredPairCombination;

public interface ScoringStrategy {

    ScoredPairCombination score(PairCombination pairCombination);
}
