package dev.coldhands.pair.stairs.legacy.logic.legacy;

import com.google.common.collect.Sets;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.legacy.domain.Pair;
import dev.coldhands.pair.stairs.legacy.domain.PairCombination;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class LegacyCombinationService implements CombinationService<PairCombination> {
    private final List<String> availableDevelopers;

    public LegacyCombinationService(List<String> availableDevelopers) {
        this.availableDevelopers = availableDevelopers;
    }

    @Override
    public Set<PairCombination> getAllCombinations() {
        Set<String> allDevelopers = new HashSet<>(availableDevelopers);
        Set<Pair> allPossiblePairs = Sets.combinations(allDevelopers, 2).stream()
                .map(ArrayList::new)
                .peek(Collections::sort)
                .map(pairAsList -> new Pair(pairAsList.getFirst(), pairAsList.get(1)))
                .collect(Collectors.toCollection(HashSet::new));

        allDevelopers.forEach(dev -> allPossiblePairs.add(new Pair(dev)));

        return Sets.combinations(allPossiblePairs, Math.ceilDiv(allDevelopers.size(), 2)).stream()
                .map(PairCombination::new)
                .filter(isValidPairCombination(allDevelopers))
                .collect(toSet());
    }

    private static Predicate<PairCombination> isValidPairCombination(Set<String> allDevelopers) {
        return pairCombination -> {
            List<String> allDevsInPairs = pairCombination.pairs().stream()
                    .mapMulti((Pair pair, Consumer<String> consumer) -> {
                        consumer.accept(pair.first());
                        if (pair.second() != null) {
                            consumer.accept(pair.second());
                        }
                    })
                    .toList();
            return allDevsInPairs.size() == allDevelopers.size() &&
                    new HashSet<>(allDevsInPairs).containsAll(allDevelopers);
        };
    }
}