package dev.coldhands.pair.stairs.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerTest {

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
        underTest = Runner.createCommandLine(input, outWriter, errWriter);
        underTest.setOut(outWriter);
        underTest.setErr(errWriter);
    }

    @Test
    void runWithThreeDevelopers() throws IOException {
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
        int exitCode = underTest.execute("jamie", "jorge", "reece");

        assertThat(exitCode).isEqualTo(0);
        assertThat(out.toString())
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
                        """);
        assertThat(err.toString())
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
        userInput
                .append("n\n") // show next pair
                .append("n\n") // show next pair
                .append("n\n") // show a non existing pair
                .append("1\n"); // choose a pair
        userInput.flush();
        int exitCode = underTest.execute("jamie", "jorge", "reece");

        assertThat(exitCode).isEqualTo(0);
        assertThat(out.toString())
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
                        """);
        assertThat(err.toString())
                .isEmpty();
    }
}