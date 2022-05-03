package dev.coldhands.pair.stairs.persistance;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public class FileStorage {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS);
    private final Path dataFile;

    public FileStorage(Path dataFile) {
        this.dataFile = dataFile;
    }

    public void write(Configuration pairings) throws IOException {
        objectMapper.writeValue(dataFile.toFile(), pairings);
    }

    public Configuration read() throws IOException {
        try {
            return objectMapper.readValue(Files.newInputStream(dataFile), new TypeReference<>() {
            });
        } catch (JacksonException e) {
            throw new RuntimeException("%s is not in the correct format. Will not read".formatted(dataFile), e);
        }
    }

    public Path getDataFile() {
        return dataFile;
    }
}
