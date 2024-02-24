package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.*;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;

import java.util.Collection;
import java.util.List;

public class EveryPairShouldHappenAfterXDaysRule implements ScoringRule<Pair> {
    private final Collection<String> developers;
    private final CombinationHistoryRepository<Pair> combinationHistoryRepository;

    public EveryPairShouldHappenAfterXDaysRule(Collection<String> developers, CombinationHistoryRepository<Pair> combinationHistoryRepository) {
        this.developers = developers;
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(Combination<Pair> combination) {
        int numberOfDevelopers = developers.size();
        int minimumDays = getMinimumDaysForAllPairs(numberOfDevelopers);

        final List<Combination<Pair>> allCombinations = combinationHistoryRepository.getMostRecentCombinations(minimumDays);
        if (allCombinations.isEmpty()) {
            return new BasicScoreResult(0);
        }

        int totalUniquePairs = getTotalUniquePairs(numberOfDevelopers);

        final long uniquePairCombinations = allCombinations.stream()
                .map(Combination::pairs)
                .flatMap(Collection::stream)
                .map(Pair::developers)
                .distinct()
                .count();

        int score = uniquePairCombinations < totalUniquePairs ? 1 : 0;

        return new BasicScoreResult(score);
    }

    private int getTotalUniquePairs(int numberOfDevelopers) {
        final int numberOfUniquePairsOfTwoDevelopers = (numberOfDevelopers * (numberOfDevelopers - 1)) / 2;
        return numberOfDevelopers % 2 == 0
                ? numberOfUniquePairsOfTwoDevelopers
                : numberOfUniquePairsOfTwoDevelopers + numberOfDevelopers // to include pairs containing solo developers
                ;
    }

    private int getMinimumDaysForAllPairs(int numberOfDevelopers) {
        int pairsPerDay = Math.ceilDiv(numberOfDevelopers, 2);
        int totalPairs = getTotalUniquePairs(numberOfDevelopers);

        return Math.ceilDiv(totalPairs, pairsPerDay);
    }

}
