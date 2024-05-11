package dev.coldhands.pair.stairs.core.domain;

import java.util.Set;

public record Combination<T>(Set<T> pairs) {

    @Override
    public int hashCode() {
        return pairs.stream()
                .map(Object::hashCode)
                .sorted()
                .toList()
                .hashCode();
    }
}
