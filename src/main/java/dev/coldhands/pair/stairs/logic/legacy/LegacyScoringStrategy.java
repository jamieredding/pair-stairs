package dev.coldhands.pair.stairs.logic.legacy;

import dev.coldhands.pair.stairs.PairUtils;
import dev.coldhands.pair.stairs.domain.*;
import dev.coldhands.pair.stairs.logic.DecideOMatic;
import dev.coldhands.pair.stairs.logic.PairStatsScorer;
import dev.coldhands.pair.stairs.logic.ScoringStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.coldhands.pair.stairs.PairUtils.scorePairCombinationUsing;
import static java.util.stream.Collectors.toMap;

public class LegacyScoringStrategy implements ScoringStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecideOMatic.class);
    private final Map<Pair, Integer> allPairsAndTheirScore;

    public LegacyScoringStrategy(List<String> availableDevelopers, List<Pairing> pairings, List<String> newJoiners) {
        allPairsAndTheirScore = getAllPairsAndTheirScore(new HashSet<>(availableDevelopers), pairings, new HashSet<>(newJoiners));
    }

    @Override
    public ScoredPairCombination score(PairCombination pairCombination) {
        return toScoredPairCombination(allPairsAndTheirScore)
                .apply(pairCombination.pairs()); // todo refactor me
    }

    public static Map<Pair, Integer> getAllPairsAndTheirScore(Set<String> availableDevelopers, List<Pairing> pairings, Set<String> newJoiners) {
        List<PairStats> allPairStats = PairUtils.calculatePairStats(availableDevelopers, pairings);
        LocalDate mostRecentDate = PairUtils.mostRecentDate(allPairStats).orElse(null);

        var allPairsAndTheirScore = allPairStats
                .stream()
                .collect(toMap(PairStats::pair, pairStats -> PairStatsScorer.score(pairStats, newJoiners, mostRecentDate)));

        if (LOGGER.isDebugEnabled()) {
            log(allPairsAndTheirScore);
        }

        return allPairsAndTheirScore;
    }

    private static void log(Map<Pair, Integer> allPairsAndTheirScore) {
        var output = allPairsAndTheirScore.entrySet().stream()
                .sorted(pairAndScoreComparator())
                .map(entry -> format(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
        LOGGER.debug(STR."Pairs and their score:\n\n\{output}\n");
    }

    private static Comparator<? super Map.Entry<Pair, Integer>> pairAndScoreComparator() {
        Comparator<Map.Entry<Pair, Integer>> comparing = Map.Entry.comparingByValue();
        return comparing.thenComparing(entry -> entry.getKey().first());
    }

    private static String format(Pair pair, Integer score) {
        return STR."\{String.join(" ", pair.members())} -> \{score}";
    }

    private Function<Set<Pair>, ScoredPairCombination> toScoredPairCombination(Map<Pair, Integer> allPairsAndTheirScore) {
        return pairCombination -> new ScoredPairCombination(pairCombination,
                scorePairCombinationUsing(allPairsAndTheirScore).score(pairCombination));
    }
}
