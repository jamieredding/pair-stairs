package dev.coldhands.pair.stairs.backend.domain;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;

public record PairStream(List<DeveloperInfo> developers, StreamInfo stream) implements Comparable<PairStream> {

    public static final Comparator<PairStream> COMPARATOR = comparing(ps -> ps.stream.displayName());

    @Override
    public int compareTo(PairStream o) {
        return COMPARATOR.compare(this, o);
    }
}
