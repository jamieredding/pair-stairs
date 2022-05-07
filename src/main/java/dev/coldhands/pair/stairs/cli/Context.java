package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.DecideOMatic;
import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.ScoredPairCombination;
import dev.coldhands.pair.stairs.persistance.ArtifactoryStorage;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;
import dev.coldhands.pair.stairs.persistance.Storage;

import java.nio.file.NoSuchFileException;
import java.time.LocalDate;
import java.util.*;

import static dev.coldhands.pair.stairs.cli.State.INITIAL_OUTPUT;
import static java.util.Optional.ofNullable;

class Context {

    final Storage storage;

    Set<String> allDevelopers;
    Set<String> availableDevelopers;
    List<String> newJoiners;
    List<Pairing> startingPairings;

    final Set<Pair> customPickedPairs;
    int pairCombinationsIndex;
    int selection;
    private State state;

    List<String> customDevelopersLeftToPick;
    List<ScoredPairCombination> scoredPairCombinations;
    List<Pairing> actualNextPairings;

    private Context(Runner runner) {
        Runner.Persistence persistence = runner.getPersistence();
        if (persistence.dataFile != null) {
            storage = new FileStorage(persistence.dataFile);
        } else {
            storage = new ArtifactoryStorage(persistence.artifactoryLocation, runner.getEnvironment());
        }

        Configuration configuration;
        try {
            configuration = storage.read();
        } catch (NoSuchFileException e) {
            configuration = new Configuration(List.of(), List.of(), List.of());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        allDevelopers = initialiseAllDevelopers(runner, configuration);
        availableDevelopers = initialiseAvailableDevelopers(runner);
        newJoiners = initialiseNewJoiners(runner, configuration);
        startingPairings = initialiseStartingPairings(configuration);
        scoredPairCombinations = computeScoredPairCombinations();

        // todo move these closer to where they are needed in the state machine
        customPickedPairs = new HashSet<>();
        pairCombinationsIndex = 0;
        selection = -1;
        state = INITIAL_OUTPUT;
    }

    private List<Pairing> initialiseStartingPairings(Configuration configuration) {
        LocalDate now = LocalDate.now();
        return configuration.pairings().stream()
                .filter(pairing -> !Objects.equals(pairing.date(), now))
                .toList();
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
                    """.formatted(storage.describe()));
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

    private List<ScoredPairCombination> computeScoredPairCombinations() {
        DecideOMatic decideOMatic = new DecideOMatic(startingPairings, availableDevelopers, new HashSet<>(newJoiners));
        return decideOMatic.getScoredPairCombinations();
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
