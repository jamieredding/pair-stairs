package dev.coldhands.pair.stairs;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

public class DecideOMatic {
    private final List<Pairing> pairings;
    private final Set<String> availableDevelopers;
    private final Set<String> newJoiners;

    public DecideOMatic(List<Pairing> pairings, Set<String> availableDevelopers) {
        this(pairings, availableDevelopers, Set.of());
    }

    public DecideOMatic(List<Pairing> pairings, Set<String> availableDevelopers, Set<String> newJoiners) {
        this.pairings = pairings;
        this.availableDevelopers = availableDevelopers;
        this.newJoiners = newJoiners;
    }

    public Set<Pair> getNextPairs() {
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(availableDevelopers);
        List<PairCount> pairCounts = PairUtils.countPairs(availableDevelopers, pairings);
        Set<Pair> recentPairs = pairCounts.stream()
                // todo if someone is off today, do we always still rotate?
                .filter(PairCount::wasRecent)
                .map(PairCount::pair)
                .collect(toSet());
        List<Pair> allPairsSortedByPairCount = pairCounts
                .stream()
                .sorted(new PairCountComparator())
                .map(PairCount::pair)
                .toList();

        return allPairCombinations.stream()
                .sorted(new PairCombinationComparator(allPairsSortedByPairCount))
                .filter(hasNoRecentPairs(recentPairs))
                .filter(hasNoSoloNewJoiners())
                .findFirst()
                .get();
    }

    private Predicate<? super Set<Pair>> hasNoRecentPairs(Set<Pair> recentPairs) {
        return pairs -> recentPairs.stream()
                .noneMatch(pairs::contains);
    }

    private Predicate<? super Set<Pair>> hasNoSoloNewJoiners() {
        return pairs -> newJoiners.stream()
                .map(Pair::new)
                .noneMatch(pairs::contains);
    }

}
