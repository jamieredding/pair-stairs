package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;

public final class StreamMapper {

    private StreamMapper() {
    }

    public static StreamInfo coreToInfo(String id, LookupById<StreamEntity> lookupById) {
        final StreamEntity entity = lookupById.lookup(Long.parseLong(id));
        return new StreamInfo(entity.getId(), entity.getName());
    }

    public static String entityToCore(StreamEntity entity) {
        return entity.getId().toString();
    }
}
