package dev.coldhands.pair.stairs;

import com.google.common.collect.Sets;

import java.time.LocalDate;
import java.util.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

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
        LocalDate mostRecentPair = pairings.stream()
                .map(Pairing::date)
                .sorted(reverseOrder())
                .findFirst()
                .orElse(null);

        developers.stream().sorted()
                .forEach(dev -> allPairs.stream()
                        .filter(pair -> pair.first().equals(dev))
                        .sorted(comparing(Pair::first)
                                .thenComparing(Pair::second, nullsFirst(naturalOrder())))
                        .forEach(pair -> {
                            Collect result = pairings.stream()
                                    .filter(pairing -> pair.equivalentTo(pairing.pair()))
                                    .collect(teeing(
                                            counting(),
                                            maxBy(comparing(Pairing::date)),
                                            (a, b) -> new Collect(a, b.map(Pairing::date).orElse(null))));

                            pairCounts.add(new PairCount(pair, (int) result.count, Objects.equals(mostRecentPair, result.mostRecentOccurrence)));
                        }));
        return pairCounts;
    }

    private record Collect(long count, LocalDate mostRecentOccurrence) {
    }
}
