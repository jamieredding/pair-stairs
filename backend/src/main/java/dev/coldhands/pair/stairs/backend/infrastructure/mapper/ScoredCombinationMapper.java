package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;

import static java.util.Comparator.comparing;

public final class ScoredCombinationMapper {

    private ScoredCombinationMapper() {
    }

    public static ScoredCombination coreToDomain(dev.coldhands.pair.stairs.core.domain.ScoredCombination<PairStream> core,
                                          LookupById<DeveloperEntity> developerLookup,
                                          LookupById<StreamEntity> streamLookup) {
        return new ScoredCombination(
                core.totalScore(),
                core.combination().pairs().stream()
                        .map(ps -> PairStreamMapper.coreToDomain(ps, developerLookup, streamLookup))
                        .sorted(comparing(ps -> ps.stream().displayName()))
                        .toList()
        );
    }
}
