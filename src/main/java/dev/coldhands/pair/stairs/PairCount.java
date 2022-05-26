package dev.coldhands.pair.stairs;

import java.time.LocalDate;
import java.util.Optional;

record PairCount(Pair pair,
                 // todo is this required any more
                 int count,
                 LocalDate lastPairingDate) {

    Optional<LocalDate> getLastPairingDate() {
        return Optional.ofNullable(lastPairingDate);
    }
}
