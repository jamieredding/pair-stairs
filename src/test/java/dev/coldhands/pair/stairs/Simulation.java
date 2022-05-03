package dev.coldhands.pair.stairs;

import com.google.common.collect.Sets;
import com.jakewharton.picnic.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class Simulation {

    @Test
    @Disabled
    void name() throws Exception {
        Set<String> allDevelopers = Set.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");
        List<Pairing> pairings = new ArrayList<>();
        Set<Pair> previousPairs = Set.of();
        for (int dayNumber = 0; dayNumber < 100; dayNumber++) {
            Set<Pair> nextPairs = simulateDay(dayNumber, allDevelopers, pairings);
            printData(allDevelopers, pairings, nextPairs, previousPairs);
            previousPairs = nextPairs;

            Thread.sleep(Duration.ofMillis(300).toMillis());
        }
    }

    private void printData(Set<String> allDevelopers, List<Pairing> pairings, Set<Pair> nextPairs, Set<Pair> previousPairs) {
        printPairs(nextPairs, previousPairs);
        System.out.print("\n--------------\n\n");
        printPairStairs(allDevelopers, pairings);

    }

    private void printPairStairs(Set<String> allDevelopers, List<Pairing> pairings) {
        System.out.println(PairPrinter.drawPairStairs(allDevelopers, pairings));
    }

    private void printPairs(Set<Pair> nextPairs, Set<Pair> previousPairs) {
        Table.Builder table = new Table.Builder();
        table.setCellStyle(new CellStyle.Builder()
                .setAlignment(TextAlignment.MiddleCenter)
                .setPaddingLeft(1)
                .setPaddingRight(1)
                .build());

        table.setHeader(new TableSection.Builder()
                .addRow("first", "second")
                .build());
        TableSection.Builder body = new TableSection.Builder();
        nextPairs.forEach(pair -> {
            Row.Builder builder = new Row.Builder()
                    .addCell(new Cell.Builder(pair.first())
                            .setColumnSpan(pair.second() == null ? 2 : 1)
                            .build());
            if (pair.second() != null) {
                builder.addCell(pair.second());
            }

            body.addRow(builder.build());
        });
        table.setBody(body.build());

        System.out.print("Pairs:");
        Sets.SetView<Pair> intersection = Sets.intersection(nextPairs, previousPairs);
        if (!intersection.isEmpty()) {
            throw new RuntimeException(" duplicate pair!: " + intersection);
        }
        System.out.println("\n" + table.build());
    }

    private Set<Pair> simulateDay(int dayIndex, Set<String> allDevelopers, List<Pairing> pairings) {
        DecideOMatic decideOMatic = new DecideOMatic(pairings, allDevelopers);
        Set<Pair> nextPairs = decideOMatic.getNextPairs();
        nextPairs.stream()
                .map(pair -> new Pairing(LocalDate.now().plusDays(dayIndex), pair))
                .forEach(pairings::add);
        return nextPairs;
    }
}
