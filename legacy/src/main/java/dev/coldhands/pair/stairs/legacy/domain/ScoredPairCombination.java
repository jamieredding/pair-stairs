package dev.coldhands.pair.stairs.legacy.domain;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record ScoredPairCombination(Set<Pair> pairCombination, int score) {

    public String scoreBreakdown(Map<Pair, Integer> allPairsAndTheirScore) {
        var pairs = pairCombination.stream()
                .map(pair -> STR."\{pair.members().stream()
                        .collect(Collectors.joining(",", "[", "]"))}=\{allPairsAndTheirScore.get(pair)}"
                )
                .collect(Collectors.joining("|"));
        return STR."\{pairs}|total=\{score}";
    }
}
