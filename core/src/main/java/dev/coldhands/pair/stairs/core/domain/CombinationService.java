package dev.coldhands.pair.stairs.core.domain;

import java.util.Set;

public interface CombinationService<Combination> { // todo make it take Pair only

    Set<Combination> getAllCombinations();
}
