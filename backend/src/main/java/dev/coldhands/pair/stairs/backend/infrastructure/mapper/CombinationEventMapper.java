package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.CombinationEvent;
import dev.coldhands.pair.stairs.backend.domain.PairStream;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity;

import java.util.List;

public final class CombinationEventMapper {

    private CombinationEventMapper() {
    }

    public static CombinationEvent entityToDomain(CombinationEventEntity entity) {
        final List<PairStream> combination = CombinationMapper.entityToDomain(entity.getCombination());
        return new CombinationEvent(entity.getId(), entity.getDate(), combination);
    }
}
