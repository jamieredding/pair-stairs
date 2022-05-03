package dev.coldhands.pair.stairs;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static dev.coldhands.pair.stairs.PairUtils.scorePairCombinationUsing;
import static java.util.Comparator.*;

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

    public List<ScoredPairCombination> getScoredPairCombinations() {
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(availableDevelopers);
        List<PairCount> pairCounts = PairUtils.countPairs(availableDevelopers, pairings);
        List<Pair> allPairsSortedByPairCount = pairCounts
                .stream()
                .sorted(new PairCountComparator(newJoiners))
                .map(PairCount::pair)
                .toList();

        return allPairCombinations.stream()
                .map(toScoredPairCombination(allPairsSortedByPairCount))
                .sorted(comparing(ScoredPairCombination::score))
                .toList();
    }

    private Function<Set<Pair>, ScoredPairCombination> toScoredPairCombination(List<Pair> allPairsSortedByPairCount) {
        return pairCombination -> new ScoredPairCombination(pairCombination,
                scorePairCombinationUsing(allPairsSortedByPairCount, newJoiners).score(pairCombination));
    }

    public Set<Pair> getNextPairs() {
        return getScoredPairCombinations().stream()
                .map(ScoredPairCombination::pairCombination)
                .findFirst()
                .get();
    }

}
