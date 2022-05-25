package dev.coldhands.pair.stairs;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static dev.coldhands.pair.stairs.PairCountComparator.score;
import static dev.coldhands.pair.stairs.PairUtils.scorePairCombinationUsing;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

public class DecideOMatic {
    private final List<Pairing> pairings;
    private final Set<String> availableDevelopers;
    private final Set<String> newJoiners;

    public DecideOMatic(List<Pairing> pairings, Set<String> availableDevelopers) {
        this(pairings, availableDevelopers, Set.of());
    }

    public DecideOMatic(List<Pairing> pairings, Set<String> availableDevelopers, Set<String> newJoiners) {
        this.pairings = pairings;
        this.availableDevelopers = availableDevelopers;
        this.newJoiners = newJoiners;
    }

    public List<ScoredPairCombination> getScoredPairCombinations() {
        Set<Set<Pair>> allPairCombinations = PairUtils.calculateAllPairCombinations(availableDevelopers);
        Map<Pair, Integer> allPairsAndTheirScore = getAllPairsAndTheirScore();

        return allPairCombinations.stream()
                .map(toScoredPairCombination(allPairsAndTheirScore))
                .sorted(comparing(ScoredPairCombination::score))
                .toList();
    }

    Map<Pair, Integer> getAllPairsAndTheirScore() {
        return PairUtils.countPairs(availableDevelopers, pairings)
                .stream()
                .collect(toMap(PairCount::pair, pairCount -> score(pairCount, newJoiners)));
    }

    private Function<Set<Pair>, ScoredPairCombination> toScoredPairCombination(Map<Pair, Integer> allPairsAndTheirScore) {
        return pairCombination -> new ScoredPairCombination(pairCombination,
                scorePairCombinationUsing(allPairsAndTheirScore).score(pairCombination));
    }

    public Set<Pair> getNextPairs() {
        return getScoredPairCombinations().stream()
                .map(ScoredPairCombination::pairCombination)
                .findFirst()
                .get();
    }

}
