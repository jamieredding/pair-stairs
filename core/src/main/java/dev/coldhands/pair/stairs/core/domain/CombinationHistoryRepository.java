package dev.coldhands.pair.stairs.core.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CombinationHistoryRepository<Combination> {

    void saveCombination(Combination combination, LocalDate date);

    Optional<Combination> getMostRecentCombination();

    List<Combination> getAllCombinations();
}