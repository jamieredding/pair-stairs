package dev.coldhands.pair.stairs;

import java.time.LocalDate;
import java.util.Optional;

record PairStats(Pair pair,
                 int count,
                 LocalDate lastPairingDate) {

    Optional<LocalDate> getLastPairingDate() {
        return Optional.ofNullable(lastPairingDate);
    }
}
