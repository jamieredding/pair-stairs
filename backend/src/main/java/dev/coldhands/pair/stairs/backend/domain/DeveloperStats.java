package dev.coldhands.pair.stairs.backend.domain;

import java.util.List;

public record DeveloperStats(List<RelatedDeveloperStats> developerStats,
                             List<RelatedStreamStats> streamStats) {
}
