package dev.coldhands.pair.stairs.logic.legacy;

import dev.coldhands.pair.stairs.PairUtils;
import dev.coldhands.pair.stairs.domain.PairCombination;
import dev.coldhands.pair.stairs.logic.PairCombinationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class LegacyPairCombinationService implements PairCombinationService {
    private final List<String> availableDevelopers;

    public LegacyPairCombinationService(List<String> availableDevelopers) {
        this.availableDevelopers = availableDevelopers;
    }

    @Override
    public Set<PairCombination> getAllPairCombinations() {
        return PairUtils.calculateAllPairCombinations(new HashSet<>(availableDevelopers)).stream()
                .map(PairCombination::new)
                .collect(toSet());
    }
}
