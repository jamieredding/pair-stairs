package dev.coldhands.pair.stairs.core;

import java.time.LocalDate;

public record CombinationEvent<Combination>(Combination combination, LocalDate date) {

}
