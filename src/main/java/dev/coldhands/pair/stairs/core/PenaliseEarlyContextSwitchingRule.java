package dev.coldhands.pair.stairs.core;

import java.util.Optional;
import java.util.SortedSet;

public class PenaliseEarlyContextSwitchingRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository;
    private final int minimumDaysInStream = 2;

    public PenaliseEarlyContextSwitchingRule(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(PairStreamCombination pairStreamCombination) {
        final SortedSet<CombinationEvent<PairStreamCombination>> allEvents = combinationHistoryRepository.getAllEvents();

        final var totalDevelopersSwitchingEarly = pairStreamCombination.pairs().stream() // todo is this the simplest way of doing this?
                .mapToInt(pair -> {
                    final var newStream = pair.stream();

                    final var devsInPairThatAreSwitchingEarly = pair.developers().stream()
                            .mapToInt(developer -> {
                                final var maybeCombination = combinationHistoryRepository.getMostRecentCombination();
                                if (maybeCombination.isEmpty()) {
                                    return 0;
                                }

                                final PairStreamCombination mostRecentCombination = maybeCombination.get();

                                final Optional<String> maybeOldStream = previousStreamDeveloperWasIn(developer, mostRecentCombination);

                                if (maybeOldStream.isEmpty()) {
                                    return 0;
                                }

                                final String oldStream = maybeOldStream.get();

                                if (oldStream.equals(newStream)) {
                                    return 0;
                                }

                                final int daysInOldStream = howManyDaysInOldStream(developer, oldStream, allEvents);

                                return daysInOldStream < minimumDaysInStream
                                        ? 1
                                        : 0;
                            })
                            .sum();

                    return devsInPairThatAreSwitchingEarly;
                })
                .sum();

        return new BasicScoreResult(totalDevelopersSwitchingEarly);
    }

    private int howManyDaysInOldStream(String developer, String oldStream, SortedSet<CombinationEvent<PairStreamCombination>> allEvents) {
        return Math.toIntExact(allEvents.reversed().stream()
                .map(CombinationEvent::combination)
                .map(PairStreamCombination::pairs)
                .takeWhile(pairs ->
                        pairs.stream()
                                .filter(pair -> pair.developers().contains(developer))
                                .anyMatch(pair -> pair.stream().equals(oldStream))
                )
                .count());
    }

    private Optional<String> previousStreamDeveloperWasIn(String developer, PairStreamCombination combination) {
        return combination.pairs().stream()
                .filter(pair -> pair.developers().contains(developer))
                .map(Pair::stream)
                .findFirst();
    }
}
