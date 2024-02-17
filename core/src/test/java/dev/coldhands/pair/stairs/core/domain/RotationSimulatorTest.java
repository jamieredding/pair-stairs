package dev.coldhands.pair.stairs.core.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RotationSimulatorTest {

    @Test
    void runSimulationCallsStepSimulationXTimes() {
        final var underTest = new TestCombinationRotationSimulator();

        assertThat(underTest.runSimulation(5))
                .containsExactly(
                        new ScoredCombination<>(new TestCombination(0), 0, List.of()),
                        new ScoredCombination<>(new TestCombination(1), 1, List.of()),
                        new ScoredCombination<>(new TestCombination(2), 2, List.of()),
                        new ScoredCombination<>(new TestCombination(3), 3, List.of()),
                        new ScoredCombination<>(new TestCombination(4), 4, List.of())
                );
    }

    private record TestCombination(int value) {

    }

    private static class TestCombinationRotationSimulator implements RotationSimulator<TestCombination> {
        private int count = 0;

        @Override
        public ScoredCombination<TestCombination> stepSimulation() {
            int value = count++;
            return new ScoredCombination<>(new TestCombination(value), value, List.of());
        }
    }
}