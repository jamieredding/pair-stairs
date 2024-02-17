package dev.coldhands.pair.stairs.legacy.persistance;

public interface Storage {
    void write(Configuration pairings) throws Exception;

    Configuration read() throws Exception;

    String describe();
}
