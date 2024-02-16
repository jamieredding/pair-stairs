package dev.coldhands.pair.stairs.core;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

public class InMemoryCombinationHistoryRepository<Combination> implements CombinationHistoryRepository<Combination> {

    private final SortedSet<CombinationEvent<Combination>> combinations = new TreeSet<>(Comparator.comparing(CombinationEvent::date));

    @Override
    public void saveCombination(Combination combination, LocalDate date) {
        combinations.add(new CombinationEvent<>(combination, date));
    }

    @Override
    public Optional<Combination> getMostRecentCombination() { // todo should this exclude today's combination or should that be at a higher level?
        return combinations.isEmpty()
                ? Optional.empty()
                : Optional.of(combinations.last().combination);
    }

    private record CombinationEvent<Combination>(Combination combination, LocalDate date) {

    }
}
