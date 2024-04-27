package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.PairStream;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;

import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

public final class PairStreamMapper {

    private PairStreamMapper() {
    }

    public static PairStream coreToDomain(dev.coldhands.pair.stairs.core.domain.pairstream.PairStream core,
                                   LookupById<DeveloperEntity> developerLookup,
                                   LookupById<StreamEntity> streamLookup) {
        return new PairStream(
                core.developers().stream()
                        .map(developerId -> DeveloperMapper.coreToInfo(developerId, developerLookup))
                        .sorted(comparing(DeveloperInfo::displayName))
                        .toList(),
                StreamMapper.coreToInfo(core.stream(), streamLookup)
        );
    }

    public static dev.coldhands.pair.stairs.core.domain.pairstream.PairStream entityToCore(PairStreamEntity entity) {
        final Set<String> developerIds = entity.getDevelopers().stream()
                .map(DeveloperMapper::entityToCore)
                .collect(toSet());

        final String streamId = StreamMapper.entityToCore(entity.getStream());

        return new dev.coldhands.pair.stairs.core.domain.pairstream.PairStream(developerIds, streamId);
    }
}
