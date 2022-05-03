package dev.coldhands.pair.stairs.persistance;

import dev.coldhands.pair.stairs.Pair;
import dev.coldhands.pair.stairs.Pairing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageTest {

    @TempDir
    Path temp;

    @Test
    void canWriteDataToFile() throws IOException {
        Path dataFile = temp.resolve("data.json");

        Files.createFile(dataFile);

        List<Pairing> pairings = List.of(
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("c-dev", "d-dev")),
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("e-dev")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("d-dev", "e-dev")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("c-dev"))
        );

        FileStorage underTest = new FileStorage(dataFile);
        final Configuration configuration = new Configuration(
                List.of("c-dev", "d-dev", "e-dev"),
                List.of("e-dev"),
                pairings);
        underTest.write(configuration);

        assertThat(String.join("\n", Files.readAllLines(dataFile)))
                .isEqualTo("""
                                   {""" +
                           """
                                   "allDevelopers":["c-dev","d-dev","e-dev"],""" +
                           """
                                   "newJoiners":["e-dev"],""" +
                           """
                                   "pairings":[""" +
                           """
                                   {"date":"2022-04-29","pair":{"first":"c-dev","second":"d-dev"}},""" +
                           """
                                   {"date":"2022-04-29","pair":{"first":"e-dev","second":null}},""" +
                           """
                                   {"date":"2022-04-28","pair":{"first":"d-dev","second":"e-dev"}},""" +
                           """
                                   {"date":"2022-04-28","pair":{"first":"c-dev","second":null}}""" +
                           """
                                   ]}""");
    }

    @Test
    void willOverwriteExistingFile() throws IOException {
        Path dataFile = temp.resolve("data.json");

        Files.writeString(dataFile, "some not valid json");

        List<Pairing> pairings = List.of(new Pairing(LocalDate.of(2022, 4, 28), new Pair("c-dev")));

        FileStorage underTest = new FileStorage(dataFile);
        underTest.write(new Configuration(List.of("c-dev"), List.of(), pairings));

        assertThat(String.join("\n", Files.readAllLines(dataFile)))
                .isEqualTo("""
                                   {""" +
                           """
                                   "allDevelopers":["c-dev"],""" +
                           """
                                   "newJoiners":[],""" +
                           """
                                   "pairings":[""" +
                           """
                                   {"date":"2022-04-28","pair":{"first":"c-dev","second":null}}""" +
                           """
                                   ]}""");
    }

    @Test
    void canReadWrittenFile() throws IOException {
        Path dataFile = temp.resolve("data.json");

        List<Pairing> pairings = List.of(
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("c-dev", "d-dev")),
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("e-dev")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("d-dev", "e-dev")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("c-dev"))
        );
        Configuration configuration = new Configuration(List.of("c-dev", "d-dev", "e-dev"), List.of("e-dev"), pairings);

        FileStorage underTest = new FileStorage(dataFile);
        underTest.write(configuration);
        Configuration actual = underTest.read();

        assertThat(actual).isEqualTo(configuration);
    }

    @Test
    void readThrowsExceptionWhenUnableToParseFileContent() throws IOException {
        Path dataFile = temp.resolve("data.json");

        Files.writeString(dataFile, "not json");

        FileStorage underTest = new FileStorage(dataFile);
        assertThatThrownBy(underTest::read)
                .hasMessage("%s is not in the correct format. Will not read".formatted(dataFile));
    }
}