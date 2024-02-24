package dev.coldhands.pair.stairs.core.usecases.pairstream.rules;

import dev.coldhands.pair.stairs.core.domain.BasicScoreResult;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.ScoreResult;
import dev.coldhands.pair.stairs.core.domain.ScoringRule;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;

import java.util.List;
import java.util.Optional;

public class PenaliseEarlyContextSwitchingRule implements ScoringRule<PairStreamCombination> {

    private final CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository;
    private final int minimumDaysInStream = 2;

    public PenaliseEarlyContextSwitchingRule(CombinationHistoryRepository<PairStreamCombination> combinationHistoryRepository) {
        this.combinationHistoryRepository = combinationHistoryRepository;
    }

    @Override
    public ScoreResult score(PairStreamCombination pairStreamCombination) {
        final List<PairStreamCombination> allCombinations = combinationHistoryRepository.getMostRecentCombinations(minimumDaysInStream);

        if (allCombinations.isEmpty()) {
            return new BasicScoreResult(0);
        }

        final var totalDevelopersSwitchingEarly = pairStreamCombination.pairs().stream() // todo is this the simplest way of doing this?
                .mapToInt(pair -> {
                    final var newStream = pair.stream();

                    final var devsInPairThatAreSwitchingEarly = pair.developers().stream()
                            .mapToInt(developer -> {
                                final PairStreamCombination mostRecentCombination = allCombinations.getFirst();

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

    private int howManyDaysInOldStream(String developer, String oldStream, List<PairStreamCombination> allCombinations) {
        return Math.toIntExact(allCombinations.stream()
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
