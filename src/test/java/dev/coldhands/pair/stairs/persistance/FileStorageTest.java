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
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("jamie", "jorge")),
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("reece")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("jorge", "reece")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("jamie"))
        );

        FileStorage underTest = new FileStorage(dataFile);
        underTest.write(pairings);

        assertThat(String.join("\n", Files.readAllLines(dataFile)))
                .isEqualTo("""
                                   [""" +
                           """
                                   {"date":"2022-04-29","pair":{"first":"jamie","second":"jorge"}},""" +
                           """
                                   {"date":"2022-04-29","pair":{"first":"reece","second":null}},""" +
                           """
                                   {"date":"2022-04-28","pair":{"first":"jorge","second":"reece"}},""" +
                           """
                                   {"date":"2022-04-28","pair":{"first":"jamie","second":null}}""" +
                           """
                                   ]""");
    }

    @Test
    void willOverwriteExistingFile() throws IOException {
        Path dataFile = temp.resolve("data.json");

        Files.writeString(dataFile, "some not valid json");

        List<Pairing> pairings = List.of(new Pairing(LocalDate.of(2022, 4, 28), new Pair("jamie")));

        FileStorage underTest = new FileStorage(dataFile);
        underTest.write(pairings);

        assertThat(String.join("\n", Files.readAllLines(dataFile)))
                .isEqualTo("""
                                   [""" +
                           """
                                   {"date":"2022-04-28","pair":{"first":"jamie","second":null}}""" +
                           """
                                   ]""");
    }

    @Test
    void canReadWrittenFile() throws IOException {
        Path dataFile = temp.resolve("data.json");

        List<Pairing> pairings = List.of(
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("jamie", "jorge")),
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("reece")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("jorge", "reece")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("jamie"))
        );

        FileStorage underTest = new FileStorage(dataFile);
        underTest.write(pairings);
        List<Pairing> actual = underTest.read();

        assertThat(actual).isEqualTo(pairings);
    }

    @Test
    void readReturnsEmptyDataWhenFileDoesNotExist() throws IOException {
        Path dataFile = temp.resolve("data.json");

        FileStorage underTest = new FileStorage(dataFile);
        List<Pairing> actual = underTest.read();

        assertThat(actual).isEmpty();
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