package dev.coldhands.pair.stairs;

import dev.coldhands.pair.stairs.cli.PairPrinter;
import dev.coldhands.pair.stairs.domain.Pairing;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class TestData {
    public static final List<Pairing> EXAMPLE_PAIRINGS = List.of(
            new Pairing(LocalDate.now(), "c-dev"),
            new Pairing(LocalDate.now(), "d-dev", "b-dev"),
            new Pairing(LocalDate.now(), "a-dev", "e-dev"),
            new Pairing(LocalDate.now().minusDays(3), "d-dev"),
            new Pairing(LocalDate.now().minusDays(3), "c-dev", "e-dev"),
            new Pairing(LocalDate.now().minusDays(3), "a-dev", "b-dev"),
            new Pairing(LocalDate.now().minusDays(4), "c-dev"),
            new Pairing(LocalDate.now().minusDays(4), "d-dev", "a-dev"),
            new Pairing(LocalDate.now().minusDays(4), "e-dev", "b-dev"),
            new Pairing(LocalDate.now().minusDays(6), "e-dev"),
            new Pairing(LocalDate.now().minusDays(6), "c-dev", "a-dev"),
            new Pairing(LocalDate.now().minusDays(6), "d-dev", "b-dev")
    );

    @Test
    void showTestData() {
        System.out.println(PairPrinter.drawPairStairs(Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev"), EXAMPLE_PAIRINGS));
    }
}
