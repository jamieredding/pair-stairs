package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.core.LegacyCombinationService;
import dev.coldhands.pair.stairs.domain.Pair;
import dev.coldhands.pair.stairs.domain.Pairing;
import dev.coldhands.pair.stairs.domain.ScoredPairCombination;
import dev.coldhands.pair.stairs.logic.EntryPoint;
import dev.coldhands.pair.stairs.logic.legacy.LegacyScoringStrategy;
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
    final List<String> availableDevelopers;
    final List<Pairing> startingPairings;
    final List<String> newJoiners;

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
        this.availableDevelopers = initialiseAvailableDevelopers(runner);
        this.startingPairings = initialiseStartingPairings(configuration);
        this.newJoiners = initialiseNewJoiners(runner, configuration);

        final List<Pairing> initialPairings = initialiseStartingPairings(configuration);
        final List<String> availableDevelopers = initialiseAvailableDevelopers(runner);
        final List<String> newJoiners = initialiseNewJoiners(runner, configuration);

        final var combinationService = new LegacyCombinationService(availableDevelopers);
        final var scoringStrategy = new LegacyScoringStrategy(availableDevelopers, initialPairings, newJoiners);
        final var entryPoint = new EntryPoint(combinationService, scoringStrategy);
        scoredPairCombinations = entryPoint.computeScoredPairCombinations();

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

    private List<String> initialiseAvailableDevelopers(Runner runner) {
        var availableDevelopers = new ArrayList<>(allDevelopers);
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
