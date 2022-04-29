package dev.coldhands.pair.stairs.persistance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.coldhands.pair.stairs.Pairing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

class FileStorage {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS);
    private final Path dataFile;

    public FileStorage(Path dataFile) {
        this.dataFile = dataFile;
    }

    public void write(List<Pairing> pairings) throws IOException {
        objectMapper.writeValue(dataFile.toFile(), pairings);
    }

    public List<Pairing> read() throws IOException {
        return objectMapper.readValue(Files.newInputStream(dataFile), new TypeReference<>() {
        });
    }
}
