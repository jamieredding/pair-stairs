package dev.coldhands.pair.stairs.core.domain;

import java.util.List;
import java.util.stream.IntStream;

public interface RotationSimulator<T> {

    ScoredCombination<Combination<T>> stepSimulation();

    default List<ScoredCombination<Combination<T>>> runSimulation(int steps) {
        return IntStream.range(0, steps)
                .mapToObj(_ -> stepSimulation())
                .toList();
    }
}
