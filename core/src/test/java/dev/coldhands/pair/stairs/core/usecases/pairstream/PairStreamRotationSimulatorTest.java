package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

class PairStreamRotationSimulatorTest {

    @Test
    void runForThreeSteps() {
        final var repository = new InMemoryCombinationHistoryRepository<PairStreamCombination>();

        final List<String> developers = List.of("a-dev", "b-dev", "c-dev");
        final List<String> streams = List.of("1-stream", "2-stream");

        final var underTest = new PairStreamRotationSimulator(developers, streams, repository);

        final List<ScoredCombination<PairStreamCombination>> scoredCombinations = underTest.runSimulation(3);

        record AllParties(Set<String> developers, Set<String> streams) {

        }

        assertThat(scoredCombinations)
                .hasSize(3)
                .allSatisfy(scoredCombination -> {
                    final var allParties = scoredCombination.combination().pairs().stream()
                                    .collect(teeing(
                                            flatMapping(pair -> pair.developers().stream(), toSet()),
                                            mapping(Pair::stream, toSet()),
                                            AllParties::new
                                    ));
                    assertThat(allParties.developers).containsAll(developers);
                    assertThat(allParties.streams).containsAll(streams);
                });

        final var actualCombinations = scoredCombinations.stream()
                .map(ScoredCombination::combination)
                .toArray(PairStreamCombination[]::new);

        assertThat(repository.getAllCombinations())
                .containsExactly(actualCombinations)
        ;
    }
}