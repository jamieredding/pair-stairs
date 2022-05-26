package dev.coldhands.pair.stairs;

import java.time.LocalDate;
import java.util.Optional;

record PairCount(Pair pair, int count, LocalDate mostRecentOccurrence) {

    Optional<LocalDate> getMostRecentOccurrence() {
        return Optional.ofNullable(mostRecentOccurrence);
    }
}
