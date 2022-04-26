package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

class TestData {
    static final List<Pairing> EXAMPLE_PAIRINGS = List.of(
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

    @Test
    void showTestData() {
        System.out.println(PairPrinter.draw(Set.of("jorge", "jamie", "reece", "andy", "cip"), EXAMPLE_PAIRINGS));
    }
}
