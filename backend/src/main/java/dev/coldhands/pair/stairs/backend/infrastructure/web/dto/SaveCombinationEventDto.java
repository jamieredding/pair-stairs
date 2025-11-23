package dev.coldhands.pair.stairs.backend.infrastructure.web.dto;

import dev.coldhands.pair.stairs.backend.domain.DeveloperId;
import dev.coldhands.pair.stairs.backend.domain.StreamId;

import java.time.LocalDate;
import java.util.List;

public record SaveCombinationEventDto(LocalDate date,
                                      List<PairStreamByIds> combination) {

    public record PairStreamByIds(List<DeveloperId> developerIds,
                                  StreamId streamId) {
    }
}
