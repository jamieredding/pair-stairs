package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.BaseRuleTest;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Disabled;
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
    void doNotContributeToScoreWhenOnePairIsNew() {
        final var yesterdayCombination = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        final var allDifferentPairs = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
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

    @Test
    @Disabled("adding this behaviour currently causes cycles of devs not pairing with each other")
    void itIsWorseForMoreDevelopersToPairMultipleTimes() {
        final var yesterday = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "f-dev"), "3-stream"),
                new Pair(Set.of("g-dev", "h-dev"), "4-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterday, LocalDate.now().minusDays(1));

        final var onePairIsTheSame = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "f-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "h-dev"), "3-stream"),
                new Pair(Set.of("g-dev", "d-dev"), "4-stream")
        ));
        final var twoPairsAreTheSame = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "h-dev"), "3-stream"),
                new Pair(Set.of("g-dev", "f-dev"), "4-stream")
        ));

        assertThat(underTest.score(onePairIsTheSame).score())
                .isLessThan(underTest.score(twoPairsAreTheSame).score());
    }

}