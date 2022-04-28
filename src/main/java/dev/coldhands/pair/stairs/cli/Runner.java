package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.*;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static dev.coldhands.pair.stairs.PairPrinter.drawPairStairs;
import static picocli.CommandLine.Parameters;

class Runner implements Callable<Integer> {

    private final PrintWriter out;
    private final PrintWriter err;

    @Parameters(arity = "3..*", description = "at least 3 developers must be available")
    private Set<String> availableDevelopers;

    public Runner(PrintWriter out, PrintWriter err) {
        this.out = out;
        this.err = err;
    }

    public static CommandLine createCommandLine(PrintWriter out, PrintWriter err) {
        return new CommandLine(new Runner(out, err));
    }

    @Override
    public Integer call() {
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
        out.println("best choice (%s):\n\n%s\n".formatted(best.score, drawPairStairs(availableDevelopers, best.pairings)));
        PrintableNextPairings alternative = printableNextPairings.get(1);
        out.println("alternative (%s):\n\n%s\n".formatted(alternative.score, drawPairStairs(availableDevelopers, alternative.pairings)));
        PrintableNextPairings yetAnother = printableNextPairings.get(2);
        out.println("yet another (%s):\n\n%s".formatted(yetAnother.score, drawPairStairs(availableDevelopers, yetAnother.pairings)));

        return 0;
    }

    private Function<ScoredPairCombination, PrintableNextPairings> toPrintableNextPairings(List<Pairing> startingPairings) {
        return scoredPairCombination -> {
            var predictedNextPairings = new ArrayList<>(startingPairings);
            scoredPairCombination.pairCombination().forEach(pair -> predictedNextPairings.add(new Pairing(LocalDate.now(), pair)));
            return new PrintableNextPairings(predictedNextPairings, scoredPairCombination.score());
        };
    }

    public static void main(String... args) {
        int exitCode = createCommandLine(new PrintWriter(System.out), new PrintWriter(System.err)).execute(args);
        System.exit(exitCode);
    }

    private record PrintableNextPairings(List<Pairing> pairings, int score) {
    }

}
