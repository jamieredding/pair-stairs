package dev.coldhands.pair.stairs;

import java.time.LocalDate;

public record Pairing(LocalDate date, Pair pair) {

    public Pairing(LocalDate date, String first, String second) {
        this(date, new Pair(first, second));
    }

    public Pairing(LocalDate date, String solo) {
        this(date, new Pair(solo, null));
    }
}
