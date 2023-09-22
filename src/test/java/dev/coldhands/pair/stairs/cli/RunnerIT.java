package dev.coldhands.pair.stairs.cli;

import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.nio.file.FileVisitResult.*;
import static org.assertj.core.api.Assertions.assertThat;

public class RunnerIT {

    private static final Properties PROPERTIES = loadProperties();
    private static final String BASE_DIR = "target/";
    private static final String EXTRACTED_ZIP_DIR = BASE_DIR + PROPERTIES.getProperty("finalName");
    private static final String PATH_TO_SCRIPT = EXTRACTED_ZIP_DIR + "/bin/" + PROPERTIES.getProperty("scriptName");
    private static final Path CONFIG_FILE_PATH = Path.of(EXTRACTED_ZIP_DIR + "/config.json");

    @BeforeAll
    static void unzipPackagedApplication() throws IOException {
        try (final ZipFile zip = new ZipFile(BASE_DIR + PROPERTIES.getProperty("finalName") + ".zip")) {
            zip.extractAll(BASE_DIR);
        }
    }

    @AfterEach
    void deleteConfigFile() throws IOException {
        Files.deleteIfExists(CONFIG_FILE_PATH);
    }

    @AfterAll
    static void deleteUnzippedDirectory() throws IOException {
        Files.walkFileTree(Path.of(EXTRACTED_ZIP_DIR), new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }

    @Test
    void runHelpOption() throws IOException, ExecutionException, InterruptedException {
        ProcessResult result = runProcess(PATH_TO_SCRIPT, "--help");

        assertThat(result.stderr).isEmpty();
        assertThat(result.stdout).startsWith("Usage: pair-stairs.sh [OPTIONS]");
        assertThat(result.exitCode).isEqualTo(0);
    }

    @Test
    void runVersionOption() throws IOException, ExecutionException, InterruptedException {
        ProcessResult result = runProcess(PATH_TO_SCRIPT, "--version");

        assertThat(result.stderr).isEmpty();
        assertThat(result.stdout).startsWith(PROPERTIES.getProperty("version"));
        assertThat(result.exitCode).isEqualTo(0);
    }

    @Test
    void smokeTest() throws IOException, ExecutionException, InterruptedException {
        ProcessResult result = runProcess(PATH_TO_SCRIPT,
                "-f", CONFIG_FILE_PATH.toString(),
                "-d", "a-dev",
                "-d", "b-dev");

        assertThat(result.stderr).isEmpty();
        assertThat(result.stdout).startsWith("Only one option:");
        assertThat(result.exitCode).isEqualTo(0);

        assertThat(CONFIG_FILE_PATH)
                .exists()
                .content()
                .contains("a-dev", "b-dev");
    }

    @Test
    void smokeTestWithVerboseOption() throws IOException, ExecutionException, InterruptedException {
        ProcessResult result = runProcess(PATH_TO_SCRIPT,
                "--verbose",
                "-f", CONFIG_FILE_PATH.toString(),
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
            properties.load(RunnerIT.class.getClassLoader().getResourceAsStream("integration-test.properties"));
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
