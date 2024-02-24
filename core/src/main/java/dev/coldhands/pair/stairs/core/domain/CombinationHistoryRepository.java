package dev.coldhands.pair.stairs.core.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CombinationHistoryRepository<T> {

    void saveCombination(Combination<T> combination, LocalDate date);

    default Optional<Combination<T>> getMostRecentCombination() {
        return getMostRecentCombinations(1).stream().findFirst();
    }

    List<Combination<T>> getMostRecentCombinations(int count); // todo should this exclude today's combination or should that be at a higher level?
}
