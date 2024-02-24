package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.BaseRuleTest;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PenaliseRepeatingDeveloperPairsRuleTest implements BaseRuleTest<Pair> {

    private final CombinationHistoryRepository<Pair> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private final PenaliseRepeatingDeveloperPairsRule underTest = new PenaliseRepeatingDeveloperPairsRule(combinationHistoryRepository);

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
    void doNotContributeToScoreWhenAllPairsAreDifferent() {
        final var yesterdayCombination = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        final var allDifferentPairs = new Combination<>(Set.of(
                new Pair(Set.of("d-dev", "e-dev"), "1-stream"),
                new Pair(Set.of("f-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(allDifferentPairs).score())
                .isEqualTo(0);
    }

    @Test
    void increaseScoreWhenOnePairIsSameAsYesterday() {
        final var yesterdayCombination = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));

        final var allDifferentPairs = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("f-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterdayCombination, LocalDate.now().minusDays(1));

        assertThat(underTest.score(allDifferentPairs).score())
                .isGreaterThan(0);
    }

    @Test
    void itIsWorseForMoreDevelopersToPairMultipleTimes() {
        final var yesterday = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "f-dev"), "3-stream"),
                new Pair(Set.of("g-dev", "h-dev"), "4-stream")
        ));

        combinationHistoryRepository.saveCombination(yesterday, LocalDate.now().minusDays(1));

        final var onePairIsTheSame = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "f-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "h-dev"), "3-stream"),
                new Pair(Set.of("g-dev", "d-dev"), "4-stream")
        ));
        final var twoPairsAreTheSame = new Combination<>(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "h-dev"), "3-stream"),
                new Pair(Set.of("g-dev", "f-dev"), "4-stream")
        ));

        assertThat(underTest.score(onePairIsTheSame).score())
                .isLessThan(underTest.score(twoPairsAreTheSame).score());
    }

}