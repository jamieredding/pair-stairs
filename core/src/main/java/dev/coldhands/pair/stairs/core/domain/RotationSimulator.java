package dev.coldhands.pair.stairs.core.domain;

import java.util.List;
import java.util.stream.IntStream;

public interface RotationSimulator<Combination> {

    ScoredCombination<Combination> stepSimulation();

    default List<ScoredCombination<Combination>> runSimulation(int steps) {
        return IntStream.range(0, steps)
                .mapToObj(_ -> stepSimulation())
                .toList();
    }
}
