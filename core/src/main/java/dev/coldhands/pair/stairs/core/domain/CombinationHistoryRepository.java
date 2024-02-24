package dev.coldhands.pair.stairs.core.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CombinationHistoryRepository<Combination> {

    void saveCombination(Combination combination, LocalDate date);

    default Optional<Combination> getMostRecentCombination() {
        return getMostRecentCombinations(1).stream().findFirst();
    }

    List<Combination> getMostRecentCombinations(int count); // todo should this exclude today's combination or should that be at a higher level?
}
