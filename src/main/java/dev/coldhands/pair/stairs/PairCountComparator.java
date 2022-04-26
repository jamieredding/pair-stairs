package dev.coldhands.pair.stairs;

import java.util.Comparator;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

class PairCountComparator implements Comparator<PairCount> {
    private static final Comparator<PairCount> COMPARATOR =
            comparing(secondPairMember(), putSoloPairsLast())
                    .thenComparing(PairCount::count)
                    .thenComparing(pairCount -> pairCount.pair().first())
                    .thenComparing(pairCount -> pairCount.pair().second());

    private static Comparator<String> putSoloPairsLast() {
        return nullsLast((o1, o2) -> 0);
    }

    private static Function<PairCount, String> secondPairMember() {
        return pairCount -> pairCount.pair().second();
    }

    @Override
    public int compare(PairCount o1, PairCount o2) {
        return COMPARATOR.compare(o1, o2);
    }
}
