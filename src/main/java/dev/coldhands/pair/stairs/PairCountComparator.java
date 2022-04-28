package dev.coldhands.pair.stairs;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

class PairCountComparator implements Comparator<PairCount> {
    private final Comparator<PairCount> comparator;

    public PairCountComparator(Set<String> newJoiners) {
        comparator = comparing(soloNewJoinersLast(newJoiners))
                .thenComparing(recentPairsLast())
                .thenComparing(secondPairMember(), putSoloPairsLast())
                .thenComparing(PairCount::count)
                .thenComparing(pairCount -> pairCount.pair().first())
                .thenComparing(pairCount -> pairCount.pair().second());
    }

    private static Function<PairCount, Boolean> soloNewJoinersLast(Set<String> newJoiners) {
        return pairCount -> {
            Pair pair = pairCount.pair();
            return pair.second() == null && newJoiners.contains(pair.first());
        };
    }

    private static Function<PairCount, Boolean> recentPairsLast() {
        return PairCount::wasRecent;
    }

    private static Comparator<String> putSoloPairsLast() {
        return nullsLast((o1, o2) -> 0);
    }

    private static Function<PairCount, String> secondPairMember() {
        return pairCount -> pairCount.pair().second();
    }

    @Override
    public int compare(PairCount o1, PairCount o2) {
        return comparator.compare(o1, o2);
    }
}
