package dev.coldhands.pair.stairs.core.domain;

import java.util.Set;

public interface CombinationService<Combination> {

    Set<Combination> getAllCombinations();
}