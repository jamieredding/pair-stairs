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
        var pair = toScore.pair();
        int score = 0;

        if (pairIsASoloNewJoiner(pair, newJoiners)) {
            score += 100000;
        }

        if (toScore.wasRecent()) {
            score += 10000;
        }

        if (pairIsASoloNonNewJoiner(pair, newJoiners)) {
            score += 100;
        }

        //compare count
        score += toScore.count();

        return score;
    }

    private static boolean pairIsASoloNewJoiner(Pair pair, Set<String> newJoiners) {
        return pair.second() == null &&
               newJoiners.contains(pair.first());
    }

    private static boolean pairIsASoloNonNewJoiner(Pair pair, Set<String> newJoiners) {
        return pair.second() == null &&
               !newJoiners.contains(pair.first());
    }
}
