package dev.coldhands.pair.stairs.core.usecases.pair;

import com.google.common.collect.Sets;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.*;

public class PairCombinationService implements CombinationService<Set<String>> {

    private final Set<String> developers;

    public PairCombinationService(Collection<String> developers) {
        this.developers = new HashSet<>(developers);
    }

    @Override
    public Set<Combination<Set<String>>> getAllCombinations() {
        if (developers.size() == 1) {
            final var singleDeveloper = developers.stream().findFirst().get();
            return Set.of(
                    new Combination<>(Set.of(Set.of(singleDeveloper)))
            );
        }

        final int requiredNumberOfPairsPerCombination = Math.ceilDiv(developers.size(), 2);

        final Set<JustDevs> allPossiblePairsOfDevelopers = Sets.combinations(developers, 2).stream()
                .map(JustDevs::new)
                .collect(toCollection(HashSet::new));

        developers.forEach(dev -> allPossiblePairsOfDevelopers.add(new JustDevs(Set.of(dev))));

        return Sets.combinations(allPossiblePairsOfDevelopers, requiredNumberOfPairsPerCombination).stream()
                .filter(combinationHasAllDevelopers(developers))
                .map(combo -> combo.stream()
                        .map(devs -> devs.members)
                        .collect(collectingAndThen(toSet(), Combination::new)))
                .collect(toSet());
    }

    private Predicate<? super Set<JustDevs>> combinationHasAllDevelopers(Set<String> developers) {
        return pairs -> {
            final List<String> devsFromAllPairs = pairs.stream()
                    .flatMap(pair -> pair.members().stream())
                    .toList();
            return devsFromAllPairs.size() == developers.size() &&
                    new HashSet<>(devsFromAllPairs).equals(developers);
        };
    }

    private record JustDevs(Set<String> members) {

    }
}
