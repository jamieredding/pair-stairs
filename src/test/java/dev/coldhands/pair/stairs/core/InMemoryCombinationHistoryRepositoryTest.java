package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCombinationHistoryRepositoryTest {

    @Test
    void saveCombination() {
        final var underTest = new InMemoryCombinationHistoryRepository<TestCombination>();

        underTest.saveCombination(new TestCombination(1), LocalDate.of(2024, 2, 16));

        assertThat(underTest.getMostRecentCombination())
                .contains(new TestCombination(1));
    }

    @Test
    void mostRecentCombinationConsidersDate() {
        final var underTest = new InMemoryCombinationHistoryRepository<TestCombination>();

        underTest.saveCombination(new TestCombination(1), LocalDate.of(2024, 2, 16));
        underTest.saveCombination(new TestCombination(2), LocalDate.of(2024, 2, 29));
        underTest.saveCombination(new TestCombination(3), LocalDate.of(2024, 2, 1));

        assertThat(underTest.getMostRecentCombination())
                .contains(new TestCombination(2));
    }

    @Test
    void mostRecentCombinationWhenNoPreviousCombinations() {
        final var underTest = new InMemoryCombinationHistoryRepository<TestCombination>();

        assertThat(underTest.getMostRecentCombination())
                .isEmpty();
    }

    private record TestCombination(int value) {

    }
}