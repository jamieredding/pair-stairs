package dev.coldhands.pair.stairs.legacy.logic.legacy;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.legacy.domain.Pair;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyCombinationServiceTest {

    private CombinationService<Pair> underTest;

    private void givenDevelopers(String... developers) {
        underTest = new LegacyCombinationService(Arrays.asList(developers));
    }

    @Test
    void calculateAllPairCombinations() {
        givenDevelopers("c-dev", "d-dev", "e-dev");

        Set<Combination<Pair>> allPairCombinations = underTest.getAllCombinations();


        assertThat(allPairCombinations)
                .containsOnly(
                        new Combination<>(Set.of(new Pair("c-dev", "d-dev"), new Pair("e-dev"))),
                        new Combination<>(Set.of(new Pair("d-dev", "e-dev"), new Pair("c-dev"))),
                        new Combination<>(Set.of(new Pair("c-dev", "e-dev"), new Pair("d-dev"))));
    }

    @Test
    void calculateAllPairCombinationsOfAnEvenNumberOfDevelopers() {
        givenDevelopers("c-dev", "d-dev", "e-dev", "a-dev");

        Set<Combination<Pair>> allPairCombinations = underTest.getAllCombinations();

        assertThat(allPairCombinations)
                .containsOnly(
                        new Combination<>(Set.of(new Pair("c-dev", "d-dev"), new Pair("a-dev", "e-dev"))),
                        new Combination<>(Set.of(new Pair("d-dev", "e-dev"), new Pair("a-dev", "c-dev"))),
                        new Combination<>(Set.of(new Pair("a-dev", "d-dev"), new Pair("c-dev", "e-dev"))));
    }

    @Test
    void calculateAllPairCombinationsWhereThereShouldBeThreePairsInEachPairCombination() {
        givenDevelopers("c-dev", "d-dev", "e-dev", "a-dev", "b-dev");

        Set<Combination<Pair>> allPairCombinations = underTest.getAllCombinations();

        assertThat(allPairCombinations)
                .hasSize(15)
                .allSatisfy(pairCombination ->
                        assertThat(pairCombination.pairs()).hasSize(3));
    }

}