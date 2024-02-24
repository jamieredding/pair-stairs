package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.BaseRuleTest;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PenaliseEarlyContextSwitchingRuleTest implements BaseRuleTest<Pair> {

    private final CombinationHistoryRepository<Pair> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private final PenaliseEarlyContextSwitchingRule underTest = new PenaliseEarlyContextSwitchingRule(combinationHistoryRepository);

    @Override
    public ScoringRule<Pair> underTest() {
        return underTest;
    }

    @Override
    public Combination<Pair> exampleCombination() {
        return new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));
    }

    @Test
    void doNotContributeToScoreWhenAllMembersDoNotContextSwitch() {
        final var yesterdayCombination = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(yesterdayCombination).score())
                .isEqualTo(0);
    }

    @Test
    void increaseScoreIfDeveloperSwitchesAfterOneDayInStream() {
        final var dayBeforeYesterdayCombination = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));
        final var yesterdayCombination = new Combination<>(Set.of(
                new Pair(Set.of("b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "a-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(dayBeforeYesterdayCombination, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        final var aSwitchesAfterOneDay = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        assertThat(underTest.score(aSwitchesAfterOneDay).score())
                .isGreaterThan(0);
    }

    @Test
    @Disabled("This is out of scope of this rule") // todo if this is wanted it should be a new rule
    void itIsWorseToStayLongerInAStream() {
        final var combination = new Combination<>(Set.of(
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
    void itIsWorseForMoreDevelopersToContextSwitch() {
        final var dayBeforeYesterday = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev"), "3-stream")
        ));
        final var yesterday = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "d-dev"), "1-stream"), // d swapped
                new Pair(Set.of("c-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "b-dev"), "3-stream")  // b swapped
        ));

        combinationHistoryRepository.saveCombination(dayBeforeYesterday, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(yesterday, LocalDate.now().minusDays(1));


        final var oneDevSwitchesEarly = new Combination<>(Set.of(
                new Pair(Set.of("a-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"), // d switches early
                new Pair(Set.of("e-dev", "b-dev"), "3-stream")
        ));
        final var twoDevsSwitchEarly = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"), // b switches early
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"), // d switches early
                new Pair(Set.of("e-dev"), "3-stream")
        ));

        assertThat(underTest.score(oneDevSwitchesEarly).score())
                .isLessThan(underTest.score(twoDevsSwitchEarly).score());
    }

    @Nested
    class MissingParts {

        @Test
        void doNotContributeToScoreIfDeveloperWasOffYesterday() {
            final var dayBeforeYesterdayCombination = new Combination<>(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev", "d-dev"), "2-stream")
            ));
            final var yesterdayCombination = new Combination<>(Set.of(
                    new Pair(Set.of("b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev", "a-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(dayBeforeYesterdayCombination, LocalDate.now().minusDays(2));
            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            final var dReturns = new Combination<>(Set.of(
                    new Pair(Set.of("b-dev", "c-dev"), "1-stream"),
                    new Pair(Set.of("a-dev", "d-dev"), "2-stream")
            ));

            assertThat(underTest.score(dReturns).score())
                    .isEqualTo(0);
        }
    }

    /*
    todo
        - missing
            - developer not in yesterday
                - should I consider what they were last on?
     */
}