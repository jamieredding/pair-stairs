package dev.coldhands.pair.stairs.core.usecases.pair;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class PairCombinationServiceTest {

    private CombinationService<Set<String>> underTest;

    private void given(Collection<String> developers) {
        underTest = new PairCombinationService(developers);
    }

    @Test
    void calculateAllCombinationsForOddNumberOfDevelopers() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev");

        given(developers);

        Set<Combination<Set<String>>> allCombinations = underTest.getAllCombinations(); // todo make better types!

        assertThat(allCombinations)
                .containsOnly(
                        new Combination<>(Set.of(Set.of("a-dev", "b-dev"), Set.of("c-dev"))),
                        new Combination<>(Set.of(Set.of("a-dev", "c-dev"), Set.of("b-dev"))),
                        new Combination<>(Set.of(Set.of("b-dev", "c-dev"), Set.of("a-dev")))
                );
    }

    @Test
    void calculateAllCombinationsForEvenNumberOfDevelopers() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev");

        given(developers);

        Set<Combination<Set<String>>> allCombinations = underTest.getAllCombinations(); // todo make better types!

        assertThat(allCombinations)
                .containsOnly(
                        new Combination<>(Set.of(Set.of("a-dev", "b-dev"), Set.of("c-dev", "d-dev"))),
                        new Combination<>(Set.of(Set.of("a-dev", "c-dev"), Set.of("b-dev", "d-dev"))),
                        new Combination<>(Set.of(Set.of("b-dev", "c-dev"), Set.of("a-dev", "d-dev")))
                );
    }

    @Test
    void calculateAllCombinationsWhereThereShouldBeThreePairsInEachCombination() {
        List<String> developers = List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev");

        given(developers);

        Set<Combination<Set<String>>> allCombinations = underTest.getAllCombinations();

        assertThat(allCombinations)
                .hasSize(15)
                .allSatisfy(combination -> {
                    assertThat(combination.pairs()).hasSize(3);

                    final Set<String> allDevelopers = combination.pairs().stream()
                            .flatMap(Collection::stream)
                            .collect(toSet());

                    assertThat(allDevelopers)
                            .containsExactlyInAnyOrderElementsOf(developers);

                });
    }

}