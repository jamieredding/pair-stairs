package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairStreamMapperTest {

    @Test
    void canMapFromCoreToDomain() {
        final PairStreamMapper underTest = new PairStreamMapper(
                new DeveloperMapper(Map.of(
                        0L, new DeveloperEntity(0L, "dev-0")
                )),
                new StreamMapper(Map.of(
                        0L, new StreamEntity(0L, "stream-a")
                )));

        final dev.coldhands.pair.stairs.backend.domain.PairStream actual = underTest.coreToDomain(new PairStream(Set.of("0"), "0"));

        assertThat(actual).isEqualTo(new dev.coldhands.pair.stairs.backend.domain.PairStream(
                List.of(
                        new DeveloperInfo(0, "dev-0")
                ),
                new StreamInfo(0, "stream-a")
        ));
    }

    @Test
    void willSortDevelopersByDisplayNameAlphabetically() {
        final PairStreamMapper underTest = new PairStreamMapper(
                new DeveloperMapper(Map.of(
                        0L, new DeveloperEntity(0L, "a"),
                        1L, new DeveloperEntity(1L, "b"),
                        2L, new DeveloperEntity(2L, "c")
                )),
                new StreamMapper(Map.of(
                        0L, new StreamEntity(0L, "stream-a")
                )));

        final dev.coldhands.pair.stairs.backend.domain.PairStream actual = underTest.coreToDomain(new PairStream(Set.of("0", "1", "2"), "0"));

        assertThat(actual.developers()).isEqualTo(List.of(
                new DeveloperInfo(0, "a"),
                new DeveloperInfo(1, "b"),
                new DeveloperInfo(2, "c")
        ));
    }
}