package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.*;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;

import java.util.List;
import java.util.Optional;

public class PenaliseEarlyContextSwitchingRule implements ScoringRule<Pair> {

    private final CombinationHistoryRepository<Pair> combinationHistoryRepository;
    private final int minimumDaysInStream = 2;

    public PenaliseEarlyContextSwitchingRule(CombinationHistoryRepository<Pair> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(Combination<Pair> combination) {
        final List<Combination<Pair>> allCombinations = combinationHistoryRepository.getMostRecentCombinations(minimumDaysInStream);

        if (allCombinations.isEmpty()) {
            return new BasicScoreResult(0);
        }

        final var totalDevelopersSwitchingEarly = combination.pairs().stream() // todo is this the simplest way of doing this?
                .mapToInt(pair -> {
                    final var newStream = pair.stream();

                    final var devsInPairThatAreSwitchingEarly = pair.developers().stream()
                            .mapToInt(developer -> {
                                final Combination<Pair> mostRecentCombination = allCombinations.getFirst();

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

    private int howManyDaysInOldStream(String developer, String oldStream, List<Combination<Pair>> allCombinations) {
        return Math.toIntExact(allCombinations.stream()
                .map(Combination::pairs)
                .takeWhile(pairs ->
                        pairs.stream()
                                .filter(pair -> pair.developers().contains(developer))
                                .anyMatch(pair -> pair.stream().equals(oldStream))
                )
                .count());
    }

    private Optional<String> previousStreamDeveloperWasIn(String developer, Combination<Pair> combination) {
        return combination.pairs().stream()
                .filter(pair -> pair.developers().contains(developer))
                .map(Pair::stream)
                .findFirst();
    }
}
