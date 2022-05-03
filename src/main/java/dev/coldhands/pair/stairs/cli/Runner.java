package dev.coldhands.pair.stairs.cli;

import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "pair-stairs.sh",
        descriptionHeading = "%n",
        description = "Generate a pair stair for today.",
        optionListHeading = "%nOptions:%n%n",
        sortOptions = false,
        abbreviateSynopsis = true)
class Runner implements Callable<Integer> {

    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    @Option(names = {"-f", "--config-file"},
            required = true,
            paramLabel = "FILE",
            description = {
                    "Data file to use for persistence.",
                    "This will contain all pairings that occur",
                    "and all of the developers to consider."
            })
    private Path dataFile;

    @Option(names = {"-i", "--ignore"},
            arity = "0..*",
            paramLabel = "DEV",
            description = {
                    "Developers to ignore from pairing.",
                    "This is useful when someone is absent."
            })
    private List<String> missingDevelopers = List.of();

    @Option(names = {"-d", "--devs"},
            arity = "0..*",
            paramLabel = "DEV",
            description = {
                    "Specify all developers to be shown in pair stairs.",
                    "Only required on first run or to overwrite the",
                    "developers that have been persisted."
            })
    private List<String> overrideDevelopers;

    @Option(names = {"-n", "--new"},
            arity = "0..*",
            paramLabel = "DEV",
            description = {
                    "Specify new joiners.",
                    "This is useful to prevent new joiners from",
                    "working solo.",
                    "Only required on first run or to overwrite the",
                    "new joiners that have been persisted."
            })
    private List<String> newJoiners;

    @Option(names = {"-h", "--help"},
            usageHelp = true,
            description = "Display this help message.")
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
        try {
            final StateMachine stateMachine = new StateMachine(in, out, err, runner);
            while (stateMachine.getState() != State.COMPLETE) {
                stateMachine.run();
            }
            return 0;
        } catch (StateMachineException e) {
            err.println(e.getMessage());
            return 1;
        }
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

    public List<String> getNewJoiners() {
        return newJoiners;
    }
}
