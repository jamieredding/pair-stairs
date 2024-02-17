package dev.coldhands.pair.stairs.legacy.logic;

import dev.coldhands.pair.stairs.legacy.domain.Pair;
import dev.coldhands.pair.stairs.legacy.domain.PairStats;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public class PairStatsScorer {

    public static int score(PairStats toScore, Set<String> newJoiners, LocalDate mostRecentDate) {
        var pair = toScore.pair();
        int score = 0;

        if (pairIsASoloNewJoiner(pair, newJoiners)) {
            score += 100000;
        }

        if (pairIsTwoNewJoiners(pair, newJoiners)) {
            score += 50000;
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

    private static long getNumberOfDaysSinceMostRecentDate(PairStats toScore, LocalDate mostRecentDate) {
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

    private static boolean mostRecentOccurrenceIsMostRecentDate(PairStats toScore, LocalDate mostRecentDate) {
        return toScore.getLastPairingDate()
                .filter(date -> date.equals(mostRecentDate))
                .isPresent();
    }

    private static boolean pairIsASoloNewJoiner(Pair pair, Set<String> newJoiners) {
        return pair.second() == null &&
                newJoiners.contains(pair.first());
    }

    private static boolean pairIsTwoNewJoiners(Pair pair, Set<String> newJoiners) {
        return pair.members().size() == 2 &&
                newJoiners.contains(pair.first()) &&
                newJoiners.contains(pair.second());
    }

    private static boolean pairIsASoloNonNewJoiner(Pair pair, Set<String> newJoiners) {
        return pair.second() == null &&
                !newJoiners.contains(pair.first());
    }
}
