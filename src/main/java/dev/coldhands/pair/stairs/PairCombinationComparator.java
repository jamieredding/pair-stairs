package dev.coldhands.pair.stairs;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.Comparator.comparing;

class PairCombinationComparator implements Comparator<Set<Pair>> {

    private final Comparator<Set<Pair>> comparator;

    public PairCombinationComparator(List<Pair> pairsSortedByPairCount) {
        comparator = comparing(scorePairCombinationUsing(pairsSortedByPairCount));
    }

    private Function<Set<Pair>, Integer> scorePairCombinationUsing(List<Pair> pairsSortedByPairCount) {
        return pairCombination -> pairCombination.stream()
                .mapToInt(pairsSortedByPairCount::indexOf)
                .sum();
    }

    @Override
    public int compare(Set<Pair> o1, Set<Pair> o2) {
        return comparator.compare(o1, o2);
    }
}
