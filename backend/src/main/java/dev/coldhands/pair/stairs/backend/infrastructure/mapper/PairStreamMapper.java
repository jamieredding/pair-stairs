package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.PairStream;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;

import static java.util.Comparator.comparing;

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
}
