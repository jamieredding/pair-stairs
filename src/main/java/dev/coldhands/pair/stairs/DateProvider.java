package dev.coldhands.pair.stairs;

import java.time.LocalDate;

@FunctionalInterface
interface DateProvider {

    LocalDate now();
}
