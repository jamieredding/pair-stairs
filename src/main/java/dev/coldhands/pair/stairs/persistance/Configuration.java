package dev.coldhands.pair.stairs.persistance;

import dev.coldhands.pair.stairs.Pairing;

import java.util.List;

public record Configuration(List<String> allDevelopers, List<Pairing> pairings) {
}
