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

    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    @Parameters(arity = "3..*", description = "at least 3 developers must be available")
    private Set<String> availableDevelopers;

    public Runner(InputStream in, PrintWriter out, PrintWriter err) {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = out;
        this.err = err;
    }

    public static CommandLine createCommandLine(InputStream in, PrintWriter out, PrintWriter err) {
        return new CommandLine(new Runner(in, out, err));
    }

    @Override
    public Integer call() {
        try {
            runStateMachine(availableDevelopers);

            return 0;
        } catch (Exception e) {
            e.printStackTrace(err);
            return 1;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace(err);
            }
        }
    }

    private void runStateMachine(Set<String> availableDevelopers) throws IOException {
        StateMachine stateMachine = new StateMachine(in, out, err, availableDevelopers);
        while (stateMachine.getState() != State.COMPLETE) {
            stateMachine.run();
        }
    }

    public static void main(String... args) {
        int exitCode = createCommandLine(System.in, new PrintWriter(System.out, true), new PrintWriter(System.err, true)).execute(args);
        System.exit(exitCode);
    }

}
