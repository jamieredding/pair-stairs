package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DecideOMaticTest {

    @Test
    void findPairs() {
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");
        List<Pairing> pairings = List.of(
                new Pairing(LocalDate.now(), "jamie"),
                new Pairing(LocalDate.now(), "jorge", "cip"),
                new Pairing(LocalDate.now(), "andy", "reece"),
                new Pairing(LocalDate.now().minusDays(3), "jorge"),
                new Pairing(LocalDate.now().minusDays(3), "jamie", "reece"),
                new Pairing(LocalDate.now().minusDays(3), "andy", "cip"),
                new Pairing(LocalDate.now().minusDays(4), "jamie"),
                new Pairing(LocalDate.now().minusDays(4), "jorge", "andy"),
                new Pairing(LocalDate.now().minusDays(4), "reece", "cip"),
                new Pairing(LocalDate.now().minusDays(6), "reece"),
                new Pairing(LocalDate.now().minusDays(6), "jamie", "andy"),
                new Pairing(LocalDate.now().minusDays(6), "jorge", "cip")
        );
        DateProvider dateProvider = () -> LocalDate.now().plusDays(1);

        DecideOMatic underTest = new DecideOMatic(dateProvider, allDevelopers, pairings, allDevelopers);

        Set<Pair> actualPairs = underTest.getNextPairs();
        assertThat(actualPairs)
                .containsOnly(new Pair("cip", "jamie"),
                        new Pair("jorge", "reece"),
                        new Pair("andy"));
    }

    /*
    todo
     - someone off
     - allow picking of pairs
     - offer multiple options
     - show bad pairings
     */
}
