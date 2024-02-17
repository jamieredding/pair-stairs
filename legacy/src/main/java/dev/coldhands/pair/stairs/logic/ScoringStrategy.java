package dev.coldhands.pair.stairs.logic;

import dev.coldhands.pair.stairs.domain.PairCombination;
import dev.coldhands.pair.stairs.domain.ScoredPairCombination;

public interface ScoringStrategy {

    ScoredPairCombination score(PairCombination pairCombination);
}
