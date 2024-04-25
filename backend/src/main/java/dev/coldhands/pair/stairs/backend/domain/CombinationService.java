package dev.coldhands.pair.stairs.backend.domain;

import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ScoredCombinationDto;

import java.util.List;

public interface CombinationService {

    List<ScoredCombinationDto> calculate(List<Developer> developers, List<Stream> streams, int page);
}
