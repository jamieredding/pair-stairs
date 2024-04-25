package dev.coldhands.pair.stairs.backend.infrastructure.web.dto;

import java.util.List;

public record CalculateInputDto(List<Long> developerIds, List<Long> streamIds) {
}
