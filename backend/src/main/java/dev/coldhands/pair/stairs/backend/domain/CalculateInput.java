package dev.coldhands.pair.stairs.backend.domain;

import java.util.List;

public record CalculateInput(List<Developer> developers, List<Stream> streams) {
}
