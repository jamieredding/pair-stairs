package dev.coldhands.pair.stairs.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

abstract class RunnerContractIT {

    static final Properties PROPERTIES = loadProperties();

    abstract Path getConfigFilePath();

    abstract String getPathToScript();

    @AfterEach
    final void deleteConfigFile() throws IOException {
        Files.deleteIfExists(getConfigFilePath());
    }

    @Test
    final void runHelpOption() throws IOException, ExecutionException, InterruptedException {
        ProcessResult result = runProcess(getPathToScript(), "--help");

        assertThat(result.stderr).isEmpty();
        assertThat(result.stdout).startsWith("Usage: pair-stairs.sh [OPTIONS]");
        assertThat(result.exitCode).isEqualTo(0);
    }

    @Test
    final void runVersionOption() throws IOException, ExecutionException, InterruptedException {
        ProcessResult result = runProcess(getPathToScript(), "--version");

        assertThat(result.stderr).isEmpty();
        assertThat(result.stdout).startsWith(PROPERTIES.getProperty("version"));
        assertThat(result.exitCode).isEqualTo(0);
    }

    @Test
    final void smokeTest() throws IOException, ExecutionException, InterruptedException {
        ProcessResult result = runProcess(getPathToScript(),
                "-f", getConfigFilePath().toString(),
                "-d", "a-dev",
                "-d", "b-dev");

        assertThat(result.stderr).isEmpty();
        assertThat(result.stdout).startsWith("Only one option:");
        assertThat(result.exitCode).isEqualTo(0);

        assertThat(getConfigFilePath())
                .exists()
                .content()
                .contains("a-dev", "b-dev");
    }

    @Test
    final void smokeTestWithVerboseOption() throws IOException, ExecutionException, InterruptedException {
        ProcessResult result = runProcess(getPathToScript(),
                "--verbose",
                "-f", getConfigFilePath().toString(),
                "-d", "a-dev",
                "-d", "b-dev");

        assertThat(result.stderr).isEmpty();

        List<String> stdoutLines = result.stdout.lines().toList();

        assertThat(stdoutLines.get(0)).contains("[main] DEBUG", "Pairs and their score:");
        assertThat(stdoutLines.get(2)).startsWith("a-dev b-dev ->");

        assertThat(result.stdout)
                .contains("Only one option:");

        assertThat(result.exitCode).isEqualTo(0);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(RunnerContractIT.class.getClassLoader().getResourceAsStream("integration-test.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private record ProcessResult(int exitCode, String stdout, String stderr) {
    }

    private ProcessResult runProcess(String... command) throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        Process process = processBuilder.start();

        CompletableFuture<String> stdout = readStream(process.getInputStream());
        CompletableFuture<String> stderr = readStream(process.getErrorStream());

        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timed out waiting for command to finish: " + Arrays.toString(command));
        }

        return new ProcessResult(process.exitValue(), stdout.get(), stderr.get());
    }

    private CompletableFuture<String> readStream(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return output.toString();
        });
    }

}
