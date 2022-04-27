package dev.coldhands.pair.stairs;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairPrinterTest {

    @Test
    void print() {
        List<Pairing> pairings = TestData.EXAMPLE_PAIRINGS;
        Set<String> allDevelopers = Set.of("jorge", "jamie", "reece", "andy", "cip");

        assertThat(PairPrinter.draw(allDevelopers, pairings))
                .isEqualTo("""
                        \s       andy  cip  jamie  jorge  reece\s
                        \sandy    0     1     1      1     1 * \s
                        \scip           0     0     2 *     1  \s
                        \sjamie              2 *     0      1  \s
                        \sjorge                      1      0  \s
                        \sreece                             1  \s""");
    }
}