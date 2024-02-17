package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.BaseRuleTest;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MaintainStreamKnowledgeTransferRuleTest implements BaseRuleTest<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private final MaintainStreamKnowledgeTransferRule underTest = new MaintainStreamKnowledgeTransferRule(combinationHistoryRepository);

    @Override
    public ScoringRule<PairStreamCombination> underTest() {
        return underTest;
    }

    @Override
    public PairStreamCombination exampleCombination() {
        return new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));
    }

    @Test
    void preferCombinationThatMaintainsContextWithinStreams() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(yesterdayCombination).score())
                .isEqualTo(0);
    }

    @Test
    void increaseScoreForCombinationThatLosesContextOfPreviousStreams() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        final var combinationWithoutContext = new PairStreamCombination(Set.of(
                new Pair(Set.of("c-dev"), "1-stream"),
                new Pair(Set.of("a-dev", "b-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(combinationWithoutContext).score())
                .isGreaterThan(0);

    }

    @Test
    void increaseScoreForCombinationThatLosesMoreContextVsPreviousStreams() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev"), "3-stream")
        ));

        final var onePairLostContext = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "e-dev"), "2-stream"),
                new Pair(Set.of("d-dev"), "3-stream")
        ));

        final var twoPairsLostContext = new PairStreamCombination(Set.of(
                new Pair(Set.of("c-dev", "d-dev"), "1-stream"),
                new Pair(Set.of("a-dev", "b-dev"), "2-stream"),
                new Pair(Set.of("e-dev"), "3-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(onePairLostContext).score())
                .isLessThan(underTest.score(twoPairsLostContext).score());

    }

    @Nested
    class MissingParts {

        @Test
        void onlyDeveloperWithContextIsMissing() {
            final var yesterdayCombination = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev"), "2-stream")
            ));

            final var cIsOff = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "d-dev"), "1-stream"),
                    new Pair(Set.of("b-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            assertThat(underTest.score(cIsOff).score())
                    .isGreaterThan(0);

        }

        @Test
        void aNewStreamAndDevIsAdded() {
            final var yesterdayCombination = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev", "d-dev"), "2-stream")
            ));

            final var eIsBackSo3CanContinue = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "c-dev"), "1-stream"),
                    new Pair(Set.of("b-dev", "d-dev"), "2-stream"),
                    new Pair(Set.of("e-dev"), "3-stream")
            ));

            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            assertThat(underTest.score(eIsBackSo3CanContinue).score())
                    .isGreaterThan(0);
        }

        @Test
        void aPreviousStreamIsNotPresentInCombination() {
            final var yesterdayCombination = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                    new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                    new Pair(Set.of("e-dev"), "3-stream")
            ));

            final var eIsOffSo3CanNotContinue = new PairStreamCombination(Set.of(
                    new Pair(Set.of("a-dev", "c-dev"), "1-stream"),
                    new Pair(Set.of("b-dev", "d-dev"), "2-stream")
            ));

            combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

            assertThat(underTest.score(eIsOffSo3CanNotContinue).score())
                    .isEqualTo(0);
        }
    }

    /*
    todo
        - should it be able to score two single devs on streams?
     */
}