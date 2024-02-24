package dev.coldhands.pair.stairs.core.infrastructure;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class InMemoryCombinationHistoryRepository<Combination> implements CombinationHistoryRepository<Combination> {

    private final SortedSet<CombinationEvent<Combination>> combinations = new TreeSet<>(Comparator.comparing(CombinationEvent::date));

    @Override
    public void saveCombination(Combination combination, LocalDate date) {
        combinations.add(new CombinationEvent<>(combination, date));
    }

    @Override
    public List<Combination> getMostRecentCombinations(int count) {
        return combinations.reversed().stream()
                .limit(count)
                .map(CombinationEvent::combination)
                .toList();
    }

    private record CombinationEvent<Combination>(Combination combination, LocalDate date) {

    }
}
