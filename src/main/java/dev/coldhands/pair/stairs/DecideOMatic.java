package dev.coldhands.pair.stairs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DecideOMatic {
    private final Set<String> allDevelopers;
    private final List<Pairing> pairings;
    private final Set<String> availableDevelopers;

    public DecideOMatic(Set<String> allDevelopers, List<Pairing> pairings, Set<String> availableDevelopers) {
        this.allDevelopers = allDevelopers;
        this.pairings = pairings;
        this.availableDevelopers = availableDevelopers;
    }

    Set<Pair> getNextPairs() {
        List<PairCount> pairCounts = PairUtils.countPairs(allDevelopers, pairings);
        Set<String> outstandingDevelopers = new HashSet<>(availableDevelopers);
        Set<Pair> nextPairs = new HashSet<>();
        while (!outstandingDevelopers.isEmpty()) {
            Pair nextBestPair = selectBestPair(outstandingDevelopers, pairCounts);
            outstandingDevelopers.remove(nextBestPair.first());
            outstandingDevelopers.remove(nextBestPair.second());

            nextPairs.add(nextBestPair);
        }
        return nextPairs;
    }

    private Pair selectBestPair(Set<String> outstandingDevelopers, List<PairCount> pairCounts) {
        return pairCounts.stream()
                .sorted(new PairCountComparator())
                .map(PairCount::pair)
                .filter(pair -> pair.canBeMadeFrom(outstandingDevelopers))
                .findFirst()
                .get();
    }

}
