package dev.coldhands.pair.stairs.backend.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.List;

public record SaveCombinationEventDto(LocalDate date,
                                      List<PairStreamByIds> combination) {

    public record PairStreamByIds(List<Long> developerIds,
                                  long streamId) {
    }
}
