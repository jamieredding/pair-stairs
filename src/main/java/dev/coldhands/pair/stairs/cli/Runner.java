package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.DecideOMatic;
import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.PairPrinter;
import dev.coldhands.pair.stairs.Pairing;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

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
        List<Pairing> pairings = new ArrayList<>();
        DecideOMatic decideOMatic = new DecideOMatic(pairings, availableDevelopers);
        Set<Pair> nextPairs = decideOMatic.getNextPairs();

        nextPairs.forEach(pair -> pairings.add(new Pairing(LocalDate.now(), pair)));

        out.println(PairPrinter.drawPairStairs(availableDevelopers, pairings));
        return 0;
    }

    public static void main(String... args) {
        int exitCode = createCommandLine(new PrintWriter(System.out), new PrintWriter(System.err)).execute(args);
        System.exit(exitCode);
    }
}
