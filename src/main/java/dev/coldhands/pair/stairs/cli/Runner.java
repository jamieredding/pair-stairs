package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.*;
import picocli.CommandLine;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static dev.coldhands.pair.stairs.PairPrinter.drawPairStairs;
import static picocli.CommandLine.Parameters;

class Runner implements Callable<Integer> {

    private final InputStream in;
    private final PrintWriter out;
    private final PrintWriter err;

    @Parameters(arity = "3..*", description = "at least 3 developers must be available")
    private Set<String> availableDevelopers;

    public Runner(InputStream in, PrintWriter out, PrintWriter err) {
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public static CommandLine createCommandLine(InputStream in, PrintWriter out, PrintWriter err) {
        return new CommandLine(new Runner(in, out, err));
    }

    @Override
    public Integer call() {
        try {
            List<Pairing> startingPairings = new ArrayList<>();
            DecideOMatic decideOMatic = new DecideOMatic(startingPairings, availableDevelopers);
            List<ScoredPairCombination> scoredPairCombinations = decideOMatic.getScoredPairCombinations().stream()
                    .limit(3)
                    .toList();

            List<PrintableNextPairings> printableNextPairings = scoredPairCombinations.stream()
                    .map(toPrintableNextPairings(startingPairings))
                    .toList();

            out.println("Possible pairs (lowest score is better)\n");

            PrintableNextPairings best = printableNextPairings.get(0);
            out.println("1. best choice (%s):\n\n%s\n".formatted(best.score, drawPairStairs(availableDevelopers, best.pairings)));
            PrintableNextPairings alternative = printableNextPairings.get(1);
            out.println("2. alternative (%s):\n\n%s\n".formatted(alternative.score, drawPairStairs(availableDevelopers, alternative.pairings)));
            PrintableNextPairings yetAnother = printableNextPairings.get(2);
            out.println("3. yet another (%s):\n\n%s\n".formatted(yetAnother.score, drawPairStairs(availableDevelopers, yetAnother.pairings)));

            out.println("Choose a suggestion [1-3]:\n");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            String userInput = bufferedReader.readLine();
            int userSelection = Integer.parseInt(userInput);

            out.println("Picked %s:\n".formatted(userSelection));

            PrintableNextPairings nextPairing = printableNextPairings.get(userSelection - 1);
            out.println(drawPairStairs(availableDevelopers, nextPairing.pairings()));

            return 0;
        } catch (IOException e) {
            e.printStackTrace(err);
            return 1;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to close input stream", e);
            }
        }
    }

    private Function<ScoredPairCombination, PrintableNextPairings> toPrintableNextPairings(List<Pairing> startingPairings) {
        return scoredPairCombination -> {
            var predictedNextPairings = new ArrayList<>(startingPairings);
            scoredPairCombination.pairCombination().forEach(pair -> predictedNextPairings.add(new Pairing(LocalDate.now(), pair)));
            return new PrintableNextPairings(predictedNextPairings, scoredPairCombination.score());
        };
    }

    public static void main(String... args) {
        int exitCode = createCommandLine(System.in, new PrintWriter(System.out), new PrintWriter(System.err)).execute(args);
        System.exit(exitCode);
    }

    private record PrintableNextPairings(List<Pairing> pairings, int score) {
    }

}
