package dev.coldhands.pair.stairs.core;

import java.time.LocalDate;
import java.util.Optional;

public interface CombinationHistoryRepository<Combination> {

    void saveCombination(Combination combination, LocalDate date);

    Optional<Combination> getMostRecentCombination();
}
