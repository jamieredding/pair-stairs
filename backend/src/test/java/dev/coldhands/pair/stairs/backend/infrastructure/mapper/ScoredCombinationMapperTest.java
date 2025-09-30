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
        LookupById<DeveloperEntity> developerLookup = id ->
                Map.of(
                        0L, new DeveloperEntity(0L, "dev-0", false),
                        1L, new DeveloperEntity(1L, "dev-1", false)
                ).get(id);
        LookupById<StreamEntity> streamLookup = id ->
                Map.of(
                        0L, new StreamEntity(0L, "stream-a"),
                        1L, new StreamEntity(1L, "stream-b")
                ).get(id);

        final ScoredCombination actual = ScoredCombinationMapper.coreToDomain(new dev.coldhands.pair.stairs.core.domain.ScoredCombination<>(
                new Combination<>(Set.of(
                        new PairStream(Set.of("0"), "0"),
                        new PairStream(Set.of("1"), "1"))),
                10,
                List.of()
        ), developerLookup, streamLookup);

        assertThat(actual).isEqualTo(new ScoredCombination(10,
                List.of(new dev.coldhands.pair.stairs.backend.domain.PairStream(
                                List.of(
                                        new DeveloperInfo(0, "dev-0", false)
                                ),
                                new StreamInfo(0, "stream-a")
                        ),
                        new dev.coldhands.pair.stairs.backend.domain.PairStream(
                                List.of(
                                        new DeveloperInfo(1, "dev-1", false)
                                ),
                                new StreamInfo(1, "stream-b")
                        ))
        ));

    }

    @Test
    void willSortPairStreamsByStreamDisplayName() {
        LookupById<DeveloperEntity> developerLookup = id ->
                Map.of(
                        0L, new DeveloperEntity(0L, "", false)
                ).get(id);
        LookupById<StreamEntity> streamLookup = id ->
                Map.of(
                        0L, new StreamEntity(0L, "a"),
                        1L, new StreamEntity(1L, "b"),
                        2L, new StreamEntity(2L, "c")
                ).get(id);

        final ScoredCombination actual = ScoredCombinationMapper.coreToDomain(new dev.coldhands.pair.stairs.core.domain.ScoredCombination<>(
                new Combination<>(Set.of(
                        new PairStream(Set.of("0"), "0"),
                        new PairStream(Set.of("0"), "1"),
                        new PairStream(Set.of("0"), "2"))),
                0,
                List.of()
        ), developerLookup, streamLookup);

        final List<String> streamDisplayNames = actual.combination().stream()
                .map(dev.coldhands.pair.stairs.backend.domain.PairStream::stream)
                .map(StreamInfo::displayName)
                .toList();

        assertThat(streamDisplayNames).containsExactly("a", "b", "c");
    }
}