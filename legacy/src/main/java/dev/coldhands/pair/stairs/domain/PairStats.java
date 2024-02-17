package dev.coldhands.pair.stairs.domain;

import java.time.LocalDate;
import java.util.Optional;

public record PairStats(Pair pair,
                        int count,
                        LocalDate lastPairingDate) {

    public Optional<LocalDate> getLastPairingDate() {
        return Optional.ofNullable(lastPairingDate);
    }
}
