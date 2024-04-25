package dev.coldhands.pair.stairs.backend.infrastructure.web.dto;

import java.util.List;

public record PairStreamDto(List<String> developers, String stream) {
}
