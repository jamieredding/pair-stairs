package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PairStreamMapperTest {

    @Nested
    class CoreToDomain {

        @Test
        void canMapFromCoreToDomain() {
            LookupById<DeveloperEntity> developerLookup = _ -> new DeveloperEntity(0L, "dev-0", false);
            LookupById<StreamEntity> streamLookup = _ -> new StreamEntity(0L, "stream-a", false);

            final dev.coldhands.pair.stairs.backend.domain.PairStream actual = PairStreamMapper.coreToDomain(new PairStream(Set.of("0"), "0"),
                    developerLookup, streamLookup);

            assertThat(actual).isEqualTo(new dev.coldhands.pair.stairs.backend.domain.PairStream(
                    List.of(
                            new DeveloperInfo(0, "dev-0", false)
                    ),
                    new StreamInfo(0, "stream-a", false)
            ));
        }

        @Test
        void willSortDevelopersByDisplayNameAlphabetically() {
            LookupById<DeveloperEntity> developerLookup = id ->
                    Map.of(
                            0L, new DeveloperEntity(0L, "a", false),
                            1L, new DeveloperEntity(1L, "b", false),
                            2L, new DeveloperEntity(2L, "c", false)
                    ).get(id);
            LookupById<StreamEntity> streamLookup = id ->
                    Map.of(
                            0L, new StreamEntity(0L, "stream-a", false)
                    ).get(id);

            final dev.coldhands.pair.stairs.backend.domain.PairStream actual = PairStreamMapper.coreToDomain(new PairStream(Set.of("0", "1", "2"), "0"),
                    developerLookup, streamLookup);

            assertThat(actual.developers()).isEqualTo(List.of(
                    new DeveloperInfo(0, "a", false),
                    new DeveloperInfo(1, "b", false),
                    new DeveloperInfo(2, "c", false)
            ));
        }
    }

    @Nested
    class EntityToInfo {

        @Test
        void canMapFromEntityToInfo() {
            final dev.coldhands.pair.stairs.backend.domain.PairStream actual = PairStreamMapper.entityToInfo(
                    new PairStreamEntity(
                            List.of(new DeveloperEntity(0L, "dev-0", false), new DeveloperEntity(1L, "dev-1", false)),
                            new StreamEntity(10L, "stream-a", false)
                    ));

            assertThat(actual).isEqualTo(new dev.coldhands.pair.stairs.backend.domain.PairStream(
                    List.of(
                            new DeveloperInfo(0, "dev-0", false),
                            new DeveloperInfo(1, "dev-1", false)
                    ),
                    new StreamInfo(10, "stream-a", false)
            ));
        }

        @Test
        void willSortDevelopersByDisplayNameAlphabetically() {
            final dev.coldhands.pair.stairs.backend.domain.PairStream actual = PairStreamMapper.entityToInfo(
                    new PairStreamEntity(
                            List.of(new DeveloperEntity(0L, "b", false), new DeveloperEntity(1L, "a", false)),
                            new StreamEntity(10L, "", false)
                    ));

            assertThat(actual).isEqualTo(new dev.coldhands.pair.stairs.backend.domain.PairStream(
                    List.of(
                            new DeveloperInfo(1, "a", false),
                            new DeveloperInfo(0, "b", false)
                    ),
                    new StreamInfo(10, "", false)
            ));
        }
    }

    @Nested
    class EntityToCore {

        @Test
        void canMapFromEntityToCore() {
            assertThat(PairStreamMapper.entityToCore(
                    new PairStreamEntity(
                            List.of(new DeveloperEntity(0L, "", false), new DeveloperEntity(1L, "", false)),
                            new StreamEntity(10L, "", false)
                    )
            )).isEqualTo(new PairStream(Set.of("0", "1"), "10"));
        }

    }
}