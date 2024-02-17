package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.BaseRuleTest;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PreventConsecutivePairRepeatsRuleTest implements BaseRuleTest<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private final PreventConsecutivePairRepeatsRule underTest = new PreventConsecutivePairRepeatsRule(combinationHistoryRepository);

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