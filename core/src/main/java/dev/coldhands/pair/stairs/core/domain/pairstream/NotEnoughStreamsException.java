package dev.coldhands.pair.stairs.core.domain.pairstream;

public class NotEnoughStreamsException extends IllegalArgumentException {
    public NotEnoughStreamsException(int requiredNumberOfStreamsPerCombination, int actualNumberOfStreamsPerCombination) {
        super("Not enough streams for developers. Requires [%s] streams but only [%s] available.".formatted(requiredNumberOfStreamsPerCombination, actualNumberOfStreamsPerCombination));
    }
}
