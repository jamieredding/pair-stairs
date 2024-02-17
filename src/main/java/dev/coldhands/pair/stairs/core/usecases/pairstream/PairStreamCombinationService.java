package dev.coldhands.pair.stairs.core.usecases.pairstream;

import com.google.common.collect.Sets;
import dev.coldhands.pair.stairs.core.domain.CombinationService;
import dev.coldhands.pair.stairs.core.domain.pairstream.Pair;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toCollection;
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

        final Set<JustDevs> allPossiblePairsOfDevelopers = Sets.combinations(developers, 2).stream()
                .map(JustDevs::new)
                .collect(toCollection(HashSet::new));

        developers.forEach(dev -> allPossiblePairsOfDevelopers.add(new JustDevs(Set.of(dev))));

        final Set<Pair> allPairs = allPossiblePairsOfDevelopers.stream()
                .flatMap(pair -> streams.stream().map(stream -> new Pair(pair.members, stream)))
                .collect(toSet());

        return Sets.combinations(allPairs, requiredNumberOfPairsPerCombination).stream()
                .filter(combinationHasAllDevelopers(developers))
                .filter(combinationHasAllStreams(streams))
                .map(PairStreamCombination::new)
                .collect(toSet());
    }

    private Predicate<? super Set<Pair>> combinationHasAllDevelopers(Set<String> developers) {
        return pairs -> {
            final List<String> devsFromAllPairs = pairs.stream()
                    .flatMap(pair -> pair.developers().stream())
                    .toList();
            return devsFromAllPairs.size() == developers.size() &&
                    new HashSet<>(devsFromAllPairs).equals(developers);
        };
    }

    private Predicate<? super Set<Pair>> combinationHasAllStreams(Set<String> streams) {
        return pairs -> {
            final List<String> allStreams = pairs.stream()
                    .map(Pair::stream)
                    .toList();
            return allStreams.size() == streams.size() &&
                    new HashSet<>(allStreams).equals(streams);
        };
    }

    private record JustDevs(Set<String> members) {

    }
}
