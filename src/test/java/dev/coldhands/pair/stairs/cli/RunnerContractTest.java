package dev.coldhands.pair.stairs.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import dev.coldhands.pair.stairs.DecideOMatic;
import dev.coldhands.pair.stairs.PairPrinter;
import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static dev.coldhands.pair.stairs.TestUtils.unWindows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@ExtendWith(StdIOExtension.class)
interface RunnerContractTest {

    Logger LOGGER = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);

    @AfterEach
    default void resetLoggingLevel() {
        LOGGER.setLevel(Level.INFO);
    }

    @Test
    default void runWithThreeDevelopers(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("a-dev", "b-dev", "c-dev");
        List<Pairing> pairings = List.of();
        persistConfiguration(new Configuration(allDevelopers, pairings));

        stdIO.inputWriter()
                .append("kjhasdskjh\n") // not a number
                .append("0.9\n") // not an integer
                .append("0\n") // too low bound
                .append("4\n") // too high bound
                .append("2\n"); // valid selection
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)
                                                           
                                   """ + pairChoices(pairings, allDevelopers) + """  
                                       
                                                         
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
                                                           
                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
                .isEqualTo("""
                        Invalid input.
                                                
                        Invalid input.
                                               
                        Invalid input.
                                               
                        Invalid input.
                                                
                        """);
    }

    @Test
    default void loadExistingPairingsFromFileAndPersistNewPairings(StdIO stdIO) throws Exception {
        LocalDate now = LocalDate.now();

        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev")
        );
        persistConfiguration(new Configuration(allDevelopers, pairings));

        stdIO.inputWriter()
                .append("1\n"); // choose a pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                        Options (lowest score is better)

                                   """ + pairChoices(pairings, allDevelopers) + """
                                                           

                                   Choose a suggestion [1-3]:
                                   Or override with your own pairs [o]

                                   Picked 1:

                                           c-dev  d-dev  e-dev\s
                                    c-dev    0     1 *     1  \s
                                    d-dev           1      0  \s
                                    e-dev                 1 * \s

                                   Saved pairings to: %s

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
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
    default void optionallySpecifyAbsentDeveloper(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        List<Pairing> pairings = List.of();
        persistConfiguration(new Configuration(allDevelopers, pairings));

        stdIO.inputWriter()
                .append("1\n"); // choose a pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest("-i", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)
                                                           
                                   """ + pairChoices(pairings, List.of("c-dev", "d-dev", "e-dev")) + """
                                                           

                                   Choose a suggestion [1-3]:
                                   Or override with your own pairs [o]

                                   Picked 1:

                                           a-dev  c-dev  d-dev  e-dev\s
                                    a-dev    0      0      0      0  \s
                                    c-dev           0     1 *     0  \s
                                    d-dev                  0      0  \s
                                    e-dev                        1 * \s

                                   Saved pairings to: %s

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
                .isEmpty();
    }

    @Test
    default void ifOnlyOneOptionIsAvailableThenJustPrintThat(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        int exitCode = executeUnderTest("-i", "a-dev", "e-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                        Only one option:

                                a-dev  c-dev  d-dev  e-dev\s
                         a-dev    0      0      0      0  \s
                         c-dev           0     1 *     0  \s
                         d-dev                  0      0  \s
                         e-dev                         0  \s

                        Saved pairings to: %s

                        """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
                .isEmpty();
    }

    static Stream<Arguments> canSetLoggingLevelFromArgument() {
        return Stream.of(
                arguments(new String[]{"--verbose"}, Level.DEBUG),
                arguments(new String[]{}, Level.INFO)
        );
    }

    @ParameterizedTest
    @MethodSource
    default void canSetLoggingLevelFromArgument(String[] args, Level logLevel) throws Exception {
        List<String> allDevelopers = List.of("a-dev", "b-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        int exitCode = executeUnderTest(args);

        assertThat(exitCode).isEqualTo(0);

        assertThat(LOGGER.getLevel()).isEqualTo(logLevel);
    }

    @Test
    default void supportReadingNewJoinersFromConfiguration(StdIO stdIO) throws Exception {
        LocalDate now = LocalDate.now();

        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev"),
                new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                new Pairing(now.minusDays(2), "c-dev")
        );
        List<String> newJoiners = List.of("e-dev");
        persistConfiguration(new Configuration(allDevelopers, newJoiners, pairings));

        stdIO.inputWriter()
                .append("1\n"); // choose a pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)
                                                                      
                                   """ + pairChoices(pairings, allDevelopers, newJoiners) + """
                                                           

                                   Choose a suggestion [1-3]:
                                   Or override with your own pairs [o]

                                   Picked 1:

                                           c-dev  d-dev  e-dev\s
                                    c-dev   2 *     0      1  \s
                                    d-dev           1     2 * \s
                                    e-dev                  0  \s

                                   Saved pairings to: %s

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
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
    default void supportOverridingNewJoinersAtCommandLine(StdIO stdIO) throws Exception {
        LocalDate now = LocalDate.now();

        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        List<Pairing> pairings = List.of(
                new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                new Pairing(now.minusDays(1), "d-dev"),
                new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                new Pairing(now.minusDays(2), "c-dev")
        );
        persistConfiguration(new Configuration(allDevelopers,
                List.of(),
                pairings));

        stdIO.inputWriter()
                .append("1\n"); // choose a pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest("--new", "c-dev", "e-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)
                                                           
                                   """ + pairChoices(pairings, allDevelopers, List.of("c-dev", "e-dev")) + """


                                   Choose a suggestion [1-3]:
                                   Or override with your own pairs [o]

                                   Picked 1:

                                           c-dev  d-dev  e-dev\s
                                    c-dev    1      0     2 * \s
                                    d-dev          2 *     1  \s
                                    e-dev                  0  \s

                                   Saved pairings to: %s

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
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
    default void allowOverridingWithYourOwnPairPick_oddNumberOfPairs(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev", "b-dev");
        List<Pairing> pairings = List.of();
        persistConfiguration(new Configuration(allDevelopers, pairings));

        stdIO.inputWriter()
                .append("o\n") // override
                .append("1 4\n") // choose a-dev and d-dev as pair
                .append("1 3\n"); // choose b-dev and e-dev as pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)

                                   """ + pairChoices(pairings, allDevelopers) + """


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

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
                .isEmpty();
    }

    @Test
    default void allowOverridingWithYourOwnPairPick_evenNumberOfPairs(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        List<Pairing> pairings = List.of();
        persistConfiguration(new Configuration(allDevelopers, pairings));

        stdIO.inputWriter()
                .append("o\n") // override
                .append("1 2\n"); // choose a-dev and c-dev as pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)

                                   """ + pairChoices(pairings, allDevelopers) + """


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

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
                .isEmpty();
    }

    @Test
    default void allowOverridingWithYourOwnPairPick_validation(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        List<Pairing> pairings = List.of();
        persistConfiguration(new Configuration(allDevelopers, pairings));

        stdIO.inputWriter()
                .append("o\n") // override
                .append("aslkdj\n")
                .append("1\n")
                .append("1 2 3\n")
                .append("0.1 2\n")
                .append("0 1\n")
                .append("1 5\n")
                .append("1 2\n"); // choose a-dev and c-dev as pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)

                                   """ + pairChoices(pairings, allDevelopers) + """


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

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
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
    default void allowSpecifyAllDevelopersAtCommandLineAndOverwriteTheConfigFile(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev");
        List<Pairing> pairings = List.of();
        persistConfiguration(new Configuration(allDevelopers, pairings));

        stdIO.inputWriter()
                .append("1\n"); // choose first option
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest(
                "--devs", "c-dev", "d-dev", "e-dev", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)

                                   """ + pairChoices(pairings, List.of("c-dev", "d-dev", "e-dev", "a-dev")) + """


                                   Choose a suggestion [1-3]:
                                   Or override with your own pairs [o]

                                   Picked 1:

                                           a-dev  c-dev  d-dev  e-dev\s
                                    a-dev    0      0      0     1 * \s
                                    c-dev           0     1 *     0  \s
                                    d-dev                  0      0  \s
                                    e-dev                         0  \s

                                   Saved pairings to: %s

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
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
    default void allowStartUpIfConfigFileIsMissingButDevsAreSpecifiedAtCommandLine(StdIO stdIO) throws Exception {
        stdIO.inputWriter()
                .append("1\n"); // choose first option
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest(
                "--devs", "c-dev", "d-dev", "e-dev", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)

                                   """ + pairChoices(List.of(), List.of("c-dev", "d-dev", "e-dev", "a-dev")) + """


                                   Choose a suggestion [1-3]:
                                   Or override with your own pairs [o]

                                   Picked 1:

                                           a-dev  c-dev  d-dev  e-dev\s
                                    a-dev    0      0      0     1 * \s
                                    c-dev           0     1 *     0  \s
                                    d-dev                  0      0  \s
                                    e-dev                         0  \s

                                   Saved pairings to: %s

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
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
    default void ignoreTodaysPairsInCaseItIsRunTwiceInOneDay(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        LocalDate now = LocalDate.now();
        persistConfiguration(new Configuration(allDevelopers, List.of(
                new Pairing(now, "a-dev", "e-dev"),
                new Pairing(now, "c-dev", "d-dev")
        )));

        stdIO.inputWriter()
                .append("2\n"); // choose second option
        stdIO.inputWriter().flush();

        int exitCode = executeUnderTest(
                "--devs", "c-dev", "d-dev", "e-dev", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                                   Options (lowest score is better)

                                   """ + pairChoices(List.of(), List.of("c-dev", "d-dev", "e-dev", "a-dev")) + """
                                           
                                           
                                   Choose a suggestion [1-3]:
                                   Or override with your own pairs [o]

                                   Picked 2:

                                           a-dev  c-dev  d-dev  e-dev\s
                                    a-dev    0      0     1 *     0  \s
                                    c-dev           0      0     1 * \s
                                    d-dev                  0      0  \s
                                    e-dev                         0  \s

                                   Saved pairings to: %s

                                   """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
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
                                   {"date":"%s","pair":{"first":"a-dev","second":"d-dev"}},""".formatted(now) +
                           """
                                   {"date":"%s","pair":{"first":"c-dev","second":"e-dev"}}""".formatted(now) +
                           """
                                   ]}""");
    }

    @Test
    default void errorIfNoConfigFileAndNoDevsSpecifiedAtCommandLine(StdIO stdIO) {
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(1);
        assertThat(unWindows(stdIO.out().toString()))
                .isEmpty();
        assertThat(unWindows(stdIO.err().toString()))
                .isEqualTo("""
                        Unable to start.
                        No pairs specified in %s

                        Rerun and specify which devs to include via the '--devs' option

                        """.formatted(storage().describe()));
    }

    @Test
    default void usageText(StdIO stdIO) {
        int exitCode = underTest().execute("-h");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
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
                              --verbose            Enable verbose output.
                          -h, --help               Display this help message.
                              --version            Display version information and exit.
                                                
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

    @Test
    default void versionOutput(StdIO stdIO) {
        givenTheManifestContainsLine("Implementation-Version: manifestVersion");
        givenTheManifestContainsLine("Implementation-Title: pair-stairs");

        int exitCode = underTest().execute("--version");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
                        manifestVersion
                        """);
    }

    static void givenTheManifestContainsLine(String manifestLine) {
        ClassLoader classLoader = RunnerContractTest.class.getClassLoader();
        boolean found = classLoader.resources("META-INF/MANIFEST.MF")
                .anyMatch(url -> {
                    try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                        return bufferedReader.lines()
                                .anyMatch(line -> line.equals(manifestLine));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        assertThat(found)
                .describedAs("manifest to contain line " + manifestLine)
                .isTrue();

    }

    void persistConfiguration(Configuration pairings) throws Exception;

    String readPersistedData() throws Exception;

    int executeUnderTest(String... args);

    CommandLine underTest();

    Storage storage();

    private String pairChoices(List<Pairing> pairings, List<String> allDevelopers) {
        return pairChoices(pairings, allDevelopers, List.of());
    }

    private String pairChoices(List<Pairing> pairings, List<String> allDevelopers, List<String> newJoiners) {
        return PairPrinter.drawPairChoices(new DecideOMatic(pairings, new HashSet<>(allDevelopers), new HashSet<>(newJoiners))
                .getScoredPairCombinations(), 3);
    }
}