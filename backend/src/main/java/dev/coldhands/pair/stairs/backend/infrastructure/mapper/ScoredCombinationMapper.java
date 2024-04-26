package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;

import static java.util.Comparator.comparing;

public class ScoredCombinationMapper {

    private final PairStreamMapper pairStreamMapper;

    public ScoredCombinationMapper(PairStreamMapper pairStreamMapper) {
        this.pairStreamMapper = pairStreamMapper;
    }

    public ScoredCombination coreToDomain(dev.coldhands.pair.stairs.core.domain.ScoredCombination<PairStream> core) {
        return new ScoredCombination(
                core.totalScore(),
                core.combination().pairs().stream()
                        .map(pairStreamMapper::coreToDomain)
                        .sorted(comparing(ps -> ps.stream().displayName()))
                        .toList()
        );
    }
}
