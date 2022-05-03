package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.Pairing;

import java.util.List;

record PossibleScoredPairings(List<Pairing> pairings, int score) {
}
