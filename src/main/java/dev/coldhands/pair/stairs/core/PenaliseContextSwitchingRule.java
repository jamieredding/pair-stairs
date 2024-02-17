package dev.coldhands.pair.stairs.core;

import java.util.SortedSet;

public class PenaliseContextSwitchingRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository;

    public PenaliseContextSwitchingRule(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(PairStreamCombination pairStreamCombination) {
        final SortedSet<CombinationEvent<PairStreamCombination>> allEvents = combinationHistoryRepository.getAllEvents();

        final var totalDaysDevelopersAreInSameStream = pairStreamCombination.pairs().stream()
                .mapToInt(pair -> {
                    final var newStream = pair.stream();

                    final var totalDaysEachDeveloperHasBeenInCurrentStream = pair.developers().stream()
                            .mapToInt(developer -> {
                                final long longCount = allEvents.reversed().stream()
                                        .takeWhile(event -> developerIsInSameStream(developer, newStream, event.combination()))
                                        .count();

                                final var consecutiveDaysInNewStream = Math.toIntExact(longCount);
                                return consecutiveDaysInNewStream == 1 // todo explanation: if only one day then don't count it, not sure if this is a good idea
                                        ? 0
                                        : consecutiveDaysInNewStream;
                            })
                            .sum();

                    return totalDaysEachDeveloperHasBeenInCurrentStream;
                })
                .sum();

        return new BasicScoreResult(totalDaysDevelopersAreInSameStream);
    }

    private boolean developerIsInSameStream(String developer, String streamToCheck, PairStreamCombination combination) {
        return combination.pairs().stream()
                .filter(pair -> pair.stream().equals(streamToCheck))
                .anyMatch(pair -> pair.developers().contains(developer));
    }
}
