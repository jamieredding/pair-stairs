package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEntity;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public final class CombinationMapper {

    private CombinationMapper() {
    }

    public static Combination<PairStream> entityToCore(CombinationEntity entity) {
        final Set<PairStream> pairs = entity.getPairs().stream()
                .map(PairStreamMapper::entityToCore)
                .collect(toSet());

        return new Combination<>(pairs);
    }

    public static List<dev.coldhands.pair.stairs.backend.domain.PairStream> entityToDomain(CombinationEntity entity) {
        return entity.getPairs().stream()
                .map(PairStreamMapper::entityToInfo)
                .sorted()
                .toList();
    }
}
