package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.PairStream;

import static java.util.Comparator.comparing;

public class PairStreamMapper {

    private final DeveloperMapper developerMapper;
    private final StreamMapper streamMapper;

    public PairStreamMapper(DeveloperMapper developerMapper, StreamMapper streamMapper) {
        this.developerMapper = developerMapper;
        this.streamMapper = streamMapper;
    }

    public PairStream coreToDomain(dev.coldhands.pair.stairs.core.domain.pairstream.PairStream core) {
        return new PairStream(
                core.developers().stream()
                        .map(developerMapper::coreToInfo)
                        .sorted(comparing(DeveloperInfo::displayName))
                        .toList(),
                streamMapper.coreToInfo(core.stream())
        );
    }
}
