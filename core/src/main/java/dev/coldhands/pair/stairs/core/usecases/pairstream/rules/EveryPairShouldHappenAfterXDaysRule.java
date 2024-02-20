package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.BasicScoreResult;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;

import java.util.Collection;
import java.util.List;

public class EveryPairShouldHappenAfterXDaysRule implements ScoringRule<PairStreamCombination> {
    private final Collection<String> developers;
    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository;

    public EveryPairShouldHappenAfterXDaysRule(Collection<String> developers, CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        this.developers = developers;
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(PairStreamCombination combination) {
        final List<PairStreamCombination> allCombinations = combinationHistoryRepository.getAllCombinations();
        if (allCombinations.isEmpty()) {
            return new BasicScoreResult(0);
        }

        int numberOfDevelopers = developers.size();
        int minimumDays = getMinimumDaysForAllPairs(numberOfDevelopers);
        int totalUniquePairs = getTotalUniquePairs(numberOfDevelopers);

        final long uniquePairCombinations = allCombinations.reversed().stream()
                .limit(minimumDays)
                .map(PairStreamCombination::pairs)
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
