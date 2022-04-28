package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.DecideOMatic;
import dev.coldhands.pair.stairs.PairPrinter;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.ScoredPairCombination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static dev.coldhands.pair.stairs.cli.State.*;

class StateMachine {

    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    private final List<Pairing> startingPairings = List.of();
    private final Set<String> availableDevelopers;

    private State state = BEGIN;
    private int pairCombinationsIndex = 0;
    private List<PrintableNextPairings> printableNextPairings;
    private int selection;

    public StateMachine(BufferedReader in,
                        PrintWriter out,
                        PrintWriter err,
                        Set<String> availableDevelopers) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.availableDevelopers = availableDevelopers;
    }

    public void run() throws IOException {
        switch (state) {
            case BEGIN -> {
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
                        or choose from options [c] ?
                        """.formatted(pairCombinationsIndex + 1,
                        current.score(),
                        PairPrinter.drawPairStairs(availableDevelopers, current.pairings())));
                state = PROCESS_INPUT_AFTER_NEXT_PAIR;
            }
            case PROCESS_INPUT_AFTER_NEXT_PAIR -> {
                String selection = in.readLine();
                switch (selection) {
                    case "n" -> {
                        pairCombinationsIndex++;
                        state = SHOW_NEXT_PAIR;
                    }
                    case "c" -> {
                        state = ASK_FOR_A_PAIR;
                    }
                    default -> {
                        err.println("""
                                Invalid input.
                                """);
                        state = PROCESS_INPUT_AFTER_NEXT_PAIR;
                    }
                }
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
                var chosen = printableNextPairings.get(selection - 1);
                out.println("""
                        Picked %s:
                                                
                        %s""".formatted(
                        selection,
                        PairPrinter.drawPairStairs(availableDevelopers, chosen.pairings())
                ));
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
