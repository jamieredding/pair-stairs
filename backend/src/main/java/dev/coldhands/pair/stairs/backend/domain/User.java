package dev.coldhands.pair.stairs.backend.domain;

public record User(long id,
                   String oidcSub,
                   String displayName) {
}
