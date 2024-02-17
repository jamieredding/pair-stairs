package dev.coldhands.pair.stairs.persistance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileStorageTest implements StorageContractTest {

    @TempDir
    Path temp;

    private Path dataFile;
    private Storage underTest;

    @BeforeEach
    void setUp() {
        dataFile = temp.resolve("data.json");
        underTest = new FileStorage(dataFile);
    }

    @Override
    public Storage underTest() {
        return underTest;
    }

    @Override
    public String readPersistedData() throws IOException {
        return String.join("\n", Files.readAllLines(dataFile));
    }

    @Override
    public void writePersistedData(String data) throws IOException {
        Files.writeString(dataFile, data);
    }

    @Override
    public String storageDescription() {
        return STR."File -> \{dataFile.toAbsolutePath()}";
    }
}