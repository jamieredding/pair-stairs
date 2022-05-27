package dev.coldhands.pair.stairs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.coldhands.pair.stairs.PairStatsComparator.score;
import static dev.coldhands.pair.stairs.PairUtils.scorePairCombinationUsing;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

public class DecideOMatic {
    private static final Logger LOGGER = LoggerFactory.getLogger(DecideOMatic.class);

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
        List<PairStats> allPairStats = PairUtils.calculatePairStats(availableDevelopers, pairings);
        LocalDate mostRecentDate = PairUtils.mostRecentDate(allPairStats).orElse(null);

        var allPairsAndTheirScore = allPairStats
                .stream()
                .collect(toMap(PairStats::pair, pairStats -> score(pairStats, newJoiners, mostRecentDate)));

        if (LOGGER.isDebugEnabled()) {
            log(allPairsAndTheirScore);
        }

        return allPairsAndTheirScore;
    }

    private void log(Map<Pair, Integer> allPairsAndTheirScore) {
        var output = allPairsAndTheirScore.entrySet().stream()
                .sorted(pairAndScoreComparator())
                .map(entry -> format(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
        LOGGER.debug("Pairs and their score:\n\n"+ output + "\n");
    }

    private Comparator<? super Map.Entry<Pair, Integer>> pairAndScoreComparator() {
        Comparator<Map.Entry<Pair, Integer>> comparing = Map.Entry.comparingByValue();
        return comparing.thenComparing(entry -> entry.getKey().first());
    }

    private String format(Pair pair, Integer score) {
        return String.join(" ", pair.members()) + " -> " + score;
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
