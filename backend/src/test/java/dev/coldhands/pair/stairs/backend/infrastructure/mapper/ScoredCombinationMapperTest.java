package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScoredCombinationMapperTest {

    @Test
    void canMapFromCoreToDomain() {
        final DeveloperMapper developerMapper = new DeveloperMapper(Map.of(
                0L, new DeveloperEntity(0L, "dev-0"),
                1L, new DeveloperEntity(1L, "dev-1")
        ));
        final StreamMapper streamMapper = new StreamMapper(Map.of(
                0L, new StreamEntity(0L, "stream-a"),
                1L, new StreamEntity(1L, "stream-b")
        ));
        final ScoredCombinationMapper underTest = new ScoredCombinationMapper(
                new PairStreamMapper(developerMapper, streamMapper));

        final ScoredCombination actual = underTest.coreToDomain(new dev.coldhands.pair.stairs.core.domain.ScoredCombination<>(
                new Combination<>(Set.of(
                        new PairStream(Set.of("0"), "0"),
                        new PairStream(Set.of("1"), "1"))),
                10,
                List.of()
        ));

        assertThat(actual).isEqualTo(new ScoredCombination(10,
                List.of(new dev.coldhands.pair.stairs.backend.domain.PairStream(
                                List.of(
                                        new DeveloperInfo(0, "dev-0")
                                ),
                                new StreamInfo(0, "stream-a")
                        ),
                        new dev.coldhands.pair.stairs.backend.domain.PairStream(
                                List.of(
                                        new DeveloperInfo(1, "dev-1")
                                ),
                                new StreamInfo(1, "stream-b")
                        ))
        ));

    }

    @Test
    void willSortPairStreamsByStreamDisplayName() {
        final DeveloperMapper developerMapper = new DeveloperMapper(Map.of(
                0L, new DeveloperEntity(0L, "")
        ));
        final StreamMapper streamMapper = new StreamMapper(Map.of(
                0L, new StreamEntity(0L, "a"),
                1L, new StreamEntity(1L, "b"),
                2L, new StreamEntity(2L, "c")
        ));
        final ScoredCombinationMapper underTest = new ScoredCombinationMapper(
                new PairStreamMapper(developerMapper, streamMapper));

        final ScoredCombination actual = underTest.coreToDomain(new dev.coldhands.pair.stairs.core.domain.ScoredCombination<>(
                new Combination<>(Set.of(
                        new PairStream(Set.of("0"), "0"),
                        new PairStream(Set.of("0"), "1"),
                        new PairStream(Set.of("0"), "2"))),
                0,
                List.of()
        ));

        final List<String> streamDisplayNames = actual.combination().stream()
                .map(dev.coldhands.pair.stairs.backend.domain.PairStream::stream)
                .map(StreamInfo::displayName)
                .toList();

        assertThat(streamDisplayNames).containsExactly("a", "b", "c");
    }
}