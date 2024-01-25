package dev.coldhands.pair.stairs.logic;

import dev.coldhands.pair.stairs.domain.Pairing;
import dev.coldhands.pair.stairs.domain.ScoredPairCombination;

import java.util.HashSet;
import java.util.List;

public class EntryPoint {

    private final List<Pairing> historicalPairings;
    private final List<String> availableDevelopers;
    private final List<String> newJoiners;

    public EntryPoint(List<Pairing> historicalPairings, List<String> availableDevelopers, List<String> newJoiners) {
        this.historicalPairings = historicalPairings;
        this.availableDevelopers = availableDevelopers;
        this.newJoiners = newJoiners;
    }

    public List<ScoredPairCombination> computeScoredPairCombinations() {
        DecideOMatic decideOMatic = new DecideOMatic(historicalPairings, new HashSet<>(availableDevelopers), new HashSet<>(newJoiners));
        return decideOMatic.getScoredPairCombinations();
    }
}
