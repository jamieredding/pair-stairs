package dev.coldhands.pair.stairs.backend.domain;

import java.time.LocalDate;
import java.util.List;

public record CombinationEvent(long id,
                               LocalDate date,
                               List<PairStream> combination) {
}
