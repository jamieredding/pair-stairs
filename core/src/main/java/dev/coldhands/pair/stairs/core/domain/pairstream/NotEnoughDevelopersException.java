package dev.coldhands.pair.stairs.core.domain.pairstream;

public class NotEnoughDevelopersException extends IllegalArgumentException {
    public NotEnoughDevelopersException(int requiredNumberOfPairsPerCombination, int actualNumberOfPairsPerCombination) {
        super("Not enough developers to pair on streams. Requires [%s] pairs but only [%s] available.".formatted(requiredNumberOfPairsPerCombination, actualNumberOfPairsPerCombination));
    }
}
