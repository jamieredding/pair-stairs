package dev.coldhands.pair.stairs.backend.domain;

import java.util.Comparator;

// todo make kotlin
public record DeveloperInfo(long id, String displayName, boolean archived) implements Comparable<DeveloperInfo> {

    public static final Comparator<DeveloperInfo> COMPARATOR = Comparator.comparing(DeveloperInfo::displayName);

    @Override
    public int compareTo(DeveloperInfo o) {
        return COMPARATOR.compare(this,o);
    }
}
