package dev.coldhands.pair.stairs;

import java.util.Comparator;
import java.util.Set;

class PairCountComparator implements Comparator<PairCount> {
    private final Set<String> newJoiners;

    public PairCountComparator(Set<String> newJoiners) {
        this.newJoiners = newJoiners;
    }

    @Override
    public int compare(PairCount o1, PairCount o2) {
        return Comparator.<PairCount, Integer>comparing(pairCount -> score(pairCount, newJoiners)).compare(o1, o2);
    }

    public static int score(PairCount toScore, Set<String> newJoiners) {
        int score = 0;

        if (toScore.pair().second() == null) {
            // solo new joiners last
            if (newJoiners.contains(toScore.pair().first())) {
                score += 100000;
            } else {
                // solo pairs last
                score += 100;
            }
        }
        // recent pairs last
        if (toScore.wasRecent()) {
            score += 10000;
        }
        //compare count
        score += toScore.count();

        return score;
    }
}
