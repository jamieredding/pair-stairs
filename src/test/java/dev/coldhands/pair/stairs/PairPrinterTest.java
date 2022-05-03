package dev.coldhands.pair.stairs;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairPrinterTest {

    @Test
    void drawPairStairs() {
        List<Pairing> pairings = TestData.EXAMPLE_PAIRINGS;
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");

        assertThat(PairPrinter.drawPairStairs(allDevelopers, pairings))
                .isEqualTo("""
                        \s       a-dev  b-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      1      1      1     1 * \s
                         b-dev           0      0     2 *     1  \s
                         c-dev                 2 *     0      1  \s
                         d-dev                         1      0  \s
                         e-dev                                1  \s""");
    }
}