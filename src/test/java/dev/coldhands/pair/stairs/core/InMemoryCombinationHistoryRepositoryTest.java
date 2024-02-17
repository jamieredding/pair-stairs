package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCombinationHistoryRepositoryTest {

    private final InMemoryCombinationHistoryRepository<TestCombination> underTest = new InMemoryCombinationHistoryRepository<TestCombination>();

    @Test
    void saveCombination() {
        underTest.saveCombination(new TestCombination(1), LocalDate.of(2024, 2, 16));

        assertThat(underTest.getMostRecentCombination())
                .contains(new TestCombination(1));
    }

    @Test
    void mostRecentCombinationConsidersDate() {
        underTest.saveCombination(new TestCombination(1), LocalDate.of(2024, 2, 16));
        underTest.saveCombination(new TestCombination(2), LocalDate.of(2024, 2, 29));
        underTest.saveCombination(new TestCombination(3), LocalDate.of(2024, 2, 1));

        assertThat(underTest.getMostRecentCombination())
                .contains(new TestCombination(2));
    }

    @Test
    void mostRecentCombinationWhenNoPreviousCombinations() {
        assertThat(underTest.getMostRecentCombination())
                .isEmpty();
    }

    @Test
    void getAllEventsWhenNoEvents() {
        assertThat(underTest.getAllEvents())
                .isEmpty();
    }

    @Test
    void getAllEventsIsUnmodifiable() {
        assertThat(underTest.getAllEvents())
                .isUnmodifiable();
    }

    @Test
    void getAllEventsContainsAllEventsInChronologicalOrder() {
        underTest.saveCombination(new TestCombination(1), LocalDate.of(2024, 2, 29));
        underTest.saveCombination(new TestCombination(1), LocalDate.of(2024, 2, 1));

        assertThat(underTest.getAllEvents())
                .containsExactly(
                        new CombinationEvent<>(new TestCombination(1), LocalDate.of(2024, 2, 1)),
                        new CombinationEvent<>(new TestCombination(1), LocalDate.of(2024, 2, 29))
                );
    }

    private record TestCombination(int value) {

    }
}