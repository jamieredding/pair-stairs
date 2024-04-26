package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;

import java.util.Map;

public class DeveloperMapper {

    private final Map<Long, DeveloperEntity> lookupById;

    public DeveloperMapper(Map<Long, DeveloperEntity> lookupById) {
        this.lookupById = lookupById;
    }

    public DeveloperInfo coreToInfo(String id) {
        final DeveloperEntity entity = lookupById.get(Long.parseLong(id));
        return new DeveloperInfo(entity.getId(), entity.getName());
    }
}
