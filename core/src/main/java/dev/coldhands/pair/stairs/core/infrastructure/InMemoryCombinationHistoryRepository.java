package dev.coldhands.pair.stairs.core.infrastructure;

import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class InMemoryCombinationHistoryRepository<T> implements CombinationHistoryRepository<T> {

    private final SortedSet<CombinationEvent<T>> combinations = new TreeSet<>(Comparator.comparing(CombinationEvent::date));

    public void saveCombination(Combination<T> combination, LocalDate date) {
        combinations.add(new CombinationEvent<>(combination, date));
    }

    @Override
    public List<Combination<T>> getMostRecentCombinations(int count) {
        return combinations.reversed().stream()
                .limit(count)
                .map(CombinationEvent::combination)
                .toList();
    }

    private record CombinationEvent<T>(Combination<T> combination, LocalDate date) {

    }
}
