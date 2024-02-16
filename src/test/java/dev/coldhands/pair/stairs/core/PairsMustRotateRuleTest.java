package dev.coldhands.pair.stairs.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairsMustRotateRuleTest {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private final PairsMustRotateRule underTest = new PairsMustRotateRule(combinationHistoryRepository);

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
    void doNotContributeToScoreWhenAllPairsAreDifferent() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        final var allDifferentPairs = new PairStreamCombination(Set.of(
                new Pair(Set.of("d-dev", "e-dev"), "1-stream"),
                new Pair(Set.of("f-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(allDifferentPairs).score())
                .isEqualTo(0);
    }

    @Test
    void increaseScoreWhenPairDevelopersAreTheSameAsYesterday() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(yesterdayCombination).score())
                .isGreaterThan(0);
    }

    @Test
    void increaseScoreWhenPairDevelopersAreTheSameAsYesterdayButStreamsAreDifferent() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        final var yesterdayOnDifferentStreams = new PairStreamCombination(Set.of(
                new Pair(Set.of("c-dev"), "1-stream"),
                new Pair(Set.of("a-dev", "b-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(yesterdayOnDifferentStreams).score())
                .isGreaterThan(0);
    }

    /*
    todo
        - should this apply some smaller amount of score if any pairs are the same?
            - e.g. add 1 per pair that is the same as yesterday?
     */

}