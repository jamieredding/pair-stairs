package dev.coldhands.pair.stairs.cli;

import dev.coldhands.pair.stairs.persistance.Configuration;
import dev.coldhands.pair.stairs.persistance.FileStorage;
import dev.coldhands.pair.stairs.persistance.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

class RunnerWithFileStorageTest implements RunnerContractTest {

    @TempDir
    Path temp;

    private CommandLine underTest;
    private Path dataFile;
    private Storage storage;

    @BeforeEach
    void setUp(StdIO stdIO) {
        underTest = Runner.createCommandLine(stdIO.in(), stdIO.outWriter(), stdIO.errWriter(), Map.of());
        underTest.setOut(stdIO.outWriter());
        underTest.setErr(stdIO.errWriter());

        dataFile = temp.resolve("data.json");

        storage = new FileStorage(dataFile);
    }

    @Override
    public void persistConfiguration(Configuration pairings) throws Exception {
        storage.write(pairings);
    }

    @Override
    public String readPersistedData() throws Exception {
        return String.join("\n", Files.readAllLines(dataFile));
    }

    @Override
    public int executeUnderTest(String... args) {
        String[] actualArgs = Arrays.copyOf(args, args.length + 2);
        actualArgs[actualArgs.length - 2] = "-f";
        actualArgs[actualArgs.length - 1] = dataFile.toAbsolutePath().toString();

        return underTest.execute(actualArgs);
    }

    @Override
    public CommandLine underTest() {
        return underTest;
    }

    @Override
    public Storage storage() {
        return storage;
    }
}
