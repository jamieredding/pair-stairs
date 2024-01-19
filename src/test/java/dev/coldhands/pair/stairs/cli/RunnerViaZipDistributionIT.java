package dev.coldhands.pair.stairs.cli;

import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

class RunnerViaZipDistributionIT extends RunnerContractIT {

    private static final String BASE_DIR = "target/";
    private static final String EXTRACTED_ZIP_DIR = BASE_DIR + PROPERTIES.getProperty("finalName");
    private static final String PATH_TO_SCRIPT = STR."\{EXTRACTED_ZIP_DIR}/bin/\{PROPERTIES.getProperty("scriptName")}";
    private static final Path CONFIG_FILE_PATH = Path.of(STR."\{EXTRACTED_ZIP_DIR}/config.json");

    @Override
    Path getConfigFilePath() {
        return CONFIG_FILE_PATH;
    }

    @Override
    String getPathToScript() {
        return PATH_TO_SCRIPT;
    }

    @BeforeAll
    static void unzipPackagedApplication() throws IOException {
        try (final ZipFile zip = new ZipFile(STR."\{BASE_DIR}\{PROPERTIES.getProperty("finalName")}.zip")) {
            zip.extractAll(BASE_DIR);
        }
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

}
