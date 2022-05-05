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

    @Test
    void drawPairChoices_oddNumberOfPairs() {
        List<ScoredPairCombination> toPrint = List.of(
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-b"),
                        new Pair("dev-c", "dev-d"),
                        new Pair("dev-e")
                ), 10),
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-e"),
                        new Pair("dev-b", "dev-c"),
                        new Pair("dev-d")
                ), 15),
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-d"),
                        new Pair("dev-d", "dev-e"),
                        new Pair("dev-c")
                ), 30),
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-c"),
                        new Pair("dev-d", "dev-e"),
                        new Pair("dev-b")
                ), 40)
        );

        assertThat(PairPrinter.drawPairChoices(toPrint, 3))
                .isEqualTo("""
                        \s             1             2             3      \s
                        \sPair a  dev-a  dev-b  dev-a  dev-e  dev-a  dev-d\s
                        \sPair b  dev-c  dev-d  dev-b  dev-c  dev-d  dev-e\s
                        \sPair c     dev-e         dev-d         dev-c    \s
                        \s             10            15            30     \s""");
    }

    @Test
    void drawPairChoices_evenNumberOfPairs() {
        List<ScoredPairCombination> toPrint = List.of(
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-b"),
                        new Pair("dev-c", "dev-d")
                ), 10),
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-e"),
                        new Pair("dev-b", "dev-c")
                ), 15),
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-d"),
                        new Pair("dev-d", "dev-e")
                ), 30)
        );

        assertThat(PairPrinter.drawPairChoices(toPrint, 3))
                .isEqualTo("""
                        \s             1             2             3      \s
                        \sPair a  dev-a  dev-b  dev-a  dev-e  dev-a  dev-d\s
                        \sPair b  dev-c  dev-d  dev-b  dev-c  dev-d  dev-e\s
                        \s             10            15            30     \s""");
    }

    @Test
    void drawPairChoicesCanSpecifyANumberOfOptionsToShow() {
        List<ScoredPairCombination> toPrint = List.of(
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-b"),
                        new Pair("dev-c", "dev-d")
                ), 10),
                new ScoredPairCombination(Set.of(
                        new Pair("dev-a", "dev-e"),
                        new Pair("dev-b", "dev-c")
                ), 15)
        );

        assertThat(PairPrinter.drawPairChoices(toPrint, 1))
                .isEqualTo("""
                        \s             1      \s
                        \sPair a  dev-a  dev-b\s
                        \sPair b  dev-c  dev-d\s
                        \s             10     \s""");
    }
}