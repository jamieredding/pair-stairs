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
        List<String> allDevelopers = List.of("jamie", "jorge", "reece");
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
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie   1 *     0      0  \s
                        \sjorge           0     1 * \s
                        \sreece                  0  \s
                                                
                        See more options [n]
                        or choose from options [c] ?
                                                
                        2. score = 5
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie    0      0     1 * \s
                        \sjorge          1 *     0  \s
                        \sreece                  0  \s
                                                
                        See more options [n]
                        or choose from options [c] ?
                                                
                        3. score = 5
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie    0     1 *     0  \s
                        \sjorge           0      0  \s
                        \sreece                 1 * \s
                                                
                        See more options [n]
                        or choose from options [c] ?
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Picked 2:
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie    0      0     1 * \s
                        \sjorge          1 *     0  \s
                        \sreece                  0  \s
                                                
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
    void handleUserExhaustingPossiblePairs() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("jamie", "jorge", "reece");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("n\n") // show next pair
                .append("n\n") // show next pair
                .append("n\n") // show a non-existing pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie   1 *     0      0  \s
                        \sjorge           0     1 * \s
                        \sreece                  0  \s
                                                
                        See more options [n]
                        or choose from options [c] ?
                                                
                        2. score = 5
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie    0      0     1 * \s
                        \sjorge          1 *     0  \s
                        \sreece                  0  \s
                                                
                        See more options [n]
                        or choose from options [c] ?
                                                
                        3. score = 5
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie    0     1 *     0  \s
                        \sjorge           0      0  \s
                        \sreece                 1 * \s
                                                
                        See more options [n]
                        or choose from options [c] ?
                                                
                        That's all of the available pairs.
                                                
                        Choose a suggestion [1-3]:
                                                
                        Picked 1:
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie   1 *     0      0  \s
                        \sjorge           0     1 * \s
                        \sreece                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void loadExistingPairingsFromFileAndPersistNewPairings() throws IOException {
        LocalDate now = LocalDate.now();

        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("jamie", "jorge", "reece");
        fileStorage.write(new Configuration(allDevelopers,
                List.of(
                        new Pairing(now.minusDays(1), "jamie", "reece"),
                        new Pairing(now.minusDays(1), "jorge")
                )));

        userInput
                .append("c\n") // show next pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Possible pairs (lowest score is better)
                                                
                        1. score = 3
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie   1 *     0      1  \s
                        \sjorge           1     1 * \s
                        \sreece                  0  \s
                                                
                        See more options [n]
                        or choose from options [c] ?
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie   1 *     0      1  \s
                        \sjorge           1     1 * \s
                        \sreece                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();

        assertThat(String.join("\n", Files.readAllLines(dataFile)))
                .isEqualTo("""
                                   {""" +
                           """
                                   "allDevelopers":["jamie","jorge","reece"],""" +
                           """
                                   "pairings":[""" +
                           """
                                   {"date":"%s","pair":{"first":"jamie","second":"reece"}},""".formatted(now.minusDays(1)) +
                           """
                                   {"date":"%s","pair":{"first":"jorge","second":null}},""".formatted(now.minusDays(1)) +
                           """
                                   {"date":"%s","pair":{"first":"jorge","second":"reece"}},""".formatted(now) +
                           """
                                   {"date":"%s","pair":{"first":"jamie","second":null}}""".formatted(now) +
                           """
                                   ]}""");
    }

    @Test
    void optionallySpecifyAbsentDevelopers() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("jamie", "jorge", "reece", "andy");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("c\n") // show next pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString(), "-i", "andy", "reece");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Possible pairs (lowest score is better)
                                                
                        1. score = 0
                                                
                                andy  jamie  jorge  reece\s
                        \sandy    0      0      0      0  \s
                        \sjamie          0     1 *     0  \s
                        \sjorge                 0      0  \s
                        \sreece                        0  \s
                                                
                        See more options [n]
                        or choose from options [c] ?
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                andy  jamie  jorge  reece\s
                        \sandy    0      0      0      0  \s
                        \sjamie          0     1 *     0  \s
                        \sjorge                 0      0  \s
                        \sreece                        0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }
}