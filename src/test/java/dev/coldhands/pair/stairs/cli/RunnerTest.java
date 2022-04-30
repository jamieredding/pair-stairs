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
                        Yesterday's pair stairs
                                                
                                jamie  jorge  reece\s
                         jamie    0      0      0  \s
                         jorge           0      0  \s
                         reece                  0  \s
                        
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie   1 *     0      0  \s
                         jorge           0     1 * \s
                         reece                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        2. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie    0      0     1 * \s
                         jorge          1 *     0  \s
                         reece                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        3. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie    0     1 *     0  \s
                         jorge           0      0  \s
                         reece                 1 * \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Choose a suggestion [1-3]:
                                                
                        Picked 2:
                                                
                                jamie  jorge  reece\s
                         jamie    0      0     1 * \s
                         jorge          1 *     0  \s
                         reece                  0  \s
                                                
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
        List<String> allDevelopers = List.of("jamie", "jorge", "reece");
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
                        
                                jamie  jorge  reece\s
                         jamie    0      0      0  \s
                         jorge           0      0  \s
                         reece                  0  \s
                        
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie   1 *     0      0  \s
                         jorge           0     1 * \s
                         reece                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        2. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie    0      0     1 * \s
                         jorge          1 *     0  \s
                         reece                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        3. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie    0     1 *     0  \s
                         jorge           0      0  \s
                         reece                 1 * \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        That's all of the available pairs.
                        
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-3]:
                                                
                        Picked 1:
                                                
                                jamie  jorge  reece\s
                         jamie   1 *     0      0  \s
                         jorge           0     1 * \s
                         reece                  0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void handleUserExhaustingPossiblePairsThenChoosingACustomPairing() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("jamie", "jorge", "reece");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("n\n") // show next pair
                .append("n\n") // show next pair
                .append("n\n") // show a non-existing pair
                .append("o\n") // pick custom pair
                .append("1 2\n"); // pick jamie and jorge
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                jamie  jorge  reece\s
                         jamie    0      0      0  \s
                         jorge           0      0  \s
                         reece                  0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie   1 *     0      0  \s
                         jorge           0     1 * \s
                         reece                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        2. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie    0      0     1 * \s
                         jorge          1 *     0  \s
                         reece                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        3. score = 5
                                                
                                jamie  jorge  reece\s
                         jamie    0     1 *     0  \s
                         jorge           0      0  \s
                         reece                 1 * \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        That's all of the available pairs.
                        
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        [1] jamie
                        [2] jorge
                        [3] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'jamie' and 'jorge'
                                                
                        Picked custom pairs:
                                                
                                jamie  jorge  reece\s
                         jamie    0     1 *     0  \s
                         jorge           0      0  \s
                         reece                 1 * \s
                                                
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
                        Yesterday's pair stairs
                                                
                                jamie  jorge  reece\s
                         jamie    0      0     1 * \s
                         jorge          1 *     0  \s
                         reece                  0  \s
                        
                        Possible pairs (lowest score is better)
                                                
                        1. score = 3
                                                
                                jamie  jorge  reece\s
                         jamie   1 *     0      1  \s
                         jorge           1     1 * \s
                         reece                  0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                jamie  jorge  reece\s
                         jamie   1 *     0      1  \s
                         jorge           1     1 * \s
                         reece                  0  \s
                                                
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
                        Yesterday's pair stairs
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0      0  \s
                         jamie          0      0      0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                        
                        Possible pairs (lowest score is better)
                                                
                        1. score = 0
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0      0  \s
                         jamie          0     1 *     0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0      0  \s
                         jamie          0     1 *     0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void allowOverridingWithYourOwnPairPick_oddNumberOfPairs() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("jamie", "jorge", "reece", "andy", "cip");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("o\n") // override
                .append("1 4\n") // choose andy and jorge as pair
                .append("1 3\n"); // choose cip and reece as pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                andy  cip  jamie  jorge  reece\s
                         andy    0     0     0      0      0  \s
                         cip           0     0      0      0  \s
                         jamie               0      0      0  \s
                         jorge                      0      0  \s
                         reece                             0  \s
                                                
                        Possible pairs (lowest score is better)
                        
                        1. score = 20
                                                
                                andy  cip  jamie  jorge  reece\s
                         andy    0     0     0      0     1 * \s
                         cip           0     0     1 *     0  \s
                         jamie              1 *     0      0  \s
                         jorge                      0      0  \s
                         reece                             0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        [1] andy
                        [2] cip
                        [3] jamie
                        [4] jorge
                        [5] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'cip'
                                                
                        Remaining:
                                                
                        [1] cip
                        [2] jamie
                        [3] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'cip' and 'jamie'
                                                
                        Picked custom pairs:
                                                
                                andy  cip  jamie  jorge  reece\s
                         andy    0     0     0     1 *     0  \s
                         cip           0     0      0     1 * \s
                         jamie              1 *     0      0  \s
                         jorge                      0      0  \s
                         reece                             0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void allowOverridingWithYourOwnPairPick_evenNumberOfPairs() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("jamie", "jorge", "reece", "andy");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("o\n") // override
                .append("1 2\n"); // choose andy and jamie as pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0      0  \s
                         jamie          0      0      0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0     1 * \s
                         jamie          0     1 *     0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        [1] andy
                        [2] jamie
                        [3] jorge
                        [4] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'jamie'
                        
                        Picked custom pairs:
                                                
                                andy  jamie  jorge  reece\s
                         andy    0     1 *     0      0  \s
                         jamie          0      0      0  \s
                         jorge                 0     1 * \s
                         reece                        0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void allowOverridingWithYourOwnPairPick_validation() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);
        List<String> allDevelopers = List.of("jamie", "jorge", "reece", "andy");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("o\n") // override
                .append("aslkdj\n")
                .append("1\n")
                .append("1 2 3\n")
                .append("0.1 2\n")
                .append("0 1\n")
                .append("1 5\n")
                .append("1 2\n"); // choose andy and jamie as pair
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0      0  \s
                         jamie          0      0      0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0     1 * \s
                         jamie          0     1 *     0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        [1] andy
                        [2] jamie
                        [3] jorge
                        [4] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'jamie'
                                                
                        [1] andy
                        [2] jamie
                        [3] jorge
                        [4] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'jamie'
                                                
                        [1] andy
                        [2] jamie
                        [3] jorge
                        [4] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'jamie'
                                                
                        [1] andy
                        [2] jamie
                        [3] jorge
                        [4] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'jamie'
                                                
                        [1] andy
                        [2] jamie
                        [3] jorge
                        [4] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'jamie'
                                                
                        [1] andy
                        [2] jamie
                        [3] jorge
                        [4] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'jamie'
                                                
                        [1] andy
                        [2] jamie
                        [3] jorge
                        [4] reece
                                                
                        Type two numbers to choose them,
                        e.g. '1 2' for 'andy' and 'jamie'
                        
                        Picked custom pairs:
                                                
                                andy  jamie  jorge  reece\s
                         andy    0     1 *     0      0  \s
                         jamie          0      0      0  \s
                         jorge                 0     1 * \s
                         reece                        0  \s
                                                
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
        List<String> allDevelopers = List.of("jamie", "jorge", "reece");
        fileStorage.write(new Configuration(allDevelopers, List.of()));

        userInput
                .append("c\n") // choose pair
                .append("1\n"); // choose first option
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString(),
                "--devs", "jamie", "jorge", "reece", "andy");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0      0  \s
                         jamie          0      0      0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0     1 * \s
                         jamie          0     1 *     0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0     1 * \s
                         jamie          0     1 *     0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();


        assertThat(fileStorage.read())
                .isEqualTo(new Configuration(List.of("andy", "jamie", "jorge", "reece"),
                        List.of(new Pairing(LocalDate.now(), "andy", "reece"),
                                new Pairing(LocalDate.now(), "jamie", "jorge"))));
    }

    @Test
    void allowStartUpIfConfigFileIsMissingButDevsAreSpecifiedAtCommandLine() throws IOException {
        FileStorage fileStorage = new FileStorage(dataFile);

        userInput
                .append("c\n") // choose pair
                .append("1\n"); // choose first option
        userInput.flush();
        int exitCode = underTest.execute("-f", dataFile.toAbsolutePath().toString(),
                "--devs", "jamie", "jorge", "reece", "andy");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(out.toString()))
                .isEqualTo("""
                        Yesterday's pair stairs
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0      0  \s
                         jamie          0      0      0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        Possible pairs (lowest score is better)
                                                
                        1. score = 5
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0     1 * \s
                         jamie          0     1 *     0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        See more options [n]
                        Choose from options [c]
                        Override with your own pairs [o]
                                                
                        Choose a suggestion [1-1]:
                                                
                        Picked 1:
                                                
                                andy  jamie  jorge  reece\s
                         andy    0      0      0     1 * \s
                         jamie          0     1 *     0  \s
                         jorge                 0      0  \s
                         reece                        0  \s
                                                
                        Saved pairings to: %s
                                                
                        """.formatted(dataFile));
        assertThat(unWindows(err.toString()))
                .isEmpty();


        assertThat(fileStorage.read())
                .isEqualTo(new Configuration(List.of("andy", "jamie", "jorge", "reece"),
                        List.of(new Pairing(LocalDate.now(), "andy", "reece"),
                                new Pairing(LocalDate.now(), "jamie", "jorge"))));
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
}