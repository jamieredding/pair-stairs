package dev.coldhands.pair.stairs.cli;

import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "pair-stairs.sh")
class Runner implements Callable<Integer> {

    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    @Option(names = "-f", required = true, description = "data file to use for pairing persistence")
    private Path dataFile;

    @Option(names = "-i", arity = "0..*", description = "developers to ignore from pairing")
    private List<String> missingDevelopers = List.of();

    @Option(names = "--devs", arity = "0..*", description = "specify all developers")
    private List<String> overrideDevelopers;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean helpRequested;

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
            return runStateMachine(this);
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

    private int runStateMachine(Runner runner) throws IOException {
        StateMachine stateMachine = new StateMachine(in, out, err, runner);
        while (stateMachine.getState() != State.COMPLETE &&
               stateMachine.getState() != State.FAILED) {
            stateMachine.run();
        }
        return stateMachine.getState() == State.COMPLETE ? 0 : 1;
    }

    public static void main(String... args) {
        AnsiConsole.systemInstall();
        int exitCode = createCommandLine(System.in, new PrintWriter(System.out, true), new PrintWriter(System.err, true)).execute(args);
        AnsiConsole.systemUninstall();
        System.exit(exitCode);
    }

    public Path getDataFile() {
        return dataFile;
    }

    public List<String> getMissingDevelopers() {
        return missingDevelopers;
    }

    List<String> getOverrideDevelopers() {
        return overrideDevelopers;
    }
}
