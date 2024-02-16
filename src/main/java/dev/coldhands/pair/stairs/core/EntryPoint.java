package dev.coldhands.pair.stairs.core;

import java.util.List;

public interface EntryPoint<Combination> {

    List<ScoredCombination<Combination>> computeScoredCombinations();
}
