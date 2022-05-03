package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.DecideOMatic;
import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.ScoredPairCombination;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static dev.coldhands.pair.stairs.cli.State.HEADER;
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
    List<PossibleScoredPairings> possibleScoredPairings;
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
        possibleScoredPairings = computePossibleScoredPairings();

        // todo move these closer to where they are needed in the state machine
        customPickedPairs = new HashSet<>();
        pairCombinationsIndex = 0;
        selection = -1;
        state = HEADER;
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

    private List<PossibleScoredPairings> computePossibleScoredPairings() {
        DecideOMatic decideOMatic = new DecideOMatic(startingPairings, availableDevelopers, new HashSet<>(newJoiners));
        return decideOMatic.getScoredPairCombinations().stream()
                .map(addNewPairCombinationTo(startingPairings))
                .toList();
    }

    private Function<ScoredPairCombination, PossibleScoredPairings> addNewPairCombinationTo(List<Pairing> startingPairings) {
        return scoredPairCombination -> {
            var possiblePairings = new ArrayList<>(startingPairings);
            scoredPairCombination.pairCombination().forEach(pair -> possiblePairings.add(new Pairing(LocalDate.now(), pair)));
            return new PossibleScoredPairings(possiblePairings, scoredPairCombination.score());
        };
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
