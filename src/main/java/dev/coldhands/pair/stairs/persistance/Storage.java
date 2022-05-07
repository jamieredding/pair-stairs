package dev.coldhands.pair.stairs.persistance;

interface Storage {
    void write(Configuration pairings) throws Exception;

    Configuration read() throws Exception;
}
