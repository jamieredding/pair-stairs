package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MaintainStreamKnowledgeTransferRuleTest {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private final MaintainStreamKnowledgeTransferRule underTest = new MaintainStreamKnowledgeTransferRule(combinationHistoryRepository);

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

    /*
    todo
        - only developer that had context is not in combination
        - a new stream has been added
        - a previous stream isn't present in this combo
        - weighting?
     */
}