package dev.coldhands.pair.stairs.legacy.persistance;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStorage implements Storage {

    private final ObjectMapper objectMapper = ConfigurationUtils.objectMapper();

    private final Path dataFile;

    public FileStorage(Path dataFile) {
        this.dataFile = dataFile;
    }

    @Override
    public void write(Configuration pairings) throws IOException {
        objectMapper.writeValue(dataFile.toFile(), pairings);
    }

    @Override
    public Configuration read() throws IOException {
        try {
            return objectMapper.readValue(Files.newInputStream(dataFile), new TypeReference<>() {
            });
        } catch (JacksonException e) {
            throw new RuntimeException("%s is not in the correct format. Will not read".formatted(dataFile), e);
        }
    }

    @Override
    public String describe() {
        return STR."File -> \{dataFile.toAbsolutePath().toAbsolutePath()}";
    }

    public Path getDataFile() {
        return dataFile;
    }
}
