package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.DecideOMatic;
import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.ScoredPairCombination;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.coldhands.pair.stairs.PairPrinter.drawPairStairs;
import static dev.coldhands.pair.stairs.cli.State.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;

class StateMachine {

    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    private final FileStorage fileStorage;
    private final Path dataFile;
    private final List<String> missingDevelopers;
    private final List<String> overrideDevelopers;
    private final Set<Pair> customPickedPairs = new HashSet<>();

    private State state = INITIALISE;
    private Set<String> allDevelopers;
    private Set<String> availableDevelopers;
    private List<String> customDevelopersLeftToPick;
    private int pairCombinationsIndex = 0;
    private List<PrintableNextPairings> printableNextPairings;
    private int selection = -1;
    private List<Pairing> actualNextPairings;
    private List<Pairing> startingPairings;
    private List<String> newJoiners;

    public StateMachine(BufferedReader in,
                        PrintWriter out,
                        PrintWriter err,
                        Runner runner) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.dataFile = runner.getDataFile();
        this.missingDevelopers = runner.getMissingDevelopers();
        fileStorage = new FileStorage(this.dataFile);
        overrideDevelopers = runner.getOverrideDevelopers();
        newJoiners = runner.getNewJoiners();
    }

    private State initialiseStateMachine() throws IOException {
        Configuration configuration;
        try {
            configuration = fileStorage.read();
        } catch (NoSuchFileException e) {
            configuration = new Configuration(List.of(), List.of(), List.of());
        }
        allDevelopers = new LinkedHashSet<>(ofNullable(overrideDevelopers)
                .map(devs -> devs.stream().sorted().toList())
                .orElse(configuration.allDevelopers()));
        if (allDevelopers.isEmpty()) {
            err.println("""
                    Unable to start.
                    No pairs specified in %s
                                            
                    Rerun and specify which devs to include via the '--devs' option
                    """.formatted(dataFile.toAbsolutePath()));
            return FAILED;
        }
        availableDevelopers = new HashSet<>(allDevelopers);
        missingDevelopers.forEach(availableDevelopers::remove);
        newJoiners = ofNullable(newJoiners)
                .orElse(configuration.newJoiners());
        startingPairings = configuration.pairings();
        return SHOW_PREVIOUS_PAIR_STAIR;
    }

    private State showPreviousPairStair() {
        out.println("""
                Yesterday's pair stairs
                                        
                %s
                """.formatted(drawPairStairs(allDevelopers, startingPairings)));
        return CALCULATE_PAIRS;
    }

    private State calculatePairs() {
        DecideOMatic decideOMatic = new DecideOMatic(startingPairings, availableDevelopers, new HashSet<>(newJoiners));
        printableNextPairings = decideOMatic.getScoredPairCombinations().stream()
                .map(toPrintableNextPairings(startingPairings))
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
        var current = printableNextPairings.get(pairCombinationsIndex);
        out.println("""
                %s. score = %s
                                        
                %s
                                        
                See more options [n]""".formatted(pairCombinationsIndex + 1, // todo,
                //  this should actually be an optional part
                //  of the next state
                current.score(),
                drawPairStairs(allDevelopers, current.pairings())));
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
                if (pairCombinationsIndex == printableNextPairings.size() - 1) {
                    return SHOW_OUT_OF_PAIRS;
                } else {
                    pairCombinationsIndex++;
                    return SHOW_NEXT_PAIR;
                }
            }
            case "c" -> {
                return ASK_FOR_A_PAIR;
            }
            case "o" -> {
                customDevelopersLeftToPick = availableDevelopers.stream()
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
        String formattedRemainingDevelopers = IntStream.rangeClosed(1, customDevelopersLeftToPick.size())
                .mapToObj(i -> "[%s] %s".formatted(i, customDevelopersLeftToPick.get(i - 1)))
                .collect(Collectors.joining("\n"));

        out.println("""
                %s
                                        
                Type two numbers to choose them,
                e.g. '1 2' for '%s' and '%s'
                """.formatted(
                formattedRemainingDevelopers,
                customDevelopersLeftToPick.get(0),
                customDevelopersLeftToPick.get(1)
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

        customDevelopersLeftToPick.removeAll(List.of(pickedDevelopers.first(), pickedDevelopers.second()));
        customPickedPairs.add(pickedDevelopers);

        if (customDevelopersLeftToPick.size() > 2) {
            out.println("""
                    Remaining:
                    """);
            return SHOW_NUMBERED_DEVELOPERS_TO_PICK;
        } else if (customDevelopersLeftToPick.size() == 2) {
            String remainingFirst = customDevelopersLeftToPick.remove(0);
            String remainingSecond = customDevelopersLeftToPick.remove(0);
            customPickedPairs.add(new Pair(remainingFirst, remainingSecond));
            return SHOW_SELECTION;
        } else {
            String remaining = customDevelopersLeftToPick.remove(0);
            customPickedPairs.add(new Pair(remaining));
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
                """.formatted(1, pairCombinationsIndex + 1));
        return PROCESS_SELECTION;
    }

    private State processSelection() throws IOException {
        String userInput = in.readLine();
        try {
            int selection = Integer.parseInt(userInput);
            if (selection >= 1 && selection <= pairCombinationsIndex + 1) {
                this.selection = selection;
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
        if (selection == -1) {
            selectionMessage = "custom pairs";
            actualNextPairings = new ArrayList<>(startingPairings);
            customPickedPairs.forEach(pair -> actualNextPairings.add(new Pairing(LocalDate.now(), pair)));
        } else {
            selectionMessage = String.valueOf(selection);
            actualNextPairings = printableNextPairings.get(selection - 1).pairings();
        }
        out.println("""
                Picked %s:
                                        
                %s
                """.formatted(
                selectionMessage,
                drawPairStairs(allDevelopers, actualNextPairings)
        ));
        return SAVE_DATA_FILE;
    }

    private State saveDataFile() throws IOException {
        fileStorage.write(new Configuration(allDevelopers.stream().toList(), newJoiners, actualNextPairings));
        out.println("""
                Saved pairings to: %s
                """.formatted(dataFile.toAbsolutePath().toString()));
        return COMPLETE;
    }

    public void run() throws IOException {
        state = switch (state) {
            case INITIALISE -> initialiseStateMachine();
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
            default -> throw new IllegalStateException("Unexpected value: " + state);
        };
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
            String first = customDevelopersLeftToPick.get(firstIndex);
            String second = customDevelopersLeftToPick.get(secondIndex);
            pickedDevelopers = new Pair(first, second);
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.of(pickedDevelopers);
    }

    public State getState() {
        return state;
    }

    private Function<ScoredPairCombination, PrintableNextPairings> toPrintableNextPairings(List<Pairing> startingPairings) {
        return scoredPairCombination -> {
            var predictedNextPairings = new ArrayList<>(startingPairings);
            scoredPairCombination.pairCombination().forEach(pair -> predictedNextPairings.add(new Pairing(LocalDate.now(), pair)));
            return new PrintableNextPairings(predictedNextPairings, scoredPairCombination.score());
        };
    }
}
