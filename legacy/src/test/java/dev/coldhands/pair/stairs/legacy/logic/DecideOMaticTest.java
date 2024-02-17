package dev.coldhands.pair.stairs.legacy.logic;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.Sets;
import dev.coldhands.pair.stairs.legacy.LoggingExtension;
import dev.coldhands.pair.stairs.legacy.domain.Pair;
import dev.coldhands.pair.stairs.legacy.domain.Pairing;
import dev.coldhands.pair.stairs.legacy.domain.ScoredPairCombination;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.legacy.TestData.EXAMPLE_PAIRINGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(LoggingExtension.class)
class DecideOMaticTest {

    @Test
    void whenEveryoneIsIn() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");

        DecideOMatic underTest = new DecideOMatic(EXAMPLE_PAIRINGS, allDevelopers);

        Set<Pair> actualPairs = underTest.getNextPairs();
        assertThat(actualPairs)
                .containsOnly(new Pair("b-dev", "c-dev"),
                        new Pair("d-dev", "e-dev"),
                        new Pair("a-dev"));
    }

    static Stream<Arguments> whenSomeoneIsOff() {
        return Stream.of(
                arguments(Set.of("d-dev", "c-dev", "e-dev", "a-dev"),
                        new Pair[]{
                                new Pair("d-dev", "e-dev"),
                                new Pair("a-dev", "c-dev")}),
                arguments(Set.of("d-dev", "c-dev", "e-dev", "b-dev"),
                        new Pair[]{
                                new Pair("b-dev", "c-dev"),
                                new Pair("d-dev", "e-dev")}),
                arguments(Set.of("d-dev", "c-dev", "a-dev", "b-dev"),
                        new Pair[]{
                                new Pair("a-dev", "d-dev"),
                                new Pair("b-dev", "c-dev")}),
                arguments(Set.of("d-dev", "e-dev", "a-dev", "b-dev"),
                        new Pair[]{
                                new Pair("d-dev", "e-dev"),
                                new Pair("a-dev", "b-dev")}),
                arguments(Set.of("c-dev", "e-dev", "a-dev", "b-dev"),
                        new Pair[]{
                                new Pair("a-dev", "c-dev"),
                                new Pair("b-dev", "e-dev")}),
                arguments(Set.of("c-dev", "e-dev", "a-dev"),
                        new Pair[]{
                                new Pair("a-dev", "c-dev"),
                                new Pair("e-dev")})
        );
    }

    @ParameterizedTest
    @MethodSource
    void whenSomeoneIsOff(Set<String> availableDevelopers, Pair[] expectedPairs) {
        DecideOMatic underTest = new DecideOMatic(EXAMPLE_PAIRINGS, availableDevelopers);

        Set<Pair> actualPairs = underTest.getNextPairs();
        assertThat(actualPairs)
                .containsOnly(expectedPairs);
    }

    @Test
    void pairsShouldAlwaysRotate() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = new ArrayList<>();

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers);
        Set<Pair> firstPairs = underTest.getNextPairs();
        firstPairs.stream()
                .map(pair -> new Pairing(now.plusDays(1), pair))
                .forEach(pairings::add);

        underTest = new DecideOMatic(pairings, allDevelopers);
        Set<Pair> nextPairs = underTest.getNextPairs();

        Sets.SetView<Pair> intersection = Sets.intersection(firstPairs, nextPairs);

        assertThat(intersection)
                .isEmpty();
    }

    @Test
    void whenNoExistingPairingsChooseACombination() {
        Set<String> allDevelopers = Set.of("a-dev", "b-dev", "c-dev");
        List<Pairing> pairings = new ArrayList<>();

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers);
        assertThat(underTest.getNextPairs().stream()
                .flatMap(pair -> pair.members().stream())
                .toList())
                .containsOnly("a-dev", "b-dev", "c-dev");
    }

    @Test
    void doNotAllowNewJoinersToSolo() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev"),
                new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                new Pairing(now.minusDays(2), "c-dev"),
                new Pairing(now.minusDays(3), "c-dev", "e-dev"),
                new Pairing(now.minusDays(3), "d-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers, Set.of("e-dev"));
        assertThat(underTest.getNextPairs())
                .containsOnly(new Pair("d-dev", "e-dev"),
                        new Pair("c-dev"));
    }

    @Test
    void doNotAllowNewJoinersToSoloEvenWhenTheyHaveInThePast() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev"),
                new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                new Pairing(now.minusDays(2), "c-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers, Set.of("c-dev", "e-dev"));
        assertThat(underTest.getNextPairs())
                .containsOnly(new Pair("c-dev", "e-dev"),
                        new Pair("d-dev"));
    }

    @Test
    void preferPairingNewJoinersWithNonNewJoinersInsteadOfMakingANewJoinerPair() {
        Set<String> allDevelopers = Set.of("a-dev", "b-dev", "c-dev", "d-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "a-dev", "b-dev"),
                new Pairing(now.minusDays(1), "c-dev", "d-dev"),
                new Pairing(now.minusDays(2), "a-dev", "c-dev"),
                new Pairing(now.minusDays(2), "b-dev", "d-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers, Set.of("b-dev", "c-dev"));
        assertThat(underTest.getNextPairs())
                .containsOnly(new Pair("a-dev", "c-dev"),
                        new Pair("b-dev", "d-dev"));
    }

    @Test
    void preferPairingTwoNewJoinersOverSoloNewJoiner() {
        Set<String> allDevelopers = Set.of("a-dev", "b-dev", "c-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "a-dev", "b-dev"),
                new Pairing(now.minusDays(1), "c-dev"),
                new Pairing(now.minusDays(2), "a-dev", "c-dev"),
                new Pairing(now.minusDays(2), "b-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers, Set.of("b-dev", "c-dev"));
        assertThat(underTest.getNextPairs())
                .containsOnly(new Pair("b-dev", "c-dev"),
                        new Pair("a-dev"));
    }

    @Test
    void showAllPairsWithTheirScoreInOrder() {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev"),
                new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                new Pairing(now.minusDays(2), "c-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers);

        List<ScoredPairCombination> actual = underTest.getScoredPairCombinations();
        assertThat(actual)
                .containsOnly(
                        new ScoredPairCombination(
                                Set.of(new Pair("c-dev", "d-dev"),
                                        new Pair("e-dev")),
                                1),
                        new ScoredPairCombination(
                                Set.of(new Pair("d-dev", "e-dev"),
                                        new Pair("c-dev")),
                                99),
                        new ScoredPairCombination(
                                Set.of(new Pair("c-dev", "e-dev"),
                                        new Pair("d-dev")),
                                20100)
                );
    }

    @Test
    void whenDebugLoggingEnabledThenLogAllPairsAndTheirScores(Logger logger,
                                                              ListAppender<ILoggingEvent> appender) {
        Set<String> allDevelopers = Set.of("a-dev", "b-dev", "c-dev");
        LocalDate now = LocalDate.now();
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "a-dev", "b-dev"),
                new Pairing(now.minusDays(1), "c-dev"),
                new Pairing(now.minusDays(2), "b-dev", "c-dev"),
                new Pairing(now.minusDays(2), "a-dev"));

        DecideOMatic underTest = new DecideOMatic(pairings, allDevelopers);

        logger.setLevel(Level.DEBUG);

        underTest.getScoredPairCombinations();

        assertThat(appender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(event.getLoggerName()).isEqualTo(DecideOMatic.class.getName());
                    assertThat(event.getFormattedMessage()).isEqualTo("""
                            Pairs and their score:
                                                        
                            a-dev c-dev -> -99
                            b-dev c-dev -> -1
                            a-dev -> 100
                            b-dev -> 100
                            a-dev b-dev -> 10000
                            c-dev -> 10100
                            """);
                });
    }
}
