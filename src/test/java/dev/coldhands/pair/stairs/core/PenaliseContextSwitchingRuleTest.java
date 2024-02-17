package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PenaliseContextSwitchingRuleTest {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private final PenaliseContextSwitchingRule underTest = new PenaliseContextSwitchingRule(combinationHistoryRepository);

    @Test
    void doNotContributeToScoreIfNoHistory() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        assertThat(underTest.score(yesterdayCombination).score())
                .isEqualTo(0);
    }

    @Test
    void doNotContributeToScoreWhenAllMembersDoNotContextSwitch() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(yesterdayCombination).score())
                .isEqualTo(0);
    }

    @Test
    void increaseScoreIfDeveloperSwitchesAfterOneDayInStream() {
        final var dayBeforeYesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "a-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(dayBeforeYesterdayCombination, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        final var aSwitchesAfterOneDay = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        assertThat(underTest.score(aSwitchesAfterOneDay).score())
                .isGreaterThan(0);
    }

    @Test
    void itIsWorseToStayLongerInAStream() {
        final var combination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev"), "3-stream")
        ));

        combinationHistoryRepository.saveCombination(combination, LocalDate.now().minusDays(3));
        combinationHistoryRepository.saveCombination(combination, LocalDate.now().minusDays(2));

        final var resultAfterTwoDays = underTest.score(combination);

        combinationHistoryRepository.saveCombination(combination, LocalDate.now().minusDays(1));

        final var resultAfterThreeDays = underTest.score(combination);

        assertThat(resultAfterTwoDays.score())
                .isLessThan(resultAfterThreeDays.score());

    }

    @Test
    @Disabled
    void itIsWorseForMoreDevelopersToContextSwitch() {
        final var dayBeforeYesterday = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev"), "3-stream")
        ));
        final var yesterday = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "d-dev"), "1-stream"), // d swapped
                new Pair(Set.of("c-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "b-dev"), "3-stream")  // b swapped
        ));

        combinationHistoryRepository.saveCombination(dayBeforeYesterday, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(yesterday, LocalDate.now().minusDays(1));


        final var oneDevSwitchesEarly = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"), // d switches early
                new Pair(Set.of("e-dev", "b-dev"), "3-stream")
        ));
        final var twoDevsSwitchEarly = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"), // b switches early
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"), // d switches early
                new Pair(Set.of("e-dev"), "3-stream")
        ));

        assertThat(underTest.score(oneDevSwitchesEarly).score())
                .isLessThan(underTest.score(twoDevsSwitchEarly).score());
    }

    /*
    todo
        - add to score for every developer that switches stream
        - missing
            - developer not in yesterday
                - should I consider what they were last on?
     */
}