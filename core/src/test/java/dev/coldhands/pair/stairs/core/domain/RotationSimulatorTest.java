package dev.coldhands.pair.stairs.core.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RotationSimulatorTest {

    @Test
    void runSimulationCallsStepSimulationXTimes() {
        final var underTest = new TestCombinationRotationSimulator();

        assertThat(underTest.runSimulation(5))
                .containsExactly(
                        new ScoredCombination<>(new Combination<>(Set.of(0)), 0, List.of()),
                        new ScoredCombination<>(new Combination<>(Set.of(1)), 1, List.of()),
                        new ScoredCombination<>(new Combination<>(Set.of(2)), 2, List.of()),
                        new ScoredCombination<>(new Combination<>(Set.of(3)), 3, List.of()),
                        new ScoredCombination<>(new Combination<>(Set.of(4)), 4, List.of())
                );
    }

    private static class TestCombinationRotationSimulator implements RotationSimulator<Integer> {
        private int count = 0;

        @Override
        public ScoredCombination<Integer> stepSimulation() {
            int value = count++;
            return new ScoredCombination<>(new Combination<>(Set.of(value)), value, List.of());
        }
    }
}