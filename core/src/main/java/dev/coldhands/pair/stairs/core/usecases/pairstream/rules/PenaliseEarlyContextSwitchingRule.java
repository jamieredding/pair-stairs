package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.*;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;

import java.util.List;
import java.util.Optional;

public class PenaliseEarlyContextSwitchingRule implements ScoringRule<PairStream> {

    private final CombinationHistoryRepository<PairStream> combinationHistoryRepository;
    private final int minimumDaysInStream = 2;

    public PenaliseEarlyContextSwitchingRule(CombinationHistoryRepository<PairStream> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(Combination<PairStream> combination) {
        final List<Combination<PairStream>> allCombinations = combinationHistoryRepository.getMostRecentCombinations(minimumDaysInStream);

        if (allCombinations.isEmpty()) {
            return new BasicScoreResult(0);
        }

        final var totalDevelopersSwitchingEarly = combination.pairs().stream() // todo is this the simplest way of doing this?
                .mapToInt(pair -> {
                    final var newStream = pair.stream();

                    final var devsInPairThatAreSwitchingEarly = pair.developers().stream()
                            .mapToInt(developer -> {
                                final Combination<PairStream> mostRecentCombination = allCombinations.getFirst();

                                final Optional<String> maybeOldStream = previousStreamDeveloperWasIn(developer, mostRecentCombination);

                                if (maybeOldStream.isEmpty()) {
                                    return 0;
                                }

                                final String oldStream = maybeOldStream.get();

                                if (oldStream.equals(newStream)) {
                                    return 0;
                                }

                                final int daysInOldStream = howManyDaysInOldStream(developer, oldStream, allCombinations);

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

    private int howManyDaysInOldStream(String developer, String oldStream, List<Combination<PairStream>> allCombinations) {
        return Math.toIntExact(allCombinations.stream()
                .map(Combination::pairs)
                .takeWhile(pairs ->
                        pairs.stream()
                                .filter(pair -> pair.developers().contains(developer))
                                .anyMatch(pair -> pair.stream().equals(oldStream))
                )
                .count());
    }

    private Optional<String> previousStreamDeveloperWasIn(String developer, Combination<PairStream> combination) {
        return combination.pairs().stream()
                .filter(pair -> pair.developers().contains(developer))
                .map(PairStream::stream)
                .findFirst();
    }
}
