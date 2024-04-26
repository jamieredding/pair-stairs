package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;

import java.util.Map;

public class StreamMapper {

    private final Map<Long, StreamEntity> lookupById;

    public StreamMapper(Map<Long, StreamEntity> lookupById) {
        this.lookupById = lookupById;
    }

    public StreamInfo coreToInfo(String id) {
        final StreamEntity entity = lookupById.get(Long.parseLong(id));
        return new StreamInfo(entity.getId(), entity.getName());
    }
}
