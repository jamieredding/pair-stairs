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
        userInput.append("kjhasdskjh\nkjahsdkh\n0\n0.9\n4\n2\n");
        userInput.flush();
        int exitCode = underTest.execute("jamie", "jorge", "reece");

        assertThat(exitCode).isEqualTo(0);
        assertThat(out.toString())
                .isEqualTo("""
                        Possible pairs (lowest score is better)
                                                
                        1. best choice (5):
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie   1 *     0      0  \s
                        \sjorge           0     1 * \s
                        \sreece                  0  \s
                                                
                        2. alternative (5):
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie    0      0     1 * \s
                        \sjorge          1 *     0  \s
                        \sreece                  0  \s
                                                
                        3. yet another (5):
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie    0     1 *     0  \s
                        \sjorge           0      0  \s
                        \sreece                 1 * \s
                        
                        Choose a suggestion [1-3]:
                        
                        Invalid input.
                        
                        Choose a suggestion [1-3]:
                        
                        Invalid input.
                        
                        Choose a suggestion [1-3]:
                        
                        Invalid input.
                        
                        Choose a suggestion [1-3]:
                        
                        Invalid input.
                        
                        Choose a suggestion [1-3]:
                        
                        Invalid input.
                        
                        Choose a suggestion [1-3]:
                        
                        Picked 2:
                                                
                        \s       jamie  jorge  reece\s
                        \sjamie    0      0     1 * \s
                        \sjorge          1 *     0  \s
                        \sreece                  0  \s
                        """);
        assertThat(err.toString()).isEmpty();
    }
}