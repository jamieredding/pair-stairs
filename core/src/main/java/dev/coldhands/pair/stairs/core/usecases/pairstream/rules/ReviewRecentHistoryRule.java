package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.BasicScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamStatisticsService;

public class ReviewRecentHistoryRule implements ScoringRule<PairStreamCombination> {

    private final PairStreamStatisticsService statisticsService;

    public ReviewRecentHistoryRule(PairStreamStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    public ScoreResult score(PairStreamCombination combination) {
        final int score = Math.toIntExact(combination.pairs().stream()
                .filter(pair -> statisticsService.getRecentOccurrencesOfDeveloperPair(pair.developers()) == 0 ||
                        pair.developers().stream().anyMatch(dev -> statisticsService.getRecentOccurrenceOfDeveloperInStream(dev, pair.stream()) == 0))
                .count());

        return new BasicScoreResult(- score);
    }

}
