package dev.coldhands.pair.stairs.backend.infrastructure.web.dto;

import dev.coldhands.pair.stairs.backend.domain.DeveloperId;

import java.util.List;

public record CalculateInputDto(List<DeveloperId> developerIds, List<Long> streamIds) {
}
