package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.Pairing;
import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import picocli.CommandLine;

import java.time.LocalDate;
import java.util.List;

import static dev.coldhands.pair.stairs.TestUtils.unWindows;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(StdIOExtension.class)
interface RunnerContractTest {

    @Test
    default void runWithThreeDevelopers(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("a-dev", "b-dev", "c-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

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
        persistConfiguration(new Configuration(allDevelopers,
                List.of(
                        new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                        new Pairing(now.minusDays(1), "d-dev")
                )));

        stdIO.inputWriter()
                .append("1\n"); // choose a pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
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
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        stdIO.inputWriter()
                .append("1\n"); // choose a pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest("-i", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
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

    @Test
    default void supportReadingNewJoinersFromConfiguration(StdIO stdIO) throws Exception {
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

        stdIO.inputWriter()
                .append("1\n"); // choose a pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
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
        persistConfiguration(new Configuration(allDevelopers,
                List.of(),
                List.of(
                        new Pairing(now.minusDays(1), "c-dev", "e-dev"),
                        new Pairing(now.minusDays(1), "d-dev"),
                        new Pairing(now.minusDays(2), "d-dev", "e-dev"),
                        new Pairing(now.minusDays(2), "c-dev")
                )));

        stdIO.inputWriter()
                .append("1\n"); // choose a pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest("--new", "c-dev", "e-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
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
        persistConfiguration(new Configuration(allDevelopers, List.of()));

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

                        """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
                .isEmpty();
    }

    @Test
    default void allowOverridingWithYourOwnPairPick_evenNumberOfPairs(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        stdIO.inputWriter()
                .append("o\n") // override
                .append("1 2\n"); // choose a-dev and c-dev as pair
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest();

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
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

                        """.formatted(storage().describe()));
        assertThat(unWindows(stdIO.err().toString()))
                .isEmpty();
    }

    @Test
    default void allowOverridingWithYourOwnPairPick_validation(StdIO stdIO) throws Exception {
        List<String> allDevelopers = List.of("c-dev", "d-dev", "e-dev", "a-dev");
        persistConfiguration(new Configuration(allDevelopers, List.of()));

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
        persistConfiguration(new Configuration(allDevelopers, List.of()));

        stdIO.inputWriter()
                .append("1\n"); // choose first option
        stdIO.inputWriter().flush();
        int exitCode = executeUnderTest(
                "--devs", "c-dev", "d-dev", "e-dev", "a-dev");

        assertThat(exitCode).isEqualTo(0);
        assertThat(unWindows(stdIO.out().toString()))
                .isEqualTo("""
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

    void persistConfiguration(Configuration pairings) throws Exception;

    String readPersistedData() throws Exception;

    int executeUnderTest(String... args);

    CommandLine underTest();

    Storage storage();
}