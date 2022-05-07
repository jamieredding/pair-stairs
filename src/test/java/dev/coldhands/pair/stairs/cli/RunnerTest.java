package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
        List<String> allDevelopers = List.of("a-dev", "b-dev", "c-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
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
                                                
                                a-dev  b-dev  c-dev\s
                         a-dev    0      0      0  \s
                         b-dev           0      0  \s
                         c-dev                  0  \s
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  a-dev  c-dev  b-dev  c-dev  a-dev  b-dev\s
                         Pair b     b-dev         a-dev         c-dev    \s
                         score        5             5             5      \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Picked 2:
                                                
                                a-dev  b-dev  c-dev\s
                         a-dev   1 *     0      0  \s
                         b-dev           0     1 * \s
                         c-dev                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(fileStorage.describe()));
        assertThat(unWindows(err.toString()))
                .isEqualTo("""
                        Invalid input.
                                                
                        Invalid input.
                                               
                        Invalid input.
                                               
                        Invalid input.
                                                
                        """);
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
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  c-dev  d-dev  d-dev  e-dev  c-dev  e-dev\s
                         Pair b     e-dev         c-dev         d-dev    \s
                         score        3             3             9      \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Picked 1:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    0     1 *     1  \s
                         d-dev           1      0  \s
                         e-dev                 1 * \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(fileStorage.describe()));
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
    void optionallySpecifyAbsentDeveloper() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString(), "-i", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0      0      0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  c-dev  d-dev  c-dev  e-dev  d-dev  e-dev\s
                         Pair b     e-dev         d-dev         c-dev    \s
                         score        5             5             5      \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Picked 1:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                        1 * \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(fileStorage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void ifOnlyOneOptionIsAvailableThenJustPrintThat() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

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
                                                
                        Only one option:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(fileStorage.describe()));
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
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  d-dev  e-dev  c-dev  e-dev  c-dev  d-dev\s
                         Pair b     c-dev         d-dev         e-dev    \s
                         score        3             7             11     \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Picked 1:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev   2 *     0      1  \s
                         d-dev           1     2 * \s
                         e-dev                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(fileStorage.describe()));
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
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  c-dev  e-dev  c-dev  d-dev  d-dev  e-dev\s
                         Pair b     d-dev         e-dev         c-dev    \s
                         score        5             10            12     \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Picked 1:
                                                
                                c-dev  d-dev  e-dev\s
                         c-dev    1      0     2 * \s
                         d-dev          2 *     1  \s
                         e-dev                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(fileStorage.describe()));
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
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  a-dev  d-dev  a-dev  c-dev  a-dev  d-dev\s
                         Pair b  b-dev  c-dev  b-dev  d-dev  b-dev  e-dev\s
                         Pair c     e-dev         e-dev         c-dev    \s
                         score        20            20            20     \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
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
                                                
                        """.formatted(fileStorage.describe()));
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
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  a-dev  e-dev  a-dev  d-dev  a-dev  c-dev\s
                         Pair b  c-dev  d-dev  c-dev  e-dev  d-dev  e-dev\s
                         score        5             5             5      \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
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
                                                
                        """.formatted(fileStorage.describe()));
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
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  a-dev  e-dev  a-dev  d-dev  a-dev  c-dev\s
                         Pair b  c-dev  d-dev  c-dev  e-dev  d-dev  e-dev\s
                         score        5             5             5      \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
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
                                                
                        """.formatted(fileStorage.describe()));
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
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  a-dev  e-dev  a-dev  d-dev  a-dev  c-dev\s
                         Pair b  c-dev  d-dev  c-dev  e-dev  d-dev  e-dev\s
                         score        5             5             5      \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Picked 1:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 * \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(fileStorage.describe()));
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
                                                
                        Options (lowest score is better)
                                                
                                      1             2             3      \s
                         Pair a  a-dev  e-dev  a-dev  d-dev  a-dev  c-dev\s
                         Pair b  c-dev  d-dev  c-dev  e-dev  d-dev  e-dev\s
                         score        5             5             5      \s
                                                
                        Choose a suggestion [1-3]:
                        Or override with your own pairs [o]
                                                
                        Picked 1:
                                                
                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0     1 * \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(fileStorage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();


        assertThat(fileStorage.read())
                .isEqualTo(new Configuration(List.of("a-dev", "c-dev", "d-dev", "e-dev"),
                        List.of(new Pairing(LocalDate.now(), "a-dev", "e-dev"),
                                new Pairing(LocalDate.now(), "c-dev", "d-dev"))));
    }

    @Test
    void errorIfNoConfigFileAndNoDevsSpecifiedAtCommandLine() {
        FileStorage fileStorage = new FileStorage(dataFile);
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(1);
        assertThat(unWindows(out.toString()))
                .isEmpty();
        assertThat(unWindows(err.toString()))
                .isEqualTo("""
                        Unable to start.
                        No pairs specified in %s
                                                
                        Rerun and specify which devs to include via the '--devs' option
                                                
                        """.formatted(fileStorage.describe()));


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