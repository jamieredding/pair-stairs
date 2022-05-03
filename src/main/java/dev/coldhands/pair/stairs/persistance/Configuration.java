package dev.coldhands.pair.stairs.persistance;

import dev.coldhands.pair.stairs.Pairing;

import java.util.List;

public record Configuration(List<String> allDevelopers, List<String> newJoiners, List<Pairing> pairings) {

    public Configuration(List<String> allDevelopers, List<Pairing> pairings) {
        this(allDevelopers, List.of(), pairings);
    }
}
