package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamEntryPoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

class EntryPointFactoryTest {

    @Test
    void createFunctionalEntryPoint() {
        final PairStreamEntryPoint actual = new EntryPointFactory(new InMemoryCombinationHistoryRepository<>())
                .create(
                        List.of("dev-0", "dev-1", "dev-2"),
                        List.of("stream-a", "stream-b"));

        final ScoredCombination<PairStream> firstCombination = actual.computeScoredCombinations().getFirst();

        record Tuple(List<String> developers, List<String> streams) {}

        final var result = firstCombination.combination().pairs().stream()
                .collect(teeing(
                        flatMapping(pair -> pair.developers().stream(), toList()),
                        mapping(PairStream::stream, toList()),
                        Tuple::new
                ));

        assertThat(result.developers).containsExactlyInAnyOrder("dev-0", "dev-1", "dev-2");
        assertThat(result.streams).containsExactlyInAnyOrder("stream-a", "stream-b");
    }
}