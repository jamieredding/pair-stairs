package dev.coldhands.pair.stairs;

import com.google.common.collect.Sets;

import java.util.*;

import static java.util.Comparator.*;

class PairUtils {
    static Set<Pair> allPairs(Set<String> developers) {
        var pairs = new HashSet<Pair>();
        Sets.combinations(developers, 2).stream()
                .map(ArrayList::new)
                .peek(Collections::sort)
                .map(list -> new Pair(list.get(0), list.get(1)))
                .forEach(pairs::add);
        developers.forEach(dev -> pairs.add(new Pair(dev)));

        return pairs;
    }

    public static List<PairCount> countPairs(Set<String> developers, List<Pairing> pairings) {
        var pairCounts = new ArrayList<PairCount>();

        Set<Pair> allPairs = PairUtils.allPairs(developers);

        developers.stream().sorted()
                .forEach(dev -> allPairs.stream()
                        .filter(pair -> pair.first().equals(dev))
                        .sorted(comparing(Pair::first)
                                .thenComparing(Pair::second, nullsFirst(naturalOrder())))
                        .forEach(pair -> {
                            long count = pairings.stream()
                                    .filter(pairing -> pair.equivalentTo(pairing.pair()))
                                    .count();

                            pairCounts.add(new PairCount(pair, (int) count, false));
                        }));
        return pairCounts;
    }
}
