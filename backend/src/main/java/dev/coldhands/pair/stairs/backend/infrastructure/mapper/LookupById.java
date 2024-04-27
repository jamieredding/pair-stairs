package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

@FunctionalInterface
public interface LookupById<T> {

    T lookup(long id);
}
