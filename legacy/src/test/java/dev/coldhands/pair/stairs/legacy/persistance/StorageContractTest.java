package dev.coldhands.pair.stairs.legacy.persistance;

import dev.coldhands.pair.stairs.legacy.domain.Pair;
import dev.coldhands.pair.stairs.legacy.domain.Pairing;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

interface StorageContractTest {

    @Test
    default void canWriteDataToFile() throws Exception {
        List<Pairing> pairings = List.of(
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("c-dev", "d-dev")),
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("e-dev")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("d-dev", "e-dev")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("c-dev"))
        );

        final Configuration configuration = new Configuration(
                List.of("c-dev", "d-dev", "e-dev"),
                List.of("e-dev"),
                pairings);
        underTest().write(configuration);

        assertThat(readPersistedData())
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
    default void willOverwriteExistingFile() throws Exception {
        writePersistedData("some not valid json");

        List<Pairing> pairings = List.of(new Pairing(LocalDate.of(2022, 4, 28), new Pair("c-dev")));

        underTest().write(new Configuration(List.of("c-dev"), List.of(), pairings));

        assertThat(readPersistedData())
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
    default void canReadWrittenFile() throws Exception {
        List<Pairing> pairings = List.of(
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("c-dev", "d-dev")),
                new Pairing(LocalDate.of(2022, 4, 29), new Pair("e-dev")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("d-dev", "e-dev")),
                new Pairing(LocalDate.of(2022, 4, 28), new Pair("c-dev"))
        );
        Configuration configuration = new Configuration(List.of("c-dev", "d-dev", "e-dev"), List.of("e-dev"), pairings);

        underTest().write(configuration);
        String persistedData = readPersistedData();
        writePersistedData(persistedData);
        Configuration actual = underTest().read();

        assertThat(actual).isEqualTo(configuration);
    }

    @Test
    default void readThrowsExceptionWhenUnableToParseFileContent() throws Exception {
        writePersistedData("not json");

        assertThatThrownBy(underTest()::read)
                .isNotNull();
    }

    @Test
    default void readThrowsNoSuchFileExceptionWhenUnableToReadFile() {
        assertThatThrownBy(underTest()::read)
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    default void describePersistenceLocation() {
        assertThat(underTest().describe())
                .isEqualTo(storageDescription());
    }

    Storage underTest();

    String readPersistedData() throws IOException;

    void writePersistedData(String data) throws IOException;

    String storageDescription();
}