package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;

public final class DeveloperMapper {

    private DeveloperMapper() {
    }

    public static DeveloperInfo coreToInfo(String id, LookupById<DeveloperEntity> lookupById) {
        return entityToInfo(lookupById.lookup(Long.parseLong(id)));
    }

    public static DeveloperInfo entityToInfo(DeveloperEntity entity) {
        return new DeveloperInfo(entity.getId(), entity.getName(), entity.isArchived());
    }

    public static String entityToCore(DeveloperEntity entity) {
        return entity.getId().toString();
    }
}
