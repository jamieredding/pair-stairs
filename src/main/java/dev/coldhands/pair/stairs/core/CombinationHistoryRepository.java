package dev.coldhands.pair.stairs.core;

import java.time.LocalDate;
import java.util.Optional;
import java.util.SortedSet;

public interface CombinationHistoryRepository<Combination> {

    void saveCombination(Combination combination, LocalDate date);

    Optional<Combination> getMostRecentCombination();

    SortedSet<CombinationEvent<Combination>> getAllEvents();
}
