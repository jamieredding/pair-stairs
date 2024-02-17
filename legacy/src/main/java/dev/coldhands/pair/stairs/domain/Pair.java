package dev.coldhands.pair.stairs.domain;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Pair(String first, String second) {

    public Pair(String solo) {
        this(solo, null);
    }

    public boolean contains(String dev) {
        return dev.equals(first) || dev.equals(second);
    }

    public boolean equivalentTo(Pair pair) {
        return (Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second))
               ||
               (Objects.equals(first, pair.second) &&
                Objects.equals(second, pair.first));
    }

    public boolean canBeMadeFrom(Set<String> outstandingDevelopers) {
        return outstandingDevelopers.contains(first) &&
               (second == null || outstandingDevelopers.contains(second));
    }

    public List<String> members() {
        if (second == null) {
            return List.of(first);
        } else {
            return List.of(first, second);
        }
    }
}
