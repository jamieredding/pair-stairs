package dev.coldhands.pair.stairs.core.usecases.pairstream;

import com.google.common.collect.Collections2;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;
import dev.coldhands.pair.stairs.core.usecases.pair.PairCombinationService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toSet;

public class PairStreamCombinationService implements CombinationService<PairStreamCombination> {

    private final Set<String> developers;
    private final Set<String> streams;

    public PairStreamCombinationService(Collection<String> developers, Collection<String> streams) {
        this.developers = new HashSet<>(developers);
        this.streams = new HashSet<>(streams);
    }

    @Override
    public Set<PairStreamCombination> getAllCombinations() {
        final int requiredNumberOfPairsPerCombination = Math.ceilDiv(developers.size(), 2);
        if (requiredNumberOfPairsPerCombination > streams.size()) {
            throw new IllegalStateException("Not enough streams for developers");
        }

        if (requiredNumberOfPairsPerCombination < streams.size()) {
            throw new IllegalStateException("Not enough developers to pair on streams");
        }

        final PairCombinationService pairCombinationService = new PairCombinationService(developers);
        final Set<Set<Set<String>>> allCombinationsOfDevelopers = pairCombinationService.getAllCombinations();
        final Collection<List<String>> allPermutationsOfStreams = Collections2.orderedPermutations(streams);

        return allCombinationsOfDevelopers.stream()
                .mapMulti((Set<Set<String>> combo, Consumer<PairStreamCombination> consumer) -> {
                    final List<Set<String>> orderedDeveloperPairs = List.copyOf(combo);

                    for (List<String> allPermutationsOfStream : allPermutationsOfStreams) {
                        final Set<Pair> pairs = new HashSet<>();
                        for (int i = 0; i < allPermutationsOfStream.size(); i++) { // todo replace with zipping
                            String stream = allPermutationsOfStream.get(i);
                            Set<String> developers = orderedDeveloperPairs.get(i);

                            pairs.add(new Pair(developers, stream));
                        }
                        consumer.accept(new PairStreamCombination(Set.copyOf(pairs)));
                    }

                })
                .collect(toSet());
    }

}
