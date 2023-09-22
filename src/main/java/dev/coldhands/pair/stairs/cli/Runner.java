package dev.coldhands.pair.stairs.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "pair-stairs.sh",
        descriptionHeading = "%n",
        description = "Generate a pair stair for today.",
        optionListHeading = "%nOptions:%n%n",
        sortOptions = false,
        abbreviateSynopsis = true,
        versionProvider = ManifestVersionProvider.class,
        footer = {
                "%nExamples:%n",
                "First run with file persistence",
                "    pair-stairs.sh \\",
                "      -f /path/to/config.json \\",
                "      -d dev1 -d dev2 -d dev3",
                "",
                "First run with artifactory persistence",
                "    pair-stairs.sh \\",
                "      -a http://artifactory/libs-snapshots-local/my/config.json \\",
                "      -d dev1 -d dev2 -d dev3"
        })
class Runner implements Callable<Integer> {

    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;
    private final Map<String, String> environment;

    @ArgGroup(exclusive = true, multiplicity = "1", heading = "%nPersistence%n%n")
    private Persistence persistence;

    static class Persistence {
        @Option(names = {"-f", "--config-file"},
                required = true,
                paramLabel = "FILE",
                description = {
                        "Data file to use for persistence.",
                        "This will contain all pairings that occur",
                        "and all of the developers to consider."
                })
        Path dataFile;

        @Option(names = {"-a", "--artifactory-location"},
                required = true,
                paramLabel = "URL",
                description = {
                        "URL to config file to use for persistence.",
                        "Must be an artifactory instance.",
                        "Will use basic auth credentials from",
                        "the following environment variables: ",
                        "ARTIFACTORY_USERNAME and ARTIFACTORY_PASSWORD"
                })
        String artifactoryLocation;

    }

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

    @Option(names = "--verbose",
            description = "Enable verbose output.")
    private boolean isVerbose;

    @Option(names = {"-h", "--help"},
            usageHelp = true,
            description = "Display this help message.")
    private boolean helpRequested;

    @Option(names = "--version",
            versionHelp = true,
            description = "Display version information and exit.")
    private boolean versionRequested;

    public Runner(InputStream in, PrintWriter out, PrintWriter err, Map<String, String> environment) {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = out;
        this.err = err;
        this.environment = environment;
    }

    public static CommandLine createCommandLine(InputStream in, PrintWriter out, PrintWriter err, Map<String, String> environment) {
        return new CommandLine(new Runner(in, out, err, environment));
    }

    @Override
    public Integer call() {
        try {
            if (isVerbose) {
                Logger logger = (Logger) LoggerFactory.getLogger("dev.coldhands");
                logger.setLevel(Level.DEBUG);
            }

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

    private int runStateMachine(Runner runner) throws Exception {
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
        int exitCode = createCommandLine(
                System.in,
                new PrintWriter(System.out, true),
                new PrintWriter(System.err, true),
                System.getenv()).execute(args);
        AnsiConsole.systemUninstall();
        System.exit(exitCode);
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

    public Persistence getPersistence() {
        return persistence;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }
}
