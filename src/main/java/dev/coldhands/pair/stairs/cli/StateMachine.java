package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.DecideOMatic;
import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.ScoredPairCombination;
import dev.coldhands.pair.stairs.persistance.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private State showPreviousPairStair() {
        out.println("""
                Yesterday's pair stairs
                                        
                %s
                """.formatted(drawPairStairs(context.allDevelopers, context.startingPairings)));
        return CALCULATE_PAIRS;
    }

    private State calculatePairs() {
        DecideOMatic decideOMatic = new DecideOMatic(context.startingPairings, context.availableDevelopers, new HashSet<>(context.newJoiners));
        context.printableNextPairings = decideOMatic.getScoredPairCombinations().stream()
                .map(toPrintableNextPairings(context.startingPairings))
                .toList();
        return SHOW_RESULTS;
    }

    private State showResults() {
        out.println("""
                Possible pairs (lowest score is better)
                """);
        return SHOW_NEXT_PAIR;
    }

    private State showNextPair() {
        var current = context.printableNextPairings.get(context.pairCombinationsIndex);
        out.println("""
                %s. score = %s
                                        
                %s
                                        
                See more options [n]""".formatted(context.pairCombinationsIndex + 1, // todo,
                //  this should actually be an optional part
                //  of the next state
                current.score(),
                drawPairStairs(context.allDevelopers, current.pairings())));
        return SHOW_NEXT_PAIR_OPTIONS;
    }

    private State showNextPairOptions() {
        out.println("""
                Choose from options [c]
                Override with your own pairs [o]
                """);
        return PROCESS_INPUT_AFTER_NEXT_PAIR;
    }

    private State processInputAfterNextPair() throws IOException {
        String selection = in.readLine();
        switch (selection) {
            case "n" -> {
                if (context.pairCombinationsIndex == context.printableNextPairings.size() - 1) {
                    return SHOW_OUT_OF_PAIRS;
                } else {
                    context.pairCombinationsIndex++;
                    return SHOW_NEXT_PAIR;
                }
            }
            case "c" -> {
                return ASK_FOR_A_PAIR;
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
                return PROCESS_INPUT_AFTER_NEXT_PAIR;
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

    private State showOutOfPairs() {
        out.println("""
                That's all of the available pairs.
                """);
        return SHOW_NEXT_PAIR_OPTIONS;
    }

    private State askForAPair() {
        out.println("""
                Choose a suggestion [%s-%s]:
                """.formatted(1, context.pairCombinationsIndex + 1));
        return PROCESS_SELECTION;
    }

    private State processSelection() throws IOException {
        String userInput = in.readLine();
        try {
            int selection = Integer.parseInt(userInput);
            if (selection >= 1 && selection <= context.pairCombinationsIndex + 1) {
                this.context.selection = selection;
                return SHOW_SELECTION;
            }
        } catch (Exception ignored) {
        }
        err.println("""
                Invalid input.
                """);
        return ASK_FOR_A_PAIR;
    }

    private State showSelection() {
        String selectionMessage;
        if (context.selection == -1) {
            selectionMessage = "custom pairs";
            context.actualNextPairings = new ArrayList<>(context.startingPairings);
            context.customPickedPairs.forEach(pair -> context.actualNextPairings.add(new Pairing(LocalDate.now(), pair)));
        } else {
            selectionMessage = String.valueOf(context.selection);
            context.actualNextPairings = context.printableNextPairings.get(context.selection - 1).pairings();
        }
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
                """.formatted(context.dataFile.toAbsolutePath().toString()));
        return COMPLETE;
    }

    public void run() throws IOException {
        context.setState(switch (context.getState()) {
            case SHOW_PREVIOUS_PAIR_STAIR -> showPreviousPairStair();
            case CALCULATE_PAIRS -> calculatePairs();
            case SHOW_RESULTS -> showResults();
            case SHOW_NEXT_PAIR -> showNextPair();
            case SHOW_NEXT_PAIR_OPTIONS -> showNextPairOptions();
            case PROCESS_INPUT_AFTER_NEXT_PAIR -> processInputAfterNextPair();
            case SHOW_NUMBERED_DEVELOPERS_TO_PICK -> showNumberedDevelopersToPick();
            case PROCESS_INPUT_FOR_PICKING_A_PAIR -> processInputForPickingAPair();
            case SHOW_OUT_OF_PAIRS -> showOutOfPairs();
            case ASK_FOR_A_PAIR -> askForAPair();
            case PROCESS_SELECTION -> processSelection();
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

    private Function<ScoredPairCombination, PrintableNextPairings> toPrintableNextPairings(List<Pairing> startingPairings) {
        return scoredPairCombination -> {
            var predictedNextPairings = new ArrayList<>(startingPairings);
            scoredPairCombination.pairCombination().forEach(pair -> predictedNextPairings.add(new Pairing(LocalDate.now(), pair)));
            return new PrintableNextPairings(predictedNextPairings, scoredPairCombination.score());
        };
    }
}
