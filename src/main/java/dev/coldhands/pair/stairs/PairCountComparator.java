package dev.coldhands.pair.stairs;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Set;

class PairCountComparator implements Comparator<PairCount> {
    private final Set<String> newJoiners;
    private final LocalDate mostRecentDate;

    public PairCountComparator(Set<String> newJoiners, LocalDate mostRecentDate) {
        this.newJoiners = newJoiners;
        this.mostRecentDate = mostRecentDate;
    }

    @Override
    public int compare(PairCount o1, PairCount o2) {
        return Comparator.<PairCount, Integer>comparing(pairCount -> score(pairCount, newJoiners, mostRecentDate)).compare(o1, o2);
    }

    public static int score(PairCount toScore, Set<String> newJoiners, LocalDate mostRecentDate) {
        var pair = toScore.pair();
        int score = 0;

        if (pairIsASoloNewJoiner(pair, newJoiners)) {
            score += 100000;
        }

        if (mostRecentOccurrenceIsMostRecentDate(toScore, mostRecentDate)) {
            score += 10000;
        }

        if (pairIsASoloNonNewJoiner(pair, newJoiners)) {
            score += 100;
        }

        if (pair.members().size() == 2) {
            score -= getNumberOfDaysSinceMostRecentDate(toScore, mostRecentDate);
        }

        return score;
    }

    private static long getNumberOfDaysSinceMostRecentDate(PairCount toScore, LocalDate mostRecentDate) {
        return toScore.getLastPairingDate().stream()
                .mapToLong(lastPairingDate ->
                        ChronoUnit.DAYS.between(lastPairingDate, mostRecentDate))
                .findFirst()
                /*
                This or else value only applies when a pair has never happened.

                The specific value just has to be larger than any possible value that would be
                calculated between the lastPairingDate and mostRecentDate.

                This is to ensure that each pair will be picked at least once before going
                into the normal rotation of being picked.

                99 was chosen as it seems unlikely that a pair would naturally not pair for 100 days.
                 */
                .orElse(99);
    }

    private static boolean mostRecentOccurrenceIsMostRecentDate(PairCount toScore, LocalDate mostRecentDate) {
        return toScore.getLastPairingDate()
                .filter(date -> date.equals(mostRecentDate))
                .isPresent();
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
