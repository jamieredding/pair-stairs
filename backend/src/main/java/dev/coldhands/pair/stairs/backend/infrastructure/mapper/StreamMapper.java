package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;

public final class StreamMapper {

    private StreamMapper() {
    }

    public static StreamInfo coreToInfo(String id, LookupById<StreamEntity> lookupById) {
        return entityToInfo(lookupById.lookup(Long.parseLong(id)));
    }

    public static StreamInfo entityToInfo(StreamEntity entity) {
        return new StreamInfo(entity.getId(), entity.getName(),  entity.getArchived());
    }

    public static String entityToCore(StreamEntity entity) {
        return entity.getId().toString();
    }
}
