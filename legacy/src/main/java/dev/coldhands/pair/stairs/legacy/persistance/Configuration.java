package dev.coldhands.pair.stairs.legacy.persistance;

import dev.coldhands.pair.stairs.legacy.domain.Pairing;

import java.util.List;

public record Configuration(List<String> allDevelopers, List<String> newJoiners, List<Pairing> pairings) {

    public Configuration(List<String> allDevelopers, List<Pairing> pairings) {
        this(allDevelopers, List.of(), pairings);
    }
}
