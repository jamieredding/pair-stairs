package dev.coldhands.pair.stairs;

import com.google.common.collect.Sets;
import com.jakewharton.picnic.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.joining;

class Simulation {

    @Test
    @Disabled
    void name() throws Exception {
        List<String> allDevelopers = List.of("d-dev", "c-dev", "e-dev", "a-dev", "b-dev");
        List<Pairing> pairings = new ArrayList<>();
        Set<Pair> previousPairs = Set.of();
        Random random = new Random();


        int daysToSimulate = (int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusYears(10));

        for (int dayNumber = 0; dayNumber < daysToSimulate; dayNumber++) {
            Set<String> availableDevelopers = pickAvailable(random, allDevelopers);

            DecideOMatic decideOMatic = new DecideOMatic(pairings, availableDevelopers, Set.of());
//            DecideOMatic decideOMatic = new DecideOMatic(pairings, availableDevelopers, Set.of("e-dev"));

            List<ScoredPairCombination> scoredPairCombinations = decideOMatic.getScoredPairCombinations();
            Map<Pair, Integer> allPairsAndTheirScore = decideOMatic.getAllPairsAndTheirScore();

            Set<Pair> nextPairs = scoredPairCombinations.stream()
                    .map(ScoredPairCombination::pairCombination)
                    .findFirst().get();

            addToPairings(pairings, dayNumber, nextPairs);
//            printData(new HashSet<>(allDevelopers), pairings, nextPairs, scoredPairCombinations, previousPairs, allPairsAndTheirScore);
            previousPairs = nextPairs;

            if (dayNumber == daysToSimulate - 1) {
                printData(new HashSet<>(allDevelopers), pairings, nextPairs, scoredPairCombinations, Set.of(), allPairsAndTheirScore);
            }
        }

        calculatePercentOccurrenceOfEachPair(allDevelopers, pairings, daysToSimulate);
    }

    private void calculatePercentOccurrenceOfEachPair(List<String> allDevelopers, List<Pairing> pairings, int daysToSimulate) {
        PairUtils.calculatePairStats(new HashSet<>(allDevelopers), pairings)
                .stream()
                .map(pc -> "%s -> %s -> %s".formatted(pc.pair(), pc.count(), ((double) pc.count()) / daysToSimulate))
                .forEach(System.out::println);

        System.out.println(PairUtils.calculatePairStats(new HashSet<>(allDevelopers), pairings)
                .stream()
                .filter(pairStats -> pairStats.pair().second() != null)
                .map(pc -> ((double) pc.count()) / daysToSimulate)
                .map(d -> d.toString().substring(0, 4))
                .collect(joining(",")));
    }

    private void addToPairings(List<Pairing> pairings, int dayNumber, Set<Pair> nextPairs) {
        nextPairs.stream()
                .map(pair -> new Pairing(LocalDate.now().plusDays(dayNumber), pair))
                .forEach(pairings::add);
    }

    private Set<String> pickAvailable(Random random, List<String> allDevelopers) {
        ArrayList<String> devs = new ArrayList<>(allDevelopers);
        int i = random.nextInt(10);
        if (i < allDevelopers.size()) {
            String absent = devs.remove(i);
            System.out.println("absent: " + absent);
        }
        return new HashSet<>(devs);
    }

    private void printData(Set<String> allDevelopers, List<Pairing> pairings, Set<Pair> nextPairs, List<ScoredPairCombination> scoredPairCombinations, Set<Pair> previousPairs, Map<Pair, Integer> allPairsAndTheirScore) {
        printPairs(nextPairs, previousPairs);
        System.out.print("\n--------------\n\n");
        scoredPairCombinations.stream()
                .limit(3)
                .forEach(spc -> System.out.println(spc.scoreBreakdown(allPairsAndTheirScore)));
        System.out.print("\n--------------\n\n");
        printPairStairs(allDevelopers, pairings);
        System.out.print("\n--------------\n--------------\n");
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

}
