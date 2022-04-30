package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.*;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.coldhands.pair.stairs.PairPrinter.*;
import static dev.coldhands.pair.stairs.cli.State.*;
import static java.util.stream.Collectors.toCollection;

class StateMachine {

    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    private final FileStorage fileStorage;
    private final Path dataFile;
    private final List<String> missingDevelopers;

    private State state = BEGIN;
    private Set<String> allDevelopers;
    private Set<String> availableDevelopers;
    private List<String> customDevelopersLeftToPick;
    private Set<Pair> customPickedPairs = new HashSet<>();
    private int pairCombinationsIndex = 0;
    private List<PrintableNextPairings> printableNextPairings;
    private int selection = -1;
    private List<Pairing> actualNextPairings;
    private List<Pairing> startingPairings;
    private Configuration configuration;

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
    }

    public void run() throws IOException {
        switch (state) {
            case BEGIN -> {
                state = LOAD_CONFIGURATION_FILE;
            }
            case LOAD_CONFIGURATION_FILE -> {
                configuration = fileStorage.read();
                allDevelopers = new HashSet<>(configuration.allDevelopers());
                availableDevelopers = new HashSet<>(configuration.allDevelopers());
                missingDevelopers.forEach(availableDevelopers::remove);
                startingPairings = configuration.pairings();
                state = SHOW_PREVIOUS_PAIR_STAIR;
            }
            case SHOW_PREVIOUS_PAIR_STAIR -> {
                out.println("""
                        Yesterday's pair stairs
                                                
                        %s
                        """.formatted(drawPairStairs(allDevelopers, startingPairings)));
                state = CALCULATE_PAIRS;
            }
            case CALCULATE_PAIRS -> {
                DecideOMatic decideOMatic = new DecideOMatic(startingPairings, availableDevelopers);
                printableNextPairings = decideOMatic.getScoredPairCombinations().stream()
                        .map(toPrintableNextPairings(startingPairings))
                        .toList();
                state = SHOW_RESULTS;
            }
            case SHOW_RESULTS -> {
                out.println("""
                        Possible pairs (lowest score is better)
                        """);
                state = SHOW_NEXT_PAIR;
            }
            case SHOW_NEXT_PAIR -> {
                var current = printableNextPairings.get(pairCombinationsIndex);
                out.println("""
                        %s. score = %s
                                                
                        %s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                        """.formatted(pairCombinationsIndex + 1,
                        current.score(),
                        drawPairStairs(allDevelopers, current.pairings())));
                state = PROCESS_INPUT_AFTER_NEXT_PAIR;
            }
            case PROCESS_INPUT_AFTER_NEXT_PAIR -> {
                String selection = in.readLine();
                switch (selection) {
                    case "n" -> {
                        if (pairCombinationsIndex == printableNextPairings.size() - 1) {
                            state = SHOW_OUT_OF_PAIRS;
                        } else {
                            pairCombinationsIndex++;
                            state = SHOW_NEXT_PAIR;
                        }
                    }
                    case "c" -> {
                        state = ASK_FOR_A_PAIR;
                    }
                    case "o" -> {
                        customDevelopersLeftToPick = availableDevelopers.stream()
                                .sorted()
                                .collect(toCollection(LinkedList::new));
                        state = SHOW_NUMBERED_DEVELOPERS_TO_PICK;
                    }
                    default -> {
                        err.println("""
                                Invalid input.
                                """);
                        state = PROCESS_INPUT_AFTER_NEXT_PAIR;
                    }
                }
            }
            case SHOW_NUMBERED_DEVELOPERS_TO_PICK -> {
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
                state = PROCESS_INPUT_FOR_PICKING_A_PAIR;
            }
            case PROCESS_INPUT_FOR_PICKING_A_PAIR -> {
                String userInput = in.readLine();
                String[] choices = userInput.split(" ");
                int firstIndex = Integer.parseInt(choices[0]) - 1;
                int secondIndex = Integer.parseInt(choices[1]) - 1;
                String first = customDevelopersLeftToPick.get(firstIndex);
                String second = customDevelopersLeftToPick.get(secondIndex);
                customDevelopersLeftToPick.removeAll(List.of(first, second));
                customPickedPairs.add(new Pair(first, second));

                if (customDevelopersLeftToPick.size() == 1) {
                    customPickedPairs.add(new Pair(customDevelopersLeftToPick.remove(0)));
                    state = SHOW_SELECTION;
                } else {
                    out.println("""
                            Remaining:
                            """);
                    state = SHOW_NUMBERED_DEVELOPERS_TO_PICK;
                }
            }
            case SHOW_OUT_OF_PAIRS -> {
                out.println("""
                        That's all of the available pairs.
                        """);
                state = ASK_FOR_A_PAIR;
            }
            case ASK_FOR_A_PAIR -> {
                out.println("""
                        Choose a suggestion [%s-%s]:
                        """.formatted(1, pairCombinationsIndex + 1));
                state = PROCESS_SELECTION;
            }
            case PROCESS_SELECTION -> {
                String userInput = in.readLine();
                try {
                    int selection = Integer.parseInt(userInput);
                    if (selection >= 1 && selection <= pairCombinationsIndex + 1) {
                        this.selection = selection;
                        state = SHOW_SELECTION;
                        break;
                    }
                } catch (Exception ignored) {
                }
                err.println("""
                        Invalid input.
                        """);
                state = ASK_FOR_A_PAIR;
            }
            case SHOW_SELECTION -> {
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
                state = SAVE_DATA_FILE;
            }
            case SAVE_DATA_FILE -> {
                fileStorage.write(new Configuration(configuration.allDevelopers(), actualNextPairings));
                out.println("""
                        Saved pairings to: %s
                        """.formatted(dataFile.toAbsolutePath().toString()));
                state = COMPLETE;
            }
        }
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
