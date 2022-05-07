package dev.coldhands.pair.stairs.cli;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.persistance.ArtifactoryStorage;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;
import static dev.coldhands.pair.stairs.TestUtils.unWindows;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class RunnerWithArtifactoryStorageTest {

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();

    private CommandLine underTest;
    private OutputStreamWriter userInput;

    @BeforeEach
    void setUp() throws IOException {
        var outWriter = new PrintWriter(out);
        var errWriter = new PrintWriter(err);
        var input = new PipedInputStream();
        var output = new PipedOutputStream(input);
        userInput = new OutputStreamWriter(output);
        underTest = Runner.createCommandLine(input, outWriter, errWriter, Map.of(
                "ARTIFACTORY_USERNAME", "username",
                "ARTIFACTORY_PASSWORD", "password"));
        underTest.setOut(outWriter);
        underTest.setErr(errWriter);
    }

    private static final String FILE_PATH = "/upload/path/config.json";
    private static final int HIGHEST_PRIORITY = 1;
    private Storage storage;
    private String uploadLocation;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) {
        storage = new ArtifactoryStorage(
                wireMockRuntimeInfo.getHttpBaseUrl() + FILE_PATH,
                Map.of(
                        "ARTIFACTORY_USERNAME", "username",
                        "ARTIFACTORY_PASSWORD", "password"));
        stubFor(put(FILE_PATH)
                .atPriority(HIGHEST_PRIORITY + 1)
                .withBasicAuth("username", "password")
                .willReturn(aResponse()
                        .withStatus(200)));
        uploadLocation = wireMockRuntimeInfo.getHttpBaseUrl() + FILE_PATH;
    }

    @AfterEach
    void tearDown() {
        WireMock.reset();
    }

    @Test
    void runWithThreeDevelopers() throws Exception {
        List<String> allDevelopers = List.of("a-dev", "b-dev", "c-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        userInput
                .append("kjhasdskjh\n") // not a number
                .append("0.9\n") // not an integer
                .append("0\n") // too low bound
                .append("4\n") // too high bound
                .append("2\n"); // valid selection
        userInput.flush();
        int exitCode = underTest.execute("-a", uploadLocation);

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
                                                
                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEqualTo("""
                        Invalid input.
                                                
                        Invalid input.
                                               
                        Invalid input.
                                               
                        Invalid input.
                                                
                        """);
    }

    @Test
    void loadExistingPairingsFromFileAndPersistNewPairings() throws Exception {
        LocalDate now = LocalDate.now();

        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        persistConfiguration(new Configuration(allDevelopers,
                List.of(
                        new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                        new Pairing(now.minusDays(1), "d-dev")
                )));

        userInput
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-a", uploadLocation);

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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();

        assertThat(readPersistedData())
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
    void optionallySpecifyAbsentDeveloper() throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        userInput
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("-a", uploadLocation, "-i", "a-dev");

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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void ifOnlyOneOptionIsAvailableThenJustPrintThat() throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        int exitCode = underTest.execute("-a", uploadLocation, "-i", "a-dev", "e-dev");

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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void supportReadingNewJoinersFromConfiguration() throws Exception {
        LocalDate now = LocalDate.now();

        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        persistConfiguration(new Configuration(allDevelopers,
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
        int exitCode = underTest.execute("-a", uploadLocation);

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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();

        assertThat(readPersistedData())
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
    void supportOverridingNewJoinersAtCommandLine() throws Exception {
        LocalDate now = LocalDate.now();

        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        persistConfiguration(new Configuration(allDevelopers,
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
        int exitCode = underTest.execute("-a", uploadLocation, "--new", "c-dev", "e-dev");

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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();

        assertThat(readPersistedData())
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
    void allowOverridingWithYourOwnPairPick_oddNumberOfPairs() throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev", "b-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        userInput
                .append("o\n") // override
                .append("1 4\n") // choose a-dev and d-dev as pair
                .append("1 3\n"); // choose b-dev and e-dev as pair
        userInput.flush();
        int exitCode = underTest.execute("-a", uploadLocation);

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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void allowOverridingWithYourOwnPairPick_evenNumberOfPairs() throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        userInput
                .append("o\n") // override
                .append("1 2\n"); // choose a-dev and c-dev as pair
        userInput.flush();
        int exitCode = underTest.execute("-a", uploadLocation);

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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();
    }

    @Test
    void allowOverridingWithYourOwnPairPick_validation() throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

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
        int exitCode = underTest.execute("-a", uploadLocation);

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

                        """.formatted(storage.describe()));
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
    void allowSpecifyAllDevelopersAtCommandLineAndOverwriteTheConfigFile() throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        userInput
                .append("1\n"); // choose first option
        userInput.flush();
        int exitCode = underTest.execute("-a", uploadLocation,
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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();

        assertThat(readPersistedData())
                .isEqualTo("""
                                   {""" +
                           """
                                   "allDevelopers":["a-dev","c-dev","d-dev","e-dev"],""" +
                           """
                                   "newJoiners":[],""" +
                           """
                                   "pairings":[""" +
                           """
                                   {"date":"%s","pair":{"first":"a-dev","second":"e-dev"}},""".formatted(LocalDate.now()) +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":"d-dev"}}""".formatted(LocalDate.now()) +
                           """
                                   ]}""");
    }

    @Test
    void allowStartUpIfConfigFileIsMissingButDevsAreSpecifiedAtCommandLine() throws Exception {
        userInput
                .append("1\n"); // choose first option
        userInput.flush();
        int exitCode = underTest.execute("-a", uploadLocation,
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

                        """.formatted(storage.describe()));
        assertThat(unWindows(err.toString()))
                .isEmpty();


        assertThat(readPersistedData())
                .isEqualTo("""
                                   {""" +
                           """
                                   "allDevelopers":["a-dev","c-dev","d-dev","e-dev"],""" +
                           """
                                   "newJoiners":[],""" +
                           """
                                   "pairings":[""" +
                           """
                                   {"date":"%s","pair":{"first":"a-dev","second":"e-dev"}},""".formatted(LocalDate.now()) +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":"d-dev"}}""".formatted(LocalDate.now()) +
                           """
                                   ]}""");
    }

    @Test
    void errorIfNoConfigFileAndNoDevsSpecifiedAtCommandLine() {
        int exitCode = underTest.execute("-a", uploadLocation);

        assertThat(exitCode).isEqualTo(1);
        assertThat(unWindows(out.toString()))
                .isEmpty();
        assertThat(unWindows(err.toString()))
                .isEqualTo("""
                        Unable to start.
                        No pairs specified in %s

                        Rerun and specify which devs to include via the '--devs' option

                        """.formatted(storage.describe()));
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
                        
                        Persistence
                        
                          -f, --config-file=FILE   Data file to use for persistence.
                                                   This will contain all pairings that occur
                                                   and all of the developers to consider.
                          -a, --artifactory-location=URL
                                                   URL to config file to use for persistence.
                                                   Must be an artifactory instance.
                                                   Will use basic auth credentials from
                                                   the following environment variables:
                                                   ARTIFACTORY_USERNAME and ARTIFACTORY_PASSWORD
                        
                        Examples:
                        
                        First run with file persistence
                            pair-stairs.sh \\
                              -f /path/to/config.json \\
                              -d dev1 -d dev2 -d dev3
                        
                        First run with artifactory persistence
                            pair-stairs.sh \\
                              -a http://artifactory/libs-snapshots-local/my/config.json \\
                              -d dev1 -d dev2 -d dev3
                        """);
    }

    private void persistConfiguration(Configuration pairings) throws Exception {
        storage.write(pairings);

        writePersistedData(readPersistedData());
    }

    public String readPersistedData() {
        return WireMock.findAll(allRequests()).stream()
                .sorted(Comparator.comparing(LoggedRequest::getLoggedDate)
                        .reversed())
                .map(LoggedRequest::getBodyAsString)
                .findFirst()
                .get();
    }

    public void writePersistedData(String data) {
        stubFor(get(FILE_PATH)
                .atPriority(HIGHEST_PRIORITY)
                .withBasicAuth("username", "password")
                .willReturn(aResponse()
                        .withBody(data)));
    }

    public String storageDescription() {
        return "Artifactory -> " + uploadLocation;
    }
}