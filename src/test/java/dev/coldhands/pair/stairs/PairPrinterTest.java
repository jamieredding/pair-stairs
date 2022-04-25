package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairPrinterTest {

    @Test
    void print() {
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
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");

        assertThat(PairPrinter.draw(allDevelopers, pairings))
                .isEqualTo("""
                         \s       andy  cip  jamie  jorge  reece\s
                         \sandy   0     1    1      1      1    \s
                         \scip          0    0      2      1    \s
                         \sjamie             2      0      1    \s
                         \sjorge                    1      0    \s
                         \sreece                           1    \s""");
    }
}