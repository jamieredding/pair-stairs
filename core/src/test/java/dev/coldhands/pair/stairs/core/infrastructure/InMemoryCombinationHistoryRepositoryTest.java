package dev.coldhands.pair.stairs.core.infrastructure;

import dev.coldhands.pair.stairs.core.domain.Combination;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCombinationHistoryRepositoryTest {

    private final InMemoryCombinationHistoryRepository<Integer> underTest = new InMemoryCombinationHistoryRepository<>();

    @Test
    void saveCombination() {
        underTest.saveCombination(new Combination<>(Set.of(1)), LocalDate.of(2024, 2, 16));

        assertThat(underTest.getMostRecentCombination())
                .contains(new Combination<>(Set.of(1)));
    }

    @Test
    void mostRecentCombinationConsidersDate() {
        underTest.saveCombination(new Combination<>(Set.of(1)), LocalDate.of(2024, 2, 16));
        underTest.saveCombination(new Combination<>(Set.of(2)), LocalDate.of(2024, 2, 29));
        underTest.saveCombination(new Combination<>(Set.of(3)), LocalDate.of(2024, 2, 1));

        assertThat(underTest.getMostRecentCombination())
                .contains(new Combination<>(Set.of(2)));
    }

    @Test
    void mostRecentCombinationWhenNoPreviousCombinations() {
        assertThat(underTest.getMostRecentCombination())
                .isEmpty();
    }

    @Test
    void getMostRecentCombinationsWhenNothingSaved() {
        assertThat(underTest.getMostRecentCombinations(1))
                .isEmpty();
    }

    @Test
    void getMostRecentCombinationsIsUnmodifiable() {
        assertThat(underTest.getMostRecentCombinations(1))
                .isUnmodifiable();
    }

    @Test
    void getMostRecentCombinationsContainsAllCombinationsInReverseChronologicalOrder() {
        underTest.saveCombination(new Combination<>(Set.of(1)), LocalDate.of(2024, 2, 29));
        underTest.saveCombination(new Combination<>(Set.of(2)), LocalDate.of(2024, 2, 1));

        assertThat(underTest.getMostRecentCombinations(2))
                .containsExactly(
                        new Combination<>(Set.of(1)),
                        new Combination<>(Set.of(2))
                );
    }

    @Test
    void getMostRecentCombinationsOnlyIncludeCountNumberOfResults() {
        underTest.saveCombination(new Combination<>(Set.of(1)), LocalDate.of(2024, 2, 29));
        underTest.saveCombination(new Combination<>(Set.of(2)), LocalDate.of(2024, 2, 1));

        assertThat(underTest.getMostRecentCombinations(1))
                .containsExactly(
                        new Combination<>(Set.of(1))
                );
    }
}