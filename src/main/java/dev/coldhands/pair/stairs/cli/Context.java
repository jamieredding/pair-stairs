package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static dev.coldhands.pair.stairs.cli.State.SHOW_PREVIOUS_PAIR_STAIR;
import static java.util.Optional.ofNullable;

class Context {

    final FileStorage fileStorage;
    final Path dataFile;
    final List<String> missingDevelopers;
    final List<String> overrideDevelopers;
    final Set<Pair> customPickedPairs;

    private State state;
    Set<String> allDevelopers;
    Set<String> availableDevelopers;
    List<String> customDevelopersLeftToPick;
    int pairCombinationsIndex;
    List<PrintableNextPairings> printableNextPairings;
    int selection;
    List<Pairing> actualNextPairings;
    List<Pairing> startingPairings;
    List<String> newJoiners;

    private Context(Runner runner) {
        this.dataFile = runner.getDataFile();
        this.missingDevelopers = runner.getMissingDevelopers();
        fileStorage = new FileStorage(this.dataFile);
        overrideDevelopers = runner.getOverrideDevelopers();
        newJoiners = runner.getNewJoiners();
        state = SHOW_PREVIOUS_PAIR_STAIR;
        pairCombinationsIndex = 0;
        selection = -1;

        Configuration configuration;
        try {
            configuration = fileStorage.read();
        } catch (NoSuchFileException e) {
            configuration = new Configuration(List.of(), List.of(), List.of());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        allDevelopers = new LinkedHashSet<>(ofNullable(overrideDevelopers)
                .map(devs -> devs.stream().sorted().toList())
                .orElse(configuration.allDevelopers()));
        if (allDevelopers.isEmpty()) {
            throw new StateMachineException("""
                    Unable to start.
                    No pairs specified in %s
                                            
                    Rerun and specify which devs to include via the '--devs' option
                    """.formatted(dataFile.toAbsolutePath()));
        }
        availableDevelopers = new HashSet<>(allDevelopers);
        missingDevelopers.forEach(availableDevelopers::remove);
        newJoiners = ofNullable(newJoiners)
                .orElse(configuration.newJoiners());
        startingPairings = configuration.pairings();
        customPickedPairs = new HashSet<>();
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
