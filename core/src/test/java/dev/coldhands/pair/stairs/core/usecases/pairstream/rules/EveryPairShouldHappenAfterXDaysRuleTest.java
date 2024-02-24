package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.BaseRuleTest;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EveryPairShouldHappenAfterXDaysRuleTest implements BaseRuleTest<PairStream> {

    private final CombinationHistoryRepository<PairStream> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private final EveryPairShouldHappenAfterXDaysRule underTest = new EveryPairShouldHappenAfterXDaysRule(List.of("a-dev", "b-dev", "c-dev"), combinationHistoryRepository); // todo define elsewhere...

    @Override
    public ScoringRule<PairStream> underTest() {
        return underTest;
    }

    @Override
    public Combination<PairStream> exampleCombination() {
        return new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                new PairStream(Set.of("c-dev"), "2-stream")
        ));
    }

    @Test
    void increaseScoreWhenAPairDoesNotHappenInMinimumDays_oddNumberOfDevs() {
        final var aAndBCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                new PairStream(Set.of("c-dev"), "2-stream")
        ));
        final var bAndCCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev"), "1-stream"),
                new PairStream(Set.of("b-dev", "c-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(aAndBCombo, LocalDate.now().minusDays(3));
        combinationHistoryRepository.saveCombination(bAndCCombo, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(aAndBCombo, LocalDate.now().minusDays(1));

        assertThat(underTest.score(aAndBCombo).score())
                .isGreaterThan(0);
        assertThat(underTest.score(bAndCCombo).score())
                .isGreaterThan(0);
    }

    @Test
    void increaseScoreWhenAPairDoesNotHappenInMinimumDays_evenNumberOfDevs() {
        final var abcdCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                new PairStream(Set.of("c-dev", "d-dev"), "2-stream")
        ));
        final var adbcCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "d-dev"), "1-stream"),
                new PairStream(Set.of("b-dev", "c-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(abcdCombo, LocalDate.now().minusDays(3));
        combinationHistoryRepository.saveCombination(adbcCombo, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(abcdCombo, LocalDate.now().minusDays(1));

        assertThat(underTest.score(abcdCombo).score())
                .isGreaterThan(0);
        assertThat(underTest.score(adbcCombo).score())
                .isGreaterThan(0);
    }

    @Test
    void doNotContributeToScoreWhenPairHasNotOccurredButItHasNotBeenMinimumDays_oddNumberOfDevs() {
        final var aAndBCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                new PairStream(Set.of("c-dev"), "2-stream")
        ));
        final var bAndCCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev"), "1-stream"),
                new PairStream(Set.of("b-dev", "c-dev"), "2-stream")
        ));
        final var aAndCCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "c-dev"), "1-stream"),
                new PairStream(Set.of("b-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(aAndCCombo, LocalDate.now().minusDays(3));
        combinationHistoryRepository.saveCombination(bAndCCombo, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(aAndBCombo, LocalDate.now().minusDays(1));

        assertThat(underTest.score(aAndBCombo).score())
                .isEqualTo(0);
        assertThat(underTest.score(bAndCCombo).score())
                .isEqualTo(0);
    }

    @Test
    void doNotContributeToScoreWhenPairHasNotOccurredButItHasNotBeenMinimumDays_evenNumberOfDevs() {
        final var abcdCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "b-dev"), "1-stream"),
                new PairStream(Set.of("c-dev", "d-dev"), "2-stream")
        ));
        final var adbcCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "d-dev"), "1-stream"),
                new PairStream(Set.of("b-dev", "c-dev"), "2-stream")
        ));
        final var acbdCombo = new Combination<>(Set.of(
                new PairStream(Set.of("a-dev", "c-dev"), "1-stream"),
                new PairStream(Set.of("b-dev", "d-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(acbdCombo, LocalDate.now().minusDays(3));
        combinationHistoryRepository.saveCombination(adbcCombo, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(abcdCombo, LocalDate.now().minusDays(1));

        assertThat(underTest.score(abcdCombo).score())
                .isEqualTo(0);
        assertThat(underTest.score(adbcCombo).score())
                .isEqualTo(0);
    }

    /*
    todo
        - should only consider previous min_days (total_pairs / pairs_per_day)
            - test to show within and without bounds
        - worse to have more missed pairs than fewer?
        - worse to have more repeated pairs than fewer?
     */
}