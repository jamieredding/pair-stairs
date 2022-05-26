package dev.coldhands.pair.stairs;

import com.google.common.collect.Sets;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

                            pairCounts.add(new PairCount(pair, (int) result.count, result.mostRecentOccurrence));
                        }));
        return pairCounts;
    }

    public static Set<Set<Pair>> calculateAllPairCombinations(Set<String> allDevelopers) {
        Set<Pair> allPossiblePairs = Sets.combinations(allDevelopers, 2).stream()
                .map(ArrayList::new)
                .peek(Collections::sort)
                .map(pairAsList -> new Pair(pairAsList.get(0), pairAsList.get(1)))
                .collect(Collectors.toCollection(HashSet::new));

        allDevelopers.forEach(dev -> allPossiblePairs.add(new Pair(dev)));

        return Sets.combinations(allPossiblePairs, ceilDiv(allDevelopers.size(), 2)).stream()
                .filter(isValidPairCombination(allDevelopers))
                .collect(toSet());
    }

    // FROM JAVA 18
    public static int ceilDiv(int x, int y) {
        final int q = x / y;
        // if the signs are the same and modulo not zero, round up
        if ((x ^ y) >= 0 && (q * y != x)) {
            return q + 1;
        }
        return q;
    }

    private static Predicate<Set<Pair>> isValidPairCombination(Set<String> allDevelopers) {
        return pairCombination -> {
            List<String> allDevsInPairs = pairCombination.stream()
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

    static PairCombinationScorer scorePairCombinationUsing(Map<Pair, Integer> pairsSortedByPairCount) {
        return pairCombination -> pairCombination.stream()
                .mapToInt(pairsSortedByPairCount::get)
                .sum();
    }

    public static Optional<LocalDate> mostRecentDate(List<PairCount> pairCounts) {
        return pairCounts.stream()
                .map(PairCount::lastPairingDate)
                .filter(Objects::nonNull)
                .max(naturalOrder());
    }

    @FunctionalInterface
    interface PairCombinationScorer {

        int score(Set<Pair> pairCombination);
    }

    private record Collect(long count, LocalDate mostRecentOccurrence) {
    }
}
