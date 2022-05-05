package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.persistance.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.coldhands.pair.stairs.PairPrinter.drawPairChoices;
import static dev.coldhands.pair.stairs.PairPrinter.drawPairStairs;
import static dev.coldhands.pair.stairs.cli.State.*;
import static java.util.stream.Collectors.toCollection;

class StateMachine {

    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    private final Context context;

    public StateMachine(BufferedReader in,
                        PrintWriter out,
                        PrintWriter err,
                        Runner runner) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.context = Context.from(runner);
    }

    private State initialOutput() {
        out.println("""
                Yesterday's pair stairs
                                        
                %s
                                
                Options (lowest score is better)
                                
                %s
                """.formatted(
                drawPairStairs(context.allDevelopers, context.startingPairings),
                drawPairChoices(context.scoredPairCombinations, 3)));

        return OFFER_USER_CHOICE;
    }

    private State offerUserChoice() throws IOException {
        out.println("""
                Choose a suggestion [1-3]:
                Or override with your own pairs [o]
                """);
        String selection = in.readLine();
        switch (selection) {
            case "1", "2", "3" -> {
                context.selection = Integer.parseInt(selection);
                return SHOW_SELECTION;
            }
            case "o" -> {
                context.customDevelopersLeftToPick = context.availableDevelopers.stream()
                        .sorted()
                        .collect(toCollection(LinkedList::new));
                return SHOW_NUMBERED_DEVELOPERS_TO_PICK;
            }
            default -> {
                err.println("""
                        Invalid input.
                        """);
                return OFFER_USER_CHOICE;
            }
        }
    }

    private State showNumberedDevelopersToPick() {
        String formattedRemainingDevelopers = IntStream.rangeClosed(1, context.customDevelopersLeftToPick.size())
                .mapToObj(i -> "[%s] %s".formatted(i, context.customDevelopersLeftToPick.get(i - 1)))
                .collect(Collectors.joining("\n"));

        out.println("""
                %s
                                        
                Type two numbers to choose them,
                e.g. '1 2' for '%s' and '%s'
                """.formatted(
                formattedRemainingDevelopers,
                context.customDevelopersLeftToPick.get(0),
                context.customDevelopersLeftToPick.get(1)
        ));
        return PROCESS_INPUT_FOR_PICKING_A_PAIR;
    }

    private State processInputForPickingAPair() throws IOException {
        String userInput = in.readLine();

        Optional<Pair> potentialPair = parsePickedPair(userInput);
        if (potentialPair.isEmpty()) {
            err.println("""
                    Invalid input.
                    """);
            return SHOW_NUMBERED_DEVELOPERS_TO_PICK;
        }
        Pair pickedDevelopers = potentialPair.get();

        context.customDevelopersLeftToPick.removeAll(List.of(pickedDevelopers.first(), pickedDevelopers.second()));
        context.customPickedPairs.add(pickedDevelopers);

        if (context.customDevelopersLeftToPick.size() > 2) {
            out.println("""
                    Remaining:
                    """);
            return SHOW_NUMBERED_DEVELOPERS_TO_PICK;
        } else if (context.customDevelopersLeftToPick.size() == 2) {
            String remainingFirst = context.customDevelopersLeftToPick.remove(0);
            String remainingSecond = context.customDevelopersLeftToPick.remove(0);
            context.customPickedPairs.add(new Pair(remainingFirst, remainingSecond));
            return SHOW_SELECTION;
        } else {
            String remaining = context.customDevelopersLeftToPick.remove(0);
            context.customPickedPairs.add(new Pair(remaining));
            return SHOW_SELECTION;
        }
    }

    private State showSelection() {
        String selectionMessage;
        Set<Pair> selectedPairs;
        if (context.selection == -1) {
            selectionMessage = "custom pairs";
            selectedPairs = context.customPickedPairs;
        } else {
            selectionMessage = String.valueOf(context.selection);
            selectedPairs = context.scoredPairCombinations.get(context.selection - 1).pairCombination();
        }
        context.actualNextPairings = new ArrayList<>(context.startingPairings);
        selectedPairs.forEach(pair -> context.actualNextPairings.add(new Pairing(LocalDate.now(), pair)));

        out.println("""
                Picked %s:
                                        
                %s
                """.formatted(
                selectionMessage,
                drawPairStairs(context.allDevelopers, context.actualNextPairings)
        ));
        return SAVE_DATA_FILE;
    }

    private State saveDataFile() throws IOException {
        context.fileStorage.write(new Configuration(context.allDevelopers.stream().toList(), context.newJoiners, context.actualNextPairings));
        out.println("""
                Saved pairings to: %s
                """.formatted(context.fileStorage.getDataFile().toAbsolutePath().toString()));
        return COMPLETE;
    }

    public void run() throws IOException {
        context.setState(switch (context.getState()) {
            case INITIAL_OUTPUT -> initialOutput();
            case OFFER_USER_CHOICE -> offerUserChoice();
            case SHOW_NUMBERED_DEVELOPERS_TO_PICK -> showNumberedDevelopersToPick();
            case PROCESS_INPUT_FOR_PICKING_A_PAIR -> processInputForPickingAPair();
            case SHOW_SELECTION -> showSelection();
            case SAVE_DATA_FILE -> saveDataFile();
            default -> throw new IllegalStateException("Unexpected value: " + context.getState());
        });
    }

    private Optional<Pair> parsePickedPair(String userInput) {
        Pair pickedDevelopers;
        String[] choices = userInput.split(" ");
        if (choices.length != 2) {
            return Optional.empty();
        }

        try {
            int firstIndex = Integer.parseInt(choices[0]) - 1;
            int secondIndex = Integer.parseInt(choices[1]) - 1;
            String first = context.customDevelopersLeftToPick.get(firstIndex);
            String second = context.customDevelopersLeftToPick.get(secondIndex);
            pickedDevelopers = new Pair(first, second);
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.of(pickedDevelopers);
    }

    public State getState() {
        return context.getState();
    }
}
