package dev.coldhands.pair.stairs.core.usecases.pairstream;

import com.google.common.collect.Collections2;
import com.google.common.collect.Streams;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.core.domain.pairstream.NotEnoughDevelopersException;
import dev.coldhands.pair.stairs.core.domain.pairstream.NotEnoughStreamsException;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.usecases.pair.PairCombinationService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

public class PairStreamCombinationService implements CombinationService<PairStream> {

    private final Set<String> developers;
    private final Set<String> streams;

    public PairStreamCombinationService(Collection<String> developers, Collection<String> streams) {
        this.developers = new HashSet<>(developers);
        this.streams = new HashSet<>(streams);
    }

    @Override
    public Set<Combination<PairStream>> getAllCombinations() {
        final int actualNumberOfPairsPerCombination = Math.ceilDiv(developers.size(), 2);
        final int requiredNumberOfPairsPerCombination = streams.size();

        if (actualNumberOfPairsPerCombination > requiredNumberOfPairsPerCombination) {
            throw new NotEnoughStreamsException(actualNumberOfPairsPerCombination, requiredNumberOfPairsPerCombination);
        }

        if (actualNumberOfPairsPerCombination < requiredNumberOfPairsPerCombination) {
            throw new NotEnoughDevelopersException(requiredNumberOfPairsPerCombination, actualNumberOfPairsPerCombination);
        }

        if (developers.size() == 1 && requiredNumberOfPairsPerCombination == 1) {
            final var onlyDeveloper = developers.stream().findFirst().get();
            final var onlyStream = streams.stream().findFirst().get();
            return Set.of(
                    new Combination<>(Set.of(new PairStream(Set.of(onlyDeveloper), onlyStream)))
            );
        }

        final PairCombinationService pairCombinationService = new PairCombinationService(developers);
        final Set<Combination<Set<String>>> allCombinationsOfDevelopers = pairCombinationService.getAllCombinations();
        final Collection<List<String>> allPermutationsOfStreams = Collections2.orderedPermutations(streams);

        return allCombinationsOfDevelopers.stream()
                .map(Combination::pairs)
                .mapMulti((Set<Set<String>> combo, Consumer<Combination<PairStream>> consumer) -> {
                    final List<Set<String>> orderedDeveloperPairs = List.copyOf(combo);

                    for (List<String> allPermutationsOfStream : allPermutationsOfStreams) {
                        final Combination<PairStream> combination = Streams.zip(
                                        allPermutationsOfStream.stream(),
                                        orderedDeveloperPairs.stream(),
                                        (stream, developers) -> new PairStream(developers, stream)
                                )
                                .collect(collectingAndThen(toSet(), Combination::new));

                        consumer.accept(combination);
                    }
                })
                .collect(toSet());
    }

}
