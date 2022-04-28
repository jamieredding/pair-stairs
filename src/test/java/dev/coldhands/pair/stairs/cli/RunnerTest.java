package dev.coldhands.pair.stairs.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerTest {

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();

    private CommandLine underTest;

    @BeforeEach
    void setUp() {
        var outWriter = new PrintWriter(out);
        var errWriter = new PrintWriter(err);
        underTest = Runner.createCommandLine(outWriter, errWriter);
        underTest.setOut(outWriter);
        underTest.setErr(errWriter);
    }

    @Test
    void runWithThreeDevelopers() {
        int exitCode = underTest.execute("jamie", "jorge", "reece");

        assertThat(exitCode).isEqualTo(0);
        assertThat(out.toString())
                .isEqualTo("""
                        Possible pairs (lowest score is better)
                        
                        best choice (5):
                        
                        \s       jamie  jorge  reece\s
                        \sjamie   1 *     0      0  \s
                        \sjorge           0     1 * \s
                        \sreece                  0  \s
                        
                        alternative (5):
                        
                        \s       jamie  jorge  reece\s
                        \sjamie    0      0     1 * \s
                        \sjorge          1 *     0  \s
                        \sreece                  0  \s
                        
                        yet another (5):
                        
                        \s       jamie  jorge  reece\s
                        \sjamie    0     1 *     0  \s
                        \sjorge           0      0  \s
                        \sreece                 1 * \s
                        """);
        assertThat(err.toString()).isEmpty();
    }
}