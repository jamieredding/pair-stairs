package dev.coldhands.pair.stairs;

import java.util.Objects;
import java.util.Set;

record Pair(String first, String second) {

    public Pair(String solo) {
        this(solo, null);
    }

    boolean contains(String dev) {
        return dev.equals(first) || dev.equals(second);
    }

    boolean equivalentTo(Pair pair) {
        return (Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second))
                ||
               (Objects.equals(first, pair.second) &&
                Objects.equals(second, pair.first));
    }

    boolean canBeMadeFrom(Set<String> outstandingDevelopers) {
        return outstandingDevelopers.contains(first) &&
                (second == null || outstandingDevelopers.contains(second));
    }

    // todo add members method or something that prevents duplicate safety checking of first and second
}
