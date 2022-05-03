package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static dev.coldhands.pair.stairs.TestUtils.unWindows;
import static org.assertj.core.api.Assertions.assertThat;

class RunnerTest {

    @TempDir
    Path temp;

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();

    private CommandLine underTest;
    private OutputStreamWriter userInput;
    private Path dataFile;

    @BeforeEach
    void setUp() throws IOException {
        var outWriter = new PrintWriter(out);
        var errWriter = new PrintWriter(err);
        var input = new PipedInputStream();
        var output = new PipedOutputStream(input);
        userInput = new OutputStreamWriter(output);
        underTest = Runner.createCommandLine(input, outWriter, errWriter);
        underTest.setOut(outWriter);
        underTest.setErr(errWriter);

        dataFile = temp.resolve("data.json");
    }

    @Test
    void runWithThreeDevelopers() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("n\n") // show next pair
                .append("not valid\n") // not a valid option
                .append("n\n") // show next pair
                .append("c\n") // choose from options
                .append("kjhasdskjh\n") // not a number
                .append("0.9\n") // not an integer
                .append("0\n") // too low bound
                .append("4\n") // too high bound
                .append("2\n"); // valid selection
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0      0      0  \s
                         d-dev           0      0  \s
                         e-dev                  0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0     1 *     0  \s
                         d-dev           0      0  \s
                         e-dev                 1 * \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        2. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0      0     1 * \s
                         d-dev          1 *     0  \s
                         e-dev                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        3. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev   1 *     0      0  \s
                         d-dev           0     1 * \s
                         e-dev                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Picked 2:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0      0     1 * \s
                         d-dev          1 *     0  \s
                         e-dev                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEqualTo("""
                        Invalid input.
                                                
                        Invalid input.
                                               
                        Invalid input.
                                               
                        Invalid input.
                                               
                        Invalid input.
                                                
                        """);
    }

    @Test
    void handleUserExhaustingPossiblePairsThenChoosingOneOfTheOfferings() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("n\n") // show next pair
                .append("n\n") // show next pair
                .append("n\n") // show a non-existing pair
                .append("c\n") // show a non-existing pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0      0      0  \s
                         d-dev           0      0  \s
                         e-dev                  0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0     1 *     0  \s
                         d-dev           0      0  \s
                         e-dev                 1 * \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        2. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0      0     1 * \s
                         d-dev          1 *     0  \s
                         e-dev                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        3. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev   1 *     0      0  \s
                         d-dev           0     1 * \s
                         e-dev                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        That's all of the available pairs.
                                                
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-3]:
                                                
                        Picked 1:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0     1 *     0  \s
                         d-dev           0      0  \s
                         e-dev                 1 * \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void handleUserExhaustingPossiblePairsThenChoosingACustomPairing() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("n\n") // show next pair
                .append("n\n") // show next pair
                .append("n\n") // show a non-existing pair
                .append("o\n") // pick custom pair
                .append("1 2\n"); // pick c-dev and d-dev
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0      0      0  \s
                         d-dev           0      0  \s
                         e-dev                  0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0     1 *     0  \s
                         d-dev           0      0  \s
                         e-dev                 1 * \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        2. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0      0     1 * \s
                         d-dev          1 *     0  \s
                         e-dev                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        3. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev   1 *     0      0  \s
                         d-dev           0     1 * \s
                         e-dev                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        That's all of the available pairs.
                                                
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        [1] c-dev
                        [2] d-dev
                        [3] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'c-dev' and 'd-dev'
                                                
                        Picked custom pairs:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0     1 *     0  \s
                         d-dev           0      0  \s
                         e-dev                 1 * \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void loadExistingPairingsFromFileAndPersistNewPairings() throws IOException {
        LocalDate now = LocalDate.now();

        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        fileStorage.write(new Configuration(allDevelopers,
                List.of(
                        new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                        new Pairing(now.minusDays(1), "d-dev")
                )));

        userInput
                .append("c\n") // show next pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0      0     1 * \s
                         d-dev          1 *     0  \s
                         e-dev                  0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 3
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0     1 *     1  \s
                         d-dev           1      0  \s
                         e-dev                 1 * \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0     1 *     1  \s
                         d-dev           1      0  \s
                         e-dev                 1 * \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();

        assertThat(String.join("\n", Files.readAllLines(dataFile)))
                .isEqualTo("""
                                   {""" +
                           """
                                   "allDevelopers":["c-dev","d-dev","e-dev"],""" +
                           """
                                   "newJoiners":[],""" +
                           """
                                   "pairings":[""" +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":"e-dev"}},""".formatted(now.minusDays(1)) +
                           """
                                   {"date":"%s","pair":{"first":"d-dev","second":null}},""".formatted(now.minusDays(1)) +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":"d-dev"}},""".formatted(now) +
                           """
                                   {"date":"%s","pair":{"first":"e-dev","second":null}}""".formatted(now) +
                           """
                                   ]}""");
    }

    @Test
    void optionallySpecifyAbsentDevelopers() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("c\n") // show next pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString(), "-i", "a-dev", "e-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0      0      0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 0
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void supportReadingNewJoinersFromConfiguration() throws IOException {
        LocalDate now = LocalDate.now();

        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        fileStorage.write(new Configuration(allDevelopers,
                List.of("e-dev"),
                List.of(
                        new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                        new Pairing(now.minusDays(1), "d-dev"),
                        new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                        new Pairing(now.minusDays(2), "c-dev")
                )));

        userInput
                .append("c\n") // show next pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    1      0     1 * \s
                         d-dev          1 *     1  \s
                         e-dev                  0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 3
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev   2 *     0      1  \s
                         d-dev           1     2 * \s
                         e-dev                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev   2 *     0      1  \s
                         d-dev           1     2 * \s
                         e-dev                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();

        assertThat(String.join("\n", Files.readAllLines(dataFile)))
                .isEqualTo("""
                                   {""" +
                           """
                                   "allDevelopers":["c-dev","d-dev","e-dev"],""" +
                           """
                                   "newJoiners":["e-dev"],""" +
                           """
                                   "pairings":[""" +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":"e-dev"}},""".formatted(now.minusDays(1)) +
                           """
                                   {"date":"%s","pair":{"first":"d-dev","second":null}},""".formatted(now.minusDays(1)) +
                           """
                                   {"date":"%s","pair":{"first":"d-dev","second":"e-dev"}},""".formatted(now.minusDays(2)) +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":null}},""".formatted(now.minusDays(2)) +
                           """
                                   {"date":"%s","pair":{"first":"d-dev","second":"e-dev"}},""".formatted(now) +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":null}}""".formatted(now) +
                           """
                                   ]}""");
    }

    @Test
    void supportOverridingNewJoinersAtCommandLine() throws IOException {
        LocalDate now = LocalDate.now();

        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        fileStorage.write(new Configuration(allDevelopers,
                List.of(),
                List.of(
                        new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                        new Pairing(now.minusDays(1), "d-dev"),
                        new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                        new Pairing(now.minusDays(2), "c-dev")
                )));

        userInput
                .append("c\n") // show next pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString(), "--new", "c-dev", "e-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    1      0     1 * \s
                         d-dev          1 *     1  \s
                         e-dev                  0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    1      0     2 * \s
                         d-dev          2 *     1  \s
                         e-dev                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    1      0     2 * \s
                         d-dev          2 *     1  \s
                         e-dev                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();

        assertThat(String.join("\n", Files.readAllLines(dataFile)))
                .isEqualTo("""
                                   {""" +
                           """
                                   "allDevelopers":["c-dev","d-dev","e-dev"],""" +
                           """
                                   "newJoiners":["c-dev","e-dev"],""" +
                           """
                                   "pairings":[""" +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":"e-dev"}},""".formatted(now.minusDays(1)) +
                           """
                                   {"date":"%s","pair":{"first":"d-dev","second":null}},""".formatted(now.minusDays(1)) +
                           """
                                   {"date":"%s","pair":{"first":"d-dev","second":"e-dev"}},""".formatted(now.minusDays(2)) +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":null}},""".formatted(now.minusDays(2)) +
                           """
                                   {"date":"%s","pair":{"first":"d-dev","second":null}},""".formatted(now) +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":"e-dev"}}""".formatted(now) +
                           """
                                   ]}""");
    }

    @Test
    void allowOverridingWithYourOwnPairPick_oddNumberOfPairs() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev", "b-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("o\n") // override
                .append("1 4\n") // choose a-dev and d-dev as pair
                .append("1 3\n"); // choose b-dev and e-dev as pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                a-dev  b-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0      0  \s
                         b-dev           0      0      0      0  \s
                         c-dev                  0      0      0  \s
                         d-dev                         0      0  \s
                         e-dev                                0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 20
                                                
                                a-dev  b-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 *     0  \s
                         b-dev           0     1 *     0      0  \s
                         c-dev                  0      0      0  \s
                         d-dev                         0      0  \s
                         e-dev                               1 * \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        [1] a-dev
                        [2] b-dev
                        [3] c-dev
                        [4] d-dev
                        [5] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'b-dev'
                                                
                        Remaining:
                                                
                        [1] b-dev
                        [2] c-dev
                        [3] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'b-dev' and 'c-dev'
                                                
                        Picked custom pairs:
                                                
                                a-dev  b-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 *     0  \s
                         b-dev           0      0      0     1 * \s
                         c-dev                 1 *     0      0  \s
                         d-dev                         0      0  \s
                         e-dev                                0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void allowOverridingWithYourOwnPairPick_evenNumberOfPairs() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("o\n") // override
                .append("1 2\n"); // choose a-dev and c-dev as pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0      0      0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 * \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        [1] a-dev
                        [2] c-dev
                        [3] d-dev
                        [4] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'c-dev'
                                                
                        Picked custom pairs:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0     1 *     0      0  \s
                         c-dev           0      0      0  \s
                         d-dev                  0     1 * \s
                         e-dev                         0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void allowOverridingWithYourOwnPairPick_validation() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("o\n") // override
                .append("aslkdj\n")
                .append("1\n")
                .append("1 2 3\n")
                .append("0.1 2\n")
                .append("0 1\n")
                .append("1 5\n")
                .append("1 2\n"); // choose a-dev and c-dev as pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0      0      0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 * \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        [1] a-dev
                        [2] c-dev
                        [3] d-dev
                        [4] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'c-dev'
                                                
                        [1] a-dev
                        [2] c-dev
                        [3] d-dev
                        [4] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'c-dev'
                                                
                        [1] a-dev
                        [2] c-dev
                        [3] d-dev
                        [4] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'c-dev'
                                                
                        [1] a-dev
                        [2] c-dev
                        [3] d-dev
                        [4] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'c-dev'
                                                
                        [1] a-dev
                        [2] c-dev
                        [3] d-dev
                        [4] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'c-dev'
                                                
                        [1] a-dev
                        [2] c-dev
                        [3] d-dev
                        [4] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'c-dev'
                                                
                        [1] a-dev
                        [2] c-dev
                        [3] d-dev
                        [4] e-dev
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'a-dev' and 'c-dev'
                                                
                        Picked custom pairs:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0     1 *     0      0  \s
                         c-dev           0      0      0  \s
                         d-dev                  0     1 * \s
                         e-dev                         0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEqualTo("""
                        Invalid input.
                                                
                        Invalid input.
                                               
                        Invalid input.
                                               
                        Invalid input.
                                               
                        Invalid input.
                                               
                        Invalid input.
                                                
                        """);
    }

    @Test
    void allowSpecifyAllDevelopersAtCommandLineAndOverwriteTheConfigFile() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("c\n") // choose pair
                .append("1\n"); // choose first option
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString(),
                "--devs", "c-dev", "d-dev", "e-dev", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0      0      0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 * \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 * \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();


        assertThat(fileStorage.read())
                .isEqualTo(new Configuration(List.of("a-dev", "c-dev", "d-dev", "e-dev"),
                        List.of(new Pairing(LocalDate.now(), "a-dev", "e-dev"),
                                new Pairing(LocalDate.now(), "c-dev", "d-dev"))));
    }

    @Test
    void allowStartUpIfConfigFileIsMissingButDevsAreSpecifiedAtCommandLine() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);

        userInput
                .append("c\n") // choose pair
                .append("1\n"); // choose first option
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString(),
                "--devs", "c-dev", "d-dev", "e-dev", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0      0      0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 * \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 * \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();


        assertThat(fileStorage.read())
                .isEqualTo(new Configuration(List.of("a-dev", "c-dev", "d-dev", "e-dev"),
                        List.of(new Pairing(LocalDate.now(), "a-dev", "e-dev"),
                                new Pairing(LocalDate.now(), "c-dev", "d-dev"))));
    }

    @Test
    void errorIfNoConfigFileAndNoDevsSpecifiedAtCommandLine() {
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(1);
        assertThat(unWindows(out.toString()))
                .isEmpty();
        assertThat(unWindows(err.toString()))
                .isEqualTo("""
                        Unable to start.
                        No pairs specified in %s
                                                
                        Rerun and specify which devs to include via the '--devs' option
                                                
                        """.formatted(dataFile.toAbsolutePath()));


        assertThat(dataFile).doesNotExist();

    }

    @Test
    void usageText() {
        int exitCode = underTest.execute("-h");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Usage: pair-stairs.sh [OPTIONS]
                                                
                        Generate a pair stair for today.
                                                
                        Options:
                                                
                          -f, --config-file=FILE   Data file to use for persistence.
                                                   This will contain all pairings that occur
                                                   and all of the developers to consider.
                          -i, --ignore[=DEV...]    Developers to ignore from pairing.
                                                   This is useful when someone is absent.
                          -d, --devs[=DEV...]      Specify all developers to be shown in pair stairs.
                                                   Only required on first run or to overwrite the
                                                   developers that have been persisted.
                          -n, --new[=DEV...]       Specify new joiners.
                                                   This is useful to prevent new joiners from
                                                   working solo.
                                                   Only required on first run or to overwrite the
                                                   new joiners that have been persisted.
                          -h, --help               Display this help message.
                        """);
    }
}