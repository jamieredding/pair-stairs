package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamStatisticsService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PreferPairsOrStreamsThatHaveNotHappenedRecentlyRuleTest {
    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository = new InMemoryCombinationHistoryRepository<>();
    private PreferPairsOrStreamsThatHaveNotHappenedRecentlyRule underTest;

    @Test
    void doNotContributeToScoreIfAllCombinationsHaveBeenSeenInRecentHistory() {
        final var first = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));
        final var bSees2AndC = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev"), "1-stream"),
                new Pair(Set.of("b-dev", "c-dev"), "2-stream")
        ));
        final var cSees1AndA = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "c-dev"), "1-stream"),
                new Pair(Set.of("b-dev"), "2-stream")
        ));
        final var aSees2 = new PairStreamCombination(Set.of(
                new Pair(Set.of("c-dev"), "1-stream"),
                new Pair(Set.of("a-dev", "b-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(first, LocalDate.now().minusDays(4));
        combinationHistoryRepository.saveCombination(bSees2AndC, LocalDate.now().minusDays(3));
        combinationHistoryRepository.saveCombination(cSees1AndA, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(aSees2, LocalDate.now().minusDays(1));

        initialiseUnderTest(
                List.of("a-dev", "b-dev", "c-dev"),
                List.of("1-stream", "2-stream")
        );
        assertThat(underTest.score(first).score())
                .isEqualTo(0);
    }

    @Test
    void lowerScoreIfDeveloperComboHasNotOccurred() {
        final var first = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));
        final var firstButDifferentStreams = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "2-stream"),
                new Pair(Set.of("c-dev"), "1-stream")
        ));
        final var bSeesC = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev"), "1-stream"),
                new Pair(Set.of("b-dev", "c-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(first, LocalDate.now().minusDays(3));
        combinationHistoryRepository.saveCombination(firstButDifferentStreams, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(bSeesC, LocalDate.now().minusDays(1));

        initialiseUnderTest(
                List.of("a-dev", "b-dev", "c-dev"),
                List.of("1-stream", "2-stream")
        );

        final var aAndCPair = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "c-dev"), "1-stream"),
                new Pair(Set.of("b-dev"), "2-stream")
        ));

        assertThat(underTest.score(aAndCPair).score())
                .isLessThan(0);
    }

    @Test
    void lowerScoreIfDeveloperHasNotSeenStream() {
        final var first = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev"), "2-stream")
        ));
        final var bSees2AndC = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev"), "1-stream"),
                new Pair(Set.of("b-dev", "c-dev"), "2-stream")
        ));
        final var cSeesA = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "c-dev"), "1-stream"),
                new Pair(Set.of("b-dev"), "2-stream")
        ));

        combinationHistoryRepository.saveCombination(first, LocalDate.now().minusDays(3));
        combinationHistoryRepository.saveCombination(bSees2AndC, LocalDate.now().minusDays(2));
        combinationHistoryRepository.saveCombination(cSeesA, LocalDate.now().minusDays(1));

        initialiseUnderTest(
                List.of("a-dev", "b-dev", "c-dev"),
                List.of("1-stream", "2-stream")
        );

        final var aSees2 = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "c-dev"), "2-stream"),
                new Pair(Set.of("b-dev"), "1-stream")
        ));

        assertThat(underTest.score(aSees2).score())
                .isLessThan(0);
    }

    @Test
    void preferMoreNewPairsOverFewer() {
        final var first = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "d-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "f-dev"), "3-stream")
        ));

        combinationHistoryRepository.saveCombination(first, LocalDate.now().minusDays(1));

        initialiseUnderTest(
                List.of("a-dev", "b-dev", "c-dev", "d-dev", "e-dev", "f-dev"),
                List.of("1-stream", "2-stream", "3-stream")
        );

        final var twoNewPairs = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "b-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "e-dev"), "2-stream"),
                new Pair(Set.of("d-dev", "f-dev"), "3-stream")
        ));

        final var threeNewPairs = new PairStreamCombination(Set.of(
                new Pair(Set.of("a-dev", "f-dev"), "1-stream"),
                new Pair(Set.of("c-dev", "b-dev"), "2-stream"),
                new Pair(Set.of("e-dev", "d-dev"), "3-stream")
        ));

        assertThat(underTest.score(threeNewPairs).score())
                .isLessThan(underTest.score(twoNewPairs).score());
    }

    private void initialiseUnderTest(List<String> developers, List<String> streams) {
        PairStreamStatisticsService statisticsService = new PairStreamStatisticsService(combinationHistoryRepository, developers, streams);
        statisticsService.updateStatistics();

        underTest = new PreferPairsOrStreamsThatHaveNotHappenedRecentlyRule(statisticsService);
    }
}