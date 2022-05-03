package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static dev.coldhands.pair.stairs.cli.State.SHOW_PREVIOUS_PAIR_STAIR;
import static java.util.Optional.ofNullable;

class Context {

    final FileStorage fileStorage;

    Set<String> allDevelopers;
    Set<String> availableDevelopers;
    List<String> newJoiners;
    List<Pairing> startingPairings;

    final Set<Pair> customPickedPairs;
    int pairCombinationsIndex;
    int selection;
    private State state;

    List<String> customDevelopersLeftToPick;
    List<PrintableNextPairings> printableNextPairings;
    List<Pairing> actualNextPairings;

    private Context(Runner runner) {
        fileStorage = new FileStorage(runner.getDataFile());

        Configuration configuration;
        try {
            configuration = fileStorage.read();
        } catch (NoSuchFileException e) {
            configuration = new Configuration(List.of(), List.of(), List.of());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        allDevelopers = initialiseAllDevelopers(runner, configuration);
        availableDevelopers = initialiseAvailableDevelopers(runner);
        newJoiners = initialiseNewJoiners(runner, configuration);
        startingPairings = configuration.pairings();

        customPickedPairs = new HashSet<>();
        pairCombinationsIndex = 0;
        selection = -1;
        state = SHOW_PREVIOUS_PAIR_STAIR;
    }

    private LinkedHashSet<String> initialiseAllDevelopers(Runner runner, Configuration configuration) {
        var allDevelopers = new LinkedHashSet<>(ofNullable(runner.getOverrideDevelopers())
                .map(devs -> devs.stream().sorted().toList())
                .orElse(configuration.allDevelopers()));
        if (allDevelopers.isEmpty()) {
            throw new StateMachineException("""
                    Unable to start.
                    No pairs specified in %s
                                            
                    Rerun and specify which devs to include via the '--devs' option
                    """.formatted(fileStorage.getDataFile().toAbsolutePath()));
        }
        return allDevelopers;
    }

    private HashSet<String> initialiseAvailableDevelopers(Runner runner) {
        var availableDevelopers = new HashSet<>(allDevelopers);
        runner.getMissingDevelopers().forEach(availableDevelopers::remove);
        return availableDevelopers;
    }

    private List<String> initialiseNewJoiners(Runner runner, Configuration configuration) {
        return ofNullable(runner.getNewJoiners())
                .orElse(configuration.newJoiners());
    }

    public static Context from(Runner runner) {
        return new Context(runner);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
